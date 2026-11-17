/** The outcome of one login attempt, delivered to the JavaFX controller. */
record LoginResult(Status status, String username, int attemptsRemaining, int blockSecondsRemaining) {
    enum Status {
        SUCCESS, INVALID_CREDENTIALS, BLOCKED
    }

    static LoginResult success(String username) {
        return new LoginResult(Status.SUCCESS, username, 0, 0);
    }

    static LoginResult invalidCredentials(int attemptsRemaining) {
        return new LoginResult(Status.INVALID_CREDENTIALS, null, attemptsRemaining, 0);
    }

    static LoginResult blocked(int secondsRemaining) {
        return new LoginResult(Status.BLOCKED, null, 0, secondsRemaining);
    }
}
