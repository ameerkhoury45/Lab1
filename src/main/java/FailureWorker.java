import java.util.concurrent.BlockingQueue;

/** Consumes failed attempts while the application is running. */
final class FailureWorker extends Thread {
    private final AttemptTracker tracker;
    private final BlockingQueue<FailedAttempt> failures;

    FailureWorker(int workerNumber, AttemptTracker tracker, BlockingQueue<FailedAttempt> failures) {
        super("failure-worker-" + workerNumber);
        setDaemon(true);
        this.tracker = tracker;
        this.failures = failures;
    }

    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                failures.take().process(tracker);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
