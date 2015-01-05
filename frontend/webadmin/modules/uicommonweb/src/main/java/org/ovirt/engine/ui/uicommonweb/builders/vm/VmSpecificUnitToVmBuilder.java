package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

/**
 * Maps VM-specific properties (i.e. those not present in VmBase).
 * Used when persisting VMs only (new, edit, clone), not templates.
 */
public class VmSpecificUnitToVmBuilder extends BaseSyncBuilder<UnitVmModel, VM> {
    @Override
    protected void build(UnitVmModel model, VM vm) {
        vm.setVmtGuid(model.getTemplateWithVersion().getSelectedItem().getTemplateVersion().getId());
        vm.setInstanceTypeId(model.getInstanceTypes().getSelectedItem().getId());
    }
}
