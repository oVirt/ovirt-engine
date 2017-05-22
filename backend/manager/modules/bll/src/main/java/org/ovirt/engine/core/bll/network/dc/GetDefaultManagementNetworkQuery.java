package org.ovirt.engine.core.bll.network.dc;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.network.cluster.DefaultManagementNetworkFinder;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetDefaultManagementNetworkQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private DefaultManagementNetworkFinder defaultManagementNetworkFinder;

    public GetDefaultManagementNetworkQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        final Guid dataCenterId = getParameters().getId();

        getQueryReturnValue().setReturnValue(defaultManagementNetworkFinder.findDefaultManagementNetwork(dataCenterId));
    }
}
