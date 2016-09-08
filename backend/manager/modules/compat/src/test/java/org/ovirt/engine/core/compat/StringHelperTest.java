package org.ovirt.engine.core.compat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class StringHelperTest {
    @Test
    public void join() {
        assertEquals("foo,bar,baz", StringHelper.join(",", new String[]{"foo", "bar", "baz"}));
        assertEquals("foobarbaz", StringHelper.join(null, new String[]{"foo", "bar", "baz"}));

        //TODO: is this the intended behavior?
        assertEquals("foo,,bar", StringHelper.join(",", new String[]{"foo", null, "bar"}));
        assertNull(StringHelper.join(null, null));
    }
}
