package com.zone01kisumu.backend.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Utility class for centralized logging with MDC context support.
 */
public class LoggerUtil {

    private static final Logger logger = LoggerFactory.getLogger(LoggerUtil.class);

    // Private constructor to prevent instantiation
    private LoggerUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Logs an info message with optional MDC context.
     * @param message the log message
     * @param args arguments for the message
     */
    public static void logInfo(String message, Object... args) {
        logger.info(message, args);
    }

    /**
     * Logs an error message with exception.
     * @param message the log message
     * @param throwable the exception
     */
    public static void logError(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * Logs a debug message.
     * @param message the log message
     * @param args arguments for the message
     */
    public static void logDebug(String message, Object... args) {
        logger.debug(message, args);
    }

    /**
     * Logs a warning message.
     * @param message the log message
     * @param args arguments for the message
     */
    public static void logWarn(String message, Object... args) {
        logger.warn(message, args);
    }

    /**
     * Logs an error message without exception.
     * @param message the log message
     * @param args arguments for the message
     */
    public static void logError(String message, Object... args) {
        logger.error(message, args);
    }

    /**
     * Logs a runtime error with detailed stack trace and context.
     * @param context the context where error occurred (e.g., "JWT Authentication", "User Registration")
     * @param throwable the runtime exception
     */
    public static void logRuntimeError(String context, Throwable throwable) {
        logger.error("[RUNTIME ERROR] Context: {} | Exception: {} | Message: {}",
            context,
            throwable.getClass().getName(),
            throwable.getMessage(),
            throwable);
    }

    /**
     * Logs a runtime error with custom message and stack trace.
     * @param context the context where error occurred
     * @param message custom error message
     * @param throwable the runtime exception
     */
    public static void logRuntimeError(String context, String message, Throwable throwable) {
        logger.error("[RUNTIME ERROR] Context: {} | Message: {} | Exception: {} | Details: {}",
            context,
            message,
            throwable.getClass().getName(),
            throwable.getMessage(),
            throwable);
    }

    /**
     * Sets MDC context for request tracking.
     * @param key the MDC key
     * @param value the MDC value
     */
    public static void setMDC(String key, String value) {
        MDC.put(key, value);
    }

    /**
     * Clears MDC context.
     */
    public static void clearMDC() {
        MDC.clear();
    }

    /**
     * Logs user action with user ID in MDC.
     * @param userId the user ID
     * @param action the action performed
     */
    public static void logUserAction(String userId, String action) {
        setMDC("userId", userId);
        logger.info("User action: {}", action);
        clearMDC();
    }
}