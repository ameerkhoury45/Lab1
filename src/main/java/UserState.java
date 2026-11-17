import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/** Holds the failed-attempt count and lockout deadline for one email. */
final class UserState {
    private static final long NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);

    private final int maxFailures;
    private final int blockSeconds;
    private final LongSupplier nanoTime;
    private int failures;
    private long blockedUntilNanos;

    UserState(int maxFailures, int blockSeconds, LongSupplier nanoTime) {
        this.maxFailures = maxFailures;
        this.blockSeconds = blockSeconds;
        this.nanoTime = nanoTime;
    }

    synchronized int remainingBlockSeconds() {
        return remainingBlockTime().secondsRemaining();
    }

    synchronized BlockTime remainingBlockTime() {
        // The count stays at the threshold until the lockout expires.
        if (failures < maxFailures) {
            return BlockTime.NONE;
        }

        long remainingNanos = blockedUntilNanos - nanoTime.getAsLong();
        if (remainingNanos <= 0) {
            clear();
            return BlockTime.NONE;
        }
        return new BlockTime(remainingNanos);
    }

    synchronized FailureResult recordFailure() {
        int secondsRemaining = remainingBlockSeconds();
        if (secondsRemaining > 0) {
            return new FailureResult(0, secondsRemaining);
        }

        failures++;
        if (failures == maxFailures) {
            blockedUntilNanos = nanoTime.getAsLong() + TimeUnit.SECONDS.toNanos(blockSeconds);
            return new FailureResult(0, blockSeconds);
        }
        return new FailureResult(maxFailures - failures, 0);
    }

    /** Resets the count on success, or returns the time left if blocked. */
    synchronized int recordSuccess() {
        int secondsRemaining = remainingBlockSeconds();
        if (secondsRemaining == 0) {
            clear();
        }
        return secondsRemaining;
    }

    private synchronized void clear() {
        failures = 0;
        blockedUntilNanos = 0;
    }

    record FailureResult(int attemptsRemaining, int blockSecondsRemaining) {
    }

    record BlockTime(long nanosRemaining) {
        static final BlockTime NONE = new BlockTime(0);

        int secondsRemaining() {
            return (int) ((nanosRemaining + NANOS_PER_SECOND - 1) / NANOS_PER_SECOND);
        }
    }
}
