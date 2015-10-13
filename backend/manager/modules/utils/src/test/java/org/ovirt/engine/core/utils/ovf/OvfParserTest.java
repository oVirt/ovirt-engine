package org.ovirt.engine.core.utils.ovf;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class OvfParserTest {

    @Test
    public void UtcDateStringToLocaDate_nodep() {
        Date date = OvfParser.UtcDateStringToLocaDate("1984/06/19 14:25:11");
        Assert.assertEquals(456503111000L, date.getTime());
    }
}
