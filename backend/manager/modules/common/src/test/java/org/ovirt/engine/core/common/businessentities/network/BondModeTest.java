package org.ovirt.engine.core.common.businessentities.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

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
        assertNull(BondMode.parseBondMode(" othermode=7 mode  =   4   othermode=7"));
        assertNull(BondMode.parseBondMode("  mode  =   4   "));
        assertNull(BondMode.parseBondMode("no bond mode"));
        assertNull(BondMode.parseBondMode("prefixmode=4"));
        assertNull(BondMode.parseBondMode("mode=4suffix"));
        assertNull(BondMode.parseBondMode("mode=nomode"));
        assertNull(BondMode.parseBondMode("mode=17"));
        assertNull(BondMode.parseBondMode("mode="));
        assertNull(BondMode.parseBondMode("mode= "));
        assertNull(BondMode.parseBondMode("mode17"));
    }

    @Test
    public void testGetBondMode() {
        for (BondMode bondMode : BondMode.values()){
            assertEquals(bondMode, BondMode.getBondMode(bondMode.getValue()));
            assertEquals(bondMode, BondMode.getBondMode(bondMode.getStringValue()));
            assertNull(BondMode.getBondMode("mode=" + bondMode.getValue()));
            assertNull(BondMode.getBondMode("mode=" + bondMode.getStringValue()));
        }
    }
}
