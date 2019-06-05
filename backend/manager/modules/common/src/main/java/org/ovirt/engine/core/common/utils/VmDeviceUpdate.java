package org.ovirt.engine.core.common.utils;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;

public class VmDeviceUpdate {

    private VmDeviceGeneralType generalType;
    private VmDeviceType type;
    private boolean readOnly;
    private String name;
    private boolean enable;
    private VmDevice device = null;

    public VmDeviceUpdate() {
        this(VmDeviceGeneralType.UNKNOWN, VmDeviceType.UNKNOWN, false, "", false);
    }

    public VmDeviceUpdate(VmDeviceGeneralType generalType, VmDeviceType type, boolean readOnly, String name,
                          boolean enable) {
        this.generalType = generalType;
        this.type = type;
        this.readOnly = readOnly;
        this.name = name;
        setEnable(enable);
    }

    public VmDeviceUpdate(VmDeviceGeneralType generalType, VmDeviceType type, boolean readOnly, String name,
                          VmDevice device) {
        this.generalType = generalType;
        this.type = type;
        this.readOnly = readOnly;
        this.name = name;
        setEnable(device != null);
        setDevice(device);
    }

    public VmDeviceGeneralType getGeneralType() {
        return generalType;
    }

    public void setGeneralType(VmDeviceGeneralType generalType) {
        this.generalType = generalType;
    }

    public VmDeviceType getType() {
        return type;
    }

    public void setType(VmDeviceType type) {
        this.type = type;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getName() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        if (VmDeviceType.UNKNOWN.equals(type)) {
            return generalType.name();
        }
        return type.getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public VmDevice getDevice() {
        return device;
    }

    public void setDevice(VmDevice device) {
        this.device = device;
    }

}
