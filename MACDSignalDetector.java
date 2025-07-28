import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.*;

public class MACDSignalDetector {

    private static final int SCALE = 10; // Precision for calculations

    public static List<BigDecimal> readPricesFromFile(String filepath) throws IOException {
        List<BigDecimal> prices = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    prices.add(new BigDecimal(line.trim()));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid line: " + line);
                }
            }
        }
        return prices;
    }

    public static List<BigDecimal> calculateEMA(List<BigDecimal> prices, int period) {
        List<BigDecimal> ema = new ArrayList<>();
        BigDecimal multiplier = BigDecimal.valueOf(2.0 / (period + 1)).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal sum = prices.subList(0, period).stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal prevEMA = sum.divide(BigDecimal.valueOf(period), SCALE, RoundingMode.HALF_UP);
        ema.add(prevEMA);

        for (int i = period; i < prices.size(); i++) {
            BigDecimal price = prices.get(i);
            BigDecimal currentEMA = price.subtract(prevEMA).multiply(multiplier).add(prevEMA);
            currentEMA = currentEMA.setScale(SCALE, RoundingMode.HALF_UP);
            ema.add(currentEMA);
            prevEMA = currentEMA;
        }

        return ema;
    }

    public static List<BigDecimal> calculateMACD(List<BigDecimal> prices) {
        List<BigDecimal> ema12 = calculateEMA(prices, 12);
        List<BigDecimal> ema26 = calculateEMA(prices, 26);
        int diff = ema12.size() - ema26.size();

        return IntStream.range(0, ema26.size())
            .mapToObj(i -> ema12.get(i + diff).subtract(ema26.get(i)).setScale(SCALE, RoundingMode.HALF_UP))
            .collect(Collectors.toList());
    }

    public static List<BigDecimal> generateSignalLine(List<BigDecimal> macdLine) {
        return calculateEMA(macdLine, 9);
    }

    public static List<String> detectSignals(List<BigDecimal> macd, List<BigDecimal> signal) {
        List<String> signals = new ArrayList<>();
        int start = macd.size() - signal.size();

        for (int i = 1; i < signal.size(); i++) {
            BigDecimal prevMACD = macd.get(i - 1 + start);
            BigDecimal prevSignal = signal.get(i - 1);
            BigDecimal currMACD = macd.get(i + start);
            BigDecimal currSignal = signal.get(i);

            if (prevMACD.compareTo(prevSignal) < 0 && currMACD.compareTo(currSignal) > 0) {
                signals.add("Buy");
            } else if (prevMACD.compareTo(prevSignal) > 0 && currMACD.compareTo(currSignal) < 0) {
                signals.add("Sell");
            } else {
                signals.add("Hold");
            }
        }

        return signals;
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java MACDSignalDetector <path_to_price_file> [<path_to_output_file>]");
            return;
        }

        try {
            List<BigDecimal> prices = readPricesFromFile(args[0]);
            if (prices.size() < 26) {
                System.out.println("Not enough data points (at least 26 needed)");
                return;
            }

            List<BigDecimal> macd = calculateMACD(prices);
            List<BigDecimal> signal = generateSignalLine(macd);
            List<String> signals = detectSignals(macd, signal);

            System.out.println("MACD Line:");
            macd.forEach(val -> System.out.println(val.toPlainString()));

            System.out.println("\nSignal Line:");
            signal.forEach(val -> System.out.println(val.toPlainString()));

            System.out.println("\nTrading Signals:");
            for (int i = 0; i < signals.size(); i++) {
                System.out.printf("Day %d: %s%n", i + 1, signals.get(i));
            }

            if(args.length >= 2) {
                int signalStartIndex = prices.size() - signals.size();
                List<BigDecimal> trimmedPrices = prices.subList(signalStartIndex, prices.size());
                SignalLogger.writeSignalsToFile(trimmedPrices, signals, args[1]);
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
