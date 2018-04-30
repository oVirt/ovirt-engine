package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link VmTemplateHandler} class. */
public class VmTemplateHandlerTest {

    private VmTemplateHandler vmTemplateHandler = new VmTemplateHandler();

    @BeforeEach
    public void setUp() {
        vmTemplateHandler.init();
    }

    @Test
    public void testUpdateFieldsName() {
        VmTemplate src = new VmTemplate();
        src.setName(RandomUtils.instance().nextString(10));

        VmTemplate dest = new VmTemplate();
        dest.setName(RandomUtils.instance().nextString(10));

        assertTrue(
                vmTemplateHandler.isUpdateValid(src, dest), "Update should be valid for different names");
    }

    @Test
    public void testUpdateFieldsQuotaEnforcementType() {
        VmTemplate src = new VmTemplate();
        src.setQuotaEnforcementType(QuotaEnforcementTypeEnum.DISABLED);

        VmTemplate dest = new VmTemplate();
        dest.setQuotaEnforcementType(QuotaEnforcementTypeEnum.HARD_ENFORCEMENT);

        assertTrue(
                vmTemplateHandler.isUpdateValid(src, dest),
                "Update should be valid for different quota enforcement types");
    }

    @Test
    public void testUpdateFieldsIsQuotaDefault() {
        VmTemplate src = new VmTemplate();
        src.setQuotaDefault(true);

        VmTemplate dest = new VmTemplate();
        dest.setQuotaDefault(false);

        assertTrue(
                vmTemplateHandler.isUpdateValid(src, dest),
                "Update should be valid for different quota default statuses");
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
        assertTrue(updateIsValid, "Update should be valid for different cluster IDs");
    }
}
