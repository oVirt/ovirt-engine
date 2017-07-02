package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.ovirt.engine.core.common.queries.QueryType.VdcQueryAuthType;

/** A test case for the {@link QueryType} class. */
public class QueryTypeTest {

    @Test
    public void testForValue() {
        assertEquals("wrong value",
                QueryType.GetAgentFenceOptions,
                QueryType.forValue(QueryType.GetAgentFenceOptions.getValue()));
    }

    @Test
    public void testAuthTypes() {
        assertEquals("Unknown should not be an admin query", VdcQueryAuthType.User, QueryType.Unknown.getAuthType());
        assertFalse("Unknown should not be an admin query", QueryType.Unknown.isAdmin());
    }
}
