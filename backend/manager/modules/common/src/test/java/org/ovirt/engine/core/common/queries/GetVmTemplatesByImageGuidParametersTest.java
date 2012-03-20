package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

/** A test case for the {@link GetVmTemplatesByImageGuidParameters} class. */
public class GetVmTemplatesByImageGuidParametersTest {

    @Test
    public void testEmptyConstructor() {
        GetVmTemplatesByImageGuidParameters params = new GetVmTemplatesByImageGuidParameters();
        assertEquals("Default constructor should use empty GUID", Guid.Empty, params.getImageGuid());
    }

    @Test
    public void testGuidConstructor() {
        Guid expected = Guid.NewGuid();
        GetVmTemplatesByImageGuidParameters params = new GetVmTemplatesByImageGuidParameters(expected);
        assertEquals("Default constructor should use empty GUID", expected, params.getImageGuid());
    }
}
