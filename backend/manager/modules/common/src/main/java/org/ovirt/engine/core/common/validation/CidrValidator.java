package org.ovirt.engine.core.common.validation;

import org.ovirt.engine.core.common.utils.ValidationUtils;

public class CidrValidator {

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
     * <b>Note!</b> the function is not validating that IP and mask match to a network address, please see @see
     * {@link CidrValidator#isCidrFormatValid(String)}
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
        long ipAsInteger = covnertIpToInt(temp[0]);
        int mask = Integer.parseInt(temp[1]);
        return isNetworkAddress(ipAsInteger, mask);
    }

    private static long covnertIpToInt(String ipAdd) {
        String[] subAdd = ipAdd.split("\\.");
        long output = 0;
        long temp;
        for (int index = 3; index > -1; index--) {
            temp = Integer.parseInt(subAdd[3 - index]);
            temp <<= (index * 8);
            output |= temp;
        }

        return output;
    }

    private static boolean isNetworkAddress(long ip, int mask) {
        long postfix = (long) Math.pow(2, 32 - mask) - 1;
        return (ip & postfix) == 0;
    }

}
