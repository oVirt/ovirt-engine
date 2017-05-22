package org.ovirt.engine.core.bll.network.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.UnmanagedNetwork;
import org.ovirt.engine.core.common.queries.UnmanagedNetworkParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetUnmanagedNetworkByHostIdAndNameQuery extends QueriesCommandBase<UnmanagedNetworkParameters> {

    @Inject
    private UnmanagedNetworksHelper unmanagedNetworksHelper;

    public GetUnmanagedNetworkByHostIdAndNameQuery(UnmanagedNetworkParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {

        Guid hostId = getParameters().getHostId();
        String networkName = getParameters().getNetworkName();
        UnmanagedNetwork unmanagedNetwork = unmanagedNetworksHelper.getUnmanagedNetwork(hostId, networkName);

        getQueryReturnValue().setReturnValue(unmanagedNetwork);
    }
}
