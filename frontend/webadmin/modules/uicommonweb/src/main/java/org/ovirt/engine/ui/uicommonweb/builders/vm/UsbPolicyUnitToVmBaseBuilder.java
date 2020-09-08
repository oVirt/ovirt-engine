package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class UsbPolicyUnitToVmBaseBuilder<T extends VmBase> extends BaseSyncBuilder<UnitVmModel, T> {
    @Override
    protected void build(UnitVmModel model, VmBase vm) {
        if (model.getIsUsbEnabled().getEntity()) {
            vm.setUsbPolicy(UsbPolicy.ENABLED_NATIVE);
        } else {
            vm.setUsbPolicy(UsbPolicy.DISABLED);
        }
    }
}
