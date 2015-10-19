package org.ovirt.engine.core.common.utils;

public interface IPAddressConverter {
    long convertIpAddressToLong(String ipV4Add);

    String convertPrefixToNetmask(String prefix);
}
