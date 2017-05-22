package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.ActionGroupDao;

public class GetRoleActionGroupsByRoleIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private ActionGroupDao actionGroupDao;

    public GetRoleActionGroupsByRoleIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(actionGroupDao.getAllForRole(getParameters().getId()));
    }
}
