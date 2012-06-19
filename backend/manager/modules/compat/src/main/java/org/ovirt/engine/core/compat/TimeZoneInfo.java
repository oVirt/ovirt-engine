package org.ovirt.engine.core.compat;

@Deprecated
public class TimeZoneInfo {

    public final static TimeZoneInfo Local = new TimeZoneInfo();

    private String DefaultTimeZone = "Eastern Standard Time";

    public String getId() {
        // JTODO: this should be changed to return the timezone of the local
        // server, in SysprepHandler.timeZoneIndex format,
        // or simply make it a config option.
        return this.DefaultTimeZone;
    }

}
