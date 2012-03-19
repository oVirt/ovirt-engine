package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

/** A test case for the {@link GetVmsByImageGuidParameters} class. */
public class GetVmsByImageGuidParametersTest {

    @Test
    public void testEmptyConstructor() {
        GetVmsByImageGuidParameters params = new GetVmsByImageGuidParameters();
        assertEquals("Default constructor should use empty GUID", Guid.Empty, params.getImageGuid());
    }

    @Test
    public void testGuidConstructor() {
        Guid expected = Guid.NewGuid();
        GetVmsByImageGuidParameters params = new GetVmsByImageGuidParameters(expected);
        assertEquals("Default constructor should use empty GUID", expected, params.getImageGuid());
    }
}
