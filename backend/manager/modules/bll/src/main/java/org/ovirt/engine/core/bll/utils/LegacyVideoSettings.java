package org.ovirt.engine.core.bll.utils;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

/**
 * Legacy video settings, for clusters older than 3.6.
 * Not necesessarily correct but this is what the engine used to send to the hosts,
 * so it's best to reuse the values in order not to break old setups.
 */
@Singleton
public class LegacyVideoSettings {

    private static final int BASE_RAM_SIZE = 65536; // KB
    private static final int VRAM_SIZE = 32768; // KB

    public Map<String, Integer> getVideoDeviceSettings(VmBase vmBase) {
        Map<String, Integer> settings = new HashMap<>();
        boolean singleQxlPci = vmBase.getSingleQxlPci();
        int numOfMonitors = vmBase.getNumOfMonitors();
        int heads = singleQxlPci ? numOfMonitors : 1;
        settings.put(VdsProperties.VIDEO_HEADS, heads);
        settings.put(VdsProperties.VIDEO_VRAM, VRAM_SIZE);
        if (singleQxlPci) {
            settings.put(VdsProperties.VIDEO_RAM, BASE_RAM_SIZE * heads);
        }
        return settings;
    }

}
