package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.TimeZoneQueryParams;

public class GetDefaultTimeZoneQuery<P extends TimeZoneQueryParams> extends QueriesCommandBase<P> {
    public GetDefaultTimeZoneQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        ConfigValues defaultTimeZoneConfigKey;
        switch (getParameters().getTimeZoneType()) {

        default:
        case GENERAL_TIMEZONE:
            defaultTimeZoneConfigKey = ConfigValues.DefaultGeneralTimeZone;
            break;
        case WINDOWS_TIMEZONE:
            defaultTimeZoneConfigKey = ConfigValues.DefaultWindowsTimeZone;
            break;
        }

        String timeZone = Config.<String> getValue(defaultTimeZoneConfigKey);
        getQueryReturnValue().setReturnValue(timeZone);
    }
}
