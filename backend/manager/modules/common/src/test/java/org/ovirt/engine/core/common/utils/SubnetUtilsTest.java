package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.validation.IPv4MaskValidator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SubnetUtilsTest {

    private static final String VALID_PREFIX = "VALID_PREFIX";
    private static final String DIFFERENT_PREFIX = "DIFFERENT_PREFIX";
    private static final String VALID_NETMASK_FORMAT = "VALID_NETMASK_FORMAT";
    private static final String INVALID_NETMASK_FORMAT = "INVALID_NETMASK_FORMAT";

    @Spy
    private SubnetUtils underTest = SubnetUtils.getInstance();

    @Mock
    private IPv4MaskValidator mockedIPv4MaskValidator;

    @Mock
    private IPAddressConverter mockIpAddressConverter;

    @BeforeEach
    public void setup() {
        when(underTest.getMaskValidator()).thenReturn(mockedIPv4MaskValidator);
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
        when(mockedIPv4MaskValidator.isValidNetmaskFormat(VALID_NETMASK_FORMAT)).thenReturn(true);
        when(mockedIPv4MaskValidator.isPrefixValid(VALID_PREFIX)).thenReturn(true);
        when(mockIpAddressConverter.convertPrefixToIPv4Netmask(VALID_PREFIX)).thenReturn(VALID_NETMASK_FORMAT);
        assertTrue(underTest.equalSubnet(VALID_NETMASK_FORMAT, VALID_PREFIX));
    }

    @Test
    public void checkDifferentSubnetStringAndIntegerRepresentation() {
        when(mockedIPv4MaskValidator.isValidNetmaskFormat(VALID_NETMASK_FORMAT)).thenReturn(true);
        when(mockedIPv4MaskValidator.isPrefixValid(DIFFERENT_PREFIX)).thenReturn(true);
        when(mockIpAddressConverter.convertPrefixToIPv4Netmask(DIFFERENT_PREFIX)).thenReturn(INVALID_NETMASK_FORMAT);
        assertFalse(underTest.equalSubnet(VALID_NETMASK_FORMAT, DIFFERENT_PREFIX));
    }

    @Test
    public void checkEqualSubnetIntegerAndStringRepresentation() {
        when(mockedIPv4MaskValidator.isValidNetmaskFormat(VALID_NETMASK_FORMAT)).thenReturn(true);
        when(mockedIPv4MaskValidator.isPrefixValid(VALID_PREFIX)).thenReturn(true);
        when(mockIpAddressConverter.convertPrefixToIPv4Netmask(VALID_PREFIX)).thenReturn(VALID_NETMASK_FORMAT);
        assertTrue(underTest.equalSubnet(VALID_PREFIX, VALID_NETMASK_FORMAT));
    }

    @Test
    public void checkDifferentSubnetIntegerAndStringRepresentation() {
        when(mockedIPv4MaskValidator.isValidNetmaskFormat(VALID_NETMASK_FORMAT)).thenReturn(true);
        when(mockedIPv4MaskValidator.isPrefixValid(DIFFERENT_PREFIX)).thenReturn(true);
        when(mockIpAddressConverter.convertPrefixToIPv4Netmask(DIFFERENT_PREFIX)).thenReturn(INVALID_NETMASK_FORMAT);
        assertFalse(underTest.equalSubnet(DIFFERENT_PREFIX, VALID_NETMASK_FORMAT));
    }
}
