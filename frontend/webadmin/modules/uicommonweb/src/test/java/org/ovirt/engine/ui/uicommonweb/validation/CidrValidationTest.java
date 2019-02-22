package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.validation.CidrValidator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CidrValidationTest {
    private static final String BAD_CIDR_FORMAT = "BAD_CIDR_FORMAT"; //$NON-NLS-1$
    private static final String CIDR_IS_NOT_A_NETWORK_ADDRESS = "CIDR_IS_NOT_A_NETWORK_ADDRESS"; //$NON-NLS-1$
    private static final String CIDR = "CIDR"; //$NON-NLS-1$

    private CidrValidation underTest;

    @Mock
    private CidrValidator mockedCidrValidator;

    @BeforeEach
    public void setup() {
        underTest = spy(new CidrValidation(true));
        doReturn(mockedCidrValidator).when(underTest).getCidrValidator();
        doReturn(BAD_CIDR_FORMAT).when(underTest).getThisFieldMustContainCidrInFormatMsg();
        doReturn(CIDR_IS_NOT_A_NETWORK_ADDRESS).when(underTest).getCidrNotNetworkAddress();
    }

    @Test
    public void checkCidrBadFormat() {
        doReturn(false).when(mockedCidrValidator).isCidrFormatValid(CIDR, true);
        ValidationResult actualResult = underTest.validate(CIDR);
        ValidationResult expectedResult = new ValidationResult(false, Collections.singletonList(BAD_CIDR_FORMAT));
        assertEquals(expectedResult, actualResult);

    }

    @Test
    public void checkCidrCidrIsNotANetworkAddress() {
        doReturn(true).when(mockedCidrValidator).isCidrFormatValid(CIDR, true);
        doReturn(false).when(mockedCidrValidator).isCidrNetworkAddressValid(CIDR, true);
        ValidationResult actualResult = underTest.validate(CIDR);
        ValidationResult expectedResult = new ValidationResult(false, Collections.singletonList(CIDR_IS_NOT_A_NETWORK_ADDRESS));
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void checkValidCidr() {
        doReturn(true).when(mockedCidrValidator).isCidrFormatValid(CIDR, true);
        doReturn(true).when(mockedCidrValidator).isCidrNetworkAddressValid(CIDR, true);
        ValidationResult actualResult = underTest.validate(CIDR);
        ValidationResult expectedResult = new ValidationResult();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void checkStringInputAssertion() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> underTest.validate(new Object()));
        assertEquals(CidrValidation.ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE, e.getMessage());
    }
}
