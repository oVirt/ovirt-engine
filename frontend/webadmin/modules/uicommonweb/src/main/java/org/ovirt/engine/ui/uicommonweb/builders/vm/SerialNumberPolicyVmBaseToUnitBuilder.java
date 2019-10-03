package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.BaseSyncBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class SerialNumberPolicyVmBaseToUnitBuilder extends BaseSyncBuilder<VmBase, UnitVmModel> {

    @Override
    protected void build(VmBase vm, UnitVmModel model) {
        model.getSerialNumberPolicy().setSelectedItem(vm.getSerialNumberPolicy());
        if (SerialNumberPolicy.CUSTOM.equals(vm.getSerialNumberPolicy())) {
            model.getCustomSerialNumber().setEntity(vm.getCustomSerialNumber());
        }
    }
}
