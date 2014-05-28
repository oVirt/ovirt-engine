package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;

public class ExistingVmModelBehaviorTest extends BaseVmModelBehaviorTest {

    VM vm = new VM();

    @Override
    protected VmBase getVm() {
        return vm.getStaticData();
    }

    @Override
    protected VmModelBehaviorBase getBehavior() {
        return new ExistingVmModelBehavior(vm) {
            @Override
            public boolean isHotSetCpuSupported() {
                return true;
            }
        };
    }

    @Override
    protected void verifyBuiltModel(UnitVmModel model) {
        verifyBuiltNameAndDescription(model);
        verifyBuiltComment(model);
        verifyBuiltCommon(model);
    }
}
