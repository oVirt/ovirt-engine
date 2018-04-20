package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

public class VmImportInterfaceListModel extends SearchableListModel {
    public VmImportInterfaceListModel() {
        setIsTimerDisabled(true);
    }

    @Override
    protected void onEntityChanged() {
        if (getEntity() != null) {
            VM vm = (VM) getEntity();
            setItems(vm.getInterfaces());
        } else {
            setItems(null);
        }
    }

    @Override
    public void setEntity(Object value) {
        super.setEntity(value != null ? ((ImportVmData) value).getVm() : null);
    }

    @Override
    protected String getListName() {
        return "VmImportInterfaceListModel"; //$NON-NLS-1$
    }
}
