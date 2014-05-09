package org.ovirt.engine.core.compat;

import org.junit.Assert;
import org.junit.Test;

public class StringHelperTest {
    @Test
    public void join() {
        Assert.assertEquals("foo,bar,baz", StringHelper.join(",", new String[]{"foo", "bar", "baz"}));
        Assert.assertEquals("foobarbaz", StringHelper.join(null, new String[]{"foo", "bar", "baz"}));

        //TODO: is this the intended behavior?
        Assert.assertEquals("foo,,bar", StringHelper.join(",", new String[]{"foo", null, "bar"}));
        Assert.assertEquals(null, StringHelper.join(null, null));
    }
}
