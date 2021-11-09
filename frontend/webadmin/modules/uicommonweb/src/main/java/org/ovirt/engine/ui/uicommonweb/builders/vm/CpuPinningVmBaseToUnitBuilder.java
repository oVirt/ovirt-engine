package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class CpuPinningVmBaseToUnitBuilder extends BaseSyncBuilder<VmBase, UnitVmModel> {

    @Override
    protected void build(VmBase vm, UnitVmModel model) {
        model.getCpuPinningPolicy().setSelectedCpuPolicy(vm.getCpuPinningPolicy());
        if (vm.getCpuPinningPolicy() == CpuPinningPolicy.MANUAL) {
            model.getCpuPinning().setEntity(vm.getCpuPinning());
        }
    }
}
