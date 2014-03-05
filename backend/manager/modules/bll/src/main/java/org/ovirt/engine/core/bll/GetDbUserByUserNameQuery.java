package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetDbUserByUserNameQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {

    public GetDbUserByUserNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getDbUserDao()
                .getByUsername(getParameters().getName()));
    }
}
