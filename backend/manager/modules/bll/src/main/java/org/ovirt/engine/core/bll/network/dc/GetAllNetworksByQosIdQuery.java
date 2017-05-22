package org.ovirt.engine.core.bll.network.dc;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetAllNetworksByQosIdQuery extends QueriesCommandBase<IdQueryParameters> {

    @Inject
    private NetworkDao networkDao;

    public GetAllNetworksByQosIdQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(networkDao.getAllForQos(getParameters().getId()));
    }

}
