package org.ovirt.engine.core.utils;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class GuidUtilsTest {
    @Test
    public void toByteArray() {
        final byte[] byteArray = GuidUtils.ToByteArray(UUID.randomUUID());
        Assert.assertNotNull(byteArray);
        Assert.assertEquals(16, byteArray.length);
    }

    @Test
    public void toByteArrayAllNoll() {
        final byte[] allNullArray = GuidUtils.ToByteArray(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        Assert.assertNotNull(allNullArray);
        for (int i = 0; i < 16; i++) {
            Assert.assertEquals(0, allNullArray[i]);
        }
    }
}
