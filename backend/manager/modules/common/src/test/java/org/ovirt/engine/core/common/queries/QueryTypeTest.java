package org.ovirt.engine.core.common.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.queries.QueryType.QueryAuthType;

/** A test case for the {@link QueryType} class. */
public class QueryTypeTest {

    @Test
    public void testForValue() {
        assertEquals(
                QueryType.GetAgentFenceOptions,
                QueryType.forValue(QueryType.GetAgentFenceOptions.getValue()), "wrong value");
    }

    @Test
    public void testAuthTypes() {
        assertEquals(QueryAuthType.User, QueryType.Unknown.getAuthType(),
                "Unknown should not be an admin query");
        assertFalse(QueryType.Unknown.isAdmin(), "Unknown should not be an admin query");
    }
}
