import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/** Starts the JavaFX program and loads the users before showing the login. */
public class LoginApp extends Application {
    private final BlockingQueue<FailedAttempt> failures = new LinkedBlockingQueue<>();
    private LoginSettings settings;
    private AuthenticationService authentication;
    private AttemptTracker tracker;
    private List<FailureWorker> workers = List.of();
    private LoginController controller;
    private byte[] loginLayout;
    private byte[] welcomeLayout;
    private String inputError;

    @Override
    public void init() throws IOException {
        BufferedReader console = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8));
        try {
            settings = LoginSettings.read(getParameters().getRaw(), console, System.out);
        } catch (IOException exception) {
            System.err.println("Unable to configure login: " + exception.getMessage());
            return;
        }

        try {
            authentication = new AuthenticationService(UserFileReader.readUsers("Users.txt"));
        } catch (IOException exception) {
            inputError = "Unable to read the users file. Check its name and path.";
            System.err.println("Unable to read users: " + exception.getMessage());
        }

        tracker = new AttemptTracker(settings.maxFailures(), settings.blockSeconds());
        loginLayout = readLayout("/login.fxml");
        welcomeLayout = readLayout("/welcome.fxml");
    }

    private static byte[] readLayout(String resource) throws IOException {
        try (InputStream input = LoginApp.class.getResourceAsStream(resource)) {
            if (input == null) {
                throw new IOException("Missing layout: " + resource);
            }
            return input.readAllBytes();
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        if (settings == null) {
            Platform.exit();
            return;
        }

        FXMLLoader loginLoader = new FXMLLoader();
        Parent root = loginLoader.load(new ByteArrayInputStream(loginLayout));
        controller = loginLoader.getController();
        if (inputError != null) {
            controller.showInputError(inputError);
        } else {
            FXMLLoader welcomeLoader = new FXMLLoader();
            Parent welcomeRoot = welcomeLoader.load(new ByteArrayInputStream(welcomeLayout));
            WelcomeController welcomeController = welcomeLoader.getController();
            controller.configure(authentication, tracker, failures, username -> {
                welcomeController.setUsername(username);
                stage.setScene(new Scene(welcomeRoot, 420, 360));
                stage.setTitle("GCM - Welcome");
            });
            workers = List.of(new FailureWorker(1, tracker, failures),
                    new FailureWorker(2, tracker, failures));
            workers.forEach(Thread::start);
        }

        stage.setTitle("GCM - Login");
        stage.setScene(new Scene(root, 420, 360));
        stage.setMinWidth(380);
        stage.setMinHeight(380);
        // The same Stage is used for both screens, so X always ends the app.
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
        workers.forEach(Thread::interrupt);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
