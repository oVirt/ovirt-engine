package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetVmByVmIdQuery<P extends GetVmByVmIdParameters> extends QueriesCommandBase<P> {
    public GetVmByVmIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = DbFacade.getInstance().getVmDAO().getById(getParameters().getId());
        if (vm != null) {
            VmHandler.updateDisksFromDb(vm);
            VmHandler.UpdateVmGuestAgentVersion(vm);
            getQueryReturnValue().setReturnValue(vm);
        }
    }
}
