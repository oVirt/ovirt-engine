package org.ovirt.engine.core.utils.ovf;

import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.utils.VmDeviceType;

public class OvfReaderWriterUtils {

    /**
     * is special device - device which is managed, but contains the general properties
     */
    static boolean isSpecialDevice(String device, VmDeviceGeneralType type, boolean includeHostDev) {
        if (VmDeviceType.USB.getName().equals(device)) {
            return true;
        }

        switch (type) {
        case MDEV:
        case SOUND:
            return true;
        case CONSOLE:
            return VmDeviceType.CONSOLE.getName().equals(device);
        case SMARTCARD:
            return VmDeviceType.SMARTCARD.getName().equals(device);
        case REDIR:
            return VmDeviceType.SPICEVMC.getName().equals(device);
        case BALLOON:
            return VmDeviceType.MEMBALLOON.getName().equals(device);
        case WATCHDOG:
            return VmDeviceType.WATCHDOG.getName().equals(device);
        case RNG:
            return VmDeviceType.VIRTIO.getName().equals(device);
        case CONTROLLER:
            return VmDeviceType.VIRTIOSERIAL.getName().equals(device)
                    || VmDeviceType.VIRTIOSCSI.getName().equals(device);
        case HOSTDEV:
            return includeHostDev;
        default:
            return false;
        }
    }
}
