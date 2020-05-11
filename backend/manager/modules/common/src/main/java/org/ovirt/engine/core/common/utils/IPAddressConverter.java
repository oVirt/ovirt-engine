package org.ovirt.engine.core.common.utils;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IPAddressConverter {

    private static final String EMPTY_STRING = "";
    private static final String ZERO_STRING = "0";
    private static final String COLON = ":";

    private static IPAddressConverter INSTANCE = new IPAddressConverter();

    private final BigInteger FULL_MASK_BIG_INT;

    private IPAddressConverter() {
        FULL_MASK_BIG_INT = convertIpAddressToBigInt("255.255.255.255");
    }

    public static IPAddressConverter getInstance() {
        return INSTANCE;
    }

    /**
     * All arguments should be ipv4 address strings in octet format: xxxx.xxxx.xxxx.xxxx
     * @param subnetIp an ip to define a subnet along with the subnetMask.
     * @param subnetMask a netmask to define a subnet along with the subnetIp.
     * @return true if the ipToTest is one of the subnet addresses (not including the network or broadcast address)
     */
    public Ipv4NetworkRange createIpv4NetworkRange(String subnetIp, String subnetMask) {
        BigInteger subnetIpInt = convertIpAddressToBigInt(subnetIp);
        BigInteger subnetMaskInt = convertIpAddressToBigInt(subnetMask);
        BigInteger networkAddressInt = subnetMaskInt.and(subnetIpInt);
        BigInteger hostMin = networkAddressInt.add(BigInteger.ONE);

        BigInteger wildcard = subnetMaskInt.xor(FULL_MASK_BIG_INT);
        BigInteger broadcastAddressInt = networkAddressInt.add(wildcard);
        BigInteger hostMax = broadcastAddressInt.subtract(BigInteger.ONE);

        return new Ipv4NetworkRange(hostMin, hostMax);
    }

    public static class Ipv4NetworkRange {

        private BigInteger hostMin;
        private BigInteger hostMax;

        Ipv4NetworkRange() {
        }

        Ipv4NetworkRange(BigInteger hostMin, BigInteger hostMax) {
            this.hostMin = hostMin;
            this.hostMax = hostMax;
        }

        public boolean isInRange(BigInteger ipToTest) {
            return ipToTest.compareTo(hostMin) >= 0 && ipToTest.compareTo(hostMax) <= 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Ipv4NetworkRange)) {
                return false;
            }
            Ipv4NetworkRange that = (Ipv4NetworkRange) o;
            return Objects.equals(hostMin, that.hostMin) && Objects.equals(hostMax, that.hostMax);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hostMin, hostMax);
        }
    }

        /***
     * convert a given valid IP address to BigInteger.
     *
     *
     * @param ipAddr
     *            in valid format
     * @return the IP value, represented as big integer, in network order (big endian)
     * @throws RuntimeException if the ipAddr is invalid
     */
    public BigInteger convertIpAddressToBigInt(String ipAddr) {
        if (ipAddr.matches(ValidationUtils.IPV4_PATTERN)) {
            return new BigInteger(convertIpv4AddressToBytes(ipAddr));
        } else if (ipAddr.matches(ValidationUtils.IPV6_PATTERN)) {
            return new BigInteger(expandIpv6String(ipAddr), 16);
        }
        // This should not happen as the string that is going to be converted should be already validated
        return null;
    }

    private byte[] convertIpv4AddressToBytes(String ipv4Addr) {
        Byte[] boxedBytes = Stream.of(ipv4Addr.split("\\."))
                .map(Integer::valueOf)
                .map(Integer::byteValue)
                .toArray(Byte[]::new);
        byte[] bytes = new byte[boxedBytes.length];
        for (int i = 0; i < boxedBytes.length; i++) {
            bytes[i] = boxedBytes[i];
        }
        return bytes;
    }

    private String expandIpv6String(String ipv6Addr) {
        List<String> list = Stream.of(ipv6Addr.split(COLON, -1))
                .collect(Collectors.toList());

        int emptyIndex = list.indexOf(EMPTY_STRING);
        if (emptyIndex >= 0) {
            list = list.stream()
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            int expandCount = 8 - list.size();
            for (int i = 0; i < expandCount; i++) {
                list.add(emptyIndex, ZERO_STRING);
            }
        }
        return list.stream()
                .map(this::prependZeroes)
                .collect(Collectors.joining());
    }

    private String prependZeroes(String twoBytes) {
        StringBuilder builder = new StringBuilder();
        int zeroLen = 4 - twoBytes.length();
        for (int i = 0; i < zeroLen; i++) {
            builder.append(ZERO_STRING);
        }
        builder.append(twoBytes);
        return builder.toString();
    }

    /***
     * convert a given {@link Integer} as {@link String} to its IPv4 corresponding representation
     *
     * @param prefix
     *            a {@link String} representation of an {@link Integer}
     * @return the corresponding IPv4 representation of <code>prefix</code>.
     * @throws NumberFormatException in case failed to parse <code>prefix</code> to int.
     */
    public String convertPrefixToIPv4Netmask(String prefix) {
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
