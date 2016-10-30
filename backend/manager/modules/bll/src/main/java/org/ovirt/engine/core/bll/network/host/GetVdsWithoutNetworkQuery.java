package org.ovirt.engine.core.bll.network.host;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsDao;

/**
 * A query to retrieve all Hosts that the given Network is not attached to, while the Network is assigned to their
 * Cluster
 */
public class GetVdsWithoutNetworkQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VdsDao vdsDao;

    public GetVdsWithoutNetworkQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vdsDao.getAllWithoutNetwork(getParameters().getId()));
    }
}
