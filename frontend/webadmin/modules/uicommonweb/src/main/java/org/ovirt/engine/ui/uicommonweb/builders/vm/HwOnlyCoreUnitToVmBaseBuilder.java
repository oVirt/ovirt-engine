package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

/**
 * HW only part of the core. This is part of ALL the builders (including the instance types)
 */
public class HwOnlyCoreUnitToVmBaseBuilder<T extends VmBase> extends BaseSyncBuilder<UnitVmModel, T> {

    @Override
    protected void build(UnitVmModel model, T vm) {
        vm.setMemSizeMb(model.getMemSize().getEntity());
        if (model.getIoThreadsEnabled().getEntity()) {
            vm.setNumOfIoThreads(model.getNumOfIoThreads().getEntity());
        } else {
            vm.setNumOfIoThreads(0);
        }
        vm.setNumOfSockets(model.getNumOfSockets().getSelectedItem());
        vm.setCpuPerSocket(model.getCoresPerSocket().getSelectedItem());
        vm.setThreadsPerCpu(model.getThreadsPerCore().getSelectedItem());
        vm.setDefaultDisplayType(model.getDisplayType().getSelectedItem());
        vm.setNumOfMonitors(model.getNumOfMonitors().getSelectedItem());
        vm.setSmartcardEnabled(model.getIsSmartcardEnabled().getEntity());
        vm.setSsoMethod(model.extractSelectedSsoMethod());
        vm.setMinAllocatedMem(model.getMinAllocatedMemory().getEntity());
        vm.setDefaultBootSequence(model.getBootSequence());
        vm.setSingleQxlPci(model.getIsSingleQxlEnabled().getEntity());
        vm.setCustomEmulatedMachine(model.getEmulatedMachine().getSelectedItem());
        vm.setCustomCpuName(model.getCustomCpu().getSelectedItem());
    }
}
