package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

import java.util.Arrays;
import java.util.Collection;

public class ExistingInstanceTypeModelBehavior extends InstanceTypeModelBehaviorBase {

    private InstanceType instanceType;

    public ExistingInstanceTypeModelBehavior(InstanceType instanceType) {
        this.instanceType = instanceType;
    }

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.initialize(systemTreeSelectedItem);
        updateNumOfSockets();

        getModel().getIsSoundcardEnabled().setIsChangable(true);
        getModel().getName().setEntity(instanceType.getName());
        getModel().getDescription().setEntity(instanceType.getDescription());

        getModel().getMemSize().setEntity(instanceType.getMemSizeMb());
        getModel().getTotalCPUCores().setEntity(Integer.toString(instanceType.getCpuPerSocket() * instanceType.getNumOfSockets()));
        getModel().getNumOfSockets().setSelectedItem(instanceType.getNumOfSockets());

        initDisplayTypes(instanceType.getDefaultDisplayType());
        getModel().getNumOfMonitors().setSelectedItem(instanceType.getNumOfMonitors());
        getModel().getIsSingleQxlEnabled().setEntity(instanceType.getSingleQxlPci());
        getModel().getIsSmartcardEnabled().setEntity(instanceType.isSmartcardEnabled());
        initSoundCard(instanceType.getId());
        updateConsoleDevice(instanceType.getId());
        getModel().getMigrationMode().setSelectedItem(instanceType.getMigrationSupport());
        getModel().setSelectedMigrationDowntime(instanceType.getMigrationDowntime());
        getModel().getIsHighlyAvailable().setEntity(instanceType.isAutoStartup());
        initPriority(instanceType.getPriority());
        getModel().getMinAllocatedMemory().setEntity(instanceType.getMinAllocatedMem());

        Frontend.getInstance().runQuery(VdcQueryType.IsBalloonEnabled, new IdQueryParameters(instanceType.getId()), new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        getModel().getMemoryBalloonDeviceEnabled().setEntity((Boolean) ((VdcQueryReturnValue) returnValue).getReturnValue());
                    }
                }
        ));

        getModel().setBootSequence(instanceType.getDefaultBootSequence());

        getModel().getUsbPolicy().setItems(Arrays.asList(UsbPolicy.values()));
        getModel().getUsbPolicy().setSelectedItem(instanceType.getUsbPolicy());

        AsyncDataProvider.getWatchdogByVmId(new AsyncQuery(this.getModel(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                UnitVmModel model = (UnitVmModel) target;
                @SuppressWarnings("unchecked")
                Collection<VmWatchdog> watchdogs =
                        ((VdcQueryReturnValue) returnValue).getReturnValue();
                for (VmWatchdog watchdog : watchdogs) {
                    model.getWatchdogAction().setSelectedItem(watchdog.getAction().name().toLowerCase());
                    model.getWatchdogModel().setSelectedItem(watchdog.getModel().name());
                }
            }
        }), instanceType.getId());
    }

}
