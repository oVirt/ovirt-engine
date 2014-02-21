package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

/**
 * Builder which maps VmBase properties (VM or Template) that are to be persisted on all VM operations - even in
 * {@link org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel#buildVmOnSave}
 * If you are adding Yet Another VmBase Field, in most cases this will be the place to add the mapping.
 * If you are adding a more specific property you may need to add it instead to {@link CommonUnitToVmBaseBuilder} or
 * {@link FullUnitToVmBaseBuilder}
 */
public class CoreUnitToVmBaseBuilder<T extends VmBase> extends BaseSyncBuilder<UnitVmModel, T> {
    @Override
    protected void build(UnitVmModel model, VmBase vm) {
        vm.setAllowConsoleReconnect(model.getAllowConsoleReconnect().getEntity());
        vm.setVmType(model.getVmType().getSelectedItem());
        vm.setVdsGroupId(model.getSelectedCluster().getId());
        vm.setTimeZone(model.getTimeZone().getIsAvailable() && model.getTimeZone().getSelectedItem() != null ? model.getTimeZone()
                .getSelectedItem().getTimeZoneKey() : ""); //$NON-NLS-1$
        vm.setDefaultBootSequence(model.getBootSequence());
        vm.setIsoPath(model.getCdImage().getIsChangable() ? model.getCdImage().getSelectedItem() : ""); //$NON-NLS-1$
        vm.setMemSizeMb(model.getMemSize().getEntity());
        vm.setMinAllocatedMem(model.getMinAllocatedMemory().getEntity());
        vm.setNumOfMonitors(model.getNumOfMonitors().getSelectedItem());
        vm.setSingleQxlPci(model.getIsSingleQxlEnabled().getEntity());
        vm.setSmartcardEnabled(model.getIsSmartcardEnabled().getEntity());
        vm.setSsoMethod(model.extractSelectedSsoMethod());
        vm.setNumOfSockets(model.getNumOfSockets().getSelectedItem());
        vm.setCpuPerSocket(Integer.parseInt(model.getTotalCPUCores().getEntity()) / model.getNumOfSockets().getSelectedItem());
        vm.setDeleteProtected(model.getIsDeleteProtected().getEntity());
        vm.setOsId(model.getOSType().getSelectedItem());
        vm.setVncKeyboardLayout(model.getVncKeyboardLayout().getSelectedItem());
        vm.setDefaultDisplayType(model.getDisplayProtocol().getSelectedItem().getEntity());
        vm.setSerialNumberPolicy(model.getSerialNumberPolicy().getSelectedSerialNumberPolicy());
        vm.setCustomSerialNumber(model.getSerialNumberPolicy().getCustomSerialNumber().getEntity());
    }
}
