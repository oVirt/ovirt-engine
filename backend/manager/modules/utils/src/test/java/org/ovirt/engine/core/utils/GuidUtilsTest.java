package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;

import junit.framework.TestCase;

public class GuidUtilsTest extends TestCase {

    public void testIsGuidWorking() {
        RefObject<Guid> gRef = new RefObject<Guid>();
        boolean returnValue = GuidUtils.isGuid("11111111-1111-1111-1111-111111111111", gRef);
        assertTrue("It should be a Guid", returnValue);
        assertNotNull("I should have a guid", gRef.argvalue);
    }

    public void testIsGuidFailing() {
        RefObject<Guid> gRef = new RefObject<Guid>();
        boolean returnValue = GuidUtils.isGuid("11111111-JJJJ-1111-1111-111111111111", gRef);
        assertFalse("It should not be a Guid", returnValue);
        assertNotNull("It should not have a guid", gRef.argvalue);
        assertEquals("The guid should be the empty Guid", org.ovirt.engine.core.compat.Guid.Empty, gRef.argvalue);
    }
}
