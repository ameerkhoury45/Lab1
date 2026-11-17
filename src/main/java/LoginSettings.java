import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/** The maximum failed attempts and lockout duration for this run. */
record LoginSettings(int maxFailures, int blockSeconds) {
    static LoginSettings read(List<String> arguments, BufferedReader input, PrintStream output)
            throws IOException {
        if (arguments.size() == 2) {
            int maxFailures = parsePositiveInteger(arguments.get(0));
            int blockSeconds = parsePositiveInteger(arguments.get(1));
            if (maxFailures > 0 && blockSeconds > 0) {
                return new LoginSettings(maxFailures, blockSeconds);
            }
        }

        output.println("Expected two positive integer arguments: n t.");
        int maxFailures = readPositiveInteger("Maximum failed attempts (n)", input, output);
        int blockSeconds = readPositiveInteger("Lockout duration in seconds (t)", input, output);
        return new LoginSettings(maxFailures, blockSeconds);
    }

    private static int readPositiveInteger(String label, BufferedReader input, PrintStream output)
            throws IOException {
        while (true) {
            output.println(label + ":");
            String line = input.readLine();
            if (line == null) {
                throw new EOFException("Input ended before both settings were provided.");
            }
            int value = parsePositiveInteger(line);
            if (value > 0) {
                return value;
            }
            output.println("Enter a whole number between 1 and " + Integer.MAX_VALUE + ".");
        }
    }

    private static int parsePositiveInteger(String text) {
        try {
            return Math.max(Integer.parseInt(text.strip()), 0);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
