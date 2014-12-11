package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetAllPoolVmsQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetAllPoolVmsQuery(P parameters) {
        super(parameters);
    }

    public GetAllPoolVmsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vmsList = getDbFacade()
                .getVmDao().getAllForVmPool(getParameters().getId());
        for (VM vm : vmsList) {
            VmHandler.updateVmGuestAgentVersion(vm);
        }
        getQueryReturnValue().setReturnValue(vmsList);
    }
}
