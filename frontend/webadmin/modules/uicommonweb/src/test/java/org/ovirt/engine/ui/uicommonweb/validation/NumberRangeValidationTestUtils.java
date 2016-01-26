package org.ovirt.engine.ui.uicommonweb.validation;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;

public abstract class NumberRangeValidationTestUtils<T extends NumberRangeValidation> {

    static final String ABOVE_MAX = "ABOVE_MAX"; //$NON-NLS-1$
    static final String BELLOW_MIN = "BELLOW_MIN"; //$NON-NLS-1$
    static final String NUMBER_RANGE = "NUMBER_RANGE"; //$NON-NLS-1$
    static final String NOT_A_NUMBER = "NOT_A_NUMBER"; //$NON-NLS-1$

    protected T underTest;
    protected Object value;

    public NumberRangeValidationTestUtils(T underTest) {
        this.underTest = spy(underTest);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Before
    public void setup() {
        doReturn(null).when(underTest).getConstManagerInstance();
        doReturn(null).when(underTest).getMessages();
        doReturn(ABOVE_MAX).when(underTest).getNumberAboveMaximumErrorMessage(any(String.class));
        doReturn(BELLOW_MIN).when(underTest).getNumberBellowMinimumErrorMessage(any(String.class));
        doReturn(NUMBER_RANGE).when(underTest).getNumberRangeErrorMessage(any(String.class));
        doReturn(NOT_A_NUMBER).when(underTest).getNumberTypeErrorMessage();

    }

    public void checkAboveMax() {
        runTest(ABOVE_MAX);
    }

    public void checkBellowMinimum() {
        runTest(BELLOW_MIN);
    }

    public void checkNumberRangeOrType() {
        runTest(NOT_A_NUMBER);
    }

    public void checkNullValue() {
        runTest(NOT_A_NUMBER);
    }

    public void checkValidValue() {
        runTest(null, true);
    }

    private void runTest(String expectedErrorMessage) {
        runTest(expectedErrorMessage, false);
    }

    private void runTest(String expectedErrorMessage, boolean expectedValidationValue) {
        ValidationResult actualResult = underTest.validate(getValue());
        ValidationResult expectedResult =
                new ValidationResult(expectedValidationValue,
                        expectedErrorMessage != null ? Arrays.asList(expectedErrorMessage) : new ArrayList<String>(0));
        Assert.assertEquals(expectedResult, actualResult);
    }
}
