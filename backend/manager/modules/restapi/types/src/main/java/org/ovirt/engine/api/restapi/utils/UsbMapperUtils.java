package org.ovirt.engine.api.restapi.utils;

import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;

public class UsbMapperUtils {

    public static boolean getIsUsbEnabled(UsbPolicy usbPolicy) {
        boolean enabled = false;
        if (usbPolicy != null) {
            enabled = usbPolicy.equals(UsbPolicy.ENABLED_NATIVE);
        }
        return enabled;
    }

    public static UsbType getUsbType(UsbPolicy usbPolicy) {
        UsbType usbType = null;
        if (usbPolicy != null) {
            if (getIsUsbEnabled(usbPolicy)) {
                usbType = UsbType.NATIVE;
            }
        }
        return usbType;
    }
}
