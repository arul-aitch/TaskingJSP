package com.tasking.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password utility compatible with Laravel bcrypt hashes.
 *
 * Laravel stores passwords as bcrypt with the $2y$ prefix. jBCrypt generates
 * $2a$ hashes but its verify routine treats $2a$/2b$/2y$ equivalently, so
 * existing Laravel hashes can be verified without changing stored passwords.
 *
 * New hashes are produced with the $2y$ prefix so they stay compatible if the
 * data is ever used from PHP/Laravel again.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /** Hash a plain-text password (Laravel-compatible $2y$ bcrypt). */
    public static String hash(String plain) {
        String h = BCrypt.hashpw(plain, BCrypt.gensalt(10));
        return "$2y$" + h.substring(4);
    }

    /**
     * Verify a plain-text password against a stored bcrypt hash.
     * Accepts $2a$, $2b$ and $2y$ prefixed hashes.
     */
    public static boolean verify(String plain, String stored) {
        if (plain == null || stored == null || stored.isBlank()) {
            return false;
        }
        try {
            String h = stored;
            if (h.startsWith("$2y$")) {
                // jBCrypt understands the 2y variant natively; normalize for safety.
                h = "$2a$" + h.substring(4);
            }
            return BCrypt.checkpw(plain, h);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
