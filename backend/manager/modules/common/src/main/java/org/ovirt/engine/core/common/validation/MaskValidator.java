package org.ovirt.engine.core.common.validation;

import org.ovirt.engine.core.common.utils.IPAddressConverter;
import org.ovirt.engine.core.common.utils.IPv4AddressConverter;
import org.ovirt.engine.core.common.utils.ValidationUtils;

public class MaskValidator {
    private IPAddressConverter ipAddressConverter = IPv4AddressConverter.getInstance();

    private static MaskValidator INSTANCE = new MaskValidator();

    private MaskValidator() {
    }

    public static MaskValidator getInstance() {
        return INSTANCE;
    }

    /***
     * Check if mask is in IPv4 format: x.x.x.x where:
     * <ul>
     * <li>x belongs to [0,255]
     * </ul>
     * <p>
     * <b>Note!</b> the function is not validating that mask value is valid, please see @see
     * {@link MaskValidator#isNetmaskValid(String)} (String)}
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
        return mask != null && mask.matches("^/?" + ValidationUtils.SUBNET_PREFIX_PATTERN + "$");
    }

    /***
     * check if mask is valid and netmasked formated and return true if does
     *
     * @param netmask in valid format , please verify first with @see {@link MaskValidator#isValidNetmaskFormat(String)}
     * @return true if the netmask is in IPv4 format and valid, false otherwise
     */
    public boolean isNetmaskValid(String netmask) {
        long addressInBits = ipAddressConverter.convertIpAddressToLong(netmask);
        long mask = 1;
        boolean isFirstOneFound = false;

        for (int i = 0; i < 32; i++) {
            if (isFirstOneFound && (mask & addressInBits) != 1) {
                return false;
            }

            if ((mask & addressInBits) == 1) {
                isFirstOneFound = true;
            }

            addressInBits >>= 1;
        }

        return true;
    }

}
