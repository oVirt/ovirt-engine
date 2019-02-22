package org.ovirt.engine.core.common.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderDefinedContext;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.errors.EngineMessage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CidrConstraintTest {

    private static ExternalSubnet testedSubnet;

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

    @BeforeAll
    public static void setupExternalSubnet() {
        testedSubnet = new ExternalSubnet();
        testedSubnet.setIpVersion(ExternalSubnet.IpVersion.IPV4);
    }

    @BeforeEach
    public void setup() {
        doReturn(mockCidrValidator).when(underTest).getCidrValidator();
    }

    @Test
    public void checkCidrFormatValidation() {
        runSetup(false, false, EngineMessage.BAD_CIDR_FORMAT.name());
        runVerify(testedSubnet, EngineMessage.BAD_CIDR_FORMAT.name());
    }

    @Test
    public void checkCidrNetworkAddressValidation() {
        runSetup(true, false, EngineMessage.CIDR_NOT_NETWORK_ADDRESS.name());
        runVerify(testedSubnet, EngineMessage.CIDR_NOT_NETWORK_ADDRESS.name());
    }

    @Test
    public void checkValidCidr() {
        runSetup(true, true, "");
        assertTrue(underTest.isValid(testedSubnet, contextMock));
        verifyZeroInteractions(contextMock);
    }

    private void runSetup(boolean isValidFormat, boolean isCidrNetworkAddressValid, String errorMessage) {
        when(mockCidrValidator.isCidrFormatValid(any(), anyBoolean())).thenReturn(isValidFormat);
        when(mockCidrValidator.isCidrNetworkAddressValid(any(), anyBoolean())).thenReturn(isCidrNetworkAddressValid);
        when(contextMock.buildConstraintViolationWithTemplate(errorMessage)).thenReturn(mockConstraintViolationBuilder);
        when(mockConstraintViolationBuilder.addNode(any())).thenReturn(mockNodeBuilderDefinedContext);
    }

    private void runVerify(ExternalSubnet testCidr, String errorMessage) {
        assertFalse(underTest.isValid(testCidr, contextMock));
        verify(contextMock).disableDefaultConstraintViolation();
        verify(contextMock).buildConstraintViolationWithTemplate(errorMessage);
        verify(mockConstraintViolationBuilder).addNode("cidr");
        verify(mockNodeBuilderDefinedContext).addConstraintViolation();
    }

}
