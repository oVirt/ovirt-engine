package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVmByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVmByVmIdQuery(P parameters) {
        super(parameters);
    }

    public GetVmByVmIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = getDbFacade().getVmDao().get(getParameters().getId(), getUserID(), getParameters().isFiltered());
        if (vm != null) {
            // Note that retrieving the VM is already filtered, and if there are no permissions for it, null will be
            // returned.
            // Thus, no additional concern should be given for permissions issues
            updateVMDetails(vm);
            getQueryReturnValue().setReturnValue(vm);
        }
    }

    protected void updateVMDetails(VM vm) {
        VmHandler.updateDisksFromDb(vm);
        VmHandler.updateVmGuestAgentVersion(vm);
        VmHandler.updateNetworkInterfacesFromDb(vm);
        VmHandler.updateVmInitFromDB(vm.getStaticData(), true);
        VmHandler.updateNumaNodesFromDb(vm);
    }
}
