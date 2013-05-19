package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetNextAvailableDiskAliasNameByVMIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetNextAvailableDiskAliasNameByVMIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String suggestedDiskName = null;
        if (getParameters().getId() == null) {
            getQueryReturnValue().setReturnValue(suggestedDiskName);
        } else {
            VM vm = getDbFacade().getVmDao().get(getParameters().getId(), getUserID(), getParameters().isFiltered());
            if (vm != null) {
                updateDisksFromDb(vm);
                suggestedDiskName =
                        ImagesHandler.getDefaultDiskAlias(vm.getName(), Integer.toString(vm.getDiskMapCount() + 1));
            }
            getQueryReturnValue().setReturnValue(suggestedDiskName);
        }
    }

    protected void updateDisksFromDb(VM vm) {
        VmHandler.updateDisksFromDb(vm);
    }
}
