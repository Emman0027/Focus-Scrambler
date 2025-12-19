package com.example.focusscrambler;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtils {

    // Define the BCrypt workload to use when generating password hashes.
    // A workload of 12 is a reasonable default.
    private static final int WORKLOAD = 12;

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private PasswordUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Hashes a plaintext password using BCrypt.
     *
     * @param plainPassword The password to hash.
     * @return A salted and hashed password string.
     */
    public static String hashPassword(String plainPassword) {
        String salt = BCrypt.gensalt(WORKLOAD);
        return BCrypt.hashpw(plainPassword, salt);
    }

    /**
     * Verifies a plaintext password against a stored hashed password.
     *
     * @param plainPassword The password to verify.
     * @param hashedPassword The stored hash to check against.
     * @return true if the password matches, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || !hashedPassword.startsWith("$2a$")) {
            // Protects against null hashes or invalid formats
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
