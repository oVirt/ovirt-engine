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
     * Check if CIDR is in correct format: x.x.x.x/y where:
     * <ul>
     * <li>x belongs to [0,255]
     * <li>y belongs to [0,32]
     * <li>both inclusive
     * </ul>
     * <p>
     * <b>Note!</b> the function is not validating that IP and mask match to a network address, please see
     * {@link CidrValidator#isCidrNetworkAddressValid(String)}.
     *
     * @return true if correct format, false otherwise.
     */
    public boolean isCidrFormatValid(String cidr) {
        return cidr != null && cidr.matches(ValidationUtils.CIDR_FORMAT_PATTERN);
    }

    /***
     * check if CIDR represent valid network add
     *
     * @param cidr
     *            in correct format, please use the following function first: @see
     *            {@link CidrValidator#isCidrFormatValid(String)}
     * @return true if valid CIDR ,false otherwise
     */
    public boolean isCidrNetworkAddressValid(String cidr) {
        String[] temp = cidr.split("/");
        BigInteger ipAsInteger = ipAddressConverter.convertIpAddressToBigInt(temp[0]);
        int mask = Integer.parseInt(temp[1]);
        return isNetworkAddress(ipAsInteger, mask);
    }

    private static boolean isNetworkAddress(BigInteger ip, int mask) {
        BigInteger prefix = BigInteger.valueOf(2).pow(32 - mask).subtract(BigInteger.ONE);
        return (prefix.and(ip)).equals(BigInteger.ZERO);
    }

}
