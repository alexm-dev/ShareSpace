package app.util;

import java.util.regex.Pattern;
import java.util.Arrays;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Autorizaton utlity for validation of email and passwords.
 */
public class AuthUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$"
    );

    private static final int HASH_WORK_FACTOR = 12;
    private static final int MIN_PASSWORD_LENGTH = 8;

    /** description of the rules, for error messages in the UI. */
    public static final String PASSWORD_RULES = "password must be at least " + MIN_PASSWORD_LENGTH
        + " chars with at least one uppercase, one lowercase, one digit and one symbol";

    /**
     * Returns true if the given string looks like a valid email address.
     *
     * @param email the candidate string (may be null)
     * @return true if it matches the accepted email shape
     */
    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            return false;
        }

        if (trimmed.startsWith(".") || trimmed.contains("..")) { 
            return false;
        }

        return EMAIL_PATTERN.matcher(trimmed).matches();
    }


    /**
     * Checks whether the given password meets the strength rules.
     * Does not modify or zero the array.
     *
     * @param password the candidate password (may be null)
     * @return true if the password is acceptable
     */
    public static boolean isValidPassword(char[] password) {
        if (password == null || password.length < MIN_PASSWORD_LENGTH) {
            return false;
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasSymbol = false;
        boolean hasDigit = false;

        for (char c : password) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) hasSymbol = true;
            else if (Character.isDigit(c)) hasDigit = true;
        }

        return hasUpper && hasLower && hasSymbol && hasDigit;
    }


    /**
     * Hashes a plain-text password and zeroes out the char[] afterwards.
     *
     * @param plain the plain-text password as a char array (zeroed on return)
     * @return the bcrypt hash to store in the database
     */
    public static String hashPassword(char[] plain) {
        try {
            return BCrypt.hashpw(new String(plain), BCrypt.gensalt(HASH_WORK_FACTOR));
        } finally {
            Arrays.fill(plain, '\0');
        }
    }


    /**
     * Verifies a plain-text password against a stored bcrypt hash,
     * then zeroes out the char[].
     *
     * @param plain the plain-text password to check (zeroed on return)
     * @param storedHash the hash retrieved from the database
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(char[] plain, String storedHash) {
        try {
            return BCrypt.checkpw(new String(plain), storedHash);
        } finally {
            Arrays.fill(plain, '\0');
        }
    }
}
