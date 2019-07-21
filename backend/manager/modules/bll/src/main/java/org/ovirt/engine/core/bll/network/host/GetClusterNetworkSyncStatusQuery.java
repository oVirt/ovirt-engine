package org.ovirt.engine.core.bll.network.host;

import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;

public class GetClusterNetworkSyncStatusQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkImplementationDetailsUtils util;

    public GetClusterNetworkSyncStatusQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Set<Guid> vdsIds = backend.runInternalQuery(QueryType.GetOutOfSyncHostsForCluster,
            new IdQueryParameters(getParameters().getId())).getReturnValue();
        getQueryReturnValue().setReturnValue(!vdsIds.isEmpty());
    }
}
