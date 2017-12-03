package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmResumeBehavior;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.IconCache;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

/**
 * Builder which maps VmBase properties (VM or Template) that are to be persisted on all VM operations - even in
 * {@link org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel#buildVmOnSave}
 * If you are adding Yet Another VmBase Field, in most cases this will be the place to add the mapping.
 * If you are adding a more specific property you may need to add it instead to {@link CommonUnitToVmBaseBuilder} or
 * {@link FullUnitToVmBaseBuilder}
 *
 * This is not shared with the instance types. If you are adding a field which is supposed to be used also by instance types,
 * please add it to {@link HwOnlyCoreUnitToVmBaseBuilder}
 */
public class CoreUnitToVmBaseBuilder extends HwOnlyCoreUnitToVmBaseBuilder {

    @Override
    protected void build(UnitVmModel model, VmBase vm) {
        super.build(model, vm);
        vm.setAllowConsoleReconnect(model.getAllowConsoleReconnect().getEntity());
        vm.setVmType(model.getVmType().getSelectedItem());
        vm.setClusterId(model.getSelectedCluster() != null ? model.getSelectedCluster().getId() : null);
        vm.setTimeZone(model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null ? model.getTimeZone()
                .getSelectedItem().getTimeZoneKey() : ""); //$NON-NLS-1$
        vm.setIsoPath(model.getCdImage().getIsChangable() && model.getCdImage().getSelectedItem() != null ?
                model.getCdImage().getSelectedItem().getRepoImageId() : ""); //$NON-NLS-1$
        vm.setDeleteProtected(model.getIsDeleteProtected().getEntity());
        vm.setOsId(model.getOSType().getSelectedItem());
        Guid largeIconId = IconCache.getInstance().getId(model.getIcon().getEntity().getIcon());
        vm.setLargeIconId(largeIconId);
        vm.setSmallIconId(model.getIcon().getEntity().getSmallIconId() != null
                ? model.getIcon().getEntity().getSmallIconId()
                : AsyncDataProvider.getInstance().isCustomIconId(largeIconId)
                        ? null
                        : AsyncDataProvider.getInstance().getSmallByLargeOsDefaultIconId(largeIconId));
        vm.setVncKeyboardLayout(model.getVncKeyboardLayout().getSelectedItem());
        vm.setSerialNumberPolicy(model.getSerialNumberPolicy().getSelectedSerialNumberPolicy());
        vm.setCustomSerialNumber(model.getSerialNumberPolicy().getCustomSerialNumber().getEntity());
        vm.setBootMenuEnabled(model.getBootMenuEnabled().getEntity());
        vm.setSpiceFileTransferEnabled(Boolean.TRUE.equals(model.getSpiceFileTransferEnabled().getEntity()));
        vm.setSpiceCopyPasteEnabled(Boolean.TRUE.equals(model.getSpiceCopyPasteEnabled().getEntity()));
        vm.setAutoConverge(model.getAutoConverge().getSelectedItem());
        vm.setMigrateCompressed(model.getMigrateCompressed().getSelectedItem());
        vm.setCustomProperties(model.getCustomPropertySheet().serialize());
        vm.setConsoleDisconnectAction(model.getConsoleDisconnectAction().getSelectedItem());
        VmResumeBehavior selectedResumeBehavior = model.getResumeBehavior().getSelectedItem();
        if (selectedResumeBehavior == null) {
            // the default
            vm.setResumeBehavior(VmResumeBehavior.AUTO_RESUME);
        } else {
            vm.setResumeBehavior(selectedResumeBehavior);
        }
        if (model.getCpuSharesAmount().getIsAvailable() && model.getCpuSharesAmount().getEntity() != null) {
            vm.setCpuShares(model.getCpuSharesAmount().getEntity());
        }
        vm.setCustomCompatibilityVersion(model.getCustomCompatibilityVersion().getSelectedItem());
    }
}
