package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

/** A test case for the {@link GetAllRelevantQuotasForStorageParameters} class. */
public class GetAllRelevantQuotasForStorageParametersTest {

    @Test
    public void testEmptyConstructor() {
        GetAllRelevantQuotasForStorageParameters params = new GetAllRelevantQuotasForStorageParameters();
        assertEquals("Default constructor should use empty GUID", Guid.Empty, params.getStorageId());
    }

    @Test
    public void testGuidConstructor() {
        Guid expected = Guid.NewGuid();
        GetAllRelevantQuotasForStorageParameters params = new GetAllRelevantQuotasForStorageParameters(expected);
        assertEquals("parameterized constructor has the wrong GUID", expected, params.getStorageId());
    }
}
