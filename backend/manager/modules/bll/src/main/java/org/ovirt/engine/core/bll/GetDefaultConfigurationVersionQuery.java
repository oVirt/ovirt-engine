package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetDefaultConfigurationVersionQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetDefaultConfigurationVersionQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(ConfigCommon.defaultConfigurationVersion);
    }
}
