package use_case.accounts;

/**
 * Hashes and checks passwords, so use cases never store or compare raw
 * passwords themselves. The real implementation lives outside use_case/.
 */
public interface PasswordHasher {
    /** Returns a hash of the raw password that is safe to store. */
    String hash(String rawPassword);

    /** True when the raw password matches a hash produced by {@link #hash}. */
    boolean matches(String rawPassword, String storedHash);
}
