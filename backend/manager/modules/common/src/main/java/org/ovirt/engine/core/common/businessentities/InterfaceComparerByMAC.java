package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

//C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the methods implemented will need adjustment:
public class InterfaceComparerByMAC implements java.util.Comparator<NetworkInterface<?>>, Serializable {
    private static final long serialVersionUID = 8440455227895969691L;

    @Override
    public int compare(NetworkInterface<?> x, NetworkInterface<?> y) {
        return (x.getMacAddress().compareTo(y.getMacAddress()));
    }

    public InterfaceComparerByMAC() {
    }
}
