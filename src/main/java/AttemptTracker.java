import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/** Shares account lockout state across login attempts. */
final class AttemptTracker {
    private final ConcurrentHashMap<String, UserState> states = new ConcurrentHashMap<>();
    private final int maxFailures;
    private final int blockSeconds;
    private final LongSupplier nanoTime;

    AttemptTracker(int maxFailures, int blockSeconds) {
        this(maxFailures, blockSeconds, System::nanoTime);
    }

    AttemptTracker(int maxFailures, int blockSeconds, LongSupplier nanoTime) {
        if (maxFailures < 1 || blockSeconds < 1) {
            throw new IllegalArgumentException("Both lockout settings must be positive.");
        }
        this.maxFailures = maxFailures;
        this.blockSeconds = blockSeconds;
        this.nanoTime = Objects.requireNonNull(nanoTime);
    }

    int remainingBlockSeconds(String email) {
        UserState state = states.get(email);
        return state == null ? 0 : state.remainingBlockSeconds();
    }

    /** The authentication service must have confirmed that this email exists. */
    UserState.FailureResult recordFailure(String knownEmail) {
        UserState state = states.computeIfAbsent(knownEmail,
                email -> new UserState(maxFailures, blockSeconds, nanoTime));
        return state.recordFailure();
    }

    int recordSuccess(String email) {
        UserState state = states.get(email);
        return state == null ? 0 : state.recordSuccess();
    }
}
