package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.vms.BaseVmModelBehaviorTest;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmModelBehaviorBase;

public class ExistingInstanceTypeModelBehaviorTest extends BaseVmModelBehaviorTest {

    VmTemplate instance = new VmTemplate();

    @Override
    protected VmBase getVm() {
        return instance;
    }

    @Override
    protected VmModelBehaviorBase getBehavior() {
        return new ExistingNonClusterModelBehavior(instance);
    }

    @Override
    protected void verifyBuiltModel(UnitVmModel model) {
        verifyBuiltNameAndDescription(model);
        verifyBuiltHardware(model);
    }
}
