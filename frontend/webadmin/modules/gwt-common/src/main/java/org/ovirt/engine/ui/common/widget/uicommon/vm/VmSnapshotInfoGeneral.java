package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.tooltip.WidgetTooltip;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class VmSnapshotInfoGeneral extends GeneralFormPanel {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    private final FormBuilder formBuilder;

    private StringValueLabel definedMemory = new StringValueLabel();
    private StringValueLabel minAllocatedMemory = new StringValueLabel();
    private StringValueLabel cpuInfo = new StringValueLabel();

    public VmSnapshotInfoGeneral() {

        formBuilder = new FormBuilder(this, 1, 3);

        formBuilder.addFormItem(new FormItem(constants.definedMemoryVm(), definedMemory, 0, 0), 3, 9);
        formBuilder.addFormItem(new FormItem(constants.physMemGauranteedVm(), minAllocatedMemory, 1, 0), 3, 9);

        WidgetTooltip cpuInfoWithTooltip = new WidgetTooltip(cpuInfo);
        cpuInfoWithTooltip.setHtml(SafeHtmlUtils.fromString(constants.numOfCpuCoresTooltip()));
        formBuilder.addFormItem(new FormItem(constants.numOfCpuCoresVm(), cpuInfoWithTooltip, 2, 0), 3, 9);

        formBuilder.setRelativeColumnWidth(0, 12);
        setWidth("100%"); //$NON-NLS-1$
    }

    public void update(VM vm) {
        setVisible(vm != null);

        if (vm == null) {
            return;
        }

        definedMemory.setValue(messages.megabytes(String.valueOf(vm.getVmMemSizeMb())));
        minAllocatedMemory.setValue(messages.megabytes(String.valueOf(vm.getMinAllocatedMem())));
        cpuInfo.setValue(messages.cpuInfoLabel(vm.getNumOfCpus(),
                vm.getNumOfSockets(), vm.getCpuPerSocket(), vm.getThreadsPerCpu()));
    }
}
