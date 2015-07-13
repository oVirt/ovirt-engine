package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Matchers;
import org.ovirt.engine.core.common.validation.MaskValidator;

@RunWith(Parameterized.class)
public class SubnetMaskValidationTest {

    private final String mask;
    private final boolean isMaskValid;
    private final boolean isPrefixAllowed;

    public SubnetMaskValidationTest(String mask, boolean isMaskValid, boolean isPrefixAllowed) {
        this.mask = mask;
        this.isMaskValid = isMaskValid;
        this.isPrefixAllowed = isPrefixAllowed;
    }

    private SubnetMaskValidation createUnderTest(boolean isPrefixAllowed) {
        SubnetMaskValidation underTest = spy(new SubnetMaskValidation(isPrefixAllowed));
        doReturn(ErrorMessage.invalidMask.name()).when(underTest).getInvalidMask();
        doReturn(ErrorMessage.badNetmaskFormatMessage.name()).when(underTest).getBadNetmaskFormatMessage();
        doReturn(ErrorMessage.badPrefixOrNetmaskFormatMessage.name()).when(underTest).getBadPrefixOrNetmaskFormatMessage();
        return underTest;
    }

    @Test
    public void checkValidMask() {
        SubnetMaskValidation underTest = createUnderTest(isPrefixAllowed);
        assertEquals("Failed to validate mask: " + mask, isMaskValid, underTest.validate(mask).getSuccess());//$NON-NLS-1$
    }

    @Test
    public void checkValidErrorMessage() {
        Random random = new Random();
        runErrorMessageCheck(random.nextBoolean(), true, false, false, ErrorMessage.invalidMask);
        runErrorMessageCheck(true, false, random.nextBoolean(), false, ErrorMessage.badPrefixOrNetmaskFormatMessage);
        runErrorMessageCheck(false,
                false,
                random.nextBoolean(),
                random.nextBoolean(),
                ErrorMessage.badNetmaskFormatMessage);
        runErrorMessageCheck(random.nextBoolean(), true, true, false, ErrorMessage.NoError);
        runErrorMessageCheck(true, false, random.nextBoolean(), true, ErrorMessage.NoError);
    }

    private void runErrorMessageCheck(boolean isPrefixAllowed,
            boolean isNetmaskValidFormat,
            boolean isNetmaskValidValue,
            boolean isPrefixValid,
            ErrorMessage errorType) {

        MaskValidator validator = mock(MaskValidator.class);
        doReturn(isPrefixValid).when(validator).isPrefixValid(anyString());
        doReturn(isNetmaskValidFormat).when(validator).isValidNetmaskFormat(Matchers.anyString());
        doReturn(isNetmaskValidValue).when(validator).isNetmaskValid(Matchers.anyString());

        SubnetMaskValidation subnetMaskValidationSpy = createUnderTest(isPrefixAllowed);
        doReturn(validator).when(subnetMaskValidationSpy).getMaskValidator();

        ValidationResult actualResult = subnetMaskValidationSpy.validate(Matchers.anyString());

        final String exceptionMessage =
                String.format("Failed to validate subnet result message: expected: %s\tresult: %s\t for: isPrefixAllowed: %b\tisNetmaskValidFormat: %b\t isMaskValid: %b\t isPrefixValid: %b", //$NON-NLS-1$
                        errorType.name(),
                        actualResult.getReasons().isEmpty() ? ErrorMessage.NoError : actualResult.getReasons().get(0),
                        isPrefixAllowed,
                        isNetmaskValidFormat,
                        isNetmaskValidValue,
                        isPrefixValid);

        if (actualResult.getSuccess()) {
            assertEquals(exceptionMessage,
                    isNetmaskValidValue || isPrefixAllowed && isPrefixValid, actualResult.getReasons().isEmpty());//$NON-NLS-1$
        } else {
            assertEquals(exceptionMessage, errorType.name(), actualResult.getReasons().get(0));//$NON-NLS-1$
        }

    }

    enum ErrorMessage {
        NoError,
        badPrefixOrNetmaskFormatMessage,
        badNetmaskFormatMessage,
        invalidMask
    }

    @Parameterized.Parameters
    public static Collection<Object[]> namesParams() {
        return Arrays.asList(new Object[][] {

                // Bad Format
                { null, false, true }, //$NON-NLS-1$
                { "", false, true }, //$NON-NLS-1$
                { "a.a.a.a", false, true }, //$NON-NLS-1$
                { "255.255.0", false, true }, //$NON-NLS-1$
                { "255.255.0.0.0", false, true }, //$NON-NLS-1$
                { "255.255.0.0.", false, true }, //$NON-NLS-1$

                { "31 ", false, true }, //$NON-NLS-1$ /* note extra space */
                { "31 ", false, false }, //$NON-NLS-1$ /* note extra space */
                { "/31 ", false, true }, //$NON-NLS-1$ /*note extra space*/
                { "31/", false, true }, //$NON-NLS-1$
                { "31*", false, true }, //$NON-NLS-1$
                { "//31 ", false, true }, //$NON-NLS-1$
                { "33", false, true }, //$NON-NLS-1$
                { "33", false, false }, //$NON-NLS-1$
                { "/33", false, true }, //$NON-NLS-1$

                { "01", false, true }, //$NON-NLS-1$
                { "01/", false, true }, //$NON-NLS-1$


                // Not Valid
                { "255.255.0.1", false, true }, //$NON-NLS-1$
                { "255.255.0.1", false, false }, //$NON-NLS-1$
                { "255.0.255.0", false, true }, //$NON-NLS-1$
                { "224.0.255.0", false, true }, //$NON-NLS-1$
                { "255.0.0.255", false, true }, //$NON-NLS-1$


                // Valid
                { "255.255.0.0", true, true }, //$NON-NLS-1$
                { "255.255.0.0", true, false }, //$NON-NLS-1$
                { "255.255.255.255", true, true }, //$NON-NLS-1$

                // prefix supported
                { "31", true, true }, //$NON-NLS-1$
                { "/31", true, true }, //$NON-NLS-1$
                { "2", true, true }, //$NON-NLS-1$
                { "/2", true, true }, //$NON-NLS-1$

                // prefix not supported
                { "31", false, false }, //$NON-NLS-1$
                { "/31", false, false }, //$NON-NLS-1$
                { "2", false, false }, //$NON-NLS-1$
                { "/2", false, false }, //$NON-NLS-1$



        });
    }

}
