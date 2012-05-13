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
            VM vm = getDbFacade().getVmDAO().get(getParameters().getVmId(), getUserID(), getParameters().isFiltered());
            if (vm != null) {
                updateDisksFromDb(vm);
                suggestedDiskName =
                        ImagesHandler.getDefaultDiskAlias(vm.getvm_name(), Integer.toString(vm.getDiskMapCount() + 1));
            }
            getQueryReturnValue().setReturnValue(suggestedDiskName);
        }
    }

    protected void updateDisksFromDb(VM vm) {
        VmHandler.updateDisksFromDb(vm);
    }
}
