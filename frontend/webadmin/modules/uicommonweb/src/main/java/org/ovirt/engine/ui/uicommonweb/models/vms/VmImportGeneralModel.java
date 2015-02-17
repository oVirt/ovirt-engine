package org.ovirt.engine.ui.uicommonweb.models.vms;

public class VmImportGeneralModel extends VmGeneralModel {

    @SuppressWarnings("unchecked")
    public void setEntity(ImportVmData value) {
        super.setEntity(value == null ? null : value.getVm());
    }
}
