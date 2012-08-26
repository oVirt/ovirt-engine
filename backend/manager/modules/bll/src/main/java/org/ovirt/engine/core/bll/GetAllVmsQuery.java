package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllVmsQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllVmsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<VM> vmsList = getDbFacade()
                .getVmDAO().getAll(getUserID(), getParameters().isFiltered());
        for (VM vm : vmsList) {
            VmHandler.UpdateVmGuestAgentVersion(vm);
        }
        getQueryReturnValue().setReturnValue(vmsList);
    }
}
