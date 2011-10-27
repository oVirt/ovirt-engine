package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetVdsGroupByVdsGroupIdQuery<P extends GetVdsGroupByVdsGroupIdParameters> extends QueriesCommandBase<P> {
    public GetVdsGroupByVdsGroupIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVdsGroupDAO().get(getParameters().getVdsGroupId()));
    }
}
