package org.ovirt.engine.core.utils.log;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation for configuring logging behavior of a command.<br>
 * Use this annotation on a command class to configure how it should be logged, for example:
 *
 * <pre>
 * &#64;Logged(errorLogLevel = LogLevel.WARN, executionLevel = LogLevel.DEBUG)
 * class A {
 *     ...
 * }
 * </pre>
 *
 * Will define that the invocation of A should be logged with log level WARN when logging an exception, and that in case
 * of return values it shouldn't expand them (print their <code>toString()</code> representation) unless the log level
 * DEBUG is on. All other definitions are taken from their default values.
 *
 * @see LoggedUtils LoggedUtils has some helper methods for logging using this annotation.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Logged {

    /* --- Static Inner Types --- */

    /**
     * Log level to use when logging with the {@link Logged} annotation.<br> {@link LogLevel#OFF} means logging will
     * never-ever occur, other values correspond to the ones in {@link Log}.
     */
    public static enum LogLevel {
        OFF,
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL;

        /**
         * Get the lesser log level from the given two levels.
         *
         * @param firstLevel
         *            First log level.
         * @param secondLevel
         *            Second log level.
         * @return The log level which is minimal.
         */
        public static LogLevel getMinimalLevel(LogLevel firstLevel, LogLevel secondLevel) {
            if (firstLevel.ordinal() < secondLevel.ordinal()) {
                return firstLevel;
            }

            return secondLevel;
        }
    }

    /* --- Annotation Properties --- */

    /**
     * The log level for method execution (entry + return).<br>
     * <b>Default:</b> {@link LogLevel#INFO}.
     */
    LogLevel executionLevel() default LogLevel.INFO;

    /**
     * The log level for method exit (by exception).<br>
     * <b>Default:</b> {@link LogLevel#ERROR}.
     */
    LogLevel errorLevel() default LogLevel.ERROR;

    /**
     * The parameters level determines when should the command parameters be expanded (printed in whole). The
     * {@link Logged#executionLevel()} is used as a logical upper-bound for this parameter, so it makes no sense to set
     * it higher than that.<br>
     * If the log level is lower than {@link Logged#executionLevel()}, and the {@link Logged#executionLevel()} is
     * enabled in the log, then:
     * <ul>
     * <li>If the parameters level is enabled in the log, then the parameters are expanded.</li>
     * <li>Otherwise, the full class name is printed.</li>
     * </ul>
     * <b>Default:</b> {@link LogLevel#INFO}.
     */
    LogLevel parametersLevel() default LogLevel.INFO;

    /**
     * The return level determines when should the command's return value be expanded (printed in whole). The
     * {@link Logged#executionLevel()} is used as a logical upper-bound for this parameter, so it makes no sense to set
     * it higher than that.<br>
     * If the log level is lower than {@link Logged#executionLevel()}, and the {@link Logged#executionLevel()} is
     * enabled in the log, then:
     * <ul>
     * <li>If the return level is enabled in the log, then the return value is printed.</li>
     * <li>Otherwise, the return value is not printed, only a return message.</li>
     * </ul>
     * <b>Default:</b> {@link LogLevel#INFO}.
     */
    LogLevel returnLevel() default LogLevel.INFO;
}
