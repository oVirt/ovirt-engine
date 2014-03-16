package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class KernelParamsUnitToVmBaseBuilder<T extends VmBase> extends BaseSyncBuilder<UnitVmModel, T> {
    @Override
    protected void build(UnitVmModel model, VmBase vm) {
        vm.setInitrdUrl(model.getInitrd_path().getEntity());
        vm.setKernelUrl(model.getKernel_path().getEntity());
        vm.setKernelParams(model.getKernel_parameters().getEntity());
    }
}
