package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetAdGroupByIdQuery<P extends GetAdGroupByIdParameters> extends QueriesCommandBase<P> {
    public GetAdGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance().getAdGroupDAO().get(getParameters().getId()));
    }
}
