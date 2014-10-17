package org.ovirt.engine.core.utils.log;

import java.text.MessageFormat;

@Deprecated
public class Log {

    private final org.slf4j.Logger log;

    protected Log(org.slf4j.Logger log) {
        this.log = log;
    }

    public void debug(Object msg) {
        log.debug(convertToString(msg));
    }

    public void debug(Object msg, Throwable t) {
        log.debug(convertToString(msg), t);
    }

    public void error(Object msg) {
        log.error(convertToString(msg));
    }

    public void error(Object msg, Throwable t) {
        log.error(convertToString(msg), t);
    }

    public void info(Object msg) {
        log.info(convertToString(msg));
    }

    public void info(Object msg, Throwable t) {
        log.info(convertToString(msg), t);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return log.isErrorEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    public void trace(Object msg) {
        log.trace(convertToString(msg));
    }

    public void trace(Object msg, Throwable t) {
        log.trace(convertToString(msg), t);
    }

    public void warn(Object msg) {
        log.warn(convertToString(msg));
    }

    public void warn(Object msg, Throwable t) {
        log.warn(convertToString(msg), t);
    }

    public void traceFormat(String formatString, Object... args) {
        if (isTraceEnabled()) {
            Throwable throwable = extractException(args);
            if (throwable != null)
                trace(transform(formatString, args), throwable);
            else
                trace(transform(formatString, args));
        }
    }

    public void infoFormat(String formatString, Object... args) {
        Throwable throwable = extractException(args);
        if (throwable != null)
            info(transform(formatString, args), throwable);
        else
            info(transform(formatString, args));
    }

    public void warnFormat(String formatString, Object... args) {
        Throwable throwable = extractException(args);
        if (throwable != null)
            warn(transform(formatString, args), throwable);
        else
            warn(transform(formatString, args));
    }

    public void debugFormat(String formatString, Object... args) {
        if (isDebugEnabled()) {
            Throwable throwable = extractException(args);
            if (throwable != null)
                debug(transform(formatString, args), throwable);
            else
                debug(transform(formatString, args));
        }
    }

    public void errorFormat(String formatString, Object... args) {
        Throwable throwable = extractException(args);
        if (throwable != null)
            error(transform(formatString, args), throwable);
        else
            error(transform(formatString, args));
    }

    public void fatal(Object msg) {
        error(msg);
    }

    public void fatal(Object msg, Throwable t) {
        error(msg, t);
    }

    public void fatalFormat(String formatString, Object... args) {
        errorFormat(formatString, args);
    }

    protected String transform(String formatString, Object... args) {
        return MessageFormat.format(formatString.replaceAll("'", ""), args);
    }

    private Throwable extractException(Object... args) {
        for (Object arg : args) {
            if (arg instanceof Throwable)
                return (Throwable) arg;
        }

        return null;
    }

    private String convertToString(Object logMessage) {
        return logMessage == null ? null : logMessage.toString();
    }

}
