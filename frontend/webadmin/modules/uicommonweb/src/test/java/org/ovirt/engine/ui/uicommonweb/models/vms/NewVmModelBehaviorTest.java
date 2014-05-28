package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;

public class NewVmModelBehaviorTest extends BaseVmModelBehaviorTest {
    VmTemplate template = new VmTemplate();

    @Override
    protected VmBase getVm() {
        return template;
    }

    @Override
    protected VmModelBehaviorBase getBehavior() {
        return new NewVmModelBehavior();
    }

    @Override
    protected void verifyBuiltModel(UnitVmModel model) {
        verifyBuiltCore(model);
    }
}
