package org.ovirt.engine.core.common.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.errors.EngineMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MaskConstraintTest {

    private static final String TEST_MASK = "TEST_MASK";

    @Spy
    private MaskConstraint underTest;

    @Mock
    private IPv4MaskValidator mockIPv4MaskValidator;

    @Mock
    private ConstraintValidatorContext contextMock;

    @Mock
    private ConstraintViolationBuilder mockConstraintViolationBuilder;

    @Mock
    private NodeBuilderDefinedContext mockNodeBuilderDefinedContext;

    @BeforeEach
    public void setup() {
        doReturn(mockIPv4MaskValidator).when(underTest).getMaskValidator();
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
        when(mockIPv4MaskValidator.isValidNetmaskFormat(testMask)).thenReturn(isValidFormat);
        when(mockIPv4MaskValidator.isPrefixValid(testMask)).thenReturn(isMaskValidValue);
        when(contextMock.buildConstraintViolationWithTemplate(errorMessage)).thenReturn(mockConstraintViolationBuilder);
        when(mockConstraintViolationBuilder.addNode(any())).thenReturn(mockNodeBuilderDefinedContext);
    }

    private void runVerify(String testMask, String errorMessage) {
        assertFalse(underTest.isValid(testMask, contextMock));
        verify(contextMock).disableDefaultConstraintViolation();
        verify(contextMock).buildConstraintViolationWithTemplate(errorMessage);
        verify(mockConstraintViolationBuilder).addNode("mask");
        verify(mockNodeBuilderDefinedContext).addConstraintViolation();
    }

}
