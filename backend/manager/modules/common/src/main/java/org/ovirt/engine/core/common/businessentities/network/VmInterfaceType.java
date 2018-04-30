package org.ovirt.engine.core.common.businessentities.network;

import java.util.HashMap;
import java.util.Map;

public enum VmInterfaceType {
    /**
     * @deprecated This needs to cleaned up. We are leaving it in place to support import of
     * VMs created using previous versions.
     */
    @Deprecated
    rtl8139_pv(0, "Dual mode rtl8139, VirtIO", "rtl8139_pv", 1000),
    rtl8139(1, "rtl8139", "rtl8139", 100),
    e1000(2, "e1000", "e1000", 1000),
    pv(3, "VirtIO", "pv", 10000),
    spaprVlan(4, "sPAPR VLAN", "spapr-vlan", 1000),
    pciPassthrough(5, "PCI Passthrough", "pci-passthorugh", 1000);

    private int value;
    private String description;
    private String internalName;
    private int speed;
    private static final Map<Integer, VmInterfaceType> mappings = new HashMap<>();

    static {
        for (VmInterfaceType vmInterfaceType : values()) {
            mappings.put(vmInterfaceType.getValue(), vmInterfaceType);
        }
    }

    private VmInterfaceType(int value, String description, String internalName, int speed) {
        this.value = value;
        this.description = description;
        this.internalName = internalName;
        this.speed = speed;
    }

    public static VmInterfaceType forValue(int value) {
        return mappings.get(value);
    }

    public String getDescription() {
        return description;
    }

    public String getInternalName() {
        return internalName;
    }

    public int getValue() {
        return value;
    }

    public int getSpeed() {
        return speed;
    }
}
