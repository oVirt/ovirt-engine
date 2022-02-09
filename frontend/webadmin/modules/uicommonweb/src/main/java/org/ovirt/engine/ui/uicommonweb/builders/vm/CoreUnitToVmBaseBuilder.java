package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.builders.MigrationsModelToEntityBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.IconCache;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

/**
 * Builder which maps VmBase properties (VM or Template) that are to be persisted on all VM operations -
 * New/Edit of VMs, Templates and Pools.
 * If you are adding Yet Another VmBase Field, in most cases this will be the place to add the mapping.
 * If you are adding a more specific property you may need to add it instead to {@link CommonUnitToVmBaseBuilder} or
 * {@link FullUnitToVmBaseBuilder}
 *
 * This is not shared with the instance types. Fields used also by the instance types are
 * in {@link HwOnlyCoreUnitToVmBaseBuilder}. However, since the instance types are deprecated,
 * no new fields should be added there.
 */
public class CoreUnitToVmBaseBuilder extends HwOnlyCoreUnitToVmBaseBuilder {

    public CoreUnitToVmBaseBuilder() {
        // the policy is copied for VMs and Templates in MigrationOptionsUnitToVmBaseBuilder
        super(
                new MigrationsModelToEntityBuilder<UnitVmModel, VmBase>(false),
                new CpuPinningUnitToVmBaseBuilder());
    }

    @Override
    protected void postBuild(UnitVmModel model, VmBase vm) {
        super.postBuild(model, vm);
        // Header
        vm.setClusterId(model.getSelectedCluster() != null ? model.getSelectedCluster().getId() : null);
        vm.setVmType(model.getVmType().getSelectedItem());
        vm.setOsId(model.getOSType().getSelectedItem());
        // General
        vm.setDeleteProtected(model.getIsDeleteProtected().getEntity());
        // System
        vm.setCustomCompatibilityVersion(model.getCustomCompatibilityVersion().getSelectedItem());
        vm.setTimeZone(model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null ? model.getTimeZone()
                .getSelectedItem().getTimeZoneKey() : ""); //$NON-NLS-1$
        vm.setSerialNumberPolicy(model.getSerialNumberPolicy().getSelectedItem());
        if (SerialNumberPolicy.CUSTOM.equals(model.getSerialNumberPolicy().getSelectedItem())) {
            vm.setCustomSerialNumber(model.getCustomSerialNumber().getEntity());
        } else {
            vm.setCustomSerialNumber(null);
        }
        // Console
        vm.setVncKeyboardLayout(model.getVncKeyboardLayout().getSelectedItem());
        vm.setConsoleDisconnectAction(model.getConsoleDisconnectAction().getSelectedItem());
        vm.setConsoleDisconnectActionDelay(model.getConsoleDisconnectActionDelay().getEntity());
        vm.setSpiceFileTransferEnabled(Boolean.TRUE.equals(model.getSpiceFileTransferEnabled().getEntity()));
        vm.setSpiceCopyPasteEnabled(Boolean.TRUE.equals(model.getSpiceCopyPasteEnabled().getEntity()));
        vm.setAllowConsoleReconnect(model.getAllowConsoleReconnect().getEntity());
        // Host
        vm.setUseHostCpuFlags(model.getHostCpu().getEntity());
        vm.setUseTscFrequency(model.getTscFrequency().getEntity());
        // Resource allocation
        if (model.getCpuProfiles().getSelectedItem() != null) {
            vm.setCpuProfileId(model.getCpuProfiles().getSelectedItem().getId());
        }
        if (model.getCpuSharesAmount().getIsAvailable() && model.getCpuSharesAmount().getEntity() != null) {
            vm.setCpuShares(model.getCpuSharesAmount().getEntity());
        }
        vm.setMultiQueuesEnabled(model.getMultiQueues().getEntity());
        // Boot
        vm.setIsoPath(model.getCdImage().getIsChangable() && model.getCdImage().getSelectedItem() != null ?
                model.getCdImage().getSelectedItem().getRepoImageId() : ""); //$NON-NLS-1$
        vm.setBootMenuEnabled(model.getBootMenuEnabled().getEntity());
        // Icons
        Guid largeIconId = IconCache.getInstance().getId(model.getIcon().getEntity().getIcon());
        vm.setLargeIconId(largeIconId);
        vm.setSmallIconId(model.getIcon().getEntity().getSmallIconId() != null
                ? model.getIcon().getEntity().getSmallIconId()
                : AsyncDataProvider.getInstance().isCustomIconId(largeIconId)
                        ? null
                        : AsyncDataProvider.getInstance().getSmallByLargeOsDefaultIconId(largeIconId));
        // Custom Properties
        vm.setCustomProperties(model.getCustomPropertySheet().serialize());
    }
}
