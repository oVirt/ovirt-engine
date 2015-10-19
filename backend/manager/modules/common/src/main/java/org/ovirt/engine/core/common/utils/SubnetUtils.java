package org.ovirt.engine.core.common.utils;

import java.util.Objects;

import org.ovirt.engine.core.common.validation.MaskValidator;

public class SubnetUtils {
    private static SubnetUtils INSTANCE = new SubnetUtils();

    private SubnetUtils() {
    }

    public static SubnetUtils getInstance() {
        return INSTANCE;
    }

    /***
     * Check if two subnet represent the same value regardless of each prefix or netmask representation
     * @param subnetA
     *            valid prefix or netmask format
     * @param subnetB
     *            valid prefix or netmask format
     * @return true if both has equals subnet value false otherwise
     */
    public boolean equalSubnet(String subnetA, String subnetB) {
        if (Objects.equals(subnetA, subnetB)) {
            return true;
        } else if (validNetmaskFormat(subnetA) && validPrefixFormat(subnetB)) {
            final String subnetBAsPrefix = convertStringToIpv4Address(subnetB);
            return subnetBAsPrefix.equals(subnetA);
        } else if (validNetmaskFormat(subnetB) && validPrefixFormat(subnetA)) {
            final String subnetAAsPrefix = convertStringToIpv4Address(subnetA);
            return subnetAAsPrefix.equals(subnetB);
        }
        return false;
    }

    protected String convertStringToIpv4Address(String subnetB) {
        return getIpAddressConverter().convertPrefixToNetmask(subnetB);
    }

    protected IPAddressConverter getIpAddressConverter() {
        return IPv4AddressConverter.getInstance();
    }

    protected boolean validNetmaskFormat(String subnet) {
        return getMaskValidator().isValidNetmaskFormat(subnet);
    }

    protected boolean validPrefixFormat(String subnet) {
        return getMaskValidator().isPrefixValid(subnet);
    }

    protected MaskValidator getMaskValidator() {
        return MaskValidator.getInstance();
    }

}
