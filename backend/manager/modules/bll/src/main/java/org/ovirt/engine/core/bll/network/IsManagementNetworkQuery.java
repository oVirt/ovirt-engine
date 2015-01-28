package org.ovirt.engine.core.bll.network;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class IsManagementNetworkQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    public IsManagementNetworkQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final Guid networkId = getParameters().getId();

        getQueryReturnValue().setReturnValue(managementNetworkUtil.isManagementNetwork(networkId));
    }
}
