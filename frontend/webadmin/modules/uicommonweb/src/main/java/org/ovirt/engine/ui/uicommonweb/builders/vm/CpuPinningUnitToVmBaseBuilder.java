package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class CpuPinningUnitToVmBaseBuilder extends BaseSyncBuilder<UnitVmModel, VmBase> {

    @Override
    protected void build(UnitVmModel model, VmBase vm) {
        vm.setCpuPinningPolicy(model.getCpuPinningPolicy().getSelectedItem().getPolicy());
        if (model.getCpuPinningPolicy().getSelectedItem().getPolicy() == CpuPinningPolicy.MANUAL) {
            vm.setCpuPinning(model.getCpuPinning().getEntity());
        }
    }
}
