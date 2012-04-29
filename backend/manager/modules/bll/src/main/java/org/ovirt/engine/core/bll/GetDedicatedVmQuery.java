package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetDedicatedVmParameters;

public class GetDedicatedVmQuery<P extends GetDedicatedVmParameters> extends QueriesCommandBase<P> {
    public GetDedicatedVmQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                getDbFacade().getVmDAO().getAllForDedicatedPowerClientByVds(getParameters().getId()));
    }
}
