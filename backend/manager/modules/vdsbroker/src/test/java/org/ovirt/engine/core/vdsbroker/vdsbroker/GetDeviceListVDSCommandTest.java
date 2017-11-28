package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigRule;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingRule;

public class GetDeviceListVDSCommandTest {

    @Rule
    public RandomUtilsSeedingRule rusr = new RandomUtilsSeedingRule();

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.PassDiscardSupported, Version.v4_0, false),
            mockConfig(ConfigValues.PassDiscardSupported, Version.v4_1, true)
    );

    @Test
    public void parseLunReturnsIscsiByDefault() throws Exception {
        testParseLunForDevtypeField(StorageType.ISCSI, "");
    }

    @Test
    public void parseLunReturnsFcpForFcp() throws Exception {
        testParseLunForDevtypeField(StorageType.FCP, GetDeviceListVDSCommand.DEVTYPE_VALUE_FCP);
    }

    /**
     * Test that parseLun parses the {@link GetDeviceListVDSCommand#DEVTYPE_FIELD} correctly.
     *
     * @param expectedStorageType
     *            The storage type expected to return.
     * @param mockDevtype
     *            The value that the XML RPC will hold.
     */
    private static void testParseLunForDevtypeField(StorageType expectedStorageType, String mockDevtype) {
        Map<String, Object> xlun = new HashMap<>();
        xlun.put(GetDeviceListVDSCommand.DEVTYPE_FIELD, mockDevtype);

        LUNs lun = GetDeviceListVDSCommand.parseLun(xlun, Version.v4_1);

        assertEquals(expectedStorageType, lun.getLunType());
    }

    @Test
    public void parseLunReturnsUnknownForNoField() throws Exception {
        Map<String, Object> xlun = new HashMap<>();
        LUNs lun = GetDeviceListVDSCommand.parseLun(xlun, Version.v4_1);

        assertEquals(StorageType.UNKNOWN, lun.getLunType());
    }

    @Test
    public void parseLunPathStatus() throws Exception {
        int numActivePaths = 1 + RandomUtils.instance().nextInt(3);
        int numNonActivePaths = 2 + RandomUtils.instance().nextInt(3);
        int numPaths = numActivePaths + numNonActivePaths;

        // Randomize some devices
        String physicalDevicePrefix = "physical";
        List<Map<String, Object>> paths = new ArrayList<>(numPaths);
        for (int i = 0; i < numPaths; ++i) {
            Map<String, Object> path = new HashMap<>();
            path.put(GetDeviceListVDSCommand.LUN_FIELD, String.valueOf(i));
            path.put(GetDeviceListVDSCommand.PHYSICAL_DEVICE_FIELD, physicalDevicePrefix + i);
            path.put(GetDeviceListVDSCommand.DEVICE_STATE_FIELD,
                    i < numActivePaths ?
                            GetDeviceListVDSCommand.DEVICE_ACTIVE_VALUE : RandomUtils.instance().nextString(10));
            paths.add(path);
        }

        Map<String, Object> xlun = new HashMap<>();
        xlun.put(GetDeviceListVDSCommand.PATHSTATUS, paths.toArray(new Map[paths.size()]));

        // Parse lun
        LUNs lun = GetDeviceListVDSCommand.parseLun(xlun, Version.v4_1);

        // Go over the directory
        assertEquals("wrong number of paths", numPaths, lun.getPathCount());
        Map<String, Boolean> pathDir = new HashMap<>(lun.getPathsDictionary());
        for (int i = 0; i < numPaths; ++i) {
            // Assert for each device
            String device = physicalDevicePrefix + i;
            Boolean isActive = pathDir.remove(device);

            assertNotNull("Device " + device + " isn't in the device map", isActive);
            assertEquals("Device " + device + " has the wrong state ", i < numActivePaths, isActive);
        }

        // After remove all the expected devices, the directory should be empty
        assertTrue("Wrong devices in the device map", pathDir.isEmpty());
    }

    @Test
    public void discardMaxBytesFieldIsParsed() {
        testDiscardFieldParsing(Version.v4_1, 1024L);
    }

    @Test
    public void discardMaxBytesFieldIsNotParsed() {
        testDiscardFieldParsing(Version.v4_0, null);
    }

    private void testDiscardFieldParsing(Version poolCompatibilityVersion, Long expectedDiscardMaxSize) {
        Map<String, Object> xlun = new HashMap<>();
        xlun.put("discard_max_bytes", 1024L);
        LUNs lun = GetDeviceListVDSCommand.parseLun(xlun, poolCompatibilityVersion);
        assertEquals(lun.getDiscardMaxSize(), expectedDiscardMaxSize);
    }
}
