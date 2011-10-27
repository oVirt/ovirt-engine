package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetVdsGroupByIdQuery<P extends GetVdsGroupByIdParameters> extends QueriesCommandBase<P> {
    public GetVdsGroupByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        // QueryReturnValue.ReturnValue = ResourceManager.Instance.getVds(
        // ((GetVdsGroupByIdParameters)Parameters).getVdsId());
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVdsGroupDAO().get(getParameters().getVdsId()));
    }
}
