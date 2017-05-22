package org.ovirt.engine.core.bll.network.dc;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetAllNetworksQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private NetworkDao networkDao;

    public GetAllNetworksQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        if (getParameters().getId() == null
                || getParameters().getId().equals(Guid.Empty)) {
            getQueryReturnValue().setReturnValue(networkDao.getAll(getUserID(), getParameters().isFiltered()));
        } else {
            getQueryReturnValue().setReturnValue(
                    networkDao.getAllForDataCenter(getParameters().getId(), getUserID(), getParameters().isFiltered()));
        }
    }
}
