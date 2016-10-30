package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.FenceAgentDao;

public class GetFenceAgentByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private FenceAgentDao fenceAgentDao;

    public GetFenceAgentByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(fenceAgentDao.get(getParameters().getId()));
    }
}
