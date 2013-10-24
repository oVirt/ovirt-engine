package org.ovirt.engine.core.config.entity.helper;

import org.ovirt.engine.core.common.TimeZoneType;

public class GeneralTimeZoneValueHelper extends BaseTimeZoneValueHelper {
    @Override
    public TimeZoneType getTimeZoneType() {
        return TimeZoneType.GENERAL_TIMEZONE;
    }

    @Override
    public String getHelpNoteType() {
        return "time zone";
    }

    @Override
    public String getExample() {
        return "Please enter a value like 'Etc/GMT' or 'Europe/London'";
    }
}
