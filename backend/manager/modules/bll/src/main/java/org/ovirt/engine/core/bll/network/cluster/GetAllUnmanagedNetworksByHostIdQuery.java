package org.ovirt.engine.core.bll.network.cluster;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.UnmanagedNetwork;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetAllUnmanagedNetworksByHostIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private UnmanagedNetworksHelper unmanagedNetworksHelper;

    public GetAllUnmanagedNetworksByHostIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Guid hostId = getParameters().getId();
        List<UnmanagedNetwork> unmanagedNetworks = unmanagedNetworksHelper.getUnmanagedNetworks(hostId);
        getQueryReturnValue().setReturnValue(unmanagedNetworks);
    }

}
