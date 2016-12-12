package org.slf4j;

import java.util.logging.Level;

/**
 * GWT override for SLF4J Logger.
 */
public class Logger {

    private final java.util.logging.Logger log;

    public Logger(java.util.logging.Logger log) {
        this.log = log;
    }

    public void debug(String msg) {
        log.log(Level.FINE, msg);
    }

    public void debug(String format, Object... arguments) {
        doFormat(Level.FINE, format, arguments);
    }

    public void debug(String msg, Throwable t) {
        log.log(Level.FINE, msg, t);
    }

    public void error(String msg) {
        log.log(Level.SEVERE, msg);
    }

    public void error(String format, Object... arguments) {
        doFormat(Level.SEVERE, format, arguments);
    }

    public void error(String msg, Throwable t) {
        log.log(Level.SEVERE, msg, t);
    }

    public void info(String msg) {
        log.log(Level.INFO, msg);
    }

    public void info(String format, Object... arguments) {
        doFormat(Level.INFO, format, arguments);
    }

    public void info(String msg, Throwable t) {
        log.log(Level.INFO, msg, t);
    }

    public void trace(String msg) {
        log.log(Level.FINER, msg);
    }

    public void trace(String format, Object... arguments) {
        doFormat(Level.FINER, format, arguments);
    }

    public void trace(String msg, Throwable t) {
        log.log(Level.FINER, msg, t);
    }

    public void warn(String msg) {
        log.log(Level.WARNING, msg);
    }

    public void warn(String format, Object... arguments) {
        doFormat(Level.WARNING, format, arguments);
    }

    public void warn(String msg, Throwable t) {
        log.log(Level.WARNING, msg, t);
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

    private void doFormat(Level level, String format, Object... arguments) {
        if (log.isLoggable(level) && format != null) {
            StringBuilder msg = new StringBuilder();
            int i = 0;
            int last = 0;
            int current;
            while ((current = format.indexOf("{}", last)) != -1) {
                msg.append(format, last, current);
                msg.append(arguments[i++]);
                last = current + 2;
            }
            msg.append(format, last, format.length());
            log.log(level, msg.toString());
        }
    }

}
