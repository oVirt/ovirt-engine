package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class KernelParamsVmBaseToUnitBuilder extends BaseSyncBuilder<VmBase, UnitVmModel> {
    @Override
    protected void build(VmBase vm, UnitVmModel model) {
        model.getInitrd_path().setEntity(vm.getInitrdUrl());
        model.getKernel_path().setEntity(vm.getKernelUrl());
        model.getKernel_parameters().setEntity(vm.getKernelParams());
    }
}
