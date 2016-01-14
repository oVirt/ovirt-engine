package org.ovirt.engine.core.bll.aaa;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetDbUserByUserIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetDbUserByUserIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getDbUserDao()
                        .get(getParameters().getId(), getParameters().isFiltered()));
    }
}
