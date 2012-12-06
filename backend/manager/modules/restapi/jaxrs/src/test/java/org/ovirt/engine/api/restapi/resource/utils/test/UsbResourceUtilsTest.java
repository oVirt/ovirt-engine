package org.ovirt.engine.api.restapi.resource.utils.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.api.model.Usb;
import org.ovirt.engine.api.restapi.resource.utils.UsbResourceUtils;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.compat.Version;

public class UsbResourceUtilsTest {
    @Test
    public void getUsbPolicyNullUsb() {
        Usb usb = null;
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(UsbResourceUtils.getUsbPolicy(usb, vdsGroup), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyIsSetDisabled() {
        Usb usb = new Usb();
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(UsbResourceUtils.getUsbPolicy(usb, vdsGroup), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicySetDisabled() {
        Usb usb = new Usb();
        usb.setEnabled(false);
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(UsbResourceUtils.getUsbPolicy(usb, vdsGroup), UsbPolicy.DISABLED);
    }

    @Test
    public void getUsbPolicyUsbTypeNative() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType("native");
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(UsbResourceUtils.getUsbPolicy(usb, vdsGroup), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyUsbTypeLegacy() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        usb.setType("legacy");
        VDSGroup vdsGroup = new VDSGroup();
        assertEquals(UsbResourceUtils.getUsbPolicy(usb, vdsGroup), UsbPolicy.ENABLED_LEGACY);
    }

    @Test
    public void getUsbPolicyUsbTypeNull31() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setcompatibility_version(Version.v3_1);
        assertEquals(UsbResourceUtils.getUsbPolicy(usb, vdsGroup), UsbPolicy.ENABLED_NATIVE);
    }

    @Test
    public void getUsbPolicyUsbTypeNull30() {
        Usb usb = new Usb();
        usb.setEnabled(true);
        VDSGroup vdsGroup = new VDSGroup();
        vdsGroup.setcompatibility_version(Version.v3_0);
        assertEquals(UsbResourceUtils.getUsbPolicy(usb, vdsGroup), UsbPolicy.ENABLED_LEGACY);
    }

}
