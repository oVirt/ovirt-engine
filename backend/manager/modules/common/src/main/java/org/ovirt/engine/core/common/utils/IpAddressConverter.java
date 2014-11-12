package org.ovirt.engine.core.common.utils;

public class IpAddressConverter {

    private static IpAddressConverter INSTANCE = new IpAddressConverter();

    private IpAddressConverter() {
    }

    public static IpAddressConverter getInstance() {
        return INSTANCE;
    }

    /***
     * convert a given valid address IPv4 x1.x2.x3.x4 to long.
     * <ul>
     * <li>x_i belongs to [0,255]
     * </ul>
     *
     * @param ipV4Add
     *            in valid x1.x2.x3.x4 format
     * @return the IPv4 value, represented as long, where x4 is the LSB of the returned long
     */
    public long convertIpToLong(String ipV4Add) {
        String[] octetSubIpArray = ipV4Add.split("\\.");
        long output = 0;
        for (String octet : octetSubIpArray) {
            output = (output << 8) + Integer.parseInt(octet);
        }

        return output;
    }

}
