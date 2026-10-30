import java.io.IOException;
import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/** Handles the login button and displays errors in the login screen. */
public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginButton;

    private ArrayList<User> users = new ArrayList<>();

    /** Receives the users that passed the Lab 1 validation. */
    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    /** Prevents login when the input file could not be read. */
    public void showInputError(String message) {
        errorLabel.setText(message);
        loginButton.setDisable(true);
        usernameField.setDisable(true);
        passwordField.setDisable(true);
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        errorLabel.setText("");

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        // Both values must match the same user. Comparison is case-sensitive.
        for (User user : users) {
            if (user.getName().equals(username)
                    && user.getPassword().equals(password)) {
                openWelcomeScreen(user);
                return;
            }
        }
        errorLabel.setText("User or password do not match.");
    }

    /** Loads a different FXML file and replaces the Scene in the same window. */
    private void openWelcomeScreen(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    LoginController.class.getResource("/welcome.fxml"));
            Parent root = loader.load();
            WelcomeController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) loginButton.getScene().getWindow();
            passwordField.clear();
            stage.setScene(new Scene(root, 420, 360));
            stage.setTitle("GCM - Welcome");
        } catch (IOException exception) {
            errorLabel.setText("Unable to open the welcome screen.");
            System.err.println("Unable to load welcome.fxml: "
                    + exception.getMessage());
        }
    }
}
