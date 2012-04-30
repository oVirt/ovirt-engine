package org.ovirt.engine.api.restapi.utils;

import org.junit.Test;
import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;

public class UsbMapperUtilsTest {

    @Test
    public void getIsUsbEnabledEnabledLegacy() {
        UsbPolicy usbPolicy = UsbPolicy.ENABLED_LEGACY;
        assert(UsbMapperUtils.getIsUsbEnabled(usbPolicy));
    }

    @Test
    public void getIsUsbEnabledEnabledNative() {
        UsbPolicy usbPolicy = UsbPolicy.ENABLED_NATIVE;
        assert(UsbMapperUtils.getIsUsbEnabled(usbPolicy));
    }

    @Test
    public void getIsUsbEnabledDisabled() {
        UsbPolicy usbPolicy = UsbPolicy.DISABLED;
        assert(!UsbMapperUtils.getIsUsbEnabled(usbPolicy));
    }

    @Test
    public void getUsbTypeEnabledLegacy() {
        UsbPolicy usbPolicy = UsbPolicy.ENABLED_LEGACY;
        assert(UsbMapperUtils.getUsbType(usbPolicy).equals(UsbType.LEGACY));
    }

    @Test
    public void getUsbTypeEnabledNative() {
        UsbPolicy usbPolicy = UsbPolicy.ENABLED_NATIVE;
        assert(UsbMapperUtils.getUsbType(usbPolicy).equals(UsbType.NATIVE));
    }

    @Test
    public void getUsbTypeDisabled() {
        UsbPolicy usbPolicy = UsbPolicy.DISABLED;
        assert(UsbMapperUtils.getUsbType(usbPolicy) == null);
    }

}
