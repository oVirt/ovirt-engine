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

    @Test
    public void compare() {
        Assert.assertTrue(Version.v3_5.compareTo(Version.v3_6) == -1);
        Assert.assertTrue(Version.v3_6.compareTo(Version.v3_5) == 1);
        Assert.assertTrue(Version.v3_6.compareTo(new Version("3.6")) == 0);
    }

    @Test
    public void biggerThan() {
        Assert.assertFalse(Version.v3_5.greater(Version.v3_6));
        Assert.assertTrue(Version.v3_6.greater(Version.v3_5));
    }
    @Test
    public void smallerThan() {
        Assert.assertTrue(Version.v3_5.less(Version.v3_6));
        Assert.assertFalse(Version.v3_6.less(Version.v3_5));
    }

    @Test
    public void biggerThanOrEquals() {
        Assert.assertFalse(Version.v3_5.greaterOrEquals(Version.v3_6));
        Assert.assertTrue(Version.v3_6.greaterOrEquals(Version.v3_5));
        Assert.assertTrue(Version.v3_6.greaterOrEquals(new Version("3.5")));
    }
    @Test
    public void smallerThanOrEquals() {
        Assert.assertTrue(Version.v3_5.lessOrEquals(new Version("3.5")));
        Assert.assertTrue(Version.v3_5.lessOrEquals(Version.v3_6));
        Assert.assertFalse(Version.v3_6.lessOrEquals(Version.v3_5));
    }
}
