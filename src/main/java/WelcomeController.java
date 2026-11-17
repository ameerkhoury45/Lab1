import javafx.fxml.FXML;
import javafx.scene.control.Label;

/** Shows the username after a successful login. */
public class WelcomeController {
    @FXML
    private Label usernameLabel;

    void setUsername(String username) {
        usernameLabel.setText(username);
    }
}
