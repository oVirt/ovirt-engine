package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmByVmNameForDataCenterParameters;
import org.ovirt.engine.core.dao.VmDao;

public class GetVmByVmNameForDataCenterQuery<P extends GetVmByVmNameForDataCenterParameters> extends QueriesCommandBase<GetVmByVmNameForDataCenterParameters> {

    @Inject
    private VmHandler vmHandler;

    @Inject
    private VmDao vmDao;

    public GetVmByVmNameForDataCenterQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = vmDao.getByNameForDataCenter(
                getParameters().getDataCenterId(),
                getParameters().getName(),
                getUserID(),
                getParameters().isFiltered());
        if (vm != null) {
            // Note that retrieving the VM is already filtered, and if there are no permissions for it, null will be
            // returned.
            // Thus, no additional concern should be given for permissions issues
            updateVMDetails(vm);
            getQueryReturnValue().setReturnValue(vm);
        }
    }

    protected void updateVMDetails(VM vm) {
        vmHandler.updateDisksFromDb(vm);
        vmHandler.updateVmGuestAgentVersion(vm);
        vmHandler.updateNetworkInterfacesFromDb(vm);
        vmHandler.updateVmInitFromDB(vm.getStaticData(), true);
    }
}
