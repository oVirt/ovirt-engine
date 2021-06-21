package org.ovirt.engine.core.common.queries;

public class TimeZoneQueryParameters extends QueryParametersBase {

    private TimeZoneQueryParameters.TimeZoneVerb timeZoneVerb;

    public TimeZoneQueryParameters() {
    }

    public TimeZoneQueryParameters(TimeZoneQueryParameters.TimeZoneVerb verb) {
        this.timeZoneVerb = verb;
    }

    public TimeZoneQueryParameters.TimeZoneVerb getTimeZoneVerb() {
        return timeZoneVerb;
    }

    public enum TimeZoneVerb {
        GetGeneralTimezones,
        GetWindowsTimezones
    }
}
