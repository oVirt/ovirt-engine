package org.ovirt.engine.core.compat;

import org.junit.Assert;
import org.junit.Test;

public class IntegerCompatTest {
    @Test
    public void tryParse() {
        Assert.assertNull(IntegerCompat.tryParse(""));
        Assert.assertNull(IntegerCompat.tryParse("no good"));
        Assert.assertNull(IntegerCompat.tryParse("$1"));

        Assert.assertEquals(Integer.valueOf(1), IntegerCompat.tryParse("1"));
        Assert.assertEquals(Integer.valueOf(-1), IntegerCompat.tryParse("-1"));
        Assert.assertEquals(Integer.valueOf(0), IntegerCompat.tryParse("0"));
    }
}
