package org.ovirt.engine.core.common.businessentities.network;

import java.util.HashMap;
import java.util.Map;

public enum VmInterfaceType {
    /**
     * This needs to cleaned up. We are leaving it in place to support import of
     * VMs created using previous versions.
     * @deprecated
     */
    @Deprecated
    rtl8139_pv(0, "Dual mode rtl8139, Red Hat VirtIO", 1000),
    rtl8139(1, "rtl8139", 100),
    e1000(2, "e1000", 1000),
    pv(3, "Red Hat VirtIO", 1000);

    private int value;
    private String description;
    private int speed;
    private static Map<Integer, VmInterfaceType> mappings = new HashMap<Integer, VmInterfaceType>();

    static {
        for (VmInterfaceType vmInterfaceType : values()) {
            mappings.put(vmInterfaceType.getValue(), vmInterfaceType);
        }
    }

    private VmInterfaceType(int value, String description, int speed) {
        this.value = value;
        this.description = description;
        this.speed = speed;
    }

    public static VmInterfaceType forValue(int value) {
        return mappings.get(value);
    }

    public String getDescription() {
        return description;
    }

    public int getValue() {
        return value;
    }

    public int getSpeed() {
        return speed;
    }
}
