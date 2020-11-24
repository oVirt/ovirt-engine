package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.Builder;
import org.ovirt.engine.ui.uicommonweb.builders.CompositeBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class CommonVmBaseToUnitBuilder extends CompositeBuilder<VmBase, UnitVmModel> {

    public CommonVmBaseToUnitBuilder(Builder<VmBase, UnitVmModel>... builders) {
        super(builders);
    }

    public CommonVmBaseToUnitBuilder() {
        this(
                new CoreVmBaseToUnitBuilder(),
                new HwOnlyVmBaseToUnitBuilder()
        );
    }

    @Override
    protected void postBuild(VmBase vm, UnitVmModel model) {
        model.getOSType().setSelectedItem(vm.getOsId());
        model.getAllowConsoleReconnect().setEntity(vm.isAllowConsoleReconnect());
        model.getIsStateless().setEntity(vm.isStateless());
        model.getIsRunAndPause().setEntity(vm.isRunAndPause());
    }
}
