package org.ovirt.engine.core.bll.network.host;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVfToPfMapByHostIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkDeviceHelper networkDeviceHelper;

    public GetVfToPfMapByHostIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getNetworkDeviceHelper().getVfMap(getParameters().getId()));
    }

    NetworkDeviceHelper getNetworkDeviceHelper() {
        return networkDeviceHelper;
    }
}
