package org.ovirt.engine.ui.common.uicommon;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractSpiceTest {
    @Test
    public void testAdjustLegacySecureChannels() throws Exception {
        String legacyChannels = "smain,sdisplay,sinputs,scursor,splayback,srecord,ssmartcard,susbredir"; //$NON-NLS-1$
        String correctChannels = "main,display,inputs,cursor,playback,record,smartcard,usbredir";//$NON-NLS-1$

        assertEquals(correctChannels, AbstractSpice.adjustLegacySecureChannels(legacyChannels));
    }

    @Test
    public void testAdjustLegacySecureChannelsWithEmptyChannels() throws Exception {
        String legacyChannels = "";//$NON-NLS-1$
        String correctChannels = "";//$NON-NLS-1$
        assertEquals(correctChannels, AbstractSpice.adjustLegacySecureChannels(legacyChannels));
    }

    @Test
    public void testAdjustLegacySecureChannelsWithNullChannels() throws Exception {
        String legacyChannels = null;
        String correctChannels = null;
        assertEquals(correctChannels, AbstractSpice.adjustLegacySecureChannels(legacyChannels));
    }
}
