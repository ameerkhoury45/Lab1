import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import javafx.application.Platform;

/** Runs the login flow outside the JavaFX application thread. */
final class LoginTask implements Runnable {
    private final String email;
    private final String password;
    private final AuthenticationService authentication;
    private final AttemptTracker tracker;
    private final BlockingQueue<FailedAttempt> failures;
    private final Consumer<LoginResult> onResult;

    LoginTask(String email, String password, AuthenticationService authentication,
            AttemptTracker tracker, BlockingQueue<FailedAttempt> failures,
            Consumer<LoginResult> onResult) {
        this.email = Objects.requireNonNullElse(email, "");
        this.password = password;
        this.authentication = authentication;
        this.tracker = tracker;
        this.failures = failures;
        this.onResult = onResult;
    }

    @Override
    public void run() {
        try {
            LoginResult result = authenticate();
            if (!Thread.currentThread().isInterrupted()) {
                Platform.runLater(() -> onResult.accept(result));
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private LoginResult authenticate() throws InterruptedException {
        ReentrantLock loginLock = authentication.loginLockFor(email);
        if (loginLock == null) {
            return checkAndAuthenticate(false);
        }

        // Only one attempt for this email may run between the block check and state update.
        loginLock.lockInterruptibly();
        try {
            return checkAndAuthenticate(true);
        } finally {
            loginLock.unlock();
        }
    }

    private LoginResult checkAndAuthenticate(boolean knownEmail) throws InterruptedException {
        BlockCheckThread check = new BlockCheckThread(tracker, email);
        check.start();
        try {
            check.join();
        } catch (InterruptedException exception) {
            check.interrupt();
            throw exception;
        }

        int blockSeconds = check.secondsRemaining();
        if (blockSeconds > 0) {
            return LoginResult.blocked(blockSeconds);
        }

        User user = null;
        try {
            user = authentication.authenticate(email, password);
        } catch (IllegalArgumentException exception) {
            // Malformed input follows the same rule as other bad credentials.
        }

        if (user != null) {
            int secondsRemaining = tracker.recordSuccess(email);
            return secondsRemaining > 0 ? LoginResult.blocked(secondsRemaining)
                    : LoginResult.success(user.getName());
        }
        if (!knownEmail) {
            return LoginResult.invalidCredentials(0);
        }

        FailedAttempt failure = new FailedAttempt(email);
        try {
            failures.put(failure);
            return failure.awaitReply();
        } catch (InterruptedException exception) {
            // Finish an in-progress update or cancel it before releasing the account lock.
            failure.cancel();
            throw exception;
        }
    }
}
