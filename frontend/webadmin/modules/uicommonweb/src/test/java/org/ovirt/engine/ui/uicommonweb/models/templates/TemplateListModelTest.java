package org.ovirt.engine.ui.uicommonweb.models.templates;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.vms.BaseVmListModelTest;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

@RunWith(MockitoJUnitRunner.class)
public class TemplateListModelTest extends BaseVmListModelTest {
    protected static final String VERSION_NAME = "version_name"; //$NON-NLS-1$

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    UnitVmModel model;

    @Before
    public void setUp() {
        setUpUnitVmModelExpectations(model);
    }

    @Override
    protected void setUpUnitVmModelExpectations(UnitVmModel model) {
        super.setUpUnitVmModelExpectations(model);
        when(model.getTemplateVersionName().getEntity()).thenReturn(VERSION_NAME);
    }

    @Test
    public void testBuildTemplateOnSave() {
        VmTemplate template = new VmTemplate();
        TemplateListModel.buildTemplateOnSave(model, template);
        verifyBuiltTemplate(template);
    }

    protected void verifyBuiltTemplate(VmTemplate template) {
        super.verifyBuiltCommonVm(template);

        assertEquals(VERSION_NAME, template.getTemplateVersionName());
    }
}
