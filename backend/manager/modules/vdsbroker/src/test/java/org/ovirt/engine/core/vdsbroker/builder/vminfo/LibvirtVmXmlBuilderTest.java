package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.ovirt.engine.core.vdsbroker.builder.vminfo.LibvirtVmXmlBuilder.adjustSpiceSecureChannels;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class LibvirtVmXmlBuilderTest {

    @Test
    public void testSpiceSecureChannelsAdjustment() {
        String[] channels = new String[]{"smain", "dog", "", "sinputs", "scursor", "display", "scat", "smartcard", "splayback"};
        List<String> adjustedChannels = adjustSpiceSecureChannels(channels).collect(Collectors.toList());
        assertEquals(Arrays.asList("main", "inputs", "cursor", "display", "smartcard", "playback"), adjustedChannels);
    }
}
