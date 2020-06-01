package org.ovirt.engine.core.utils.log;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.slf4j.Logger;

/**
 * Utilities for logging commands using the {@link Logged} annotation for configuring the logging behavior of a class.<br>
 * Usage example:
 *
 * <pre>
 * public void execute() {
 *     String logId = LoggedUtils.createLogId();
 *     <b>LoggedUtils.logEntry(log, logId, this);</b>
 *
 *     try {
 *         Object returnValue = executeCommand();
 *         <b>LoggedUtils.logReturn(log, logId, this, returnValue);</b>
 *         return returnValue;
 *     } catch (Exception e) {
 *         <b>LoggedUtils.logError(log, logId, this, e);</b>
 *         throw e;
 *     }
 * }
 * </pre>
 */
public class LoggedUtils {

    /* --- Constants --- */

    /**
     * Log format for entry log message.
     */
    protected static final String ENTRY_LOG = "START, {}, log id: {}";

    /**
     * Log format for return log message with no return value.
     */
    protected static final String EXIT_LOG_VOID = "FINISH, {}, log id: {}";

    /**
     * Log format for return log message with a return value.
     */
    protected static final String EXIT_LOG_RETURN_VALUE = "FINISH, {}, return: {}, log id: {}";

    /**
     * Log format for exception log message with exception message.
     */
    protected static final String ERROR_LOG = "ERROR, {}, exception: {}, log id: {}";

    /* --- Public Methods --- */

    /**
     * Get the object (reference) id for an object. This should be a rather unique value, since it's based on the
     * object's hash code.
     *
     * @param obj
     *            The object to get the id for (can be <code>null</code>).
     * @return The id, in HEX representation (for <code>null</code> it's 0).
     * @see System#identityHashCode(Object)
     */
    public static String getObjectId(Object obj) {
        return Integer.toHexString(System.identityHashCode(obj));
    }

    /**
     * Log entry into a command to the given log, using the specification in the {@link Logged} annotation on the
     * object's. If there's no {@link Logged} annotation present, or if the log level isn't sufficient to log, then
     * nothing happens.
     *
     * @param log
     *            The log to log to.
     * @param id
     *            ID of the message, for finding the return message.
     * @param obj
     *            The object to log for.
     */
    public static void logEntry(Logger log, String id, Object obj) {
        Logged logged = getAnnotation(obj);
        if (logged != null) {
            log(log, logged.executionLevel(), ENTRY_LOG, determineMessage(log, logged, obj), id);
        }
    }

    /**
     * Log return of a command to the given log, using the specification in the {@link Logged} annotation on the
     * object's. If there's no {@link Logged} annotation present, or if the log level isn't sufficient to log, then
     * nothing happens.
     *
     * @param log
     *            The log to log to.
     * @param id
     *            ID of the message, for finding the entry message.
     * @param obj
     *            The object to log for.
     */
    public static void logReturn(Logger log, String id, Object obj, Object returnValue) {
        Logged logged = getAnnotation(obj);
        if (logged != null) {
            LogLevel logLevel = logged.executionLevel();
            if (isLogLevelOn(log, logLevel)) {
                if (returnValue == null
                        || !isLogLevelOn(log, LogLevel.getMinimalLevel(logLevel, logged.returnLevel()))) {
                    log(log, logLevel, EXIT_LOG_VOID, obj.getClass().getSimpleName(), id);
                } else {
                    log(log, logLevel, EXIT_LOG_RETURN_VALUE, obj.getClass().getSimpleName(), returnValue, id);
                }
            }
        }
    }

    /**
     * Log error of a command to the given log, using the specification in the {@link Logged} annotation on the
     * object's. If there's no {@link Logged} annotation present, or if the log level isn't sufficient to log, then
     * nothing happens.
     *
     * @param log
     *            The log to log to.
     * @param id
     *            ID of the message, for finding the entry message.
     * @param obj
     *            The object to log for.
     */
    public static void logError(Logger log, String id, Object obj, Throwable t) {
        Logged logged = getAnnotation(obj);
        if (logged != null && isLogLevelOn(log, logged.errorLevel())) {
            log(log, logged.errorLevel(), ERROR_LOG, determineMessage(log, logged, obj),
                    t.getMessage(), id);
            log.error("Exception {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    /* --- Helper Methods --- */

    /**
     * Log a message to the log, taking into account the log level (checking if it's active, and using it in the log).
     * The message may contain parameters as in the {@code MessageFormat} specification, and they will undergo
     * transformation only if the log level is sufficient for the given log.
     *
     * @param log
     *            The log to log to.
     * @param logLevel
     *            The log level (may not be null).
     * @param message
     *            The message to log.
     * @param parameters
     *            Optional parameters for the message.
     * @see MessageFormat#format(Object[], StringBuffer, java.text.FieldPosition)
     */
    protected static void log(Logger log, Logged.LogLevel logLevel, String message, Object... parameters) {
        try {
            switch (logLevel) {
            case FATAL:
            case ERROR:
                log.error(message, parameters);
                break;
            case WARN:
                log.warn(message, parameters);
                break;
            case INFO:
                log.info(message, parameters);
                break;
            case DEBUG:
                log.debug(message, parameters);
                break;
            case TRACE:
                log.trace(message, parameters);
                break;
            case OFF:
            default:
                break;
            }
        } catch (Throwable th) {
            try {
                log.error("Cannot perform logging {}", ExceptionUtils.getRootCauseMessage(th));
                log.debug("Exception", th);
            } catch (Throwable th1) {
                // Cannot really do any logging - better not try again, we have a serious logging problem
            }
        }
    }

    /**
     * Return the annotation present on the object's class. Since the annotation is inherited, it can be places on a
     * superclass and retrieved from there.
     *
     * @param obj
     *            The object to get the annotation for.
     * @return The {@link Logged} annotation from the class, or <code>null</code> if there isn't any.
     */
    protected static Logged getAnnotation(Object obj) {
        return obj == null ? null : obj.getClass().getAnnotation(Logged.class);
    }

    /**
     * Determine if the given log level is active for the given log.
     *
     * @param log
     *            The log to check.
     * @param logLevel
     *            The log level to check if active.
     * @return Whether the log level is active on the log or not.
     */
    protected static boolean isLogLevelOn(Logger log, Logged.LogLevel logLevel) {
        switch (logLevel) {
        case FATAL:
        case ERROR:
            return log.isErrorEnabled();
        case WARN:
            return log.isWarnEnabled();
        case INFO:
            return log.isInfoEnabled();
        case DEBUG:
            return log.isDebugEnabled();
        case TRACE:
            return log.isTraceEnabled();
        case OFF:
        default:
            return false;
        }
    }

    /**
     * Determine the message to print, according to whether the parameters should be expanded or not.
     *
     * @param log
     *            The log is needed to check if the parameters should be printed according to their level.
     * @param logged
     *            The logging definition, for determining whether to expand the parameters or not.
     * @param obj
     *            The object for which to get the message.
     *
     * @return The message for logging.
     */
    protected static Object determineMessage(Logger log, Logged logged, Object obj) {
        if (logged != null
                && !isLogLevelOn(log, LogLevel.getMinimalLevel(logged.parametersLevel(), logged.executionLevel()))) {
            return obj.getClass().getName();
        }

        return obj;
    }
}
