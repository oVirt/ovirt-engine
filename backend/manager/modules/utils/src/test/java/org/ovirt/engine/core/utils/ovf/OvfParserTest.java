package org.ovirt.engine.core.utils.ovf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.junit.jupiter.api.Test;

public class OvfParserTest {

    @Test
    public void utcDateStringToLocaDateNoDep() {
        Date date = OvfParser.utcDateStringToLocalDate("1984/06/19 14:25:11");
        assertEquals(new Date(456503111000L), date);
    }
}
