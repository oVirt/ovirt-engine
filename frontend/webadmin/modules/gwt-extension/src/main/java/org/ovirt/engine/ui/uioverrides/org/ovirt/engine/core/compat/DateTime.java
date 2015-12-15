package org.ovirt.engine.core.compat;

import java.util.Date;
import com.google.gwt.user.datepicker.client.CalendarUtil;


public class DateTime extends Date {

    private static final String[] dayNames = EnumCompat.GetNames(DayOfWeek.class);

    public DateTime() {
        this(getMinValue());
    }

    public DateTime(Date argvalue) {
        super(argvalue.getTime());
    }

    public DateTime(long millis) {
        super(millis);
    }

    public DayOfWeek getDayOfWeek() {
        return DayOfWeek.forValue(this.getDay());
    }

    //TODO: TODO-GWT public String toString(DateFormat dateFormat) {
    public String toString(Object dateFormat) {
        //TODO: TODO-GWT return  dateFormat.format(this);
        return null;
    }

    /**
     * The Min Date in java
     * @return - a date representing - Thu Jan 01 00:00:00 IST 1970
     */
    public static Date getMinValue() {
        // Return the static milliseconds representation of the min. date to avoid using GregorianCalendar which does
        // not pass GWT compilitation
        return new Date(-7200000);
    }

    public DateTime addDays(int i) {
        Date date = new Date();
        CalendarUtil.addDaysToDate(date, i);
        return new DateTime(date);
    }

    public static DateTime getNow() {
        Date date = new Date();
        return new DateTime(date.getTime());
    }

    public static String getDayOfTheWeekAsString(int dayOfTheWeek) {
        return dayNames[dayOfTheWeek];
    }

    public DateTime resetToMidnight() {
        Date date = new Date();
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        return new DateTime(date);
    }
}
