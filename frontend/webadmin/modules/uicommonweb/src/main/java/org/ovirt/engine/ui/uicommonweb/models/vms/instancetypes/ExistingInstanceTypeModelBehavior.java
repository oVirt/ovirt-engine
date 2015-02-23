package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.HashSet;
import java.util.Set;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
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
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.HwOnlyVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.NameAndDescriptionVmBaseToUnitBuilder;
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
        getModel().getUsbPolicy().setItems(Arrays.asList(UsbPolicy.values()));

        getModel().getIsSoundcardEnabled().setIsChangable(true);

        Frontend.getInstance().runQuery(VdcQueryType.GetGraphicsDevices, new IdQueryParameters(instanceType.getId()), new AsyncQuery(
            this,
            new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue) {
                    List<GraphicsDevice> graphicsDevices = ((VdcQueryReturnValue) returnValue).getReturnValue();
                    Set<GraphicsType> graphicsTypesCollection = new HashSet<>();

                        for (GraphicsDevice graphicsDevice : graphicsDevices) {
                            graphicsTypesCollection.add(graphicsDevice.getGraphicsType());
                        }

                        initDisplayTypes(instanceType.getDefaultDisplayType(), UnitVmModel.GraphicsTypes.fromGraphicsTypes(graphicsTypesCollection));
                    }
                }
        ));

        initSoundCard(instanceType.getId());
        updateConsoleDevice(instanceType.getId());
        initPriority(instanceType.getPriority());

        buildModel((VmBase) instanceType, new BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel>() {
            @Override
            public void finished(VmBase source, UnitVmModel destination) {
                Frontend.getInstance().runQuery(VdcQueryType.IsBalloonEnabled, new IdQueryParameters(instanceType.getId()), new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object model, Object returnValue) {
                                getModel().getMemoryBalloonDeviceEnabled().setEntity((Boolean) ((VdcQueryReturnValue) returnValue).getReturnValue());
                            }
                        }
                ));

                AsyncDataProvider.getInstance().isVirtioScsiEnabledForVm(new AsyncQuery(getModel(), new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        getModel().getIsVirtioScsiEnabled().setEntity((Boolean) returnValue);
                    }
                }), instanceType.getId());

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
                AsyncDataProvider.getInstance().getTemplateNicList(getVmNicsQuery, instanceType.getId());

                AsyncDataProvider.getInstance().getWatchdogByVmId(new AsyncQuery(getModel(), new INewAsyncCallback() {
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
                getModel().getEmulatedMachine().setSelectedItem(instanceType.getCustomEmulatedMachine());
                getModel().getCustomCpu().setSelectedItem(instanceType.getCustomCpuName());
                getModel().getMigrationMode().setSelectedItem(instanceType.getMigrationSupport());
            }
        });
    }

    @Override
    protected void buildModel(VmBase vmBase, BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel> callback) {
        new BuilderExecutor<>(callback,
                              new NameAndDescriptionVmBaseToUnitBuilder(),
                              new HwOnlyVmBaseToUnitBuilder())
                .build(vmBase, getModel());
    }

    protected void initSoundCard(Guid id) {
        AsyncDataProvider.getInstance().isSoundcardEnabled(new AsyncQuery(getModel(), new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                getModel().getIsSoundcardEnabled().setEntity((Boolean) returnValue);
            }
        }), id);
    }
}
