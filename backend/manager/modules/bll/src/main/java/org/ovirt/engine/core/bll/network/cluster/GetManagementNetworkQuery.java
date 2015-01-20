package org.ovirt.engine.core.bll.network.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetManagementNetworkQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    public GetManagementNetworkQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final Guid clusterId = getParameters().getId();

        getQueryReturnValue().setReturnValue(managementNetworkUtil.getManagementNetwork(clusterId));
    }
}
