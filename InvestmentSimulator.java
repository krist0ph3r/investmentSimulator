import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class InvestmentSimulator {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java InvestmentSimulator <input.csv> <dailyAmount> [output.csv]");
            return;
        }

        String inputFilename = args[0];
        BigDecimal dailyAmount = new BigDecimal(args[1]);
        String outputFilename = args.length >= 3 ? args[2] : null;

        List<BigDecimal> prices = new ArrayList<>();
        List<String> signals = new ArrayList<>();
        readSignalLoggerOutput(inputFilename, prices, signals);

        simulate(prices, signals, dailyAmount, outputFilename);
    }

    public static void readSignalLoggerOutput(String filename,
                                              List<BigDecimal> prices,
                                              List<String> signals) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 2) {
                prices.add(new BigDecimal(parts[0].trim()));
                signals.add(parts[1].trim());
            }
        }
        reader.close();
    }

    public static void simulate(List<BigDecimal> prices, List<String> signals,
                                BigDecimal dailyAmount, String outputFilename) throws IOException {

        BigDecimal accumulatedCash = BigDecimal.ZERO;
        BigDecimal signalUnits = BigDecimal.ZERO;
        BigDecimal flatUnits = BigDecimal.ZERO;

        BufferedWriter csvWriter = null;
        if (outputFilename != null) {
            csvWriter = new BufferedWriter(new FileWriter(outputFilename));
            csvWriter.write("Day,Price,Signal,SignalStrategyValue,FlatStrategyValue,AccumulatedCash,TotalInvested");
            csvWriter.newLine();
        }

        for (int day = 0; day < prices.size(); day++) {
            BigDecimal price = prices.get(day);
            String signal = signals.get(day);
            accumulatedCash = accumulatedCash.add(dailyAmount);
            flatUnits = flatUnits.add(dailyAmount.divide(price, 8, RoundingMode.HALF_UP));

            if ("BUY".equalsIgnoreCase(signal)) {
                BigDecimal unitsToBuy = accumulatedCash.divide(price, 8, RoundingMode.HALF_UP);
                signalUnits = signalUnits.add(unitsToBuy);
                accumulatedCash = BigDecimal.ZERO;
            }

            BigDecimal signalValue = signalUnits.multiply(price);
            BigDecimal flatValue = flatUnits.multiply(price);
            BigDecimal totalInvested = signalUnits.multiply(price).add(accumulatedCash);

            if (csvWriter != null) {
                csvWriter.write((day + 1) + "," +
                                price.setScale(2, RoundingMode.HALF_UP) + "," +
                                signal + "," +
                                signalValue.setScale(2, RoundingMode.HALF_UP) + "," +
                                flatValue.setScale(2, RoundingMode.HALF_UP) + "," +
                                accumulatedCash.setScale(2, RoundingMode.HALF_UP) + "," +
                                totalInvested.setScale(2, RoundingMode.HALF_UP));
                csvWriter.newLine();
            }
        }

        BigDecimal finalPrice = prices.get(prices.size() - 1);
        BigDecimal signalTotalValue = signalUnits.multiply(finalPrice).add(accumulatedCash);
        BigDecimal flatTotalValue = flatUnits.multiply(finalPrice);

        System.out.println("Final Signal-Based Strategy Value: £" + signalTotalValue.setScale(2, RoundingMode.HALF_UP));
        System.out.println("Final Flat Investing Strategy Value: £" + flatTotalValue.setScale(2, RoundingMode.HALF_UP));

        if (csvWriter != null) {
            csvWriter.flush();
            csvWriter.close();
        }
    }
}
