package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetDedicatedVmQuery<P extends GetDedicatedVmParameters> extends QueriesCommandBase<P> {
    public GetDedicatedVmQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVmDAO().getAllForDedicatedPowerClientByVds(getParameters().getId()));
    }
}
