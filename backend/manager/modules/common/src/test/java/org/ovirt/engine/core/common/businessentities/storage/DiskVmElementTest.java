package org.ovirt.engine.core.common.businessentities.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.ovirt.engine.core.compat.Guid;

public class DiskVmElementTest {
    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    public void testCopyOf(boolean boot) {
        DiskVmElement dve = new DiskVmElement(Guid.newGuid(), Guid.newGuid());
        dve.setBoot(boot);
        DiskVmElement copy = DiskVmElement.copyOf(dve);
        assertEquals(copy, dve);
        assertNotSame(copy, dve);
    }
}
