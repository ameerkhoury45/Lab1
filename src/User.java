import java.util.regex.Pattern;

/** Stores credentials after validating the username and password. */
public class User implements Comparable<User> {
    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 12;
    private static final String PASSWORD_SYMBOLS =
            "!@#$%^&*()_+-=";

    static final String INVALID_USERNAME =
            "Please enter a valid Email as username";
    static final String USERNAME_TOO_LONG =
            "Username is too long, try something shorter";
    static final String PASSWORD_TOO_SHORT =
            "Your password is too short, add more characters";
    static final String PASSWORD_TOO_LONG =
            "Your password is too long, try a shorter one";
    static final String INVALID_PASSWORD =
            "Please enter a valid password";

    // Match the three email parts defined in the assignment.
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[A-Za-z0-9_.%+-]+@[A-Za-z0-9][A-Za-z0-9.-]*\\.[A-Za-z]{2,}");

    private final String username;
    private final String password;

    public User(String username, String password) {
        validateUsername(username);
        validatePassword(password);
        this.username = username;
        this.password = password;
    }

    private static void validateUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException(INVALID_USERNAME);
        }
        if (username.length() > MAX_USERNAME_LENGTH) {
            throw new IllegalArgumentException(USERNAME_TOO_LONG);
        }
        if (!EMAIL_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(INVALID_USERNAME);
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(PASSWORD_TOO_SHORT);
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(PASSWORD_TOO_LONG);
        }

        boolean hasLetter = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;
        for (int i = 0; i < password.length(); i++) {
            char character = password.charAt(i);
            if (isLetter(character)) {
                hasLetter = true;
            } else if (isDigit(character)) {
                hasDigit = true;
            } else if (PASSWORD_SYMBOLS.indexOf(character) >= 0) {
                hasSymbol = true;
            } else {
                throw new IllegalArgumentException(INVALID_PASSWORD);
            }
        }
        if (!hasLetter || !hasDigit || !hasSymbol) {
            throw new IllegalArgumentException(INVALID_PASSWORD);
        }
    }

    private static boolean isLetter(char character) {
        return (character >= 'A' && character <= 'Z')
                || (character >= 'a' && character <= 'z');
    }

    private static boolean isDigit(char character) {
        return character >= '0' && character <= '9';
    }

    public String getName() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /** Compares usernames in case-sensitive order. */
    @Override
    public int compareTo(User other) {
        return username.compareTo(other.username);
    }

    @Override
    public String toString() {
        return username + " " + password;
    }
}
