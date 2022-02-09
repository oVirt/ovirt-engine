package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.VirtioMultiQueueType;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class HwOnlyVmBaseToUnitBuilder extends BaseSyncBuilder<VmBase, UnitVmModel> {
    @Override
    protected void build(VmBase vm, UnitVmModel model) {
        model.getMemSize().setEntity(vm.getMemSizeMb());
        model.getMaxMemorySize().setEntity(vm.getMaxMemorySizeMb());
        model.getIoThreadsEnabled().setEntity(vm.getNumOfIoThreads() != 0);
        model.getNumOfIoThreads().setEntity(Integer.toString(vm.getNumOfIoThreads()));
        model.getMinAllocatedMemory().setEntity(vm.getMinAllocatedMem());
        model.getIsUsbEnabled().setEntity(vm.getUsbPolicy() != UsbPolicy.DISABLED);
        model.getNumOfMonitors().setSelectedItem(vm.getNumOfMonitors());
        model.setBootSequence(vm.getDefaultBootSequence());
        model.getTotalCPUCores().setEntity(Integer.toString(vm.getNumOfCpus()));
        model.getNumOfSockets().setSelectedItem(vm.getNumOfSockets());
        model.getCoresPerSocket().setSelectedItem(vm.getCpuPerSocket());
        model.getThreadsPerCore().setSelectedItem(vm.getThreadsPerCpu());
        model.getIsSmartcardEnabled().setEntity(vm.isSmartcardEnabled());
        model.setSelectedMigrationDowntime(vm.getMigrationDowntime());
        model.selectMigrationPolicy(vm.getMigrationPolicyId());
        model.getEmulatedMachine().setSelectedItem(vm.getCustomEmulatedMachine());
        model.getCustomCpu().setSelectedItem(vm.getCustomCpuName());
        model.getHostCpu().setEntity(vm.isUseHostCpuFlags());
        model.getTscFrequency().setEntity(vm.getUseTscFrequency());
        int queues = vm.getVirtioScsiMultiQueues();
        if (queues == -1) {
            model.getVirtioScsiMultiQueueTypeSelection().setSelectedItem(VirtioMultiQueueType.AUTOMATIC);
        } else if (queues == 0) {
            model.getVirtioScsiMultiQueueTypeSelection().setSelectedItem(VirtioMultiQueueType.DISABLED);
        } else {
            model.getVirtioScsiMultiQueueTypeSelection().setSelectedItem(VirtioMultiQueueType.CUSTOM);
            model.getNumOfVirtioScsiMultiQueues().setEntity(queues);
        }
        model.getMemoryBalloonEnabled().setEntity(vm.isBalloonEnabled());
    }
}
