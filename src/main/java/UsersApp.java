import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

/** Keeps the console program from Lab 1, using the shared file reader. */
public class UsersApp {
    public static void main(String[] args) {
        String inputPath = args.length == 0 ? "Users.txt" : args[0];
        ArrayList<User> users;

        try {
            users = UserFileReader.readUsers(inputPath);
        } catch (IOException exception) {
            System.err.println("Unable to read users: " + exception.getMessage());
            return;
        }

        Collections.sort(users);
        // Preserve the output format used in Lab 1.
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
}
