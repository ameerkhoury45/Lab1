import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/** Matches credentials and shares one attempt lock per known email. */
final class AuthenticationService {
    private final Map<String, Account> accounts = new HashMap<>();

    AuthenticationService(Collection<User> users) {
        for (User user : users) {
            accounts.computeIfAbsent(user.getName(), email -> new Account()).users.add(user);
        }
    }

    ReentrantLock loginLockFor(String email) {
        Account account = accounts.get(email);
        return account == null ? null : account.loginLock;
    }

    User authenticate(String email, String password) {
        User.validateUsername(email);
        User.validatePassword(password);

        Account account = accounts.get(email);
        if (account != null) {
            for (User user : account.users) {
                if (user.getPassword().equals(password)) {
                    return user;
                }
            }
        }
        return null;
    }

    private static final class Account {
        private final List<User> users = new ArrayList<>();
        private final ReentrantLock loginLock = new ReentrantLock();
    }
}
