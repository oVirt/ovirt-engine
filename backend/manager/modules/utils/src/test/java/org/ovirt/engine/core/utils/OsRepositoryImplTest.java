package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.UsbControllerModel;
import org.ovirt.engine.core.common.osinfo.MapBackedPreferences;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Version;


public class OsRepositoryImplTest {

    private static MapBackedPreferences preferences;

    public static final String SYSPREP_INF = "sysprep.inf";
    public static final String UNATTEND_XML = "unattend.xml";
    public static final String NETWORK_DEVICES = "e100,pv";
    public static final String DISK_HOTPLUGGABLE_INTERFACES = "VirtIO_SCSI, VirtIO";
    public static final String WATCH_DOG_MODELS = "model1, model2";
    public static final String MAX_PCI_DEVICES = "26";
    public static final String PATH_TO_SYSPREP = "/path/to/sysprep";
    public static final String SOME_PRODUCT_KEY = "some-product-key";
    public static final String SOUND_DEVICE = "ac97,q35/ich9";
    public static final String CD_INTERFACE = "ide,q35/sata";

    @BeforeAll
    public static void setUp() {
        preferences = new MapBackedPreferences(preferences, "");
        preferences.node("/os/rhel7/id").put("value", "777");
        preferences.node("/os/rhel7/name").put("value", "RHEL 7");
        preferences.node("/os/rhel7/family").put("value", "linux");
        preferences.node("/os/rhel7/bus").put("value", "64");
        preferences.node("/os/rhel7/devices/network").put("value", NETWORK_DEVICES);
        preferences.node("/os/rhel7/devices/disk/hotpluggableInterfaces").put("value", DISK_HOTPLUGGABLE_INTERFACES);
        preferences.node("/os/rhel7/devices/watchdog/models").put("value", WATCH_DOG_MODELS);
        preferences.node("/os/rhel7/devices/maxPciDevices").put("value", MAX_PCI_DEVICES);
        preferences.node("/os/rhel7/resources/minimum/ram").put("value", "2048");
        preferences.node("/os/rhel7/resources/minimum/ram").put("value.4.2", "1024");
        preferences.node("/os/rhel7/resources/maximum/ram").put("value", "2048");
        preferences.node("/os/rhel7/devices/display/protocols").put("value", "VNC/vga,SPICE/qxl");
        preferences.node("/os/rhel7/devices/audio/enabled").put("value", "true");
        preferences.node("/os/rhel7/devices/floppy/support").put("value", "true");
        preferences.node("/os/rhel7/sysprepPath").put("value", PATH_TO_SYSPREP);
        preferences.node("/os/rhel7/productKey").put("value", SOME_PRODUCT_KEY);
        preferences.node("/os/rhel7/devices/audio").put("value", SOUND_DEVICE);
        preferences.node("/os/rhel7/devices/cdInterface").put("value", CD_INTERFACE);
        preferences.node("/os/rhel7/isTimezoneTypeInteger").put("value", "false");
        preferences.node("/os/rhel7/q35Support").put("value", "true");
        preferences.node("/os/bados/id").put("value", "666");
        preferences.node("/os/bados/derivedFrom").put("value", "nonExistingOs");
        preferences.node("/os/rhel8/id").put("value", "888");
        preferences.node("/os/rhel8/derivedFrom").put("value", "rhel7");
        preferences.node("/os/windows_8/id").put("value", "20");
        preferences.node("/backwardCompatibility").put("Windows8", "20");
        preferences.node("/os/windows_7/id").put("value", "11");
        preferences.node("/os/windows_7/sysprepFileName").put("value", UNATTEND_XML);
        preferences.node("/os/windows_7/q35Support").put("value", "insecure");
        preferences.node("/os/windows_7/devices/hyperv/enabled").put("value", "true");
        preferences.node("/os/windows_8/cpu/unsupported").put("value", "conroe, opteron_g1");
        preferences.node("/os/windows_8/sysprepFileName").put("value", UNATTEND_XML);
        preferences.node("/os/windows_xp/id").put("value", "1");
        preferences.node("/os/windows_xp/sysprepFileName").put("value", SYSPREP_INF);
        preferences.node("/os/rhel7/devices/usb/controller").put("value", "nec-xhci,q35/qemu-xhci");
        preferences.node("/os/rhel6/id").put("value", "999");
        preferences.node("/os/rhel6/q35Support").put("value", "false");
        preferences.node("/os/rhel6/devices/usb/controller").put("value", "nec-xhci");
        preferences.node("/os/rhel6/devices/usb/controller").put("value.4.2", "none");
        preferences.node("/os/rhel7/devices/tpm").put("value", "supported");
        OsRepositoryImpl.INSTANCE.init(preferences);
    }

    @Test
    public void testDumpRepoToString() {
        String actual = OsRepositoryImpl.INSTANCE.toString();
        String expected = OsRepositoryImpl.INSTANCE.name();
        assertNotSame(expected, actual);
    }

    @Test
    public void testGetOsIds() {
        assertTrue(OsRepositoryImpl.INSTANCE.getOsIds().contains(777));
    }

    @Test
    public void testGetOsIdByUniqueName() {
        assertEquals(777, OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("rhel7"));
    }

    @Test
    public void testGetOsNames() {
        assertEquals("RHEL 7", OsRepositoryImpl.INSTANCE.getOsNames().get(777));
    }

    @Test
    public void testGetOsName() {
        assertEquals("RHEL 7", OsRepositoryImpl.INSTANCE.getOsName(777));
    }

    @Test
    public void testGetOsFamily() {
        assertEquals("linux", OsRepositoryImpl.INSTANCE.getOsFamily(777));
    }

    @Test
    public void testGetLinuxOSs() {
        assertTrue(OsRepositoryImpl.INSTANCE.getLinuxOss().contains(777));
    }

    @Test
    public void testGet64bitOss() {
        assertEquals(2, OsRepositoryImpl.INSTANCE.get64bitOss().size());
    }

    @Test
    public void testGetWindowsOss() {
        assertEquals(0, OsRepositoryImpl.INSTANCE.getWindowsOss().size());
    }

    @Test
    public void testIsWindows() {
        assertFalse(OsRepositoryImpl.INSTANCE.isWindows(777));
    }

    @Test
    public void testGetNetworkDevices() {
        List<String> networkDevices = OsRepositoryImpl.INSTANCE.getNetworkDevices(777, null);
        assertEquals(2, networkDevices.size());
        for (String device : NETWORK_DEVICES.split(",")) {
            assertTrue(networkDevices.contains(device));
        }
    }

    @Test
    public void testGetDiskHotpluggableInterfaces() {
        Set<String> diskHotpluggableInterfaces = OsRepositoryImpl.INSTANCE.getDiskHotpluggableInterfaces(777, null);
        assertEquals(2, diskHotpluggableInterfaces.size());
        for (String diskHotpluggableInterface : DISK_HOTPLUGGABLE_INTERFACES.split(",")) {
            assertTrue(diskHotpluggableInterfaces.contains(diskHotpluggableInterface.trim()));
        }
    }

    @Test
    public void testGetWatchDogModels() {
        List<String> watchDogModels = OsRepositoryImpl.INSTANCE.getWatchDogModels(777, null);
        assertEquals(2, watchDogModels.size());
        for (String model : WATCH_DOG_MODELS.split(",")) {
            assertTrue(watchDogModels.contains(model.trim()));
        }
    }

    @Test
    public void testIsLinux() {
        assertTrue(OsRepositoryImpl.INSTANCE.isLinux(777));
    }

    @Test
    public void testGetMinimumRam() {
        assertEquals(2048, OsRepositoryImpl.INSTANCE.getMinimumRam(777, null));
    }

    @Test
    public void testGetMaximumRam() {
        assertEquals(2048, OsRepositoryImpl.INSTANCE.getMaximumRam(777, null));
    }

    @Test
    public void testDisplayTypes() {
        List<Pair<GraphicsType, DisplayType>> supportedGraphicsAndDisplays = OsRepositoryImpl.INSTANCE.getGraphicsAndDisplays().get(777).get(null);

        boolean isSizeCorrect = supportedGraphicsAndDisplays.size() == 2;
        boolean containsSameElements = new HashSet<>(supportedGraphicsAndDisplays)
                .equals(new HashSet<>(Arrays.asList(
                        new Pair<>(GraphicsType.SPICE, DisplayType.qxl),
                        new Pair<>(GraphicsType.VNC, DisplayType.vga))));

        assertTrue(isSizeCorrect);
        assertTrue(containsSameElements);
    }

    @Test
    public void testFloppySupport() {
        assertTrue(OsRepositoryImpl.INSTANCE.isFloppySupported(777, null));
    }

    @Test
    public void testIsSoundDeviceEnabled() {
        assertTrue(OsRepositoryImpl.INSTANCE.isSoundDeviceEnabled(777, null));
        assertTrue(OsRepositoryImpl.INSTANCE.getSoundDeviceSupportMap().get(777).get(null));
    }

    @Test
    public void testGetMaxPciDevices() {
        assertEquals(26, OsRepositoryImpl.INSTANCE.getMaxPciDevices(777, null));
    }

    @Test
    public void testGetSysprepPath() {
        assertEquals(PATH_TO_SYSPREP, OsRepositoryImpl.INSTANCE.getSysprepPath(777, null));
    }

    @Test
    public void testGetSysprepFileName() {
        assertEquals(SYSPREP_INF, OsRepositoryImpl.INSTANCE.getSysprepFileName(1, null));
        assertEquals(UNATTEND_XML, OsRepositoryImpl.INSTANCE.getSysprepFileName(11, null));
        assertEquals(UNATTEND_XML, OsRepositoryImpl.INSTANCE.getSysprepFileName(20, null));
    }

    @Test
    public void testGetProductKey() {
        assertEquals(SOME_PRODUCT_KEY, OsRepositoryImpl.INSTANCE.getProductKey(777, null));
    }

    @Test
    public void testGetSoundDevice() {
        assertEquals("ac97", OsRepositoryImpl.INSTANCE.getSoundDevice(777, null, ChipsetType.I440FX));
    }

    @Test
    public void testGetSoundDeviceQ35() {
        assertEquals("ich9", OsRepositoryImpl.INSTANCE.getSoundDevice(777, null, ChipsetType.Q35));
    }

    @Test
    public void testGetCdInterface() {
        assertEquals("ide", OsRepositoryImpl.INSTANCE.getCdInterface(777, null, null));
        assertEquals("ide", OsRepositoryImpl.INSTANCE.getCdInterface(777, null, ChipsetType.I440FX));
        assertEquals("sata", OsRepositoryImpl.INSTANCE.getCdInterface(777, null, ChipsetType.Q35));
    }

    @Test
    public void testIsTimezoneValueInteger() {
        assertFalse(OsRepositoryImpl.INSTANCE.isTimezoneValueInteger(777, null));
    }

    @Test
    public void testNonExistingKey() {
        assertEquals("", OsRepositoryImpl.INSTANCE.getOsFamily(666));
    }

    @Test
    public void testNonExistingParentOs() {
        assertEquals("", OsRepositoryImpl.INSTANCE.getProductKey(666, null));
    }

    @Test
    public void testVersionedValue() {
        assertEquals(2048, OsRepositoryImpl.INSTANCE.getMinimumRam(777, null));
        assertEquals(1024, OsRepositoryImpl.INSTANCE.getMinimumRam(777, Version.v4_2));
    }

    @Test
    public void testDerivedVersionedValue() {
        assertEquals(1024, OsRepositoryImpl.INSTANCE.getMinimumRam(888, Version.v4_2));
    }

    @Test
    public void testdefaultVersionedValue() {
        assertEquals(2048, OsRepositoryImpl.INSTANCE.getMinimumRam(888, Version.v4_3));
    }

    @Test
    public void testBackwardCompatibility() {
        assertEquals(20, OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("Windows8"));
        assertEquals(20, OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("windows_8"));
    }

    @Test
    public void testHyperVLinux() {
        assertFalse(OsRepositoryImpl.INSTANCE.isHypervEnabled(
                OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("rhel7"),
                Version.v4_2));
    }

    @Test
    public void testHyperVWindows() {
        assertTrue(OsRepositoryImpl.INSTANCE.isHypervEnabled(
                OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("windows_7"),
                Version.v4_2));
    }

    @Test
    public void testUnsupportedCpus() {
        assertFalse(
                OsRepositoryImpl.INSTANCE.isCpuSupported(
                        OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("windows_8"),
                        Version.getLast(),
                        "OpTeRon_g1"));
        assertTrue(
                OsRepositoryImpl.INSTANCE.isCpuSupported(
                        OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("windows_8"),
                        Version.getLast(),
                        "OpTeRon_g2"));
        assertFalse(
                OsRepositoryImpl.INSTANCE.getUnsupportedCpus()
                        .get(new Pair<>(20, Version.getLast())).contains("Penrin".toLowerCase()));
        assertTrue(
                OsRepositoryImpl.INSTANCE.getUnsupportedCpus()
                        .get(new Pair<>(20, Version.getLast())).contains("Conroe".toLowerCase()));
    }

    @Test
    public void testUniqueOsIdValidation() throws BackingStoreException {
        Preferences invalidNode = preferences.node("/os/ubuntu/id");
        invalidNode.put("value", "777");
        try {
            OsRepositoryImpl.INSTANCE.init(preferences);
        } catch (RuntimeException e) {
            // expected
        }
        invalidNode.removeNode();
        OsRepositoryImpl.INSTANCE.init(preferences); // must pass with no exceptions
    }

    @Test
    public void testExistingUsbControllerModelWithoutVersion() {
        final UsbControllerModel model = OsRepositoryImpl.INSTANCE.getOsUsbControllerModel(
                OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("rhel7"),
                null,
                ChipsetType.I440FX);
        assertEquals(UsbControllerModel.NEC_XHCI, model);
    }

    @Test
    public void testExistingUsbControllerModelWithChipset() {
        final UsbControllerModel model = OsRepositoryImpl.INSTANCE.getOsUsbControllerModel(
                OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("rhel7"),
                null,
                ChipsetType.Q35);
        assertEquals(UsbControllerModel.QEMU_XHCI, model);
    }

    @Test
    public void testExistingUsbControllerModelWithVersion() {
        final UsbControllerModel model = OsRepositoryImpl.INSTANCE.getOsUsbControllerModel(
                OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("rhel6"),
                Version.v4_2,
                ChipsetType.I440FX);
        assertEquals(UsbControllerModel.NONE, model);
    }

    @Test
    public void testExistingUsbControllerModelWithNonExistingVersion() {
        final UsbControllerModel model = OsRepositoryImpl.INSTANCE.getOsUsbControllerModel(
                OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("rhel6"),
                Version.v4_3,
                ChipsetType.I440FX);
        assertEquals(UsbControllerModel.NEC_XHCI, model);
    }

    @Test
    public void testNonExistingUsbControllerModel() {
        final UsbControllerModel model = OsRepositoryImpl.INSTANCE.getOsUsbControllerModel(
                OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("windows_8"),
                null,
                ChipsetType.I440FX);
        assertNull(model);
    }

    @Test
    public void testTpmAllowed() {
        assertTrue(OsRepositoryImpl.INSTANCE.isTpmAllowed(777));
        assertTrue(OsRepositoryImpl.INSTANCE.isTpmAllowed(888));
        assertFalse(OsRepositoryImpl.INSTANCE.isTpmAllowed(999));
        assertFalse(OsRepositoryImpl.INSTANCE.isTpmAllowed(666));
    }

    @Test
    public void testQ35Supported() {
        assertTrue(OsRepositoryImpl.INSTANCE.isQ35Supported(777));
        assertTrue(OsRepositoryImpl.INSTANCE.isQ35Supported(11));
        assertFalse(OsRepositoryImpl.INSTANCE.isQ35Supported(999));
        assertTrue(OsRepositoryImpl.INSTANCE.isSecureBootSupported(777));
        assertFalse(OsRepositoryImpl.INSTANCE.isSecureBootSupported(11));
        assertFalse(OsRepositoryImpl.INSTANCE.isSecureBootSupported(999));
    }
}
