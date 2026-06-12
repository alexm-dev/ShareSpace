package app.util;

import org.slf4j.LoggerFactory;

/**
 * Application logger.
 *
 * Thin facade over SLF4J so call sites stay simple (Logger.info(...), etc.).
 * Output destinations, levels and the per-run log file are configured in
 * src/main/resources/logback.xml.
 */
public class Logger {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger("ShareSpace");
    private static boolean debugEnabled = false;

    private Logger() {}

    /**
     * Enables or disables debug logging.
     * @param enabled true to enable debug logging, false to disable
     */
    public static void setDebug(boolean enabled) { debugEnabled = enabled; }

    /**
     * Check if debug is enabled.
     */
    public static boolean isDebug() { return debugEnabled; }

    /**
     * Logs a debug message if debug logging is enabled.
     */
    public static void debug(String message) {
        if (debugEnabled) LOG.debug(message);
    }

    /**
     * Logs an info message.
     */
    public static void info(String message) {
        LOG.info(message);
    }

    /**
     * Logs a warning message.
     */
    public static void warn(String message) {
        LOG.warn(message);
    }

    /**
     * Logs an error message.
     */
    public static void error(String message) {
        LOG.error(message);
    }

    /**
     * Logs an error message with a throwable (full stack trace).
     */
    public static void error(String message, Throwable t) {
        LOG.error(message, t);
    }
}
