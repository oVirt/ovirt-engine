package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;

public class NewTemplateVmModelBehaviorTest extends BaseVmModelBehaviorTest {
    VM vm = new VM();

    @Override
    protected VmBase getVm() {
        return vm.getStaticData();
    }

    @Override
    protected VmModelBehaviorBase getBehavior() {
        return new NewTemplateVmModelBehavior(vm);
    }

    @Override
    protected void verifyBuiltModel(UnitVmModel model) {
        verifyBuiltCommon(model);
    }
}
