package org.ovirt.engine.core.compat;

import java.io.Serializable;

import com.google.gwt.regexp.shared.RegExp;

public class TimeSpan implements Comparable<TimeSpan>, Serializable {
    public static int MS_PER_SECOND = 1000;
    public static int MS_PER_MINUTE = 60 * 1000;
    public static int MS_PER_HOUR = 60 * 60 * 1000;
    public static int MS_PER_DAY = 24 * 60 * 60 * 1000;

    public int Days;
    public int Hours;
    public int Minutes;
    public int Seconds;
    public int Milliseconds;
    public double TotalDays;
    public double TotalHours;
    public double TotalMinutes;
    public double TotalSeconds;
    public long TotalMilliseconds;

    public TimeSpan() {
    }


    public TimeSpan(long milliseconds) {
        TotalMilliseconds = milliseconds;
        computeProperties();
    }

    public TimeSpan(int hours, int minutes, int seconds) {
        this(0, hours, minutes, seconds, 0);
    }

    public TimeSpan(int days, int hours, int minutes, int seconds) {
        this(days, hours, minutes, seconds, 0);
    }

    public TimeSpan(int days, int hours, int minutes, int seconds, int milliseconds) {
        TotalMilliseconds = milliseconds;
        TotalMilliseconds += seconds * MS_PER_SECOND;
        TotalMilliseconds += minutes * MS_PER_MINUTE;
        TotalMilliseconds += hours * MS_PER_HOUR;
        TotalMilliseconds += days * MS_PER_DAY;
        computeProperties();
    }


    public TimeSpan(int[] data) {
        this(data[0], data[1], data[2], data[3], data[4]);
    }


    protected void computeProperties() {
        long remainder = TotalMilliseconds;
        // Days
        Days = (int) (remainder / MS_PER_DAY);
        remainder = remainder % MS_PER_DAY;
        // Hours
        Hours = (int) (remainder / MS_PER_HOUR);
        remainder = remainder % MS_PER_HOUR;
        remainder = remainder % MS_PER_DAY;
        // Minutes
        Minutes = (int) (remainder / MS_PER_MINUTE);
        remainder = remainder % MS_PER_MINUTE;
        // Minutes
        Seconds = (int) (remainder / MS_PER_SECOND);
        remainder = remainder % MS_PER_SECOND;

        Milliseconds = (int) remainder;
    }

    // The format for a timespan is:
    // [ws][-]{ d | d.hh:mm[:ss[.ff]] | hh:mm[:ss[.ff]] }[ws]
    public static TimeSpan parse(String argvalue) {
        String cleaned = argvalue.trim();
        int multiplier = 1;
        if (cleaned.contains("-")) {
            multiplier = -1;
            cleaned = cleaned.replace("-", "").trim();
        }

        RegExp r = RegExp.compile("^[0-9]+$");
        if (r.test(cleaned)) {
            int days = Integer.parseInt(cleaned);
            return new TimeSpan(days * multiplier, 0, 0, 0);
        }

        String regex = "^[0-9]+.[0-9]{2}:[0-9]{2}(:[0-9]{2}(.[0-9]{2})?)?$";
        r = RegExp.compile(regex);
        if (r.test(cleaned)) {
            String[] items = cleaned.split("[:.]");
            int[] data = new int[5];

            for (int x = 0; x < items.length; x++) {
                data[x] = Integer.parseInt(items[x]) * multiplier;
            }

            return new TimeSpan(data);
        }

        regex = "^[0-9]{2}:[0-9]{2}(:[0-9]{2}(.[0-9]{2})?)?$";
        r = RegExp.compile(regex);
        if (r.test(cleaned)) {
            String[] items = cleaned.split("[:.]");
            int[] data = new int[5];

            for (int x = 0; x < items.length; x++) {
                data[x+1] = Integer.parseInt(items[x]) * multiplier;
            }

            return new TimeSpan(data);
        }
        // If we get to here, it is invalid
        throw new IllegalArgumentException("Invalid TimeSpan");
    }

    public static TimeSpan tryParse(String string) {
        try {
            return TimeSpan.parse(string);
        } catch (IllegalArgumentException e) {
            //eat it, return null
            return null;
        }
    }


    @Override
    public int compareTo(TimeSpan o) {
        int result;
        if (TotalMilliseconds < o.TotalMilliseconds) {
            result = -1;
        } else if (TotalMilliseconds > o.TotalMilliseconds) {
            result = 1;
        } else {
            result = 0;
        }
        return result;
    }

}
