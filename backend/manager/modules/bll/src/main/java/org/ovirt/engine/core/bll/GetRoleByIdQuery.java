package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.RoleDao;

public class GetRoleByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private RoleDao roleDao;

    public GetRoleByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(roleDao.get(getParameters().getId()));
    }
}
