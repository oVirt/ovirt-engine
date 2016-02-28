package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.HwOnlyVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.NameAndDescriptionVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class ExistingNonClusterModelBehavior extends NonClusterModelBehaviorBase {

    private VmTemplate entity;

    public ExistingNonClusterModelBehavior(VmTemplate entity) {
        this.entity = entity;
    }

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.initialize(systemTreeSelectedItem);
        updateNumOfSockets();
        getModel().getUsbPolicy().setItems(Arrays.asList(UsbPolicy.values()));

        getModel().getIsSoundcardEnabled().setIsChangeable(true);

        Frontend.getInstance().runQuery(VdcQueryType.GetGraphicsDevices, new IdQueryParameters(entity.getId()), new AsyncQuery(
                this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<GraphicsDevice> graphicsDevices = ((VdcQueryReturnValue) returnValue).getReturnValue();
                        Set<GraphicsType> graphicsTypesCollection = new HashSet<>();

                        for (GraphicsDevice graphicsDevice : graphicsDevices) {
                            graphicsTypesCollection.add(graphicsDevice.getGraphicsType());
                        }

                        initDisplayTypes(entity.getDefaultDisplayType(), UnitVmModel.GraphicsTypes.fromGraphicsTypes(graphicsTypesCollection));

                        doBuild();
                    }
                }
        ));

        initSoundCard(entity.getId());
        updateConsoleDevice(entity.getId());
        initPriority(entity.getPriority());

        Frontend.getInstance().runQuery(VdcQueryType.IsBalloonEnabled, new IdQueryParameters(entity.getId()), new AsyncQuery(this,
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
        }), entity.getId());


        AsyncDataProvider.getInstance().getWatchdogByVmId(new AsyncQuery(this.getModel(), new INewAsyncCallback() {
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
        }), entity.getId());

       Frontend.getInstance().runQuery(VdcQueryType.GetRngDevice, new IdQueryParameters(entity.getId()), new AsyncQuery(
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
        getModel().getEmulatedMachine().setSelectedItem(entity.getCustomEmulatedMachine());
        getModel().getCustomCpu().setSelectedItem(entity.getCustomCpuName());
        getModel().getMigrationMode().setSelectedItem(entity.getMigrationSupport());
        getModel().getCpuSharesAmount().setEntity(entity.getCpuShares());
        getModel().getIsHighlyAvailable().setEntity(entity.isAutoStartup());
        updateCpuSharesSelection();
    }

    public void doBuild() {
        buildModel(entity, new BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel>() {
            @Override
            public void finished(VmBase source, UnitVmModel destination) {
                Frontend.getInstance().runQuery(VdcQueryType.IsBalloonEnabled, new IdQueryParameters(entity.getId()), new AsyncQuery(this,
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
                }), entity.getId());


                AsyncDataProvider.getInstance().getWatchdogByVmId(new AsyncQuery(ExistingNonClusterModelBehavior.this.getModel(), new INewAsyncCallback() {
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
                }), entity.getId());

                Frontend.getInstance().runQuery(VdcQueryType.GetRngDevice, new IdQueryParameters(entity.getId()), new AsyncQuery(
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
                getModel().getEmulatedMachine().setSelectedItem(entity.getCustomEmulatedMachine());
                getModel().getCustomCpu().setSelectedItem(entity.getCustomCpuName());
                getModel().getMigrationMode().setSelectedItem(entity.getMigrationSupport());

                postBuild();
            }
        });
    }

    protected void postBuild() {

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
