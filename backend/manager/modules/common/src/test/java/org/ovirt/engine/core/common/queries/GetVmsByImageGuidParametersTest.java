package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

/** A test case for the {@link GetVmsByDiskGuidParameters} class. */
public class GetVmsByImageGuidParametersTest {

    @Test
    public void testEmptyConstructor() {
        GetVmsByDiskGuidParameters params = new GetVmsByDiskGuidParameters();
        assertEquals("Default constructor should use empty GUID", Guid.Empty, params.getDiskGuid());
    }

    @Test
    public void testGuidConstructor() {
        Guid expected = Guid.NewGuid();
        GetVmsByDiskGuidParameters params = new GetVmsByDiskGuidParameters(expected);
        assertEquals("Default constructor should use empty GUID", expected, params.getDiskGuid());
    }
}
