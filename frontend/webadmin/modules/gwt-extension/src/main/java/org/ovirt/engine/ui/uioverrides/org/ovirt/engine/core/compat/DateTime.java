package org.ovirt.engine.core.compat;

import java.util.ArrayList;
import java.util.Date;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;


public class DateTime extends Date {

    //public static Date Now = new DateTime();
//    public static DateTime Now2 = new DateTime();
	
    private static final String dayNames[] = EnumCompat.GetNames(DayOfWeek.class);
    
    public DateTime(int year, int month, int date) {
        this(new Date(year, month, date));
    }

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

    public long getTicks() {
        return this.getTime();
    }

    public long getTotalMilliseconds() {
        return this.getTime();
    }

	public DateTime(int i) {
		super(i);
	}

    public String toString(String formatString) {
    	//c# compatibility
    	boolean compat = false;
    	if(formatString.equals("yyyy-MM-ddTHH:mm:ss"))
    	{
    		formatString = "yyyy-MM-ddHH:mm:ss";
    		compat = true;
    	}
        
    	//TODO: GWT-TODO should be replaced
    	/*
    	SimpleDateFormat fmt = new SimpleDateFormat(formatString) ;
        String returnedValue = fmt.format(this);
        if(compat)
        {
        	returnedValue = returnedValue.substring(0,10)+"T"+returnedValue.substring(10);
        }
        return  returnedValue;*/
    	return null;
    }

    //TODO: GWT-TODO public String toString(DateFormat dateFormat) {
    public String toString(Object dateFormat) {
        //TODO: GWT-TODO return  dateFormat.format(this);
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
    
    public DateTime AddDays(int i) {
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
