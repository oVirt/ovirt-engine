package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.ovirt.engine.core.common.queries.VdcQueryType.VdcQueryAuthType;

/** A test case for the {@link VdcQueryType} class. */
public class VdcQueryTypeTest {

    @Test
    public void testForValue() {
        assertEquals("wrong value",
                VdcQueryType.forValue(VdcQueryType.GetAgentFenceOptions.getValue()),
                VdcQueryType.GetAgentFenceOptions);
    }

    @Test
    public void testAuthTypes() {
        assertEquals("Unknown should not be an admin query", VdcQueryAuthType.User, VdcQueryType.Unknown.getAuthType());
        assertFalse("Unknown should not be an admin query", VdcQueryType.Unknown.isAdmin());
    }
}
