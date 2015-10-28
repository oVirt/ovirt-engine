package org.ovirt.engine.core.common.businessentities.network;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BondModeTest {

    @Test
    public void testGetBondMode() {

        for (BondMode bondMode : BondMode.values()){
            assertEquals(bondMode, BondMode.getBondMode("mode=" + bondMode.getValue()));
            assertEquals(bondMode, BondMode.getBondMode("  mode=" + bondMode.getValue() + "   "));
            assertEquals(bondMode, BondMode.getBondMode("prefix=0 mode=" + bondMode.getValue() + " suffix=0"));
            assertEquals(bondMode, BondMode.getBondMode(" othermode=7 mode=" + bondMode.getValue() + "   othermode=7"));

        }
        assertEquals(null, BondMode.getBondMode(" othermode=7 mode  =   4   othermode=7"));
        assertEquals(null, BondMode.getBondMode("  mode  =   4   "));
        assertEquals(null, BondMode.getBondMode("no bond mode"));
        assertEquals(null, BondMode.getBondMode("prefixmode=4"));
        assertEquals(null, BondMode.getBondMode("mode=4suffix"));
        assertEquals(null, BondMode.getBondMode("mode=nomode"));
        assertEquals(null, BondMode.getBondMode("mode=17"));
        assertEquals(null, BondMode.getBondMode("mode17"));

    }
}
