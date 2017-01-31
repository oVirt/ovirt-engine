package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.VmDeviceType;

public class GraphicsDevice extends VmDevice implements Serializable {

    private GraphicsDevice() { }

    public GraphicsDevice(VmDeviceType type) {
        setType(VmDeviceGeneralType.GRAPHICS);
        setDevice(type.name().toLowerCase());
        setId(new VmDeviceId());
        setAddress("");
        setPlugged(true);
        setManaged(true);
    }

    public GraphicsDevice(VmDevice vmDev) {
        this(GraphicsType.fromString(vmDev.getDevice()).getCorrespondingDeviceType());
        setId(vmDev.getId());
        setSpecParams(vmDev.getSpecParams());
    }

    public GraphicsType getGraphicsType() {
        return GraphicsType.fromString(getDevice());
    }

}

