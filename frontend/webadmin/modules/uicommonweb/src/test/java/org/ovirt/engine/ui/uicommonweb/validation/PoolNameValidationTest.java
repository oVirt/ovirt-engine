package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.VmPool;

public class PoolNameValidationTest {

    private final PoolNameValidation poolNameValidation = new PoolNameValidation("");

    @Test
    public void testValidNonPatternName() {
        assertTrue(poolNameValidation.validate("pool-T4534f").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testNonValidNonPatternName() {
        assertFalse(poolNameValidation.validate("pool-T453&4f").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testNonValidNonPatternName2() {
        assertFalse(poolNameValidation.validate("").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testValidPatternName() {
        assertTrue(poolNameValidation.validate("pool-T4534f??".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testValidPatternName2() {
        assertTrue(poolNameValidation.validate("pool-T4534f?????rt".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testValidPatternName3() {
        assertTrue(poolNameValidation.validate("??rt".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testNonValidPatternName() {
        assertFalse(poolNameValidation.validate("???".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testNonValidPatternName2() {
        assertFalse(poolNameValidation.validate("pool-T4534f??r-t??".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testNonValidPatternName3() {
        assertFalse(poolNameValidation.validate("pool-T4534f??rt??asda".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testNonValidPatternName4() {
        assertFalse(poolNameValidation.validate("??rt??asda".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

}
