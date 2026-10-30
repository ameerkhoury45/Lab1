import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/** Reads the Lab 1 input format and keeps only valid users. */
public class UserFileReader {

    /**
     * Invalid rows are reported and skipped. A file error is passed to the
     * caller, so it can decide how to display the problem.
     */
    public static ArrayList<User> readUsers(String inputPath) throws IOException {
        ArrayList<User> users = new ArrayList<>();

        try (BufferedReader reader = openInput(inputPath)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                // A UTF-8 BOM marks the file encoding, not part of a username.
                if (firstLine && line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                firstLine = false;
                try {
                    users.add(parseUser(line));
                } catch (IllegalArgumentException exception) {
                    System.err.println(line + " " + exception.getMessage());
                }
            }
        }
        return users;
    }

    /** Keeps the standard-input option from Lab 1. */
    private static BufferedReader openInput(String inputPath) throws IOException {
        if ("-".equals(inputPath)) {
            return new BufferedReader(new InputStreamReader(
                    System.in, StandardCharsets.UTF_8));
        }
        return Files.newBufferedReader(Paths.get(inputPath),
                StandardCharsets.UTF_8);
    }

    /** Checks the row structure; the User constructor validates the values. */
    private static User parseUser(String line) {
        String trimmedLine = line.trim();
        if (trimmedLine.isEmpty()) {
            throw new IllegalArgumentException(User.INVALID_USERNAME);
        }
        String[] fields = trimmedLine.split("\\s+");
        if (fields.length < 2) {
            throw new IllegalArgumentException(User.PASSWORD_TOO_SHORT);
        }
        if (fields.length > 2) {
            throw new IllegalArgumentException(User.INVALID_PASSWORD);
        }
        return new User(fields[0], fields[1]);
    }
}
