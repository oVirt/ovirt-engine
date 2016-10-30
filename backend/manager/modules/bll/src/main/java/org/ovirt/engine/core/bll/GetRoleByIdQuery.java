package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.RoleDao;

public class GetRoleByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private RoleDao roleDao;

    public GetRoleByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(roleDao.get(getParameters().getId()));
    }
}
