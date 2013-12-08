package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.utils.RandomUtils;

/** A test case for the {@link VmTemplateHandler} class. */
public class VmTemplateHandlerTest {

    @Before
    public void setUp() {
        VmTemplateHandler.init();
    }

    @Test
    public void testUpdateFieldsName() {
        VmTemplate src = new VmTemplate();
        src.setName(RandomUtils.instance().nextString(10));

        VmTemplate dest = new VmTemplate();
        dest.setName(RandomUtils.instance().nextString(10));

        assertTrue("Update should be valid for different names",
                VmTemplateHandler.isUpdateValid(src, dest));
    }

    @Test
    public void testUpdateFieldsQuotaEnforcementType() {
        VmTemplate src = new VmTemplate();
        src.setQuotaEnforcementType(QuotaEnforcementTypeEnum.DISABLED);

        VmTemplate dest = new VmTemplate();
        dest.setQuotaEnforcementType(QuotaEnforcementTypeEnum.HARD_ENFORCEMENT);

        assertTrue("Update should be valid for different quota enforcement types",
                VmTemplateHandler.isUpdateValid(src, dest));
    }

    @Test
    public void testUpdateFieldsIsQuotaDefault() {
        VmTemplate src = new VmTemplate();
        src.setQuotaDefault(true);

        VmTemplate dest = new VmTemplate();
        dest.setQuotaDefault(false);

        assertTrue("Update should be valid for different quota default statuses",
                VmTemplateHandler.isUpdateValid(src, dest));
    }
}
