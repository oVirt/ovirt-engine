package org.apache.commons.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.core.compat.StringFormat;

/**
 * GWT Override for Log - uses {@link StringFormat#formatDotNet(String, Object...)}
 */
public class Log {
    private final Logger log;

    public Log(Logger log) {
        this.log = log;
    }

    public void debug(Object message) {
        log.log(Level.FINE, message.toString());
    }

    public void debug(Object message, Throwable arg1) {
        log.log(Level.FINE, message.toString(), arg1);
    }

    public void error(Object message) {
        log.log(Level.SEVERE, message.toString());
    }

    public void error(Object message, Throwable arg1) {
        log.log(Level.SEVERE, message.toString(), arg1);
    }

    public void fatal(Object message) {
        log.log(Level.SEVERE, message.toString());
    }

    public void fatal(Object message, Throwable arg1) {
        log.log(Level.SEVERE, message.toString(), arg1);
    }

    public void info(Object message) {
        log.log(Level.INFO, message.toString());
    }

    public void info(Object message, Throwable arg1) {
        log.log(Level.INFO, message.toString(), arg1);
    }

    public void trace(Object message) {
        log.log(Level.FINER, message.toString());
    }

    public void trace(Object message, Throwable arg1) {
        log.log(Level.FINER, message.toString(), arg1);
    }

    public void warn(Object message) {
        log.log(Level.WARNING, message.toString());
    }

    public void warn(Object message, Throwable arg1) {
        log.log(Level.WARNING, message.toString(), arg1);
    }

    public void debugFormat(String formatString, Object... args) {
        if (isDebugEnabled()) {
            Throwable throwable = extractException(args);
            if (throwable != null) {
                debug(transform(formatString, args), throwable);
            } else {
                debug(transform(formatString, args));
            }
        }
    }

    public void errorFormat(String formatString, Object... args) {
        Throwable throwable = extractException(args);
        if (throwable != null) {
            error(transform(formatString, args), throwable);
        } else {
            error(transform(formatString, args));
        }
    }

    public void fatalFormat(String formatString, Object... args) {
        Throwable throwable = extractException(args);
        if (throwable != null) {
            error(transform(formatString, args), throwable);
        } else {
            error(transform(formatString, args));
        }
    }

    public void infoFormat(String formatString, Object... args) {
        Throwable throwable = extractException(args);
        if (throwable != null) {
            info(transform(formatString, args), throwable);
        } else {
            info(transform(formatString, args));
        }
    }

    public void traceFormat(String formatString, Object... args) {
        if (isTraceEnabled()) {
            Throwable throwable = extractException(args);
            if (throwable != null) {
                trace(transform(formatString, args), throwable);
            } else {
                trace(transform(formatString, args));
            }
        }
    }

    public void warnFormat(String formatString, Object... args) {
        Throwable throwable = extractException(args);
        if (throwable != null) {
            warn(transform(formatString, args), throwable);
        } else {
            warn(transform(formatString, args));
        }
    }

    public boolean isDebugEnabled() {
        return log.isLoggable(Level.FINE);
    }

    public boolean isErrorEnabled() {
        return log.isLoggable(Level.SEVERE);
    }

    public boolean isFatalEnabled() {
        return log.isLoggable(Level.SEVERE);
    }

    public boolean isInfoEnabled() {
        return log.isLoggable(Level.INFO);
    }

    public boolean isTraceEnabled() {
        return log.isLoggable(Level.FINER);
    }

    public boolean isWarnEnabled() {
        return log.isLoggable(Level.WARNING);
    }

    private Throwable extractException(Object... args) {
        for (Object arg : args) {
            if (arg instanceof Throwable) {
                return (Throwable) arg;
            }
        }

        return null;
    }

    private String transform(String formatString, Object... args) {
        formatString = formatString.replaceAll("'", "");
        return StringFormat.formatDotNet(formatString, args);
    }

}
