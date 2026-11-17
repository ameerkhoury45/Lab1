/** Checks a single email's lockout state and exposes the result after join. */
final class BlockCheckThread extends Thread {
    private final AttemptTracker tracker;
    private final String email;
    private int secondsRemaining;

    BlockCheckThread(AttemptTracker tracker, String email) {
        super("block-check");
        this.tracker = tracker;
        this.email = email;
    }

    @Override
    public void run() {
        secondsRemaining = tracker.remainingBlockSeconds(email);
    }

    int secondsRemaining() {
        return secondsRemaining;
    }
}
