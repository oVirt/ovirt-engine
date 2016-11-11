package org.ovirt.engine.core.logutils;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * Provides ability to log records with different time zone than the default.
 */
public class TimeZoneBasedFormatter extends Formatter {
    /**
     * Log record format specified as string according to {@code java.util.Formatter}. The only exception
     * is date/time which is defined as string (format of this string is defined in
     * {@code org.ovirt.engine.core.logutils.TimeZoneBasedFormatter#dateTimeFormat}).
     * Log record format can be specified using property
     * {@code org.ovirt.engine.core.logutils.TimeZoneBasedFormatter.recordFormat}. If this property is empty, we use
     * default format {@code %1$s %4$-7s %5$s%6$s%n}
     */
    private static final String recordFormat =
            getLoggingProperty(
                    "org.ovirt.engine.core.logutils.TimeZoneBasedFormatter.recordFormat",
                    "%1$s %4$-7s %5$s%6$s%n");

    /**
     * Date/time part format of log record specified as string according to {@code java.text.SimpleDateFormat}.
     * It can be specified using property
     * {@code org.ovirt.engine.core.logutils.TimeZoneBasedFormatter.dateTimeFormat}. If this property is empty, we use
     * default format {@code yyyy-MM-dd HH:mm:ss,SSSX}
     */
    private static final SimpleDateFormat dateTimeFormat =
            new SimpleDateFormat(
                    getLoggingProperty(
                            "org.ovirt.engine.core.logutils.TimeZoneBasedFormatter.dateTimeFormat",
                            "yyyy-MM-dd HH:mm:ss,SSSX"));

    /**
     * Time zone used to format date/time part of the log record.
     * It can be specified using property
     * {@code org.ovirt.engine.core.logutils.TimeZoneBasedFormatter.timeZone}. If this property is empty, we use
     * time zone set in the underlying OS.
     */
    private static final TimeZone timeZone =
            TimeZone.getTimeZone(
                    getLoggingProperty(
                            "org.ovirt.engine.core.logutils.TimeZoneBasedFormatter.timeZone",
                            TimeZone.getDefault().getID()));

    public TimeZoneBasedFormatter() {
        super();
    }

    @Override
    public synchronized String format(LogRecord record) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(timeZone);
        dateTimeFormat.setTimeZone(timeZone);
        cal.setTimeInMillis(record.getMillis());
        String source;
        if (record.getSourceClassName() != null) {
            source = record.getSourceClassName();
            if (record.getSourceMethodName() != null) {
                source += " " + record.getSourceMethodName();
            }
        } else {
            source = record.getLoggerName();
        }
        String message = formatMessage(record);
        String throwable = "";
        if (record.getThrown() != null) {
            try (CharArrayWriter wr = new CharArrayWriter(1024);
                    PrintWriter pw = new PrintWriter(wr)) {
                pw.println();
                record.getThrown().printStackTrace(pw);
                throwable = wr.toString();
            }
        }
        return String.format(recordFormat,
                dateTimeFormat.format(cal.getTime()),
                source,
                record.getLoggerName(),
                record.getLevel().getName(),
                message,
                throwable);
    }

    private static String getLoggingProperty(String key, String defaultValue) {
        String value = LogManager.getLogManager().getProperty(key);
        return value == null || value.isEmpty() ? defaultValue : value;
    }
}
