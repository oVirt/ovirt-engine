package org.ovirt.engine.core.common.console;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConsoleOptionsTest {

    @Test
    public void testAdjustLegacySecureChannels() throws Exception {
        String legacyChannels = "smain,sdisplay,sinputs,scursor,splayback,srecord,ssmartcard,susbredir"; //$NON-NLS-1$
        String correctChannels = "main,display,inputs,cursor,playback,record,smartcard,usbredir";//$NON-NLS-1$

        assertEquals(correctChannels, ConsoleOptions.adjustLegacySecureChannels(legacyChannels));
    }

    @Test
    public void testAdjustLegacySecureChannelsWithEmptyChannels() throws Exception {
        String legacyChannels = "";//$NON-NLS-1$
        String correctChannels = "";//$NON-NLS-1$
        assertEquals(correctChannels, ConsoleOptions.adjustLegacySecureChannels(legacyChannels));
    }

    @Test
    public void testAdjustLegacySecureChannelsWithNullChannels() throws Exception {
        String legacyChannels = null;
        String correctChannels = null;
        assertEquals(correctChannels, ConsoleOptions.adjustLegacySecureChannels(legacyChannels));
    }

}
