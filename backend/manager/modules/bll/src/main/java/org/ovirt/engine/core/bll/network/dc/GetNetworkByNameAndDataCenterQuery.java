package org.ovirt.engine.core.bll.network.dc;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdAndNameQueryParameters;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetNetworkByNameAndDataCenterQuery<P extends IdAndNameQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkDao networkDao;

    public GetNetworkByNameAndDataCenterQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                networkDao.getByNameAndDataCenter(getParameters().getName(), getParameters().getId()));
    }
}
