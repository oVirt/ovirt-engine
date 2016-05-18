package org.ovirt.engine.core.common.businessentities.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ovirt.engine.core.compat.Guid;

@RunWith(Parameterized.class)
public class DiskVmElementTest {

    private DiskVmElement dve;

    public DiskVmElementTest(Guid diskId, Guid vmId, boolean boot) {
        dve = new DiskVmElement(diskId, vmId);
        dve.setBoot(boot);
    }

    @Parameters
    public static Collection<Object[]> namesParams() {
        return Arrays.asList(new Object[][] {
                {Guid.newGuid(), Guid.newGuid(), false},
                {Guid.newGuid(), Guid.newGuid(), true}
        });
    }

    @Test
    public void testCopyOf() {
        DiskVmElement copy = DiskVmElement.copyOf(dve);
        assertEquals(copy, dve);
        assertNotSame(copy, dve);
    }
}
