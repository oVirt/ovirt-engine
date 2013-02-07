package org.ovirt.engine.ui.uicommonweb.validation;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmPool;

public class PoolNameValidationTest {

    @Test
    public void testValidNonPatternName() {
        assertTrue(new TestablePoolNameValidation().Validate("pool-T4534f").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testNonValidNonPatternName() {
        assertFalse(new TestablePoolNameValidation().Validate("pool-T453&4f").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testNonValidNonPatternName2() {
        assertFalse(new TestablePoolNameValidation().Validate("").getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testValidPatternName() {
        assertTrue(new TestablePoolNameValidation().Validate("pool-T4534f??".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testValidPatternName2() {
        assertTrue(new TestablePoolNameValidation().Validate("pool-T4534f?????rt".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testValidPatternName3() {
        assertTrue(new TestablePoolNameValidation().Validate("??rt".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testNonValidPatternName() {
        assertFalse(new TestablePoolNameValidation().Validate("???".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testNonValidPatternName2() {
        assertFalse(new TestablePoolNameValidation().Validate("pool-T4534f??r-t??".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testNonValidPatternName3() {
        assertFalse(new TestablePoolNameValidation().Validate("pool-T4534f??rt??asda".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    @Test
    public void testNonValidPatternName4() {
        assertFalse(new TestablePoolNameValidation().Validate("??rt??asda".replace('?', VmPool.MASK_CHARACTER)).getSuccess()); //$NON-NLS-1$
    }

    /**
     *  {@link PoolNameValidation#composeMessage()} access {@link ConstantsManager} that can't be used inside unit tests so it's overridden
     */
    private class TestablePoolNameValidation extends PoolNameValidation {
        @Override
        protected String composeMessage() {
            return ""; //$NON-NLS-1$
        }
    }
}
