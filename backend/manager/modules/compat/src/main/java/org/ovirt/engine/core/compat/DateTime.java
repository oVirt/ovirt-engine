package org.ovirt.engine.core.compat;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateTime extends Date {
    private static final String[] dayNames = new DateFormatSymbols().getWeekdays();

    public DateTime() {
        this(getMinValue());
    }

    public DateTime(Date argvalue) {
        super(argvalue.getTime());
    }

    public DateTime(long millis) {
        super(millis);
    }

    /**
     * This method resets the datetime object to 00:00:00.000 on the same date
     */
    public DateTime resetToMidnight() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new DateTime(cal.getTime());
    }

    public DayOfWeek getDayOfWeek() {
        return DayOfWeek.forValue(this.getDay());
    }

    public String toString(DateFormat dateFormat) {
        return dateFormat.format(this);
    }

    public Date addSeconds(int i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.SECOND, i);
        return cal.getTime();
    }

    public DateTime addDays(int i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.DATE, i);
        return new DateTime(cal.getTime());
    }

    public TimeSpan subtract(Date date) {
        long span = this.getTime() - date.getTime();
        return new TimeSpan(span);
    }

    /**
     * The Min Date in java
     *
     * @return - a date representing - Thu Jan 01 00:00:00 IST 1970
     */
    public static Date getMinValue() {
        GregorianCalendar javaEpochTime = new GregorianCalendar();
        javaEpochTime.clear();
        return javaEpochTime.getTime();
    }

    public static DateTime getNow() {
        return new DateTime(System.currentTimeMillis());
    }

    public Date addMinutes(int i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this);
        cal.add(Calendar.MINUTE, i);
        return new DateTime(cal.getTime());
    }

    public static String getDayOfTheWeekAsString(int dayOfTheWeek) {
        return dayNames[dayOfTheWeek];
    }
}
