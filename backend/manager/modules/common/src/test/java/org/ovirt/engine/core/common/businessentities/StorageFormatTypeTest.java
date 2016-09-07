package org.ovirt.engine.core.common.businessentities;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
