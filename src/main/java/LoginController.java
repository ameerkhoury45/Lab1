import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/** Handles the login button and displays errors in the login screen. */
public class LoginController {
    private static final String INVALID_CREDENTIALS = "פרטים שגויים";

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginButton;
    @FXML
    private Button concurrencyButton;
    @FXML
    private Label concurrencyStatus;

    private AuthenticationService authentication;
    private AttemptTracker tracker;
    private BlockingQueue<FailedAttempt> failures;
    private Consumer<String> onSuccess;
    private Thread loginThread;
    private Thread countdownThread;
    private Thread experimentThread;
    private boolean closed;

    void configure(AuthenticationService authentication, AttemptTracker tracker,
            BlockingQueue<FailedAttempt> failures, Consumer<String> onSuccess) {
        this.authentication = authentication;
        this.tracker = tracker;
        this.failures = failures;
        this.onSuccess = onSuccess;
    }

    /** Prevents login when the input file could not be read. */
    void showInputError(String message) {
        errorLabel.setText(message);
        setLoginDisabled(true);
    }

    @FXML
    private void handleLogin() {
        if (closed || loginButton.isDisabled()) {
            return;
        }

        String email = usernameField.getText();
        String password = passwordField.getText();
        setLoginDisabled(true);
        errorLabel.setText("");

        LoginTask task = new LoginTask(email, password, authentication, tracker, failures,
                result -> showResult(email, result));
        loginThread = new Thread(task, "login-task");
        loginThread.start();
    }

    private void showResult(String email, LoginResult result) {
        loginThread = null;
        if (closed) {
            return;
        }

        switch (result.status()) {
            case SUCCESS -> {
                passwordField.clear();
                onSuccess.accept(result.username());
            }
            case INVALID_CREDENTIALS -> {
                int remaining = result.attemptsRemaining();
                errorLabel.setText(remaining > 0
                        ? INVALID_CREDENTIALS + ". נותרו " + remaining + " ניסיונות"
                        : INVALID_CREDENTIALS);
                setLoginDisabled(false);
            }
            case BLOCKED -> {
                showCountdown(result.blockSecondsRemaining());
                CountdownTask countdown = new CountdownTask(tracker, email, this::showCountdown);
                countdownThread = new Thread(countdown, "lockout-countdown");
                countdownThread.start();
            }
        }
    }

    private void showCountdown(int seconds) {
        if (closed) {
            return;
        }
        if (seconds > 0) {
            errorLabel.setText("החשבון חסום, נותרו " + seconds + " שניות");
        } else {
            countdownThread = null;
            errorLabel.setText("");
            setLoginDisabled(false);
        }
    }

    private void setLoginDisabled(boolean disabled) {
        usernameField.setDisable(disabled);
        passwordField.setDisable(disabled);
        loginButton.setDisable(disabled);
    }

    @FXML
    private void handleConcurrencyTest() {
        if (closed || concurrencyButton.isDisabled()) {
            return;
        }

        concurrencyButton.setDisable(true);
        concurrencyStatus.setText("Running the 20-thread test…");
        experimentThread = new Thread(() -> {
            try {
                ConcurrencyExperiment.Result result = ConcurrencyExperiment.run();
                Platform.runLater(() -> showExperimentResult(result));
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }, "concurrency-experiment");
        experimentThread.start();
    }

    private void showExperimentResult(ConcurrencyExperiment.Result result) {
        experimentThread = null;
        if (closed) {
            return;
        }

        String lockout = result.lockoutAtLimit()
                ? "Lockout started at failure 10,000."
                : "Lockout threshold test failed.";
        int expected = ConcurrencyExperiment.EXPECTED_UPDATES;
        concurrencyStatus.setText(lockout
                + "\nCounter with synchronized: " + result.withSynchronization() + " / " + expected
                + "\nCounter without synchronized: " + result.withoutSynchronization() + " / " + expected);
        concurrencyButton.setDisable(false);
    }

    void shutdown() {
        closed = true;
        if (loginThread != null) {
            loginThread.interrupt();
        }
        if (countdownThread != null) {
            countdownThread.interrupt();
        }
        if (experimentThread != null) {
            experimentThread.interrupt();
        }
    }
}
