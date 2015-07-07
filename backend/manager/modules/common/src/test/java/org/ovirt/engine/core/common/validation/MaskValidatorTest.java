package org.ovirt.engine.core.common.validation;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MaskValidatorTest {

    private final String mask;
    private final boolean isNetmaskValidFormat;
    private final boolean isNetmaskValidValue;
    private final boolean isPrefixValid;

    public MaskValidatorTest(String mask, boolean isNetmaskValidFormat, boolean isMaskValid, boolean isPrefixValid) {
        this.mask = mask;
        this.isNetmaskValidFormat = isNetmaskValidFormat;
        this.isNetmaskValidValue = isMaskValid;
        this.isPrefixValid = isPrefixValid;
    }

    @Test
    public void checkNetmaskFormatValidation() {
        assertEquals("Failed to validate mask's Format: " + mask,
                isNetmaskValidFormat, MaskValidator.getInstance().isValidNetmaskFormat(mask));
    }

    @Test
    public void checkPrefixFormatValidation() {
        assertEquals("Failed to validate prefix's Format: " + mask,
                 isPrefixValid, MaskValidator.getInstance().isPrefixValid(mask));
    }

    @Test
    public void checkNetmaskValidValue() {
        if (!MaskValidator.getInstance().isValidNetmaskFormat(mask)) {
            return;
        }

        assertEquals("Failed to validate mask value" + mask, isNetmaskValidValue,
                MaskValidator.getInstance().isNetmaskValid(mask));
    }

    @Parameterized.Parameters
    public static Collection<Object[]> namesParams() {
         Random random = new Random();

        return Arrays.asList(new Object[][] {

                // Bad Format
                { null, false, random.nextBoolean(), false }, //$NON-NLS-1$
                { "", false, random.nextBoolean(), false }, //$NON-NLS-1$
                { "a.a.a.a", false, random.nextBoolean(), false }, //$NON-NLS-1$
                { "255.255.0", false, random.nextBoolean(), false }, //$NON-NLS-1$
                { "255.255.0.0.0", false, random.nextBoolean(), false }, //$NON-NLS-1$
                { "255.255.0.0.", false, random.nextBoolean(), false }, //$NON-NLS-1$

                { "31 ", false, random.nextBoolean(), false }, //$NON-NLS-1$ /*note extra space*/
                { "/31 ", false, random.nextBoolean(), false }, //$NON-NLS-1$ /*note extra space*/
                { "31/", false, random.nextBoolean(), false }, //$NON-NLS-1$
                { "31*", false, random.nextBoolean(), false }, //$NON-NLS-1$
                { "//31 ", false, random.nextBoolean(), false }, //$NON-NLS-1$
                { "33", false, random.nextBoolean(), false }, //$NON-NLS-1$
                { "/33", false, random.nextBoolean(), false }, //$NON-NLS-1$


                { "01", false, random.nextBoolean(), false }, //$NON-NLS-1$
                { "01/", false, random.nextBoolean(), false }, //$NON-NLS-1$


                // Not Valid
                { "255.255.0.1", true, false, false }, //$NON-NLS-1$
                { "255.0.255.0", true, false, false }, //$NON-NLS-1$
                { "255.0.0.255", true, false, false }, //$NON-NLS-1$
                { "224.0.255.0", true, false, false }, //$NON-NLS-1$

                // Valid
                { "255.255.0.0", true, true, false }, //$NON-NLS-1$
                { "255.255.255.255", true, true, false }, //$NON-NLS-1$

                { "31", false, random.nextBoolean(), true }, //$NON-NLS-1$
                { "/31", false, random.nextBoolean(), true }, //$NON-NLS-1$
                { "7", false, random.nextBoolean(), true }, //$NON-NLS-1$
                { "/7", false, random.nextBoolean(), true }, //$NON-NLS-1$

        });
    }

}
