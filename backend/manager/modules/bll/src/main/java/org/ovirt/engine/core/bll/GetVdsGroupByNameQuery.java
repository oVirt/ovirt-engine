package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetVdsGroupByNameQuery<P extends GetVdsGroupByNameParameters> extends QueriesCommandBase<P> {
    public GetVdsGroupByNameQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getVdsGroupDAO().getByName(getParameters().getName()));
    }
}
