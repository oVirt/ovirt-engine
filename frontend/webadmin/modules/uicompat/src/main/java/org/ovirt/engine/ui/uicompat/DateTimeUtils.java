package org.ovirt.engine.ui.uicompat;

import java.util.Date;

import org.ovirt.engine.core.compat.TimeSpan;



public class DateTimeUtils {

	/**
	 * @param source
	 * @returnReturns Date object with zero time. 
	 * For example: getDate(05/05/10 12:45:00) will return 05/05/10 00:00
	 */
	public static Date getDate(Date source) {
		Date date = new Date(source.getTime());
		date.setHours(0);
		date.setMinutes(0);
		date.setSeconds(0);
		return date;
		//
	}

	/**
	 * @return The maximum date and time which is 12/31/9999 23:59:59 IST
	 */
	public static Date getMaxValue() {
		// Time in millis that represents Fri Dec 31 23:59:59 IST 9999
		return new Date(253402293599807l);
	}

	/**
	 * @return The minimum date and time (Epoch time) which is 1/1/1970 00:00:00 IST
	 */
	public static Date getMinValue() {
		// Time in millis that represents Thu Jan 01 00:00:00 IST 1970
		return new Date(-7200000);
	}

	/**
	 * @param source
	 * @return A String contains the time part of a date object, for example: 05/05/10 12:45:00 will return "12:45"
	 */
	public static String toShortTimeString(Date source) {
		// TODO Add locale support to show the time in 12 hours format
		return new String(source.getHours() + ":" + source.getMinutes()); //$NON-NLS-1$
	}

	/**
	 * @param source
	 * @return Object of type TimeSpan containing the time part of date.
	 */
	public static TimeSpan getTimeOfDay(Date source) {
		return new TimeSpan(source.getHours(), source.getMinutes(), source.getSeconds());
	}

	/**
	 * @param date
	 * @param time
	 * @return The original date minus the time span given in the "time" parameter
	 */
	public static Date substract(Date date, TimeSpan time) {
		return new Date((date.getTime() - time.TotalMilliseconds));
	
	}

	/**
	 * @param date
	 * @param time
	 * @return The original date plus the time span given in the "time" parameter
	 */
	public static Date add(Date date, TimeSpan time) {
		return new Date((date.getTime() + time.TotalMilliseconds));
	}

	/**
	 * @param date
	 * @param time
	 * @return The original date plus the number of days given in the "days" parameter
	 */
	public static Date addDays(Date date, int days) {
		return add(date, new TimeSpan(days, 0, 0, 0));
	}

}
