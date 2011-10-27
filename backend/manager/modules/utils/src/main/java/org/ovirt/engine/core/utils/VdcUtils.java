package org.ovirt.engine.core.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class VdcUtils {
    public static Date addMinutes(Date aDate, long minutes) {
        long millesecondsToAdd = 60 * 1000 * minutes;
        return new Date(aDate.getTime() + millesecondsToAdd);
    }

    public static Date addMinutes(Date aDate, double minutes) {
        long millesecondsToAdd = (long) (60 * 1000 * minutes);
        return new Date(aDate.getTime() + millesecondsToAdd);
    }

    public static final String DATE_FORMAT = "MM/dd/yyyy hh:mm:ss";

    public static String formatDate(Date aDate) {
        SimpleDateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
        return fmt.format(aDate);
    }
}
