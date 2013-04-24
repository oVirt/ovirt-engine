package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.TimeZoneType;


public class TimeZoneQueryParams extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1L;
    private TimeZoneType timeZoneType;

    public TimeZoneQueryParams() {
    }

    public TimeZoneType getTimeZoneType() {
        return timeZoneType;
    }

    public void setTimeZoneType(TimeZoneType timeZoneType) {
        this.timeZoneType = timeZoneType;
    }

}
