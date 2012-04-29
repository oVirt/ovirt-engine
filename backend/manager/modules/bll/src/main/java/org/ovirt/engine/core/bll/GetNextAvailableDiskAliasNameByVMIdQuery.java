package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.GetAllDisksByVmIdParameters;

public class GetNextAvailableDiskAliasNameByVMIdQuery<P extends GetAllDisksByVmIdParameters> extends QueriesCommandBase<P> {

    public GetNextAvailableDiskAliasNameByVMIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String suggestedDiskName = null;
        if (getParameters().getVmId() == null) {
            getQueryReturnValue().setReturnValue(suggestedDiskName);
        } else {
            VM vm = getDbFacade().getVmDAO().get(getParameters().getVmId());
            if (vm != null) {
                VmHandler.updateDisksFromDb(vm);
                suggestedDiskName =
                        ImagesHandler.getDefaultDiskAlias(vm.getvm_name(), VmHandler.getCorrectDriveForDisk(vm));
            }
            getQueryReturnValue().setReturnValue(suggestedDiskName);
        }
    }
}
