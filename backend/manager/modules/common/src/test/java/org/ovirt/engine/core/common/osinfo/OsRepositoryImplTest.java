package org.ovirt.engine.core.common.osinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ovirt.engine.core.compat.Version;

public class OsRepositoryImplTest {

    private static MapBackedPreferences preferences;

    public static final String NETWORK_DEVICES = "e100,pv";
    public static final String PATH_TO_SYSPREP = "/path/to/sysprep";
    public static final String SOME_PRODUCT_KEY = "some-product-key";
    public static final String SOUND_DEVICE = "ac97";

    @BeforeClass
    public static void setUp() throws Exception {
        preferences = new MapBackedPreferences(preferences, "");
        preferences.node("/os/rhel7/id").put("value", "777");
        preferences.node("/os/rhel7/name").put("value", "RHEL 7");
        preferences.node("/os/rhel7/family").put("value", "linux");
        preferences.node("/os/rhel7/bus").put("value", "64");
        preferences.node("/os/rhel7/devices/network").put("value", NETWORK_DEVICES);
        preferences.node("/os/rhel7/resources/minimum/ram").put("value", "1024");
        preferences.node("/os/rhel7/resources/minimum/ram").put("value.3.1", "512");
        preferences.node("/os/rhel7/resources/maximum/ram").put("value", "2048");
        preferences.node("/os/rhel7/spiceSupport").put("value", "true");
        preferences.node("/os/rhel7/sysprepPath").put("value", PATH_TO_SYSPREP);
        preferences.node("/os/rhel7/productKey").put("value", SOME_PRODUCT_KEY);
        preferences.node("/os/rhel7/devices/audio").put("value", SOUND_DEVICE);
        preferences.node("/os/rhel7/isTimezoneTypeInteger").put("value", "false");
        preferences.node("/os/bados/id").put("value", "666");
        preferences.node("/os/bados/derivedFrom").put("value", "nonExistingOs");
        preferences.node("/os/rhel8/id").put("value", "888");
        preferences.node("/os/rhel8/derivedFrom").put("value", "rhel7");
        OsRepositoryImpl.INSTANCE.init(preferences);
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
    public void testHasSpiceSupport() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.hasSpiceSupport(777, null));
    }

    @Test
    public void testGetSysprepPath() throws Exception {
        assertTrue(OsRepositoryImpl.INSTANCE.getSysprepPath(777, null).equals(PATH_TO_SYSPREP));
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
    public void testIsTimezoneValueInteger() throws Exception {
        assertFalse(OsRepositoryImpl.INSTANCE.isTimezoneValueInteger(777, null));
    }

    @Test
    public void testOsNameUpperCasedAndUnderscored() throws Exception {
        assertEquals("RHEL_6", OsRepositoryImpl.osNameUpperCasedAndUnderscored("Rhel6"));
        assertEquals("RHEL_6X64", OsRepositoryImpl.osNameUpperCasedAndUnderscored("rhel6x64"));
        assertEquals("OTHER_LINUX", OsRepositoryImpl.osNameUpperCasedAndUnderscored("OtherLinux"));
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
}
