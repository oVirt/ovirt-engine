package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VmDao;

public class GetVmByVmIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    protected VmHandler vmHandler;

    @Inject
    private VmDao vmDao;

    public GetVmByVmIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = getVm();
        if (vm != null) {
            // Note that retrieving the VM is already filtered, and if there are no permissions for it, null will be
            // returned.
            // Thus, no additional concern should be given for permissions issues
            updateVMDetails(vm);
            getQueryReturnValue().setReturnValue(vm);
        }
    }

    protected VM getVm() {
        return vmDao.get(getParameters().getId(), getUserID(), getParameters().isFiltered());
    }

    protected void updateVMDetails(VM vm) {
        vmHandler.updateDisksFromDb(vm);
        vmHandler.updateVmGuestAgentVersion(vm);
        vmHandler.updateNetworkInterfacesFromDb(vm);
        vmHandler.updateVmInitFromDB(vm.getStaticData(), true);
        vmHandler.updateNumaNodesFromDb(vm);
        vmHandler.updateVmStatistics(vm);
    }
}
