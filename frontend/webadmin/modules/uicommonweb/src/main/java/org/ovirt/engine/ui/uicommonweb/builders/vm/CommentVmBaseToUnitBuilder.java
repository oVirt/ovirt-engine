package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class CommentVmBaseToUnitBuilder extends BaseSyncBuilder<VmBase, UnitVmModel> {
    @Override
    protected void build(VmBase vm, UnitVmModel model) {
        model.getComment().setEntity(vm.getComment());
    }
}
