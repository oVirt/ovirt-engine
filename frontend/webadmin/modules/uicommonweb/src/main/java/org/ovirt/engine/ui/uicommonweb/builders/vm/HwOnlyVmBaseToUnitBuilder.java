package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class HwOnlyVmBaseToUnitBuilder extends BaseSyncBuilder<VmBase, UnitVmModel> {

    private boolean everyFeatureSupported = false;

    public HwOnlyVmBaseToUnitBuilder withEveryFeatureSupported() {
        everyFeatureSupported = true;

        return this;
    }

    @Override
    protected void build(VmBase vm, UnitVmModel model) {
        model.getMemSize().setEntity(vm.getMemSizeMb());
        if (supported(ConfigurationValues.IoThreadsSupported, model)) {
            model.getIoThreadsEnabled().setEntity(vm.getNumOfIoThreads() != 0);
            model.getNumOfIoThreads().setEntity(vm.getNumOfIoThreads());
        }

        model.getMinAllocatedMemory().setEntity(vm.getMinAllocatedMem());
        model.getUsbPolicy().setSelectedItem(vm.getUsbPolicy());
        model.getNumOfMonitors().setSelectedItem(vm.getNumOfMonitors());
        model.getIsSingleQxlEnabled().setEntity(vm.getSingleQxlPci());
        model.setBootSequence(vm.getDefaultBootSequence());
        model.getTotalCPUCores().setEntity(Integer.toString(vm.getNumOfCpus()));
        model.getNumOfSockets().setSelectedItem(vm.getNumOfSockets());
        model.getIsSmartcardEnabled().setEntity(vm.isSmartcardEnabled());
        model.setSelectedMigrationDowntime(vm.getMigrationDowntime());
        model.getEmulatedMachine().setSelectedItem(vm.getCustomEmulatedMachine());
        model.getCustomCpu().setSelectedItem(vm.getCustomCpuName());
    }

    protected boolean supported(ConfigurationValues feature, UnitVmModel model) {
        return everyFeatureSupported || AsyncDataProvider.getInstance().supportedForUnitVmModel(feature, model);
    }
}
