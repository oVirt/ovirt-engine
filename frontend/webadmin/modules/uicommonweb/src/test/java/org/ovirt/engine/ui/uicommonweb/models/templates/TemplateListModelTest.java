package org.ovirt.engine.ui.uicommonweb.models.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.vms.BaseVmListModelTest;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TemplateListModelTest extends BaseVmListModelTest {
    protected static final String VERSION_NAME = "version_name"; //$NON-NLS-1$

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    UnitVmModel model;

    @BeforeEach
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
