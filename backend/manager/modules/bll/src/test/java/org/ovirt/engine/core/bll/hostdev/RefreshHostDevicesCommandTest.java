package org.ovirt.engine.core.bll.hostdev;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.HostDevice;

public class RefreshHostDevicesCommandTest {
    @Test
    public void testFilterOrphanedHostDevices() {
        Map<String, HostDevice> fetchedDevices = Entities.entitiesByName(Arrays.asList(
            new HostDevice() {{
                setDeviceName("computer");
                setParentDeviceName("computer");
            }},
            new HostDevice() {{
                setDeviceName("good");
                setParentDeviceName("computer");
            }},
            new HostDevice() {{
                setDeviceName("child_of_good");
                setParentDeviceName("good");
            }},
            new HostDevice() {{
                setDeviceName("orphan");
                setParentDeviceName("non_existing");
            }},
            new HostDevice() {{
                setDeviceName("child_of_orphan");
                setParentDeviceName("orphan");
            }},
            new HostDevice() {{
                setDeviceName("bad");
                setParentDeviceName("");
            }},
            new HostDevice() {{
                setDeviceName("child_of_bad");
                setParentDeviceName("bad");
            }},
            new HostDevice() {{
                setDeviceName("worse");
                setParentDeviceName("no parent");
            }},
            new HostDevice() {{
                setDeviceName("child_of_worse");
                setParentDeviceName("worse");
            }}
        ));

        Map<String, HostDevice> filteredDevices = RefreshHostDevicesCommand.filterOrphanedDevices(fetchedDevices);

        Map<String, HostDevice> expectedDevices = Entities.entitiesByName(Arrays.asList(
            new HostDevice() {{
                setDeviceName("computer");
                setParentDeviceName("computer");
            }},
            new HostDevice() {{
                setDeviceName("good");
                setParentDeviceName("computer");
            }},
            new HostDevice() {{
                setDeviceName("child_of_good");
                setParentDeviceName("good");
            }}
        ));

        assertEquals(expectedDevices, filteredDevices);
    }
}
