package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class NameUnitToVmBaseBuilder extends BaseSyncBuilder<UnitVmModel, VmBase> {
    @Override
    protected void build(UnitVmModel model, VmBase vm) {
        vm.setName(model.getName().getEntity());
    }
}
