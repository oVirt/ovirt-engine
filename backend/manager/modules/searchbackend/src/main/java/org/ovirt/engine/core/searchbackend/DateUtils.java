package org.ovirt.engine.core.searchbackend;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.ovirt.engine.core.compat.DateTime;

class DateUtils {

    static Date parse(String str) {
        for (DateFormat fmt : formats(DateFormat.DEFAULT, DateFormat.FULL, DateFormat.LONG, DateFormat.MEDIUM, DateFormat.SHORT)) {
            try {
                return fmt.parse(str);
            } catch (ParseException ignore) {
            }
        }
        return null;
    }

    private static List<DateFormat> formats(int... styles) {
        List<DateFormat> formats = new ArrayList<>();
        for (int style : styles) {
            addFormat(formats, style);
        }
        return formats;
    }

    private static void addFormat(List<DateFormat> formats, int style) {
        formats.add(getFormat(style));
        formats.add(getFormat(style, style));
    }

    public static DateFormat getFormat(int dateStyle) {
        return DateFormat.getDateInstance(dateStyle);
    }

    public static DateFormat getFormat(int dateStyle, int timeStyle) {
        return DateFormat.getDateTimeInstance(dateStyle, timeStyle);
    }

    public static String getDayOfWeek(int addDays) {
        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, addDays);
        return DateTime.getDayOfTheWeekAsString(date.get(Calendar.DAY_OF_WEEK)).toUpperCase();
    }
}
