package org.ovirt.engine.ui.uicommonweb.models.vms;

public class VmImportAppListModel extends VmAppListModel {

    @SuppressWarnings("unchecked")
    @Override
    public void setEntity(Object value) {
        super.setEntity(value == null ? null : ((ImportVmData) value).getVm());
    }
}
