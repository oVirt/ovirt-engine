package org.ovirt.engine.core.compat;

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {
    @Test
    public void testToString() {
        Assert.assertEquals("1.0", new Version("1.0").toString());
        Assert.assertEquals("1.0", new Version("1.0").toString());
        Assert.assertEquals("1.2.3", new Version(1, 2, 3).toString());
    }

    @Test
    public void equals() {
        Assert.assertEquals(new Version(), new Version());
        Assert.assertEquals(new Version(1, 2), new Version(1, 2));
        Assert.assertEquals(new Version(1, 2), new Version("1.2"));
        Assert.assertEquals(new Version(1, 2, 3), new Version("1.2.3"));
        Assert.assertEquals(new Version(1, 2, 3, 4), new Version("1.2.3.4"));
        // nulls and other data types
        Assert.assertFalse(new Version().equals(null));
        Assert.assertFalse(new Version().equals("foo"));
        Assert.assertFalse(new Version().equals(1d));
    }
}
