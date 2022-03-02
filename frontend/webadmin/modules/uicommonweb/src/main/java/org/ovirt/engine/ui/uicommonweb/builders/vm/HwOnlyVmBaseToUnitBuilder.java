package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.VirtioMultiQueueType;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

/**
 * HW part of the VM configuration. The fields are managed by
 * instance types.
 *
 */
public class HwOnlyVmBaseToUnitBuilder extends BaseSyncBuilder<VmBase, UnitVmModel> {
    @Override
    protected void build(VmBase vm, UnitVmModel model) {
        // System
        model.getMemSize().setEntity(vm.getMemSizeMb());
        model.getMaxMemorySize().setEntity(vm.getMaxMemorySizeMb());
        model.getMinAllocatedMemory().setEntity(vm.getMinAllocatedMem());
        model.getTotalCPUCores().setEntity(Integer.toString(vm.getNumOfCpus()));
        model.getNumOfSockets().setSelectedItem(vm.getNumOfSockets());
        model.getCoresPerSocket().setSelectedItem(vm.getCpuPerSocket());
        model.getThreadsPerCore().setSelectedItem(vm.getThreadsPerCpu());
        model.getEmulatedMachine().setSelectedItem(vm.getCustomEmulatedMachine());
        model.getCustomCpu().setSelectedItem(vm.getCustomCpuName());
        // Console
        // display type is initialized and set in InstanceTypeManager and model behaviors
        model.getNumOfMonitors().setSelectedItem(vm.getNumOfMonitors());
        model.getIsUsbEnabled().setEntity(vm.getUsbPolicy() != UsbPolicy.DISABLED);
        model.getIsSmartcardEnabled().setEntity(vm.isSmartcardEnabled());
        // Host
        model.getMigrationMode().setSelectedItem(vm.getMigrationSupport());
        model.selectMigrationPolicy(vm.getMigrationPolicyId());
        model.setSelectedMigrationDowntime(vm.getMigrationDowntime());
        // Highly Available
        model.getIsHighlyAvailable().setEntity(vm.isAutoStartup());
        // priority is initialized and set in InstanceTypeManager and model behaviors
        // Resource Allocation
        model.getIoThreadsEnabled().setEntity(vm.getNumOfIoThreads() != 0);
        model.getNumOfIoThreads().setEntity(Integer.toString(vm.getNumOfIoThreads()));
        model.getMemoryBalloonEnabled().setEntity(vm.isBalloonEnabled());
        int queues = vm.getVirtioScsiMultiQueues();
        if (queues == -1) {
            model.getVirtioScsiMultiQueueTypeSelection().setSelectedItem(VirtioMultiQueueType.AUTOMATIC);
        } else if (queues == 0) {
            model.getVirtioScsiMultiQueueTypeSelection().setSelectedItem(VirtioMultiQueueType.DISABLED);
        } else {
            model.getVirtioScsiMultiQueueTypeSelection().setSelectedItem(VirtioMultiQueueType.CUSTOM);
            model.getNumOfVirtioScsiMultiQueues().setEntity(queues);
        }
        // Boot
        model.setBootSequence(vm.getDefaultBootSequence());
    }
}
