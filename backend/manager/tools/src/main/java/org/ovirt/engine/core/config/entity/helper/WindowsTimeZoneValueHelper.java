package org.ovirt.engine.core.config.entity.helper;

import org.ovirt.engine.core.common.TimeZoneType;

public class WindowsTimeZoneValueHelper extends BaseTimeZoneValueHelper {
    @Override
    public TimeZoneType getTimeZoneType() {
        return TimeZoneType.WINDOWS_TIMEZONE;
    }

    @Override
    public String getHelpNoteType() {
        return "time zone for Windows OS";
    }

    @Override
    public String getExample() {
        return "Please enter a value like 'GMT Standard Time' or 'Israel Standard Time'";
    }
}
