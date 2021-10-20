package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.CompositeSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.SyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.VirtioMultiQueueType;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

/**
 * HW only part of the core. This is part of ALL the builders (including the instance types)
 *
 * DO-NOT-EDIT: this builder has been used for instance types which are now deprecated. New fields
 * common for vms, templates and pools should go to the {@link CoreUnitToVmBaseBuilder}
 */
public class HwOnlyCoreUnitToVmBaseBuilder<T extends VmBase> extends CompositeSyncBuilder<UnitVmModel, T> {

    public HwOnlyCoreUnitToVmBaseBuilder(SyncBuilder<UnitVmModel, T>... builders) {
        super(builders);
    }

    @Override
    protected void postBuild(UnitVmModel model, T vm) {
        // System
        vm.setMemSizeMb(model.getMemSize().getEntity());
        vm.setMaxMemorySizeMb(model.getMaxMemorySize().getEntity());
        vm.setMinAllocatedMem(model.getMinAllocatedMemory().getEntity());
        vm.setNumOfSockets(model.getNumOfSockets().getSelectedItem());
        vm.setCpuPerSocket(model.getCoresPerSocket().getSelectedItem());
        vm.setThreadsPerCpu(model.getThreadsPerCore().getSelectedItem());
        vm.setCustomEmulatedMachine(model.getEmulatedMachine().getSelectedItem());
        vm.setCustomCpuName(model.getCustomCpu().getSelectedItem());
        vm.setBiosType(model.getBiosType().getSelectedItem());
        // Console
        vm.setDefaultDisplayType(model.getDisplayType().getSelectedItem());
        vm.setNumOfMonitors(model.getNumOfMonitors().getSelectedItem());
        vm.setSmartcardEnabled(model.getIsSmartcardEnabled().getEntity());
        vm.setSsoMethod(model.extractSelectedSsoMethod());
        // Resource Allocation
        if (model.getIoThreadsEnabled().getEntity()) {
            vm.setNumOfIoThreads(Integer.parseInt(model.getNumOfIoThreads().getEntity()));
        } else {
            vm.setNumOfIoThreads(0);
        }
        if (model.getNumOfVirtioScsiMultiQueues().getEntity() != null
                && model.getVirtioScsiMultiQueueTypeSelection().getSelectedItem() == VirtioMultiQueueType.CUSTOM) {
            vm.setVirtioScsiMultiQueues(model.getNumOfVirtioScsiMultiQueues().getEntity());
        } else if (model.getVirtioScsiMultiQueueTypeSelection().getSelectedItem() == VirtioMultiQueueType.DISABLED) {
            vm.setVirtioScsiMultiQueues(0);
        } else if (model.getVirtioScsiMultiQueueTypeSelection().getSelectedItem() == VirtioMultiQueueType.AUTOMATIC) {
            vm.setVirtioScsiMultiQueues(-1);
        }
        vm.setBalloonEnabled(model.getMemoryBalloonEnabled().getEntity());
        // Boot
        vm.setDefaultBootSequence(model.getBootSequence());
    }
}
