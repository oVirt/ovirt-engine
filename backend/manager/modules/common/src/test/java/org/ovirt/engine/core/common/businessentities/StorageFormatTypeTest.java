package org.ovirt.engine.core.common.businessentities;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class StorageFormatTypeTest {
    @Test
    public void forValue() {
        for(String val : new String[]{"0", "2", "3"}) {
            assertNotNull(StorageFormatType.forValue(val));
        }
        assertNull(StorageFormatType.forValue(null));
        assertNull(StorageFormatType.forValue("intentionally_notexisting"));
    }
}
