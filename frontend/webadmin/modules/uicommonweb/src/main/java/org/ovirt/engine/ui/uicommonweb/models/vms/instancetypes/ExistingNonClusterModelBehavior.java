package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import static org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider.getInstance;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.HwOnlyVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.NameAndDescriptionVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class ExistingNonClusterModelBehavior extends NonClusterModelBehaviorBase {

    private VmTemplate entity;

    public ExistingNonClusterModelBehavior(VmTemplate entity) {
        this.entity = entity;
    }

    @Override
    public void initialize() {
        super.initialize();
        updateNumOfSockets();

        getModel().getIsSoundcardEnabled().setIsChangeable(true);

        Frontend.getInstance().runQuery(QueryType.GetGraphicsDevices, new IdQueryParameters(entity.getId()),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    List<GraphicsDevice> graphicsDevices = returnValue.getReturnValue();
                    Set<GraphicsType> graphicsTypesCollection = new HashSet<>();

                    for (GraphicsDevice graphicsDevice : graphicsDevices) {
                        graphicsTypesCollection.add(graphicsDevice.getGraphicsType());
                    }

                    initDisplayTypes(entity.getDefaultDisplayType(), UnitVmModel.GraphicsTypes.fromGraphicsTypes(graphicsTypesCollection));

                    doBuild();
                }
        ));

        initSoundCard(entity.getId());
        updateTpm(entity.getId());
        updateConsoleDevice(entity.getId());
        initPriority(entity.getPriority());

        getInstance().isVirtioScsiEnabledForVm(new AsyncQuery<>(returnValue -> getModel().getIsVirtioScsiEnabled().setEntity(returnValue)), entity.getId());


        getInstance().getWatchdogByVmId(new AsyncQuery<QueryReturnValue>(returnValue -> {
            @SuppressWarnings("unchecked")
            Collection<VmWatchdog> watchdogs = returnValue.getReturnValue();
            for (VmWatchdog watchdog : watchdogs) {
                getModel().getWatchdogAction().setSelectedItem(watchdog.getAction());
                getModel().getWatchdogModel().setSelectedItem(watchdog.getModel());
            }
        }), entity.getId());

       Frontend.getInstance().runQuery(QueryType.GetRngDevice, new IdQueryParameters(entity.getId()),
               new AsyncQuery<QueryReturnValue>(returnValue -> {
                   List<VmDevice> rngDevices = returnValue.getReturnValue();
                   getModel().getIsRngEnabled().setEntity(!rngDevices.isEmpty());
                   if (!rngDevices.isEmpty()) {
                       VmRngDevice rngDevice = new VmRngDevice(rngDevices.get(0));
                       getModel().setRngDevice(rngDevice);
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
        buildModel(entity, (source, destination) -> {
            getInstance().isVirtioScsiEnabledForVm(new AsyncQuery<>(returnValue -> getModel().getIsVirtioScsiEnabled().setEntity(returnValue)), entity.getId());

            getInstance().getWatchdogByVmId(new AsyncQuery<QueryReturnValue>(returnValue -> {
                @SuppressWarnings("unchecked")
                Collection<VmWatchdog> watchdogs = returnValue.getReturnValue();
                for (VmWatchdog watchdog : watchdogs) {
                    getModel().getWatchdogAction().setSelectedItem(watchdog.getAction());
                    getModel().getWatchdogModel().setSelectedItem(watchdog.getModel());
                }
            }), entity.getId());

            Frontend.getInstance().runQuery(QueryType.GetRngDevice, new IdQueryParameters(entity.getId()),
                    new AsyncQuery<QueryReturnValue>(returnValue -> {
                        List<VmDevice> rngDevices = returnValue.getReturnValue();
                        getModel().getIsRngEnabled().setEntity(!rngDevices.isEmpty());
                        if (!rngDevices.isEmpty()) {
                            VmRngDevice rngDevice = new VmRngDevice(rngDevices.get(0));
                            getModel().setRngDevice(rngDevice);
                        }
                    }
            ));
            getModel().getEmulatedMachine().setSelectedItem(entity.getCustomEmulatedMachine());
            getModel().getCustomCpu().setSelectedItem(entity.getCustomCpuName());
            getModel().getMigrationMode().setSelectedItem(entity.getMigrationSupport());

            postBuild();
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
        getInstance().isSoundcardEnabled(new AsyncQuery<>(returnValue -> getModel().getIsSoundcardEnabled().setEntity(returnValue)), id);
    }
}
