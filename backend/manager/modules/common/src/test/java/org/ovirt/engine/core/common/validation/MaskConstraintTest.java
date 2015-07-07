package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
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
        Mockito.doReturn(mockMaskValidator).when(underTest).getMaskValidator();
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
        Assert.assertTrue(underTest.isValid(TEST_MASK, contextMock));
        Mockito.verifyZeroInteractions(contextMock);
    }

    private void runSetup(String testMask, boolean isValidFormat, boolean isMaskValidValue, String errorMessage) {
        Mockito.when(mockMaskValidator.isValidNetmaskFormat(testMask)).thenReturn(isValidFormat);
        Mockito.when(mockMaskValidator.isPrefixValid(testMask)).thenReturn(isMaskValidValue);
        Mockito.when(contextMock.buildConstraintViolationWithTemplate(errorMessage))
                .thenReturn(mockConstraintViolationBuilder);
        Mockito.when(mockConstraintViolationBuilder.addNode(Mockito.anyString()))
                .thenReturn(mockNodeBuilderDefinedContext);
    }

    private void runVerify(String testMask, String errorMessage) {
        Assert.assertFalse(underTest.isValid(testMask, contextMock));
        Mockito.verify(contextMock).disableDefaultConstraintViolation();
        Mockito.verify(contextMock).buildConstraintViolationWithTemplate(errorMessage);
        Mockito.verify(mockConstraintViolationBuilder).addNode("mask");
        Mockito.verify(mockNodeBuilderDefinedContext).addConstraintViolation();
    }

}
