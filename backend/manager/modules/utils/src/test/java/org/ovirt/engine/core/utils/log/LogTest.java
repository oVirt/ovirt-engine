package org.ovirt.engine.core.utils.log;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LogTest {
    Log log = LogFactory.getLog(LogTest.class);

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
