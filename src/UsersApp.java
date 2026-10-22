import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

/** Reads user data and writes valid users to out.txt in username order. */
public class UsersApp {
    public static void main(String[] args) {
        ArrayList<User> users = new ArrayList<>();
        String inputPath = args.length == 0 ? "Users.txt" : args[0];

        try (BufferedReader reader = openInput(inputPath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    users.add(parseUser(line));
                } catch (IllegalArgumentException exception) {
                    // Report the invalid row and continue with the next one.
                    System.err.println(line + " " + exception.getMessage());
                }
            }
        } catch (IOException exception) {
            System.err.println("Unable to read users: " + exception.getMessage());
            return;
        }

        Collections.sort(users);
        // Create out.txt, replacing the results from any previous run.
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get("out.txt"), StandardCharsets.UTF_8)) {
            for (User user : users) {
                writer.write(user.toString());
                writer.write("\n");
            }
        } catch (IOException exception) {
            System.err.println("Unable to write users: " + exception.getMessage());
        }
    }

    /** Opens a file, or standard input when the path is "-". */
    private static BufferedReader openInput(String inputPath) throws IOException {
        if ("-".equals(inputPath)) {
            return new BufferedReader(new InputStreamReader(
                    System.in, StandardCharsets.UTF_8));
        }
        return Files.newBufferedReader(Paths.get(inputPath),
                StandardCharsets.UTF_8);
    }

    /** Checks the row structure before validating the two fields. */
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
