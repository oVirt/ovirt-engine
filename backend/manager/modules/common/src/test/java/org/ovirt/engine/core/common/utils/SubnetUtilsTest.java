package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.validation.MaskValidator;

@RunWith(MockitoJUnitRunner.class)
public class SubnetUtilsTest {

    private static final String VALID_PREFIX = "VALID_PREFIX";
    private static final String DIFFERENT_PREFIX = "DIFFERENT_PREFIX";
    private static final String VALID_NETMASK_FORMAT = "VALID_NETMASK_FORMAT";
    private static final String INVALID_NETMASK_FORMAT = "INVALID_NETMASK_FORMAT";

    @Spy
    private SubnetUtils underTest = SubnetUtils.getInstance();

    @Mock
    private MaskValidator mockedMaskValidator;

    @Mock
    private IPAddressConverter mockIpAddressConverter;

    @Before
    public void setup() {
        when(underTest.getMaskValidator()).thenReturn(mockedMaskValidator);
        when(underTest.getIpAddressConverter()).thenReturn(mockIpAddressConverter);
    }

    @Test
    public void checkEqualNetmaskRepresentation(){
        assertTrue(underTest.equalSubnet("255.255.255.255", "255.255.255.255"));
    }

    @Test
    public void checkEqualPrefixRepresentation(){
        assertTrue(underTest.equalSubnet("8", "8"));
    }

    @Test
    public void checkNullSubnet() {
        assertTrue(underTest.equalSubnet(null, null));
    }

    @Test
    public void checkEqualSubnetStringAndIntegerRepresentation() {
        when(mockedMaskValidator.isValidNetmaskFormat(VALID_NETMASK_FORMAT)).thenReturn(true);
        when(mockedMaskValidator.isPrefixValid(VALID_PREFIX)).thenReturn(true);
        when(mockIpAddressConverter.convertPrefixToNetmask(VALID_PREFIX)).thenReturn(VALID_NETMASK_FORMAT);
        assertTrue(underTest.equalSubnet(VALID_NETMASK_FORMAT, VALID_PREFIX));
    }

    @Test
    public void checkDifferentSubnetStringAndIntegerRepresentation() {
        when(mockedMaskValidator.isValidNetmaskFormat(VALID_NETMASK_FORMAT)).thenReturn(true);
        when(mockedMaskValidator.isPrefixValid(DIFFERENT_PREFIX)).thenReturn(true);
        when(mockIpAddressConverter.convertPrefixToNetmask(DIFFERENT_PREFIX)).thenReturn(INVALID_NETMASK_FORMAT);
        assertFalse(underTest.equalSubnet(VALID_NETMASK_FORMAT, DIFFERENT_PREFIX));
    }

    @Test
    public void checkEqualSubnetIntegerAndStringRepresentation() {
        when(mockedMaskValidator.isValidNetmaskFormat(VALID_NETMASK_FORMAT)).thenReturn(true);
        when(mockedMaskValidator.isPrefixValid(VALID_PREFIX)).thenReturn(true);
        when(mockIpAddressConverter.convertPrefixToNetmask(VALID_PREFIX)).thenReturn(VALID_NETMASK_FORMAT);
        assertTrue(underTest.equalSubnet(VALID_PREFIX, VALID_NETMASK_FORMAT));
    }

    @Test
    public void checkDifferentSubnetIntegerAndStringRepresentation() {
        when(mockedMaskValidator.isValidNetmaskFormat(VALID_NETMASK_FORMAT)).thenReturn(true);
        when(mockedMaskValidator.isPrefixValid(DIFFERENT_PREFIX)).thenReturn(true);
        when(mockIpAddressConverter.convertPrefixToNetmask(DIFFERENT_PREFIX)).thenReturn(INVALID_NETMASK_FORMAT);
        assertFalse(underTest.equalSubnet(DIFFERENT_PREFIX, VALID_NETMASK_FORMAT));
    }
}
