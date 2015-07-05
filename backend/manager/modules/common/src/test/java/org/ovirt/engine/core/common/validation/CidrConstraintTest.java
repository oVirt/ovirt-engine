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
public class CidrConstraintTest {

    private static final String TEST_CIDR = "TEST_CIDR";

    @Spy
    private CidrConstraint underTest;

    @Mock
    private ConstraintValidatorContext contextMock;

    @Mock
    private ConstraintViolationBuilder mockConstraintViolationBuilder;

    @Mock
    private NodeBuilderDefinedContext mockNodeBuilderDefinedContext;

    @Mock
    private CidrValidator mockCidrValidator;

    @Before
    public void setup() {
        Mockito.doReturn(mockCidrValidator).when(underTest).getCidrValidator();
    }

    @Test
    public void checkCidrFormatValidation() {
        runSetup(TEST_CIDR, false, false, EngineMessage.BAD_CIDR_FORMAT.name());
        runVerify(TEST_CIDR, EngineMessage.BAD_CIDR_FORMAT.name());
    }

    @Test
    public void checkCidrNetworkAddressValidation() {
        runSetup(TEST_CIDR, true, false, EngineMessage.CIDR_NOT_NETWORK_ADDRESS.name());
        runVerify(TEST_CIDR, EngineMessage.CIDR_NOT_NETWORK_ADDRESS.name());
    }

    @Test
    public void checkValidCidr() {
        runSetup(TEST_CIDR, true, true, "");
        Assert.assertTrue(underTest.isValid(TEST_CIDR, contextMock));
        Mockito.verifyZeroInteractions(contextMock);
    }

    private void runSetup(String testCidr, boolean isValidFormat, boolean isCidrNetworkAddressValid, String errorMessage) {
        Mockito.when(mockCidrValidator.isCidrFormatValid(testCidr)).thenReturn(isValidFormat);
        Mockito.when(mockCidrValidator.isCidrNetworkAddressValid(testCidr)).thenReturn(isCidrNetworkAddressValid);
        Mockito.when(contextMock.buildConstraintViolationWithTemplate(errorMessage))
                .thenReturn(mockConstraintViolationBuilder);
        Mockito.when(mockConstraintViolationBuilder.addNode(Mockito.anyString()))
                .thenReturn(mockNodeBuilderDefinedContext);
    }

    private void runVerify(String testCidr, String errorMessage) {
        Assert.assertFalse(underTest.isValid(testCidr, contextMock));
        Mockito.verify(contextMock).disableDefaultConstraintViolation();
        Mockito.verify(contextMock).buildConstraintViolationWithTemplate(errorMessage);
        Mockito.verify(mockConstraintViolationBuilder).addNode("cidr");
        Mockito.verify(mockNodeBuilderDefinedContext).addConstraintViolation();
    }

}
