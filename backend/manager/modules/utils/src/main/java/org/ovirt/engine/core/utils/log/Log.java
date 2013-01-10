package org.ovirt.engine.core.utils.log;

import java.text.MessageFormat;

import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;

public class Log implements org.apache.commons.logging.Log {
    private static final String CORRELATION_ID_MESSAGE_FORMAT = "[%s] %s";
    private final org.apache.commons.logging.Log log;

    public Log(org.apache.commons.logging.Log log) {
        this.log = log;
    }

    @Override
    public void debug(Object arg0) {
        if (isDebugEnabled()) {
            log.debug(addPrefixToLogMessage(arg0));
        }
    }

    @Override
    public void debug(Object arg0, Throwable arg1) {
        if (isDebugEnabled()) {
            log.debug(addPrefixToLogMessage(arg0), arg1);
        }
    }

    @Override
    public void error(Object arg0) {
        log.error(addPrefixToLogMessage(arg0));
    }

    @Override
    public void error(Object arg0, Throwable arg1) {
        log.error(addPrefixToLogMessage(arg0), arg1);
    }

    @Override
    public void fatal(Object arg0) {
        log.fatal(addPrefixToLogMessage(arg0));
    }

    @Override
    public void fatal(Object arg0, Throwable arg1) {
        log.fatal(addPrefixToLogMessage(arg0), arg1);
    }

    @Override
    public void info(Object arg0) {
        log.info(addPrefixToLogMessage(arg0));
    }

    @Override
    public void info(Object arg0, Throwable arg1) {
        log.info(addPrefixToLogMessage(arg0), arg1);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void trace(Object arg0) {
        if (log.isTraceEnabled()) {
            log.trace(addPrefixToLogMessage(arg0));
        }
    }

    @Override
    public void trace(Object arg0, Throwable arg1) {
        if (log.isTraceEnabled()) {
            log.trace(addPrefixToLogMessage(arg0), arg1);
        }
    }

    @Override
    public void warn(Object arg0) {
        log.warn(addPrefixToLogMessage(arg0));
    }

    @Override
    public void warn(Object arg0, Throwable arg1) {
        log.warn(addPrefixToLogMessage(arg0), arg1);
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

    public void fatalFormat(String formatString, Object... args) {
        Throwable throwable = extractException(args);
        if (throwable != null)
            error(transform(formatString, args), throwable);
        else
            error(transform(formatString, args));
    }

    public String transform(String formatString, Object... args) {
        formatString = formatString.replaceAll("'", "");
        return MessageFormat.format(formatString, args);
    }

    public Throwable extractException(Object... args) {
        for (Object arg : args) {
            if (arg instanceof Throwable)
                return (Throwable) arg;
        }

        return null;
    }

    private Object addPrefixToLogMessage(Object logMessage) {
        String correlationId = ThreadLocalParamsContainer.getCorrelationId();
        if (correlationId == null) {
            return logMessage;
        }
        return String.format(CORRELATION_ID_MESSAGE_FORMAT, correlationId, logMessage);
    }

}
