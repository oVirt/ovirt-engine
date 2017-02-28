package org.ovirt.engine.core.compat;

import org.junit.Assert;
import org.junit.Test;

public class LongCompatTest {

    @Test
    public void parseValidValuesTest() {
        Assert.assertEquals(Long.valueOf(1), LongCompat.tryParse("1"));
        Assert.assertEquals(Long.valueOf(-1), LongCompat.tryParse("-1"));
        Assert.assertEquals(Long.valueOf(0), LongCompat.tryParse("0"));
    }

    @Test
    public void parseInvalidValuesTest() {
        Assert.assertNull(LongCompat.tryParse(""));
        Assert.assertNull(LongCompat.tryParse("no good"));
        Assert.assertNull(LongCompat.tryParse("$1"));
    }
}
