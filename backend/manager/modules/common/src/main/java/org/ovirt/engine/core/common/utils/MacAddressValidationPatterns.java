package org.ovirt.engine.core.common.utils;


public abstract class MacAddressValidationPatterns {

    private static final String ODD_HEX_DIGIT_PATTERN = "[13579BbDdFf]";
    private static final String EVEN_HEX_DIGIT_PATTERN = "[02468AaCcEe]";

    public static final String UNICAST_MAC_ADDRESS_FORMAT = "\\p{XDigit}" + EVEN_HEX_DIGIT_PATTERN
                                                            + "(:\\p{XDigit}{2}){5}";

    public static final String NON_MULTICAST_MAC_ADDRESS_FORMAT =
            ".*(?<!^\\p{XDigit}" + ODD_HEX_DIGIT_PATTERN + "(:\\p{XDigit}{2}){5}$)";
    public static final String VALID_MAC_ADDRESS_FORMAT =
            "^(\\p{XDigit}{2}:){5}\\p{XDigit}{2}$";

    public static final String NON_NULLABLE_MAC_ADDRESS_FORMAT = "^.*(?<!(00:){5}00)$";

}
