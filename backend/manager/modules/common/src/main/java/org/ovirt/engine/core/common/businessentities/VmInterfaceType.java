package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "VmInterfaceType")
public enum VmInterfaceType {
    rtl8139_pv(0,"Dual mode rtl8139, Red Hat VirtIO"),
    rtl8139(1,"rtl8139"),
    e1000(2,"e1000"),
    pv(3,"Red Hat VirtIO");

    private int intValue;
    private String interfaceTranslation;
    private static java.util.HashMap<Integer, VmInterfaceType> mappings = new HashMap<Integer, VmInterfaceType>();

    static {
        for (VmInterfaceType vmInterfaceType : values()) {
            mappings.put(vmInterfaceType.getValue(), vmInterfaceType);
        }
    }

    private VmInterfaceType(int value) {
        this(value, null);
    }

    private VmInterfaceType(int value, String interfaceTranslationVal) {
        intValue = value;
        interfaceTranslation = interfaceTranslationVal;
    }


    public String getInterfaceTranslation() {
        return interfaceTranslation;
    }

    public int getValue() {
        return intValue;
    }

    public static VmInterfaceType forValue(int value) {
        return mappings.get(value);
    }

    public int getSpeed() {
        if (this == VmInterfaceType.rtl8139) {
            return 100;
        }
        return 1000;
    }
}
