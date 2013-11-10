package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetVmByVmNameForDataCenterParameters;

public class GetVmByVmNameForDataCenterQuery<P extends GetVmByVmNameForDataCenterParameters> extends QueriesCommandBase<GetVmByVmNameForDataCenterParameters> {
    public GetVmByVmNameForDataCenterQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = getDbFacade().getVmDao().getByNameForDataCenter(
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
        VmHandler.updateDisksFromDb(vm);
        VmHandler.updateVmGuestAgentVersion(vm);
        VmHandler.updateNetworkInterfacesFromDb(vm);
        VmHandler.updateVmInitFromDB(vm.getStaticData(), true);
    }
}
