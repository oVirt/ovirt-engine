package org.ovirt.engine.core.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class IntegerCompatTest {
    @Test
    public void tryParse() {
        assertNull(IntegerCompat.tryParse(""));
        assertNull(IntegerCompat.tryParse("no good"));
        assertNull(IntegerCompat.tryParse("$1"));

        assertEquals(Integer.valueOf(1), IntegerCompat.tryParse("1"));
        assertEquals(Integer.valueOf(-1), IntegerCompat.tryParse("-1"));
        assertEquals(Integer.valueOf(0), IntegerCompat.tryParse("0"));
    }
}
