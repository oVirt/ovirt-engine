package org.ovirt.engine.core.common.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RunWith(MockitoJUnitRunner.class)
public class MaskConstraintTest {

    private static final String TEST_MASK = "TEST_MASK";

    @Spy
    private MaskConstraint underTest;

    @Mock
    private MaskValidator mockMaskValidator;

    @Mock
    private ConstraintValidatorContext contextMock;

    @Mock
    private ConstraintViolationBuilder mockConstraintViolationBuilder;

    @Mock
    private NodeBuilderDefinedContext mockNodeBuilderDefinedContext;

    @Before
    public void setup() {
        doReturn(mockMaskValidator).when(underTest).getMaskValidator();
    }

    @Test
    public void checkMaskFormatValidation() {
        runSetup(TEST_MASK, false, false, EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_FORMAT.name());
        runVerify(TEST_MASK, EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_FORMAT.name());
    }

    @Test
    public void checkMaskNetworkAddressValidation() {
        runSetup(TEST_MASK, true, false, EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_VALUE.name());
        runVerify(TEST_MASK, EngineMessage.UPDATE_NETWORK_ADDR_IN_SUBNET_BAD_VALUE.name());
    }

    @Test
    public void checkValidMask() {
        runSetup(TEST_MASK, true, true, "");
        assertTrue(underTest.isValid(TEST_MASK, contextMock));
        verifyZeroInteractions(contextMock);
    }

    private void runSetup(String testMask, boolean isValidFormat, boolean isMaskValidValue, String errorMessage) {
        when(mockMaskValidator.isValidNetmaskFormat(testMask)).thenReturn(isValidFormat);
        when(mockMaskValidator.isPrefixValid(testMask)).thenReturn(isMaskValidValue);
        when(contextMock.buildConstraintViolationWithTemplate(errorMessage)).thenReturn(mockConstraintViolationBuilder);
        when(mockConstraintViolationBuilder.addNode(anyString())).thenReturn(mockNodeBuilderDefinedContext);
    }

    private void runVerify(String testMask, String errorMessage) {
        assertFalse(underTest.isValid(testMask, contextMock));
        verify(contextMock).disableDefaultConstraintViolation();
        verify(contextMock).buildConstraintViolationWithTemplate(errorMessage);
        verify(mockConstraintViolationBuilder).addNode("mask");
        verify(mockNodeBuilderDefinedContext).addConstraintViolation();
    }

}
