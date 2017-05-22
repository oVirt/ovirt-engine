package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;

public class GetNetworkFilterByIdQuery extends QueriesCommandBase<IdQueryParameters> {

    @Inject
    private NetworkFilterDao networkFilterDao;

    public GetNetworkFilterByIdQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        final NetworkFilter networkFilter = networkFilterDao.getNetworkFilterById(getParameters().getId());
        getQueryReturnValue().setReturnValue(networkFilter);
    }
}
