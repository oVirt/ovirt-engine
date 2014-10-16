package org.ovirt.engine.core.utils.log;

import java.text.MessageFormat;

import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;

public class Log {
    private static final String CORRELATION_ID_MESSAGE_FORMAT = "[%s] %s";

    private final org.slf4j.Logger log;

    protected Log(org.slf4j.Logger log) {
        this.log = log;
    }

    public void debug(Object msg) {
        if (isDebugEnabled()) {
            log.debug(addPrefixToLogMessage(msg));
        }
    }

    public void debug(Object msg, Throwable t) {
        if (isDebugEnabled()) {
            log.debug(addPrefixToLogMessage(msg), t);
        }
    }

    public void error(Object msg) {
        log.error(addPrefixToLogMessage(msg));
    }

    public void error(Object msg, Throwable t) {
        log.error(addPrefixToLogMessage(msg), t);
    }

    public void info(Object msg) {
        log.info(addPrefixToLogMessage(msg));
    }

    public void info(Object msg, Throwable t) {
        log.info(addPrefixToLogMessage(msg), t);
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
        if (log.isTraceEnabled()) {
            log.trace(addPrefixToLogMessage(msg));
        }
    }

    public void trace(Object msg, Throwable t) {
        if (log.isTraceEnabled()) {
            log.trace(addPrefixToLogMessage(msg), t);
        }
    }

    public void warn(Object msg) {
        log.warn(addPrefixToLogMessage(msg));
    }

    public void warn(Object msg, Throwable t) {
        log.warn(addPrefixToLogMessage(msg), t);
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

    private String addPrefixToLogMessage(Object logMessage) {
        String correlationId = ThreadLocalParamsContainer.getCorrelationId();
        if (correlationId == null) {
            if (logMessage == null) {
                return null;
            } else {
                return logMessage.toString();
            }
        }
        return String.format(CORRELATION_ID_MESSAGE_FORMAT, correlationId, logMessage);
    }

}
