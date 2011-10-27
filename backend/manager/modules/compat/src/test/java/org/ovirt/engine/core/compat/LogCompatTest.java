package org.ovirt.engine.core.compat;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LogCompatTest {
    LogCompat log = LogFactoryCompat.getLog(LogCompatTest.class);

    @Test
    public void testSimple() {
        assertEquals("Value is 1", log.transform("Value is 1"));
    }

    @Test
    public void testOne() {
        assertEquals("Value is 1", log.transform("Value is {0}", "1"));
    }

    @Test
    public void testTwo() {
        assertEquals("Value is 1,2", log.transform("Value is {0},{1}", "1", "2"));
    }

}
