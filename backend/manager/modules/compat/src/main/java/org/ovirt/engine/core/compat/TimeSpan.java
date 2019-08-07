package org.ovirt.engine.core.compat;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

public class TimeSpan implements Comparable<TimeSpan>, Serializable {
    private static final long serialVersionUID = -7809305197875724416L;

    public static final int MS_PER_SECOND = 1000;
    public static final int MS_PER_MINUTE = 60 * 1000;
    public static final int MS_PER_HOUR = 60 * 60 * 1000;
    public static final  int MS_PER_DAY = 24 * 60 * 60 * 1000;

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
        TotalMilliseconds += (long)minutes * (long)MS_PER_MINUTE;
        TotalMilliseconds += (long)hours * (long)MS_PER_HOUR;
        TotalMilliseconds += (long)days * (long)MS_PER_DAY;
        computeProperties();
    }

    public TimeSpan(int[] data) {
        this(data[0], data[1], data[2], data[3], data[4]);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Days,
                Hours,
                Milliseconds,
                Minutes,
                Seconds,
                TotalDays,
                TotalHours,
                TotalMilliseconds,
                TotalMinutes,
                TotalSeconds);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TimeSpan other = (TimeSpan) obj;
        if (Days != other.Days) {
            return false;
        }
        if (Hours != other.Hours) {
            return false;
        }
        if (Milliseconds != other.Milliseconds) {
            return false;
        }
        if (Minutes != other.Minutes) {
            return false;
        }
        if (Seconds != other.Seconds) {
            return false;
        }
        if (Double.doubleToLongBits(TotalDays) != Double.doubleToLongBits(other.TotalDays)) {
            return false;
        }
        if (Double.doubleToLongBits(TotalHours) != Double.doubleToLongBits(other.TotalHours)) {
            return false;
        }
        if (TotalMilliseconds != other.TotalMilliseconds) {
            return false;
        }
        if (Double.doubleToLongBits(TotalMinutes) != Double.doubleToLongBits(other.TotalMinutes)) {
            return false;
        }
        if (Double.doubleToLongBits(TotalSeconds) != Double.doubleToLongBits(other.TotalSeconds)) {
            return false;
        }
        return true;
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

        if (Pattern.matches("[0-9]+", cleaned)) {
            int days = Integer.parseInt(cleaned);
            return new TimeSpan(days * multiplier, 0, 0, 0);
        }

        String regex = "[0-9]+.[0-9]{2}:[0-9]{2}(:[0-9]{2}(.[0-9]{2})?)?";
        if (Pattern.matches(regex, cleaned)) {
            String[] items = cleaned.split("[:.]");
            int[] data = new int[5];

            for (int x = 0; x < items.length; x++) {
                data[x] = Integer.parseInt(items[x]) * multiplier;
            }

            return new TimeSpan(data);
        }

        regex = "[0-9]{2}:[0-9]{2}(:[0-9]{2}(.[0-9]{2})?)?";
        if (Pattern.matches(regex, cleaned)) {
            String[] items = cleaned.split("[:.]");
            int[] data = new int[5];

            for (int x = 0; x < items.length; x++) {
                data[x + 1] = Integer.parseInt(items[x]) * multiplier;
            }

            return new TimeSpan(data);
        }
        // If we get to here, it is invalid
        throw new IllegalArgumentException("Invalid TimeSpan");
    }

    @Override
    public String toString() {
        // Ok to use String.format directly since GWT does not use TimeSpan from ui override package
        return String.format("%s%d.%02d:%02d:%02d.%03d",
                (TotalMilliseconds < 0) ? "-" : "",
                Days, Hours, Minutes, Seconds, Milliseconds);
    }

    public static TimeSpan tryParse(String string) {
        try {
            return TimeSpan.parse(string);
        } catch (IllegalArgumentException e) {
            // eat it, return null
            return null;
        }
    }

    @Override
    public int compareTo(TimeSpan o) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

}
