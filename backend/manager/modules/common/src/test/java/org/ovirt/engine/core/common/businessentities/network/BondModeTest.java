package org.ovirt.engine.core.common.businessentities.network;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class BondModeTest {

    @Test
    public void testParseBondMode() {

        for (BondMode bondMode : BondMode.values()){
            assertEquals(bondMode, BondMode.parseBondMode("mode=" + bondMode.getValue()));
            assertEquals(bondMode, BondMode.parseBondMode("mode=" + bondMode.getStringValue()));
            assertEquals(bondMode, BondMode.parseBondMode("  mode=" + bondMode.getValue() + "   "));
            assertEquals(bondMode, BondMode.parseBondMode("prefix=0 mode=" + bondMode.getValue() + " suffix=0"));
            assertEquals(bondMode, BondMode.parseBondMode(" othermode=7 mode=" + bondMode.getValue() + "   othermode=7"));

        }
        assertEquals(null, BondMode.parseBondMode(" othermode=7 mode  =   4   othermode=7"));
        assertEquals(null, BondMode.parseBondMode("  mode  =   4   "));
        assertEquals(null, BondMode.parseBondMode("no bond mode"));
        assertEquals(null, BondMode.parseBondMode("prefixmode=4"));
        assertEquals(null, BondMode.parseBondMode("mode=4suffix"));
        assertEquals(null, BondMode.parseBondMode("mode=nomode"));
        assertEquals(null, BondMode.parseBondMode("mode=17"));
        assertEquals(null, BondMode.parseBondMode("mode="));
        assertEquals(null, BondMode.parseBondMode("mode= "));
        assertEquals(null, BondMode.parseBondMode("mode17"));
    }

    @Test
    public void testGetBondMode() {
        for (BondMode bondMode : BondMode.values()){
            assertEquals(bondMode, BondMode.getBondMode(bondMode.getValue()));
            assertEquals(bondMode, BondMode.getBondMode(bondMode.getStringValue()));
            assertEquals(null, BondMode.getBondMode("mode=" + bondMode.getValue()));
            assertEquals(null, BondMode.getBondMode("mode=" + bondMode.getStringValue()));
        }
    }
}
