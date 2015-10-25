package org.ovirt.engine.ui.uicommonweb.validation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.validation.CidrValidator;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

@RunWith(MockitoJUnitRunner.class)
public class CidrValidationTest {
    private static final String BAD_CIDR_FORMAT = "BAD_CIDR_FORMAT"; //$NON-NLS-1$
    private static final String CIDR_IS_NOT_A_NETWORK_ADDRESS = "CIDR_IS_NOT_A_NETWORK_ADDRESS"; //$NON-NLS-1$
    private static final String CIDR = "CIDR"; //$NON-NLS-1$

    @Spy
    private CidrValidation underTest;

    @Mock
    private ConstantsManager mockedConstantsManager;

    @Mock
    private UIConstants mockedUiConstants;

    @Mock
    private CidrValidator mockedCidrValidator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        underTest = spy(new CidrValidation());
        doReturn(mockedCidrValidator).when(underTest).getCidrValidator();
        doReturn(mockedConstantsManager).when(underTest).getConstantsManager();
        doReturn(mockedUiConstants).when(mockedConstantsManager).getConstants();
        doReturn(BAD_CIDR_FORMAT).when(underTest).getThisFieldMustContainCidrInFormatMsg();
        doReturn(CIDR_IS_NOT_A_NETWORK_ADDRESS).when(underTest).getCidrNotNetworkAddress();
    }

    @Test
    public void checkCidrBadFormat() {
        doReturn(false).when(mockedCidrValidator).isCidrFormatValid(CIDR);
        ValidationResult actualResult = underTest.validate(CIDR);
        ValidationResult expectedResult = new ValidationResult(false, Arrays.asList(BAD_CIDR_FORMAT));
        assertEquals(expectedResult, actualResult);

    }

    @Test
    public void checkCidrCidrIsNotANetworkAddress() {
        doReturn(true).when(mockedCidrValidator).isCidrFormatValid(CIDR);
        doReturn(false).when(mockedCidrValidator).isCidrNetworkAddressValid(CIDR);
        ValidationResult actualResult = underTest.validate(CIDR);
        ValidationResult expectedResult = new ValidationResult(false, Arrays.asList(CIDR_IS_NOT_A_NETWORK_ADDRESS));
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void checkValidCidr() {
        doReturn(true).when(mockedCidrValidator).isCidrFormatValid(CIDR);
        doReturn(true).when(mockedCidrValidator).isCidrNetworkAddressValid(CIDR);
        ValidationResult actualResult = underTest.validate(CIDR);
        ValidationResult expectedResult = new ValidationResult();
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void checkStringInputAssertion() {
        expectedException.expectMessage(CidrValidation.ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE);
        expectedException.expect(IllegalArgumentException.class);
        ValidationResult actualResult = underTest.validate(new Object());
    }
}
