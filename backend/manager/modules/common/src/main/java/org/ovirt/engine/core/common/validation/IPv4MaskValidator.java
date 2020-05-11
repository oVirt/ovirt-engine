package org.ovirt.engine.core.common.validation;

import java.math.BigInteger;

import org.ovirt.engine.core.common.utils.IPAddressConverter;
import org.ovirt.engine.core.common.utils.ValidationUtils;

public class IPv4MaskValidator {
    private IPAddressConverter ipAddressConverter = IPAddressConverter.getInstance();

    private static IPv4MaskValidator INSTANCE = new IPv4MaskValidator();

    private IPv4MaskValidator() {
    }

    public static IPv4MaskValidator getInstance() {
        return INSTANCE;
    }

    /***
     * Check if mask is in IPv4 format: x.x.x.x where:
     * <ul>
     * <li>x belongs to [0,255]
     * </ul>
     * <p>
     * <b>Note!</b> the function is not validating that mask value is valid, please see @see
     * {@link IPv4MaskValidator#isNetmaskValid(String)} (String)}
     *
     * @return true if correct IPv4 format , false otherwise.
     */
    public boolean isValidNetmaskFormat(String mask) {
        return mask != null && mask.matches("^" + ValidationUtils.IPV4_PATTERN_NON_EMPTY + "$");
    }

    /***
     * Check if mask is a string [0-32] (possible with / prefix)
     *
     * @return true if a string [0-32] (possible with /) which represent a valid prefix, false otherwise.
     */
    public boolean isPrefixValid(String mask) {
        return mask != null && mask.matches("^/?" + ValidationUtils.IPV4_SUBNET_PREFIX_PATTERN + "$");
    }

    /***
     * check if mask is valid and netmasked formated and return true if does
     *
     * @param netmask in valid format , please verify first with @see {@link IPv4MaskValidator#isValidNetmaskFormat(String)}
     * @return true if the netmask is in IPv4 format and valid, false otherwise
     */
    public boolean isNetmaskValid(String netmask) {
        BigInteger addressInBits = ipAddressConverter.convertIpAddressToBigInt(netmask);
        BigInteger mask = BigInteger.ONE;
        boolean isFirstOneFound = false;

        for (int i = 0; i < 32; i++) {
            if (isFirstOneFound && !addressInBits.and(mask).equals(BigInteger.ONE)) {
                return false;
            }

            if (addressInBits.and(mask).equals(BigInteger.ONE)) {
                isFirstOneFound = true;
            }

            addressInBits = addressInBits.shiftRight(1);
        }

        return true;
    }

    /**
     * Mask in valid format. It is assumed that values passed to this function have been validated first with
     * {@link IPv4MaskValidator#isValidNetmaskFormat(String)} or with {@link IPv4MaskValidator#isPrefixValid(String)}
     * @param mask as octet-format netmask or as CIDR format prefix
     * @return the octet-formatted netmask
     * @apiNote Octet-format is a xxxx.xxxx.xxxx.xxxx style address where x belongs to [0,255]
     * @apiNote CIDR format prefix is an integer in the range [0,32]
     */
    String getOctetNetmask(String mask) {
        String netmask = "";
        if (isPrefixValid(mask)) {
            netmask = ipAddressConverter.convertPrefixToIPv4Netmask(mask);
        } else if (isValidNetmaskFormat(mask)) {
            netmask = mask;
        }
        return netmask;
    }
}
