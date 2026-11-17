import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

import javafx.application.Platform;

/** Schedules display updates using the account's actual lockout deadline. */
final class CountdownTask implements Runnable {
    private final AttemptTracker tracker;
    private final String email;
    private final IntConsumer onTick;

    CountdownTask(AttemptTracker tracker, String email, IntConsumer onTick) {
        this.tracker = tracker;
        this.email = email;
        this.onTick = onTick;
    }

    @Override
    public void run() {
        int lastSeconds = -1;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                UserState.BlockTime time = tracker.remainingBlockTime(email);
                int seconds = time.secondsRemaining();
                if (seconds != lastSeconds) {
                    Platform.runLater(() -> onTick.accept(seconds));
                    lastSeconds = seconds;
                }
                if (seconds == 0) {
                    return;
                }

                long nanosUntilNextTick = time.nanosRemaining() - TimeUnit.SECONDS.toNanos(seconds - 1L);
                TimeUnit.NANOSECONDS.sleep(nanosUntilNextTick);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
