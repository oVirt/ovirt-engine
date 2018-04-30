package org.ovirt.engine.core.common.console;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ConsoleOptionsTest {

    @Test
    public void testAdjustLegacySecureChannels() {
        String legacyChannels = "smain,sdisplay,sinputs,scursor,splayback,srecord,ssmartcard,susbredir"; //$NON-NLS-1$
        String correctChannels = "main,display,inputs,cursor,playback,record,smartcard,usbredir";//$NON-NLS-1$

        assertEquals(correctChannels, ConsoleOptions.adjustLegacySecureChannels(legacyChannels));
    }

    @Test
    public void testAdjustLegacySecureChannelsWithEmptyChannels() {
        String legacyChannels = "";//$NON-NLS-1$
        String correctChannels = "";//$NON-NLS-1$
        assertEquals(correctChannels, ConsoleOptions.adjustLegacySecureChannels(legacyChannels));
    }

    @Test
    public void testAdjustLegacySecureChannelsWithNullChannels() {
        String legacyChannels = null;
        String correctChannels = null;
        assertEquals(correctChannels, ConsoleOptions.adjustLegacySecureChannels(legacyChannels));
    }

}
