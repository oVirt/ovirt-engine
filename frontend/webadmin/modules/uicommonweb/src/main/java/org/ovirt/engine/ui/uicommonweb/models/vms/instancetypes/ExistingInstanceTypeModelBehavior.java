package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VnicInstanceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

        AsyncDataProvider.isVirtioScsiEnabledForVm(new AsyncQuery(getModel(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                getModel().getIsVirtioScsiEnabled().setEntity((Boolean) returnValue);
            }
        }), instanceType.getId());

        getModel().setBootSequence(instanceType.getDefaultBootSequence());

        getModel().getUsbPolicy().setItems(Arrays.asList(UsbPolicy.values()));
        getModel().getUsbPolicy().setSelectedItem(instanceType.getUsbPolicy());

        AsyncQuery getVmNicsQuery = new AsyncQuery();
        getVmNicsQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                List<VnicProfileView> profiles = new ArrayList<VnicProfileView>(Arrays.asList(VnicProfileView.EMPTY));
                List<VnicInstanceType> vnicInstanceTypes = new ArrayList<VnicInstanceType>();

                for (VmNetworkInterface nic : (List<VmNetworkInterface>) result) {
                    final VnicInstanceType vnicInstanceType = new VnicInstanceType(nic);
                    vnicInstanceType.setItems(profiles);
                    vnicInstanceType.setSelectedItem(VnicProfileView.EMPTY);
                    vnicInstanceTypes.add(vnicInstanceType);
                }

                getModel().getNicsWithLogicalNetworks().getVnicProfiles().setItems(profiles);
                getModel().getNicsWithLogicalNetworks().setItems(vnicInstanceTypes);
                getModel().getNicsWithLogicalNetworks().setSelectedItem(Linq.firstOrDefault(vnicInstanceTypes));
            }
        };
        AsyncDataProvider.getTemplateNicList(getVmNicsQuery, instanceType.getId());

        AsyncDataProvider.getWatchdogByVmId(new AsyncQuery(this.getModel(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                UnitVmModel model = (UnitVmModel) target;
                @SuppressWarnings("unchecked")
                Collection<VmWatchdog> watchdogs =
                        ((VdcQueryReturnValue) returnValue).getReturnValue();
                for (VmWatchdog watchdog : watchdogs) {
                    model.getWatchdogAction().setSelectedItem(watchdog.getAction());
                    model.getWatchdogModel().setSelectedItem(watchdog.getModel());
                }
            }
        }), instanceType.getId());

       Frontend.getInstance().runQuery(VdcQueryType.GetRngDevice, new IdQueryParameters(instanceType.getId()), new AsyncQuery(
               this,
               new INewAsyncCallback() {
                   @Override
                   public void onSuccess(Object model, Object returnValue) {
                       List<VmDevice> rngDevices = ((VdcQueryReturnValue) returnValue).getReturnValue();
                       getModel().getIsRngEnabled().setEntity(!rngDevices.isEmpty());
                       if (!rngDevices.isEmpty()) {
                           VmRngDevice rngDevice = new VmRngDevice(rngDevices.get(0));
                           getModel().setRngDevice(rngDevice);
                       }
                   }
               }
       ));
    }

    protected void initSoundCard(Guid id) {
        AsyncDataProvider.isSoundcardEnabled(new AsyncQuery(getModel(), new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                getModel().getIsSoundcardEnabled().setEntity((Boolean) returnValue);
            }
        }), id);
    }
}
