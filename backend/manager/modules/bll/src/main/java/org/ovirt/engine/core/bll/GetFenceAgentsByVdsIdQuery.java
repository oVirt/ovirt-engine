package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.FenceAgentDao;

public class GetFenceAgentsByVdsIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private FenceAgentDao fenceAgentDao;

    public GetFenceAgentsByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(fenceAgentDao.getFenceAgentsForHost(getParameters().getId()));
    }

}
