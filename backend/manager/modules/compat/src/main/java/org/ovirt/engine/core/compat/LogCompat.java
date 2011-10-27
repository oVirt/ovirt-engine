package org.ovirt.engine.core.compat;

import java.text.MessageFormat;

import org.apache.commons.logging.Log;

public class LogCompat implements Log {
    private final Log log;

    public LogCompat(Log log) {
        this.log = log;
    }

    @Override
    public void debug(Object arg0) {
        log.debug(arg0);
    }

    @Override
    public void debug(Object arg0, Throwable arg1) {
        log.debug(arg0, arg1);
    }

    @Override
    public void error(Object arg0) {
        log.error(arg0);
    }

    @Override
    public void error(Object arg0, Throwable arg1) {
        log.error(arg0, arg1);
    }

    @Override
    public void fatal(Object arg0) {
        log.fatal(arg0);
    }

    @Override
    public void fatal(Object arg0, Throwable arg1) {
        log.fatal(arg0, arg1);
    }

    @Override
    public void info(Object arg0) {
        log.info(arg0);
    }

    @Override
    public void info(Object arg0, Throwable arg1) {
        log.info(arg0, arg1);
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
        log.trace(arg0);
    }

    @Override
    public void trace(Object arg0, Throwable arg1) {
        log.trace(arg0, arg1);
    }

    @Override
    public void warn(Object arg0) {
        log.warn(arg0);
    }

    @Override
    public void warn(Object arg0, Throwable arg1) {
        log.warn(arg0, arg1);
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

    //
    // @Override
    // public void Debug(String msg) {
    // log.debug(msg) ;
    //
    // }
    //
    // @Override
    // public void Debug(Object message, Throwable exception) {
    // log.debug(message, exception) ;
    // }
    //
    // @Override
    // public void DebugFormat(String msg) {
    // log.debug(msg) ;
    // }
    //
    // @Override
    // public void DebugFormat(String format, Object[] args) {
    // log.debug(String.format(format, args)) ;
    // }
    //
    // @Override
    // public void Error(Object message) {
    // log.error(message) ;
    // }
    //
    // @Override
    // public void Error(Object message, Throwable exception) {
    // log.error(message, exception) ;
    // }
    //
    // @Override
    // public void ErrorFormat(String format, Object[] args) {
    // log.error(String.format(format, args)) ;
    // }
    //
    // @Override
    // public void Fatal(Object message) {
    // log.fatal(message) ;
    // }
    //
    // @Override
    // public void Fatal(Object message, Throwable exception) {
    // log.fatal(message, exception) ;
    // }
    //
    // @Override
    // public void FatalFormat(String format, Object[] args) {
    // log.fatal(String.format(format, args)) ;
    // }
    //
    // @Override
    // public void Info(Object message) {
    // log.info(message) ;
    // }
    //
    // @Override
    // public void Info(Object message, Throwable exception) {
    // log.info(message, exception) ;
    // }
    //
    // @Override
    // public void InfoFormat(String format, Object[] args) {
    // log.info(String.format(format, args)) ;
    // }
    //
    // @Override
    // public void Warn(Object message) {
    // log.warn(message) ;
    // }
    //
    // @Override
    // public void Warn(Object message, Throwable exception) {
    // log.warn(message, exception) ;
    // }
    //
    // @Override
    // public void WarnFormat(String format, Object[] args) {
    // log.warn(String.format(format, args)) ;
    // }
}
