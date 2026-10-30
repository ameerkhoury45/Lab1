import javafx.fxml.FXML;
import javafx.scene.control.Label;

/** Shows the username after a successful login. */
public class WelcomeController {
    @FXML
    private Label usernameLabel;

    public void setUser(User user) {
        usernameLabel.setText(user.getName());
    }
}
