package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class DedicatedVmForVdsUnitToVmBaseBuilder<T extends VmBase> extends BaseSyncBuilder<UnitVmModel, T> {
    @Override
    protected void build(UnitVmModel model, VmBase vm) {
        // host migration configuration
        VDS defaultHost = model.getDefaultHost().getSelectedItem();
        if (model.getIsAutoAssign().getEntity()) {
            vm.setDedicatedVmForVds(null);
        } else {
            vm.setDedicatedVmForVds(defaultHost.getId());
        }
    }
}
