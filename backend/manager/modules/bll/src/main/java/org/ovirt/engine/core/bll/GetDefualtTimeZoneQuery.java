package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.vdsbroker.vdsbroker.SysprepHandler;

public class GetDefualtTimeZoneQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetDefualtTimeZoneQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String timezone = Config.<String> GetValue(ConfigValues.DefaultTimeZone);
        getQueryReturnValue().setReturnValue(new KeyValuePairCompat<String, String>(SysprepHandler.getTimezoneKey(timezone),
                timezone));
    }
}
