package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetVdsGroupByIdParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetVdsGroupByIdQuery<P extends GetVdsGroupByIdParameters> extends QueriesCommandBase<P> {
    public GetVdsGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVdsGroupDAO().get(getParameters().getVdsId()));
    }
}
