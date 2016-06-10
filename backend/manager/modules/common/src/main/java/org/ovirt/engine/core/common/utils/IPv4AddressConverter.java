package org.ovirt.engine.core.common.utils;

public class IPv4AddressConverter implements IPAddressConverter {

    private static IPAddressConverter INSTANCE = new IPv4AddressConverter();

    private IPv4AddressConverter() {
    }

    public static IPAddressConverter getInstance() {
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
    @Override
    public long convertIpAddressToLong(String ipV4Add) {
        String[] octetSubIpArray = ipV4Add.split("\\.");
        long output = 0;
        for (String octet : octetSubIpArray) {
            output = (output << 8) + Integer.parseInt(octet);
        }

        return output;
    }

    /***
     * convert a given {@link Integer} as {@link String} to its IPv4 corresponding representation
     *
     * @param prefix
     *            a {@link String} representation of an {@link Integer}
     * @return the corresponding IPv4 representation of <code>prefix</code>.
     * @throws NumberFormatException in case failed to parse <code>prefix</code> to int.
     */
    @Override
    public String convertPrefixToNetmask(String prefix) {
        prefix = removeLeadingSlashFromNetmaskIfPresent(prefix);
        int prefixAsInt = Integer.parseInt(prefix);
        int mask = prefixAsInt == 0 ? 0 : 0xffffffff ^ (1 << 32 - prefixAsInt) - 1;
        byte[] netmaskByteArray = new byte[] { (byte) (mask >>> 24),
                (byte) (mask >>> 16),
                (byte) (mask >>> 8),
                (byte) mask };
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < netmaskByteArray.length; i++) {
            int octet = 0xFF & netmaskByteArray[i];
            stringBuilder.append(".").append(octet);
        }
        return stringBuilder.substring(1);
    }

    private String removeLeadingSlashFromNetmaskIfPresent(String prefix){
        if (prefix != null && prefix.startsWith("/")){
            return prefix.substring(1);
        }
        return prefix;
    }
}
