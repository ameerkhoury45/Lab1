import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/** Exercises the lockout threshold and demonstrates lost updates on an independent counter. */
final class ConcurrencyExperiment {
    private static final int THREAD_COUNT = 20;
    private static final int UPDATES_PER_THREAD = 499;
    static final int EXPECTED_UPDATES = THREAD_COUNT * UPDATES_PER_THREAD;
    private static final int LOCKOUT_LIMIT = 10_000;
    private static final int BLOCK_SECONDS = 30;
    private static final String TEST_EMAIL = "concurrency-test@example.test";

    private ConcurrencyExperiment() {
    }

    static Result run() throws InterruptedException {
        // A fixed clock keeps the lockout from expiring during this isolated experiment.
        AttemptTracker tracker = new AttemptTracker(LOCKOUT_LIMIT, BLOCK_SECONDS, () -> 0L);
        runConcurrent(() -> tracker.recordFailure(TEST_EMAIL));
        int accountFailures = tracker.failureCount(TEST_EMAIL);
        boolean lockoutAtLimit = checkLockoutThreshold(tracker) && accountFailures == EXPECTED_UPDATES;

        Counter withSynchronization = new Counter();
        runConcurrent(withSynchronization::incrementWithLock);
        Counter withoutSynchronization = new Counter();
        runConcurrent(withoutSynchronization::increment);
        return new Result(lockoutAtLimit, withSynchronization.value, withoutSynchronization.value,
                accountFailures);
    }

    private static boolean checkLockoutThreshold(AttemptTracker tracker) {
        boolean firstNineteenCorrect = true;
        for (int failure = EXPECTED_UPDATES + 1; failure < LOCKOUT_LIMIT; failure++) {
            UserState.FailureResult outcome = tracker.recordFailure(TEST_EMAIL);
            if (outcome.blockSecondsRemaining() != 0
                    || outcome.attemptsRemaining() != LOCKOUT_LIMIT - failure) {
                firstNineteenCorrect = false;
            }
        }
        UserState.FailureResult outcome = tracker.recordFailure(TEST_EMAIL);
        return firstNineteenCorrect && outcome.attemptsRemaining() == 0
                && outcome.blockSecondsRemaining() == BLOCK_SECONDS;
    }

    /** Runs the same experiment from the console, including the actual UserState count. */
    public static void main(String[] args) throws InterruptedException {
        Result result = run();
        System.out.println("UserState failures after 20 x 499 calls: " + result.accountFailures()
                + " / " + EXPECTED_UPDATES);
        System.out.println("19 additional failures allowed, failure 10000 blocks: "
                + result.lockoutAtLimit());
        System.out.println("Independent counter with synchronized: " + result.withSynchronization());
        System.out.println("Independent counter without synchronized: " + result.withoutSynchronization());
    }

    private static void runConcurrent(Runnable update) throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>(THREAD_COUNT);
        try {
            for (int number = 1; number <= THREAD_COUNT; number++) {
                Thread thread = new Thread(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        for (int count = 0; count < UPDATES_PER_THREAD
                                && !Thread.currentThread().isInterrupted(); count++) {
                            update.run();
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    }
                }, "concurrency-test-worker-" + number);
                threads.add(thread);
                thread.start();
            }

            ready.await();
            start.countDown();
            for (Thread thread : threads) {
                thread.join();
            }
        } finally {
            threads.forEach(Thread::interrupt);
        }
    }

    record Result(boolean lockoutAtLimit, int withSynchronization, int withoutSynchronization,
            int accountFailures) {
    }

    private static final class Counter {
        private int value;

        private synchronized void incrementWithLock() {
            increment();
        }

        private void increment() {
            int previous = value;
            // Give the scheduler the same opportunity to switch threads in both runs.
            Thread.yield();
            value = previous + 1;
        }
    }
}
