package org.ovirt.engine.core.common.validation;

import java.math.BigInteger;

import org.ovirt.engine.core.common.utils.IPAddressConverter;
import org.ovirt.engine.core.common.utils.ValidationUtils;

public class CidrValidator {

    private IPAddressConverter ipAddressConverter = IPAddressConverter.getInstance();

    private static CidrValidator cidrValidator = new CidrValidator();

    private CidrValidator() {
    }

    public static CidrValidator getInstance() {
        return cidrValidator;
    }

    /***
     * Check if CIDR is in correct format: [IPv4/IPv6]/y where:
     * <ul>
     * <li>y belongs to [0,32] for IPv4 and [0,128] for IPv6
     * <li>both inclusive
     * </ul>
     * <p>
     * <b>Note!</b> the function is not validating that IP and mask match to a network address, please see
     * {@link CidrValidator#isCidrNetworkAddressValid(String)}.
     *
     * @return true if correct format, false otherwise.
     */

    public boolean isCidrFormatValid(String cidr, boolean isIpv4) {
        return cidr != null && ((isIpv4 && cidr.matches(ValidationUtils.IPV4_CIDR_FORMAT_PATTERN))
                || !isIpv4 && cidr.matches(ValidationUtils.IPV6_CIDR_FORMAT_PATTERN));
    }

    /***
     * check if CIDR represent valid network add
     *
     * @param cidr
     *            in correct format, please use the following function first: @see
     *            {@link CidrValidator#isCidrFormatValid(String)}
     * @return true if valid CIDR ,false otherwise
     */
    public boolean isCidrNetworkAddressValid(String cidr, boolean isIpv4) {
        String[] temp = cidr.split("/");
        BigInteger ipAsInteger = ipAddressConverter.convertIpAddressToBigInt(temp[0]);
        int mask = Integer.parseInt(temp[1]);
        return isNetworkAddress(ipAsInteger, mask, isIpv4);
    }

    private static boolean isNetworkAddress(BigInteger ip, int mask, boolean isIpv4) {
        int maxPrefix = isIpv4 ? 32 : 128;
        BigInteger prefix = BigInteger.valueOf(2).pow(maxPrefix - mask).subtract(BigInteger.ONE);
        return (prefix.and(ip)).equals(BigInteger.ZERO);
    }

}
