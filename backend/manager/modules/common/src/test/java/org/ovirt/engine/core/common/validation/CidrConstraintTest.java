package org.ovirt.engine.core.common.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
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
        doReturn(mockCidrValidator).when(underTest).getCidrValidator();
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
        assertTrue(underTest.isValid(TEST_CIDR, contextMock));
        verifyZeroInteractions(contextMock);
    }

    private void runSetup(String testCidr, boolean isValidFormat, boolean isCidrNetworkAddressValid, String errorMessage) {
        when(mockCidrValidator.isCidrFormatValid(testCidr)).thenReturn(isValidFormat);
        when(mockCidrValidator.isCidrNetworkAddressValid(testCidr)).thenReturn(isCidrNetworkAddressValid);
        when(contextMock.buildConstraintViolationWithTemplate(errorMessage)).thenReturn(mockConstraintViolationBuilder);
        when(mockConstraintViolationBuilder.addNode(anyString())).thenReturn(mockNodeBuilderDefinedContext);
    }

    private void runVerify(String testCidr, String errorMessage) {
        assertFalse(underTest.isValid(testCidr, contextMock));
        verify(contextMock).disableDefaultConstraintViolation();
        verify(contextMock).buildConstraintViolationWithTemplate(errorMessage);
        verify(mockConstraintViolationBuilder).addNode("cidr");
        verify(mockNodeBuilderDefinedContext).addConstraintViolation();
    }

}
