package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MockConfigExtension;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingExtension;

@ExtendWith({MockConfigExtension.class, RandomUtilsSeedingExtension.class})
public class GetDeviceListVDSCommandTest {
    @Test
    public void parseLunReturnsIscsiByDefault() {
        testParseLunForDevtypeField(StorageType.ISCSI, "");
    }

    @Test
    public void parseLunReturnsFcpForFcp() {
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

        LUNs lun = GetDeviceListVDSCommand.parseLun(xlun, Version.v4_3);

        assertEquals(expectedStorageType, lun.getLunType());
    }

    @Test
    public void parseLunReturnsUnknownForNoField() {
        Map<String, Object> xlun = new HashMap<>();
        LUNs lun = GetDeviceListVDSCommand.parseLun(xlun, Version.v4_3);

        assertEquals(StorageType.UNKNOWN, lun.getLunType());
    }

    @Test
    public void parseLunPathStatus() {
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
        LUNs lun = GetDeviceListVDSCommand.parseLun(xlun, Version.v4_3);

        // Go over the directory
        assertEquals(numPaths, lun.getPathCount(), "wrong number of paths");
        Map<String, Boolean> pathDir = new HashMap<>(lun.getPathsDictionary());
        for (int i = 0; i < numPaths; ++i) {
            // Assert for each device
            String device = physicalDevicePrefix + i;
            Boolean isActive = pathDir.remove(device);

            assertNotNull(isActive, "Device " + device + " isn't in the device map");
            assertEquals(i < numActivePaths, isActive, "Device " + device + " has the wrong state ");
        }

        // After remove all the expected devices, the directory should be empty
        assertTrue(pathDir.isEmpty(), "Wrong devices in the device map");
    }

    @Test
    public void discardMaxBytesFieldIsParsed() {
        testDiscardFieldParsing(Version.v4_3, 1024L);
    }

    private void testDiscardFieldParsing(Version poolCompatibilityVersion, Long expectedDiscardMaxSize) {
        Map<String, Object> xlun = new HashMap<>();
        xlun.put("discard_max_bytes", 1024L);
        LUNs lun = GetDeviceListVDSCommand.parseLun(xlun, poolCompatibilityVersion);
        assertEquals(lun.getDiscardMaxSize(), expectedDiscardMaxSize);
    }
}
