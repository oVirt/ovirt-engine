package org.ovirt.engine.core.common.businessentities.comparators;

import java.io.Serializable;
import java.util.Comparator;

import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;

public class InterfaceComparerByMAC implements Comparator<NetworkInterface<?>>, Serializable {
    private static final long serialVersionUID = 8440455227895969691L;

    @Override
    public int compare(NetworkInterface<?> x, NetworkInterface<?> y) {
        return x.getMacAddress().compareTo(y.getMacAddress());
    }
}
