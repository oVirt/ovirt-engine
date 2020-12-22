package org.ovirt.engine.core.bll.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

/**
 * Video settings for clusters 3.6 and newer (they may change in future versions).
 */
@Singleton
public class VgamemVideoSettings {

    @Inject
    private OsRepository osRepository;

    // Displays up to 4 Mpix fit into this; higher resolutions are
    // supported only on Windows 10, handled by vgamemMultiplier.
    private static final int BASE_RAM_SIZE = 16384; // KB
    private static final int DEFAULT_VRAM_SIZE = 8192; // KB
    private static final int RAM_MULTIPLIER = 4;

    /**
     * Returns video device settings for QXL devices.
     *
     * @return a map of device settings
     */
    public Map<String, Integer> getQxlVideoDeviceSettings(VmBase vmBase, boolean isSingleQxlPci) {
        // Things are likely to completely change in future, so let's keep this
        // computation as simple as possible for now.
        Map<String, Integer> settings = new HashMap<>();
        int heads = isSingleQxlPci ? vmBase.getNumOfMonitors() : 1;
        int baseRam = BASE_RAM_SIZE * heads;
        int vramMultiplier = getVramMultiplier(vmBase);
        int vram = vramMultiplier == 0 ? DEFAULT_VRAM_SIZE : vramMultiplier * baseRam;
        int vgamem = getVgamemMultiplier(vmBase) * baseRam;
        settings.put(VdsProperties.VIDEO_HEADS, heads);
        settings.put(VdsProperties.VIDEO_VGAMEM, vgamem);
        settings.put(VdsProperties.VIDEO_RAM, RAM_MULTIPLIER * baseRam);
        settings.put(VdsProperties.VIDEO_VRAM, vram);
        return settings;
    }

    /**
     * Returns video device settings for VGA and Cirrus devices.
     *
     * @return a map of device settings
     */
    public Map<String, Integer> getVgaVideoDeviceSettings() {
        // No multihead, no special driver requirements, the base value should
        // just work.  Except for high resolutions on Wayland (requires twice
        // as much video RAM as other systems due to page flipping); not likely
        // to be used with vga or cirrus so we don't care about that here.
        return Collections.singletonMap(VdsProperties.VIDEO_VRAM, BASE_RAM_SIZE);
    }

    private int getVramMultiplier(VmBase vmBase) {
        return osRepository.getVramMultiplier(vmBase.getOsId());
    }

    private int getVgamemMultiplier(VmBase vmBase) {
        return osRepository.getVgamemMultiplier(vmBase.getOsId());
    }
}
