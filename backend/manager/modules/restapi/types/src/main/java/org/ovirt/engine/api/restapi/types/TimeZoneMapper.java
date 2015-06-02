package org.ovirt.engine.api.restapi.types;

public class TimeZoneMapper {
    public static String mapUtcOffsetToDisplayString(int value) {
        int minutes = Math.abs(value);
        int hours = minutes / 60;
        minutes %= 60;
        // Format string into this form: +03:00 or -09:30 etc
        return String.format("%s%02d:%02d", value < 0 ? "-" : "+", hours, minutes);
    }
}
