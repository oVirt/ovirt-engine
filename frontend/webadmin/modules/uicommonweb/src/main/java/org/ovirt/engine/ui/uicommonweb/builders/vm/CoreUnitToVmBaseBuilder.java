package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
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
        vm.setVdsGroupId(model.getSelectedCluster().getId());
        vm.setTimeZone(model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null ? model.getTimeZone()
                .getSelectedItem().getTimeZoneKey() : ""); //$NON-NLS-1$
        vm.setIsoPath(model.getCdImage().getIsChangable() ? model.getCdImage().getSelectedItem() : ""); //$NON-NLS-1$
        vm.setDeleteProtected(model.getIsDeleteProtected().getEntity());
        vm.setOsId(model.getOSType().getSelectedItem());
        vm.setVncKeyboardLayout(model.getVncKeyboardLayout().getSelectedItem());
        vm.setSerialNumberPolicy(model.getSerialNumberPolicy().getSelectedSerialNumberPolicy());
        vm.setCustomSerialNumber(model.getSerialNumberPolicy().getCustomSerialNumber().getEntity());
        vm.setBootMenuEnabled(model.getBootMenuEnabled().getEntity());
        vm.setSpiceFileTransferEnabled(model.getSpiceFileTransferEnabled().getEntity());
        vm.setSpiceCopyPasteEnabled(model.getSpiceCopyPasteEnabled().getEntity());
        vm.setAutoConverge(model.getAutoConverge().getSelectedItem());
        vm.setMigrateCompressed(model.getMigrateCompressed().getSelectedItem());
    }
}
