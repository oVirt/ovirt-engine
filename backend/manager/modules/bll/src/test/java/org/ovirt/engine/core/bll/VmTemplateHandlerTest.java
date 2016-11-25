package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link VmTemplateHandler} class. */
public class VmTemplateHandlerTest {

    private VmTemplateHandler vmTemplateHandler = new VmTemplateHandler();

    @Before
    public void setUp() {
        vmTemplateHandler.init();
    }

    @Test
    public void testUpdateFieldsName() {
        VmTemplate src = new VmTemplate();
        src.setName(RandomUtils.instance().nextString(10));

        VmTemplate dest = new VmTemplate();
        dest.setName(RandomUtils.instance().nextString(10));

        assertTrue("Update should be valid for different names",
                vmTemplateHandler.isUpdateValid(src, dest));
    }

    @Test
    public void testUpdateFieldsQuotaEnforcementType() {
        VmTemplate src = new VmTemplate();
        src.setQuotaEnforcementType(QuotaEnforcementTypeEnum.DISABLED);

        VmTemplate dest = new VmTemplate();
        dest.setQuotaEnforcementType(QuotaEnforcementTypeEnum.HARD_ENFORCEMENT);

        assertTrue("Update should be valid for different quota enforcement types",
                vmTemplateHandler.isUpdateValid(src, dest));
    }

    @Test
    public void testUpdateFieldsIsQuotaDefault() {
        VmTemplate src = new VmTemplate();
        src.setQuotaDefault(true);

        VmTemplate dest = new VmTemplate();
        dest.setQuotaDefault(false);

        assertTrue("Update should be valid for different quota default statuses",
                vmTemplateHandler.isUpdateValid(src, dest));
    }

    @Test
    public void testValidUpdateOfEditableFieldOnTemplateVm() {
        // Given
        VmTemplate src = new VmTemplate();
        src.setClusterId(Guid.newGuid());
        VmTemplate dest = new VmTemplate();
        dest.setClusterId(Guid.newGuid());

        // When
        boolean updateIsValid = vmTemplateHandler.isUpdateValid(src, dest);

        // Then
        assertTrue("Update should be valid for different cluster IDs", updateIsValid);
    }
}
