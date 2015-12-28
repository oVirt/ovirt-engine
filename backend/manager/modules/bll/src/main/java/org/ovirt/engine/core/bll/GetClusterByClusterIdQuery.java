package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetClusterByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetClusterByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getClusterDao().get(getParameters().getId()));
    }
}
