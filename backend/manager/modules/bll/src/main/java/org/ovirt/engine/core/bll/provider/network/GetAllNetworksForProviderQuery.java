package org.ovirt.engine.core.bll.provider.network;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.NetworkViewDao;

public class GetAllNetworksForProviderQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private NetworkViewDao networkViewDao;

    public GetAllNetworksForProviderQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(networkViewDao.getAllForProvider(getParameters().getId()));
    }
}
