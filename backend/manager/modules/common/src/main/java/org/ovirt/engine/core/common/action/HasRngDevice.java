package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VmRngDevice;

/**
 * Unifies command parameters of commands manipulating RNG devices.
 */
public interface HasRngDevice {

    VmRngDevice getRngDevice();

    void setRngDevice(VmRngDevice rngDevice);

    boolean isUpdateRngDevice();

    void setUpdateRngDevice(boolean updateRngDevice);
}
