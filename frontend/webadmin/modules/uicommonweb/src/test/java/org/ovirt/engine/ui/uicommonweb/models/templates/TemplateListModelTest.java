package org.ovirt.engine.ui.uicommonweb.models.templates;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.pools.BaseVmListModelTest;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

@RunWith(MockitoJUnitRunner.class)
public class TemplateListModelTest extends BaseVmListModelTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    UnitVmModel model;

    @Before
    public void setUp() {
        setUpUnitVmModelExpectations(model);
    }

    @Test
    public void testBuildTemplateOnSave() {
        VmTemplate template = new VmTemplate();
        TemplateListModel.buildTemplateOnSave(model, template);
        verifyBuiltCommonVm(template);
    }
}
