package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetRequiredNetworksByDataCenterIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private NetworkDao networkDao;

    public GetRequiredNetworksByDataCenterIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(networkDao.getRequiredNetworksByDataCenterId(getParameters().getId()));
    }
}
