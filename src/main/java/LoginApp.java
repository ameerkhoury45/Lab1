import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Starts the JavaFX program and loads the users before showing the login. */
public class LoginApp extends Application {
    private LoginSettings settings;
    private ArrayList<User> users = new ArrayList<>();
    private String inputError;

    @Override
    public void init() {
        BufferedReader console = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
        try {
            settings = LoginSettings.read(getParameters().getRaw(), console, System.out);
        } catch (IOException exception) {
            System.err.println("Unable to configure login: " + exception.getMessage());
            return;
        }

        try {
            users = UserFileReader.readUsers("Users.txt");
        } catch (IOException exception) {
            inputError = "Unable to read the users file. Check its name and path.";
            System.err.println("Unable to read users: " + exception.getMessage());
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        if (settings == null) {
            Platform.exit();
            return;
        }

        FXMLLoader loader = new FXMLLoader(
                LoginApp.class.getResource("/login.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setUsers(users);
        if (inputError != null) {
            controller.showInputError(inputError);
        }

        stage.setTitle("GCM - Login");
        stage.setScene(new Scene(root, 420, 360));
        stage.setMinWidth(380);
        stage.setMinHeight(380);
        // The same Stage is used for both screens, so X always ends the app.
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
