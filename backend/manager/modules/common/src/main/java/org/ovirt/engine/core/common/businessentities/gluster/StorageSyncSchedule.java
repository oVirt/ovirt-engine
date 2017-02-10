package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.ovirt.engine.core.compat.StringFormat;


public class StorageSyncSchedule implements Serializable{

    private static final long serialVersionUID = -3688076333085745256L;

    public enum Frequency {
        DAILY,
        WEEKLY,
        NONE
    };

    public enum Day {
        MON,
        TUE,
        WED,
        THU,
        FRI,
        SAT,
        SUN
    }

    private Frequency frequency;
    private Day[] days;
    @Min(value = 0, message = "SCHEDULE_HOUR_OUT_OF_RANGE")
    @Max(value = 23, message = "SCHEDULE_HOUR_OUT_OF_RANGE")
    private Integer hour;
    @Min(value = 0, message = "SCHEDULE_MINS_OUT_OF_RANGE")
    @Max(value = 59, message = "SCHEDULE_MINS_OUT_OF_RANGE")
    private Integer mins;

    public StorageSyncSchedule() {

    }

    public StorageSyncSchedule(String cronExpression) {
        if (cronExpression == null || cronExpression.isEmpty() || cronExpression.split(" ").length != 7) {
            this.setFrequency(StorageSyncSchedule.Frequency.NONE);
            return;
        }
        String[] cronParts = cronExpression.split(" ");
        if (cronParts[5].equals("?")) {
            this.setFrequency(StorageSyncSchedule.Frequency.DAILY);
        } else {
            this.setFrequency(StorageSyncSchedule.Frequency.WEEKLY);
             List<Day> dayList = new ArrayList<>();
             for(String day: cronParts[5].split(",")) {
                 dayList.add(StorageSyncSchedule.Day.valueOf(day));
            }
            this.setDays(dayList.toArray(new Day[dayList.size()]));
        }
        this.setMins(Integer.parseInt(cronParts[1]));
        this.setHour(Integer.parseInt(cronParts[2]));
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public Day[] getDays() {
        return days;
    }

    public void setDays(Day[] days) {
        this.days = days;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMins() {
        return mins;
    }

    public void setMins(int mins) {
        this.mins = mins;
    }

    private String toDaysCron() {
        StringBuffer sb = new StringBuffer();
        Arrays.sort(days);
        for (Day day : days) {
            sb.append(day.name()).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public String toCronExpression() {
        String cronExpression = "";
        switch (frequency) {
        case DAILY:
            if (mins != null && hour != null) {
                cronExpression = StringFormat.format("0 %s %s * * ? *", mins, hour);
            }
            break;
        case WEEKLY:
            if (mins != null && hour != null && days != null) {
                cronExpression = StringFormat.format("0 %s %s ? * %s *", mins, hour, toDaysCron());
            }
            break;
        case NONE:
            cronExpression = "";
        }
        return cronExpression;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(frequency.name());

        switch (frequency) {
        case WEEKLY:
            sb.append(" on ").append(toDaysCron());
            sb.append(" at ").append(StringFormat.format("%02d", hour)).append(":").append(StringFormat.format("%02d", mins));
            break;
        case DAILY:
            sb.append(" at ").append(StringFormat.format("%02d", hour)).append(":").append(StringFormat.format("%02d", mins));
            break;
        case NONE:
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(frequency,
                days,
                hour,
                mins);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StorageSyncSchedule)) {
            return false;
        }
        StorageSyncSchedule other = (StorageSyncSchedule) obj;
        return Objects.equals(frequency, other.frequency)
                && Arrays.equals(days, other.days )
                && Objects.equals(hour, other.hour)
                && Objects.equals(mins, other.mins);
    }

}
