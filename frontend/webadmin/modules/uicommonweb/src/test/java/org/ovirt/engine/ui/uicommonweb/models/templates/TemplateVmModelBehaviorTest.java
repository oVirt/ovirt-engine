package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.vms.BaseVmModelBehaviorTest;
import org.ovirt.engine.ui.uicommonweb.models.vms.TemplateVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmModelBehaviorBase;

public class TemplateVmModelBehaviorTest extends BaseVmModelBehaviorTest {

    VmTemplate template = new VmTemplate();

    @Override
    protected VmBase getVm() {
        return template;
    }

    @Override
    protected VmModelBehaviorBase getBehavior() {
        return new TemplateVmModelBehavior(template);
    }

    @Override
    protected void verifyBuiltModel(UnitVmModel model) {
        verifyBuiltNameAndDescription(model);
        verifyBuiltComment(model);
        verifyBuiltCommon(model);
    }
}
