import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Starts the JavaFX program and loads the users before showing the login. */
public class LoginApp extends Application {
    private ArrayList<User> users = new ArrayList<>();
    private String inputError;

    @Override
    public void init() {
        String inputPath = getParameters().getRaw().isEmpty()
                ? "Users.txt" : getParameters().getRaw().get(0);
        try {
            users = UserFileReader.readUsers(inputPath);
        } catch (IOException exception) {
            inputError = "Unable to read the users file. Check its name and path.";
            System.err.println("Unable to read users: " + exception.getMessage());
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
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
