package org.ovirt.engine.api.restapi.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.UsbType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;

public class UsbMapperUtilsTest {

    @Test
    public void getIsUsbEnabledEnabledNative() {
        UsbPolicy usbPolicy = UsbPolicy.ENABLED_NATIVE;
        assertTrue(UsbMapperUtils.getIsUsbEnabled(usbPolicy));
    }

    @Test
    public void getIsUsbEnabledDisabled() {
        UsbPolicy usbPolicy = UsbPolicy.DISABLED;
        assertFalse(UsbMapperUtils.getIsUsbEnabled(usbPolicy));
    }

    @Test
    public void getUsbTypeEnabledNative() {
        UsbPolicy usbPolicy = UsbPolicy.ENABLED_NATIVE;
        assertEquals(UsbType.NATIVE, UsbMapperUtils.getUsbType(usbPolicy));
    }

    @Test
    public void getUsbTypeDisabled() {
        UsbPolicy usbPolicy = UsbPolicy.DISABLED;
        assertNull(UsbMapperUtils.getUsbType(usbPolicy));
    }

}
