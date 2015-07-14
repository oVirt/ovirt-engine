package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllVmsForUserQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllVmsForUserQuery(P parameters) {
        super(parameters);
    }

    public GetAllVmsForUserQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vmsList = getDbFacade()
                .getVmDao().getAllForUser(getUserID());
        for (VM vm : vmsList) {
            VmHandler.updateVmGuestAgentVersion(vm);
        }
        getQueryReturnValue().setReturnValue(vmsList);
    }
}
