import java.io.*;
import java.math.*;
import java.util.List;

public class SignalLogger {
    public static void writeSignalsToFile(List<BigDecimal> prices, List<String> signals, String filename) throws IOException {
        File file = new File(filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < prices.size(); i++) {
                BigDecimal price = prices.get(i).setScale(10, RoundingMode.HALF_UP);
                String signal = signals.get(i);
                writer.write(price + "," + signal);
                writer.newLine();
            }
        }
    }
}
