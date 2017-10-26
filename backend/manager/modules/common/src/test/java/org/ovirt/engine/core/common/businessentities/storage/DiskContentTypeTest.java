package org.ovirt.engine.core.common.businessentities.storage;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DiskContentTypeTest {
    @Test
    public void testStorageNameLength() {
        for (DiskContentType d : DiskContentType.values()) {
            assertEquals(4, d.getStorageValue().length());
        }
    }
}
