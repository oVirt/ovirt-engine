package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

/** A test case for the {@link GetAllRelevantQuotasForStorageParameters} class. */
public class GetAllRelevantQuotasForVdsGroupParametersTest {

    @Test
    public void testEmptyConstructor() {
        GetAllRelevantQuotasForVdsGroupParameters params = new GetAllRelevantQuotasForVdsGroupParameters();
        assertEquals("Default constructor should use empty GUID", Guid.Empty, params.getVdsGroupId());
    }

    @Test
    public void testGuidConstructor() {
        Guid expected = Guid.NewGuid();
        GetAllRelevantQuotasForVdsGroupParameters params = new GetAllRelevantQuotasForVdsGroupParameters(expected);
        assertEquals("parameterized constructor has the wrong GUID", expected, params.getVdsGroupId());
    }
}
