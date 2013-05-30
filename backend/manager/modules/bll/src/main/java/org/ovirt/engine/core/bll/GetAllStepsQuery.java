package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllStepsQuery <P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {

    public GetAllStepsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(getDbFacade().getStepDao().getAll());
    }
}
