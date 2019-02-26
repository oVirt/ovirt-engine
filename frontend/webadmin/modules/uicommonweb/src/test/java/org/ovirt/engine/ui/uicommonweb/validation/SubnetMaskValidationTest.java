package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Random;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.core.common.validation.IPv4MaskValidator;

public class SubnetMaskValidationTest {
    private SubnetMaskValidation createUnderTest(boolean isPrefixAllowed) {
        SubnetMaskValidation underTest = spy(new SubnetMaskValidation(isPrefixAllowed));
        doReturn(ErrorMessage.invalidMask.name()).when(underTest).getInvalidMask();
        doReturn(ErrorMessage.badNetmaskFormatMessage.name()).when(underTest).getBadNetmaskFormatMessage();
        doReturn(ErrorMessage.badPrefixOrNetmaskFormatMessage.name()).when(underTest).getBadPrefixOrNetmaskFormatMessage();
        return underTest;
    }

    @ParameterizedTest
    @MethodSource
    public void checkValidMask(String mask, boolean isMaskValid, boolean isPrefixAllowed) {
        SubnetMaskValidation underTest = createUnderTest(isPrefixAllowed);
        assertEquals(isMaskValid, underTest.validate(mask).getSuccess(), "Failed to validate mask: " + mask);//$NON-NLS-1$
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

        IPv4MaskValidator validator = mock(IPv4MaskValidator.class);
        doReturn(isPrefixValid).when(validator).isPrefixValid(any());
        doReturn(isNetmaskValidFormat).when(validator).isValidNetmaskFormat(any());
        doReturn(isNetmaskValidValue).when(validator).isNetmaskValid(any());

        SubnetMaskValidation subnetMaskValidationSpy = createUnderTest(isPrefixAllowed);
        doReturn(validator).when(subnetMaskValidationSpy).getMaskValidator();

        ValidationResult actualResult = subnetMaskValidationSpy.validate(any());

        final String exceptionMessage =
                String.format("Failed to validate subnet result message: expected: %s\tresult: %s\t for: isPrefixAllowed: %b\tisNetmaskValidFormat: %b\t isMaskValid: %b\t isPrefixValid: %b", //$NON-NLS-1$
                        errorType.name(),
                        actualResult.getReasons().isEmpty() ? ErrorMessage.NoError : actualResult.getReasons().get(0),
                        isPrefixAllowed,
                        isNetmaskValidFormat,
                        isNetmaskValidValue,
                        isPrefixValid);

        if (actualResult.getSuccess()) {
            assertEquals(isNetmaskValidValue || isPrefixAllowed && isPrefixValid, actualResult.getReasons().isEmpty(),
                    exceptionMessage);
        } else {
            assertEquals(errorType.name(), actualResult.getReasons().get(0), exceptionMessage);
        }

    }

    enum ErrorMessage {
        NoError,
        badPrefixOrNetmaskFormatMessage,
        badNetmaskFormatMessage,
        invalidMask
    }

    public static Stream<Arguments> checkValidMask() {
        return Stream.of(
                // Bad Format
                Arguments.of(null, false, true), //$NON-NLS-1$
                Arguments.of("", false, true), //$NON-NLS-1$
                Arguments.of("a.a.a.a", false, true), //$NON-NLS-1$
                Arguments.of("255.255.0", false, true), //$NON-NLS-1$
                Arguments.of("255.255.0.0.0", false, true), //$NON-NLS-1$
                Arguments.of("255.255.0.0.", false, true), //$NON-NLS-1$

                Arguments.of("31 ", false, true), //$NON-NLS-1$ /* note extra space */
                Arguments.of("31 ", false, false), //$NON-NLS-1$ /* note extra space */
                Arguments.of("/31 ", false, true), //$NON-NLS-1$ /*note extra space*/
                Arguments.of("31/", false, true), //$NON-NLS-1$
                Arguments.of("31*", false, true), //$NON-NLS-1$
                Arguments.of("//31 ", false, true), //$NON-NLS-1$
                Arguments.of("33", false, true), //$NON-NLS-1$
                Arguments.of("33", false, false), //$NON-NLS-1$
                Arguments.of("/33", false, true), //$NON-NLS-1$

                Arguments.of("01", false, true), //$NON-NLS-1$
                Arguments.of("01/", false, true), //$NON-NLS-1$


                // Not Valid
                Arguments.of("255.255.0.1", false, true), //$NON-NLS-1$
                Arguments.of("255.255.0.1", false, false), //$NON-NLS-1$
                Arguments.of("255.0.255.0", false, true), //$NON-NLS-1$
                Arguments.of("224.0.255.0", false, true), //$NON-NLS-1$
                Arguments.of("255.0.0.255", false, true), //$NON-NLS-1$


                // Valid
                Arguments.of("255.255.0.0", true, true), //$NON-NLS-1$
                Arguments.of("255.255.0.0", true, false), //$NON-NLS-1$
                Arguments.of("255.255.255.255", true, true), //$NON-NLS-1$

                // prefix supported
                Arguments.of("31", true, true), //$NON-NLS-1$
                Arguments.of("/31", true, true), //$NON-NLS-1$
                Arguments.of("2", true, true), //$NON-NLS-1$
                Arguments.of("/2", true, true), //$NON-NLS-1$

                // prefix not supported
                Arguments.of("31", false, false), //$NON-NLS-1$
                Arguments.of("/31", false, false), //$NON-NLS-1$
                Arguments.of("2", false, false), //$NON-NLS-1$
                Arguments.of("/2", false, false) //$NON-NLS-1$
        );
    }

}
