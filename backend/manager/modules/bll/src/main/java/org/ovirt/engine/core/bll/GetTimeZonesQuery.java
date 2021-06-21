package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.queries.TimeZoneQueryParameters;

public class GetTimeZonesQuery<P extends TimeZoneQueryParameters> extends QueriesCommandBase<P> {
    public GetTimeZonesQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        switch (getParameters().getTimeZoneVerb()) {
            case GetGeneralTimezones:
                setReturnValue(TimeZoneType.GENERAL_TIMEZONE.getTimeZoneList());
                break;
            case GetWindowsTimezones:
                setReturnValue(TimeZoneType.WINDOWS_TIMEZONE.getTimeZoneList());
                break;

        }
    }
}
