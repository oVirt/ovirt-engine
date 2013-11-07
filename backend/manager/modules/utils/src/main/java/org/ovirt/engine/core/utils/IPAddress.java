package org.ovirt.engine.core.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPAddress {

    private byte[] ip;

    public IPAddress(byte[] ipAddr) {
        ip = ipAddr;
    }

    @Override
    public String toString() {
        if (ip == null) {
            return "";
        }
        try {
            return InetAddress.getByAddress(ip).getHostAddress();
        } catch (UnknownHostException ex) {
            RuntimeException newEx = new RuntimeException(ex.getMessage());
            newEx.setStackTrace(ex.getStackTrace());
            throw newEx;
        }
    }

}
