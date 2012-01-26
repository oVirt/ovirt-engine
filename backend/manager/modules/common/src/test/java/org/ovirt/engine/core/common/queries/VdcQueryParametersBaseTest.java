package org.ovirt.engine.core.common.queries;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class VdcQueryParametersBaseTest {

    /** The object to test */
    private VdcQueryParametersBase base;

    @Before
    public void setUp() {
        base = new VdcQueryParametersBase();
    }

    @Test
    public void testRunAsUser() {
        assertFalse("By default, query should not be run as user", base.isRunAsUser());

        base.setRunAsUser(true);
        assertTrue("Query should have been set to be run as user", base.isRunAsUser());

        base.setRunAsUser(false);
        assertFalse("Query should have been set to be NOT run as user", base.isRunAsUser());
    }
}
