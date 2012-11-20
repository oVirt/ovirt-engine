package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.TextBoxLabel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class VmSnapshotInfoGeneral extends GeneralFormPanel {

    private final CommonApplicationConstants constants;
    private final CommonApplicationMessages messages;

    private final FormBuilder generalForm;

    private TextBoxLabel definedMemory;
    private TextBoxLabel minAllocatedMemory;
    private TextBoxLabel cpuInfo;

    public VmSnapshotInfoGeneral(CommonApplicationConstants constants,
            CommonApplicationMessages messages) {
        this.constants = constants;
        this.messages = messages;
        generalForm = new FormBuilder(this, 1, 3);
    }

    private void init() {
        definedMemory = new TextBoxLabel();
        minAllocatedMemory = new TextBoxLabel();
        cpuInfo = new TextBoxLabel();

        generalForm.addFormItem(new FormItem(constants.definedMemoryVm(), definedMemory, 0, 0));
        generalForm.addFormItem(new FormItem(constants.physMemGauranteedVm(), minAllocatedMemory, 1, 0));
        generalForm.addFormItem(new FormItem(constants.numOfCpuCoresVm(), cpuInfo, 2, 0));
        generalForm.showForm(new EntityModel());

        generalForm.setColumnsWidth("100%"); //$NON-NLS-1$
        setWidth("100%"); //$NON-NLS-1$
    }

    public void update(VM vm) {
        if (vm == null) {
            clearForm();
            return;
        }

        if (!isInitialized()) {
            init();
        }

        definedMemory.setValue(vm.getVmMemSizeMb() + constants.mb());
        minAllocatedMemory.setValue(vm.getMinAllocatedMem() + constants.mb());
        cpuInfo.setValue(messages.cpuInfoLabel(vm.getNumOfCpus(), vm.getNumOfSockets(), vm.getCpuPerSocket()));
    }

    private void clearForm() {
        generalForm.clear();
    }

    private boolean isInitialized() {
        return !generalForm.isEmpty();
    }

}
