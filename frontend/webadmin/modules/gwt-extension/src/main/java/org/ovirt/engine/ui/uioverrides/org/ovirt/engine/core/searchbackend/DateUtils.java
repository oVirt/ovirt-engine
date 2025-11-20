
package org.ovirt.engine.core.searchbackend;

import java.text.DateFormat;
import java.util.Date;

import org.ovirt.engine.core.compat.DateTime;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

class DateUtils {

    static Date parse(String str) {
        try {
            return DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM).parse(str);
        } catch (IllegalArgumentException ignore) {
        }
        return null;
    }

    private static DateTimeFormat getFormat(int dateStyle) {
        switch (dateStyle) {
            case DateFormat.FULL:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_FULL);
            case DateFormat.LONG:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_LONG);
            case DateFormat.SHORT:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);
            default:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_MEDIUM);
        }
    }

    private static DateTimeFormat getFormat(int dateStyle, int timeStyle) {
        switch (timeStyle) {
            case DateFormat.FULL:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_FULL);
            case DateFormat.LONG:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_LONG);
            case DateFormat.SHORT:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
            default:
                return DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_MEDIUM);
        }
    }

    //No need to transform to ISO format on frontend
    public static String toIsoFormat(DateTime date) {
        return null;
    }

    public static String getDayOfWeek(int addDays) {
        Date date = new Date();
        return DateTime.getDayOfTheWeekAsString(date.getDay()).toUpperCase();
    }
}
