package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetDefaultConfigurationVersionQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetDefaultConfigurationVersionQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(Config.DefaultConfigurationVersion);
    }
}
