package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetRoleByNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    public GetRoleByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getRoleDao()
                .getByName(getParameters().getName()));
    }
}
