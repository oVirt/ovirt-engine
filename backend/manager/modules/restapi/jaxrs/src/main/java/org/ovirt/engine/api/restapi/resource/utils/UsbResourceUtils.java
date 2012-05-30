package org.ovirt.engine.api.restapi.resource.utils;

import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Version;

public class UsbResourceUtils {
    public static UsbPolicy getUsbPolicy(Usb usb, VDSGroup vdsGroup) {
        UsbPolicy usbPolicy = null;
        if (usb == null || !usb.isSetEnabled() || !usb.isEnabled()) {
            usbPolicy = UsbPolicy.DISABLED;
        } else{
            UsbType usbType = usb.getType() != null ? UsbType.fromValue(usb.getType()) : null;
            if (usbType == null) { //decide according to cluster version
                if (vdsGroup.getcompatibility_version().compareTo(Version.v3_1) >= 0) {
                    usbPolicy = UsbPolicy.ENABLED_NATIVE;
                } else {
                    usbPolicy = UsbPolicy.ENABLED_LEGACY;
                }
            } else {
                if (usbType.equals(UsbType.LEGACY)) {
                    usbPolicy = UsbPolicy.ENABLED_LEGACY;
                } else if (usbType.equals(UsbType.NATIVE)) {
                    usbPolicy = UsbPolicy.ENABLED_NATIVE;
                }
            }
        }
        return usbPolicy;
    }
}
