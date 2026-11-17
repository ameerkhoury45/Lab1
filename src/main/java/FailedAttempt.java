import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/** A failed login for a known email and its private, single-answer queue. */
final class FailedAttempt {
    private final String email;
    private final BlockingQueue<LoginResult> reply = new ArrayBlockingQueue<>(1);
    private boolean cancelled;

    FailedAttempt(String email) {
        this.email = email;
    }

    LoginResult awaitReply() throws InterruptedException {
        return reply.take();
    }

    synchronized void process(AttemptTracker tracker) {
        if (cancelled) {
            return;
        }
        UserState.FailureResult failure = tracker.recordFailure(email);
        int secondsRemaining = failure.blockSecondsRemaining();
        if (secondsRemaining > 0) {
            reply.add(LoginResult.blocked(secondsRemaining));
        } else {
            reply.add(LoginResult.invalidCredentials(failure.attemptsRemaining()));
        }
    }

    synchronized void cancel() {
        cancelled = true;
    }
}
