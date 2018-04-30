package org.ovirt.engine.core.common.queries;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class QueryParametersBaseTest {

    /** The object to test */
    private QueryParametersBase base;

    @BeforeEach
    public void setUp() {
        base = new QueryParametersBase();
    }

    @Test
    public void testIsFiltered() {
        assertFalse(base.isFiltered(), "By default, query should not be run as user");

        base.setFiltered(true);
        assertTrue(base.isFiltered(), "Query should have been set to be run as user");

        base.setFiltered(false);
        assertFalse(base.isFiltered(), "Query should have been set to be NOT run as user");
    }
}
