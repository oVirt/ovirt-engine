package org.ovirt.engine.ui.uicommonweb.validation;

import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

@RunWith(MockitoJUnitRunner.class)
public class KeyValueFormatValidationTest {
    @Mock
    private ConstantsManager mockConstantsManager;
    @Mock
    private UIConstants mockUiConstants;
    @Mock
    private UIMessages mockUiMessages;

    private static final String NOT_VALID = "a"; //$NON-NLS-1$
    private static final String ERROR_MESSAGE_EMPTY_NOT_ALLOWED = "ERROR_MESSAGE_EMPTY_NOT_ALLOWED";//$NON-NLS-1$
    private static final String ERROR_MESSAGE_EMPTY_ALLOWED = "ERROR_MESSAGE_EMPTY_ALLOWED";//$NON-NLS-1$
    private static final String FORMAT = "FORMAT";//$NON-NLS-1$
    private static final ValidationResult failureValidationResultEmptyNotAllowed = new ValidationResult(false,
            Arrays.asList(ERROR_MESSAGE_EMPTY_NOT_ALLOWED));
    private static final ValidationResult failureValidationResultEmptyAllowed = new ValidationResult(false,
            Arrays.asList(ERROR_MESSAGE_EMPTY_ALLOWED));
    private static final ValidationResult successfulValidationResult = new ValidationResult();
    private KeyValueFormatValidation underTest;

    @Before
    public void initialize() {
        when(mockConstantsManager.getMessages()).thenReturn(mockUiMessages);
        when(mockUiMessages.keyValueFormat()).thenReturn(FORMAT);
        when(mockUiMessages.customPropertiesValuesShouldBeInFormatReason(FORMAT)).thenReturn(
                ERROR_MESSAGE_EMPTY_NOT_ALLOWED);
        when(mockUiMessages.emptyOrValidKeyValueFormatMessage(FORMAT)).thenReturn(ERROR_MESSAGE_EMPTY_ALLOWED);
        underTest = new KeyValueFormatValidation(mockConstantsManager);
    }

    @Test
    public void nullTest() {
        ValidationResult validationResult = underTest.validate(null);
        Assert.assertEquals(failureValidationResultEmptyNotAllowed, validationResult);
    }

    @Test
    public void checkEmptyString() {
        ValidationResult validationResult = underTest.validate("");//$NON-NLS-1$
        Assert.assertEquals(failureValidationResultEmptyNotAllowed, validationResult);
    }

    @Test
    public void checkKeyOnly() {
        ValidationResult validationResult = underTest.validate(NOT_VALID);
        Assert.assertEquals(failureValidationResultEmptyNotAllowed, validationResult);
    }

    @Test
    public void checkKeyValuePairBadSeparator() {
        ValidationResult validationResult = underTest.validate("a-a");//$NON-NLS-1$
        Assert.assertEquals(failureValidationResultEmptyNotAllowed, validationResult);
    }

    @Test
    public void checkKeyValuePairExtraEqualSign() {
        ValidationResult validationResult = underTest.validate("a=a=");//$NON-NLS-1$
        Assert.assertEquals(failureValidationResultEmptyNotAllowed, validationResult);
    }

    @Test
    public void checkKeyValueKey() {
        ValidationResult validationResult = underTest.validate("a=a=a");//$NON-NLS-1$
        Assert.assertEquals(failureValidationResultEmptyNotAllowed, validationResult);
    }

    @Test
    public void checkValidKeyValuePair() {
        ValidationResult validationResult = underTest.validate("a=a");//$NON-NLS-1$
        Assert.assertEquals(successfulValidationResult, validationResult);
    }

    @Test
    public void checkValidKeyValueTwoPairs() {
        ValidationResult validationResult = underTest.validate("a=a a=a");//$NON-NLS-1$
        Assert.assertEquals(successfulValidationResult, validationResult);
    }

    @Test
    public void checkKeyValueTwoPairsFollowingWithKey() {
        ValidationResult validationResult = underTest.validate("a=a a=a a");//$NON-NLS-1$
        Assert.assertEquals(failureValidationResultEmptyNotAllowed, validationResult);
    }

    @Test
    public void checkKeyValueTwoPairsFollowingWithMissingValue() {
        ValidationResult validationResult = underTest.validate("a=a a=a a=");//$NON-NLS-1$
        Assert.assertEquals(failureValidationResultEmptyNotAllowed, validationResult);
    }

    @Test
    public void checkEmptyStringAllowed() {
        underTest = new KeyValueFormatValidation(mockConstantsManager, true);
        ValidationResult validationResult = underTest.validate("");//$NON-NLS-1$
        Assert.assertEquals(successfulValidationResult, validationResult);
    }

    @Test
    public void checkNotValidEmptyStringAllowed() {
        underTest = new KeyValueFormatValidation(mockConstantsManager, true);
        ValidationResult validationResult = underTest.validate(NOT_VALID);//$NON-NLS-1$
        Assert.assertEquals(failureValidationResultEmptyAllowed, validationResult);
    }

    @Test
    public void checkEmptyStringNotAllowedFromConstructor() {
        underTest = new KeyValueFormatValidation(mockConstantsManager, false);
        ValidationResult validationResult = underTest.validate("");//$NON-NLS-1$
        Assert.assertEquals(failureValidationResultEmptyNotAllowed, validationResult);
    }
}
