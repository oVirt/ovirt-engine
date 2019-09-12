package org.ovirt.engine.core.common.utils;

public final class CertificateUtils {
    /**
     * Creates content of subject alternate name (SAN) of host certificate from entered host name or IP address
     */
    public static String getSan(String hostNameOrIP) {
        boolean isIpAddress = ValidationUtils.isValidIpv4(hostNameOrIP) || ValidationUtils.isValidIpv6(hostNameOrIP);
        return String.format("%s:%s", isIpAddress ? "IP" : "DNS", hostNameOrIP);
    }
}
