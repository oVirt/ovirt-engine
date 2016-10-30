package org.ovirt.engine.core.bll;


import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;


public class GetAllNetworkQosByStoragePoolIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private NetworkQoSDao networkQoSDao;

    public GetAllNetworkQosByStoragePoolIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(networkQoSDao.getAllForStoragePoolId(getParameters().getId()));
    }
}
