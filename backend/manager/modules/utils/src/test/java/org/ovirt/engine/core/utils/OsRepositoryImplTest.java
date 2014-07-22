package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DisplayType;
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
    public static final String SOUND_DEVICE = "ac97";
    public static final String CD_INTERFACE = "ide";

    @BeforeClass
    public static void setUp() throws Exception {
        preferences = new MapBackedPreferences(preferences, "");
        preferences.node("/os/rhel7/id").put("value", "777");
        preferences.node("/os/rhel7/name").put("value", "RHEL 7");
        preferences.node("/os/rhel7/family").put("value", "linux");
        preferences.node("/os/rhel7/bus").put("value", "64");
        preferences.node("/os/rhel7/devices/network").put("value", NETWORK_DEVICES);
        preferences.node("/os/rhel7/devices/disk/hotpluggableInterfaces").put("value", DISK_HOTPLUGGABLE_INTERFACES);
        preferences.node("/os/rhel7/devices/watchdog/models").put("value", WATCH_DOG_MODELS);
        preferences.node("/os/rhel7/devices/maxPciDevices").put("value", MAX_PCI_DEVICES);
        preferences.node("/os/rhel7/resources/minimum/ram").put("value", "1024");
        preferences.node("/os/rhel7/resources/minimum/ram").put("value.3.1", "512");
        preferences.node("/os/rhel7/resources/maximum/ram").put("value", "2048");
        preferences.node("/os/rhel7/devices/display/protocols").put("value", "vga/cirrus,qxl/qxl"); // todo os info follow up
        preferences.node("/os/rhel7/devices/balloon/enabled").put("value", "true");
        preferences.node("/os/rhel7/sysprepPath").put("value", PATH_TO_SYSPREP);
        preferences.node("/os/rhel7/productKey").put("value", SOME_PRODUCT_KEY);
        preferences.node("/os/rhel7/devices/audio").put("value", SOUND_DEVICE);
        preferences.node("/os/rhel7/devices/cdInterface").put("value", CD_INTERFACE);
        preferences.node("/os/rhel7/isTimezoneTypeInteger").put("value", "false");
        preferences.node("/os/bados/id").put("value", "666");
        preferences.node("/os/bados/derivedFrom").put("value", "nonExistingOs");
        preferences.node("/os/rhel8/id").put("value", "888");
        preferences.node("/os/rhel8/derivedFrom").put("value", "rhel7");
        preferences.node("/os/windows_8/id").put("value", "20");
        preferences.node("/backwardCompatibility").put("Windows8", "20");
        preferences.node("/os/windows_7/id").put("value", "11");
        preferences.node("/os/windows_7/sysprepFileName").put("value", UNATTEND_XML);
        preferences.node("/os/windows_7/devices/hyperv/enabled").put("value", "true");
        preferences.node("/os/windows_8/cpu/unsupported").put("value", "conroe, opteron_g1");
        preferences.node("/os/windows_8/sysprepFileName").put("value", UNATTEND_XML);
        preferences.node("/os/windows_xp/id").put("value", "1");
        preferences.node("/os/windows_xp/sysprepFileName").put("value", SYSPREP_INF);
        OsRepositoryImpl.INSTANCE.init(preferences);
    }

    @Test
    public void testDumpRepoToString() {
        String actual = OsRepositoryImpl.INSTANCE.toString();
        String expected = OsRepositoryImpl.INSTANCE.name();
        Assert.assertNotSame(expected, actual);
        System.out.println(actual);
    }

    @Test
    public void testGetOsIds() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.getOsIds().contains(777));
    }

    @Test
    public void testGetOsIdByUniqueName() throws Exception {
        assertEquals(777, OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("rhel7"));
    }

    @Test
    public void testGetOsNames() throws Exception {
        assertEquals("RHEL 7", OsRepositoryImpl.INSTANCE.getOsNames().get(777));
    }

    @Test
    public void testGetOsName() throws Exception {
        assertEquals("RHEL 7", OsRepositoryImpl.INSTANCE.getOsName(777));
    }

    @Test
    public void testGetOsFamily() throws Exception {
        assertEquals("linux", OsRepositoryImpl.INSTANCE.getOsFamily(777));
    }

    @Test
    public void testGetLinuxOSs() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.getLinuxOss().contains(777));
    }

    @Test
    public void testGet64bitOss() throws Exception {
        assertEquals(2, OsRepositoryImpl.INSTANCE.get64bitOss().size());
    }

    @Test
    public void testGetWindowsOss() throws Exception {
        assertEquals(0, OsRepositoryImpl.INSTANCE.getWindowsOss().size());
    }

    @Test
    public void testIsWindows() throws Exception {
        assertFalse(OsRepositoryImpl.INSTANCE.isWindows(777));
    }

    @Test
    public void testGetNetworkDevices() throws Exception {
        ArrayList<String> networkDevices = OsRepositoryImpl.INSTANCE.getNetworkDevices(777, null);
        assertTrue(networkDevices.size() == 2);
        for (String device : NETWORK_DEVICES.split(",")) {
            assertTrue(networkDevices.contains(device));
        }
    }

    @Test
    public void testGetDiskHotpluggableInterfaces() throws Exception {
        Set<String> diskHotpluggableInterfaces = OsRepositoryImpl.INSTANCE.getDiskHotpluggableInterfaces(777, null);
        assertTrue(diskHotpluggableInterfaces.size() == 2);
        for (String diskHotpluggableInterface : DISK_HOTPLUGGABLE_INTERFACES.split(",")) {
            assertTrue(diskHotpluggableInterfaces.contains(diskHotpluggableInterface.trim()));
        }
    }

    @Test
    public void testGetWatchDogModels() throws Exception {
        ArrayList<String> watchDogModels = OsRepositoryImpl.INSTANCE.getWatchDogModels(777, null);
        assertTrue(watchDogModels.size() == 2);
        for (String model : WATCH_DOG_MODELS.split(",")) {
            assertTrue(watchDogModels.contains(model.trim()));
        }
    }

    @Test
    public void testIsLinux() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.isLinux(777));
    }

    @Test
    public void testGetMinimumRam() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.getMinimumRam(777, null) == 1024);
    }

    @Test
    public void testGetMaximumRam() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.getMaximumRam(777, null) == 2048);
    }

    @Test
    public void testDisplayTypes() throws Exception {
        List<DisplayType> supportedDisplays = OsRepositoryImpl.INSTANCE.getDisplayTypes().get(777).get(null);

        boolean isSizeCorrect = supportedDisplays.size() == 2;
        boolean containsSameElements = (new HashSet<DisplayType>(supportedDisplays)).equals(new HashSet<DisplayType>(Arrays.asList(DisplayType.qxl, DisplayType.vga)));

        assertTrue(isSizeCorrect);
        assertTrue(containsSameElements);
    }

    @Test
    public void testIsBalloonEnabled() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.isBalloonEnabled(777, null));
        assertTrue(OsRepositoryImpl.INSTANCE.getBalloonSupportMap().get(777).get(null));
    }

    public void testGetMaxPciDevices() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.getMaxPciDevices(777, null) == 26);
    }

    @Test
    public void testGetSysprepPath() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.getSysprepPath(777, null).equals(PATH_TO_SYSPREP));
    }

    @Test
    public void testGetSysprepFileName() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.getSysprepFileName(1, null).equals(SYSPREP_INF));
        assertTrue(OsRepositoryImpl.INSTANCE.getSysprepFileName(11, null).equals(UNATTEND_XML));
        assertTrue(OsRepositoryImpl.INSTANCE.getSysprepFileName(20, null).equals(UNATTEND_XML));
    }

    @Test
    public void testGetProductKey() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.getProductKey(777, null).equals(SOME_PRODUCT_KEY));
    }

    @Test
    public void testGetSoundDevice() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.getSoundDevice(777, null).equals(SOUND_DEVICE));
    }

    @Test
    public void testGetCdInterface() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.getCdInterface(777, null).equals(CD_INTERFACE));
    }

    @Test
    public void testIsTimezoneValueInteger() throws Exception {
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
        assertEquals(1024, OsRepositoryImpl.INSTANCE.getMinimumRam(777, null));
        assertEquals(512, OsRepositoryImpl.INSTANCE.getMinimumRam(777, Version.v3_1));
    }

    @Test
    public void testDerivedVersionedValue() {
        assertEquals(512, OsRepositoryImpl.INSTANCE.getMinimumRam(888, Version.v3_1));
    }

    @Test
    public void testdefaultVersionedValue() {
        assertEquals(1024, OsRepositoryImpl.INSTANCE.getMinimumRam(888, Version.v3_2));
    }

    @Test
    public void testBackwardCompatibility() {
        assertEquals(20, OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("Windows8"));
        assertEquals(20, OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("windows_8"));
    }

    @Test
    public void testHyperVLinux() throws Exception {
        assertFalse(OsRepositoryImpl.INSTANCE.isHypervEnabled(OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("rhel7"), Version.v3_5));
    }

    @Test
    public void testHyperVWindows() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.isHypervEnabled(OsRepositoryImpl.INSTANCE.getOsIdByUniqueName("windows_7"), Version.v3_5));
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
}
