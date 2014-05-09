package org.ovirt.engine.core.common.businessentities;

import org.junit.Assert;
import org.junit.Test;

public class StorageFormatTypeTest {
    @Test
    public void forValue() {
        for(String val : new String[]{"0", "2", "3"}) {
            Assert.assertNotNull(StorageFormatType.forValue(val));
        }
        Assert.assertNull(StorageFormatType.forValue(null));
        Assert.assertNull(StorageFormatType.forValue("intentionally_notexisting"));
    }
}
