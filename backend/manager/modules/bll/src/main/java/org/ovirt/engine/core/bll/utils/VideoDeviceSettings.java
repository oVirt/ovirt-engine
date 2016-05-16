package org.ovirt.engine.core.bll.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class VideoDeviceSettings {

    /**
     * Video settings for clusters 3.6 and newer (they may change in future versions).
     */
    private static class VgamemVideoSettings {

        // Displays up to 4 Mpix fit into this; higher resolutions are currently
        // not supported by the drivers.
        private static final int BASE_RAM_SIZE = 16384; // KB
        private static final int DEFAULT_VRAM_SIZE = 8192; // KB
        private static final int RAM_MULTIPLIER = 4;

        /**
         * Returns video device settings for QXL devices.
         *
         * @return a map of device settings
         */
        public static Map<String, Integer> getQxlVideoDeviceSettings(VmBase vmBase) {
            // Things are likely to completely change in future, so let's keep this
            // computation as simple as possible for now.
            Map<String, Integer> settings = new HashMap<>();
            int heads = vmBase.getSingleQxlPci() ? vmBase.getNumOfMonitors() : 1;
            int vgamem = BASE_RAM_SIZE * heads;
            int vramMultiplier = getVramMultiplier(vmBase);
            int vram = vramMultiplier == 0 ? DEFAULT_VRAM_SIZE : vramMultiplier * vgamem;
            settings.put(VdsProperties.VIDEO_HEADS, heads);
            settings.put(VdsProperties.VIDEO_VGAMEM, vgamem);
            settings.put(VdsProperties.VIDEO_RAM, RAM_MULTIPLIER * vgamem);
            settings.put(VdsProperties.VIDEO_VRAM, vram);
            return settings;
        }

        /**
         * Returns video device settings for VGA and Cirrus devices.
         *
         * @return a map of device settings
         */
        public static Map<String, Integer> getVgaVideoDeviceSettings() {
            // No multihead, no special driver requirements, the base value should
            // just work.  Except for high resolutions on Wayland (requires twice
            // as much video RAM as other systems due to page flipping); not likely
            // to be used with vga or cirrus so we don't care about that here.
            return Collections.singletonMap(VdsProperties.VIDEO_VRAM, BASE_RAM_SIZE);
        }

        private static int getVramMultiplier(VmBase vmBase) {
            OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
            return osRepository.getVramMultiplier(vmBase.getOsId());
        }

    }

    /**
     * Legacy video settings, for clusters older than 3.6.
     * Not necesessarily correct but this is what the engine used to send to the hosts,
     * so it's best to reuse the values in order not to break old setups.
     */
    private static class LegacyVideoSettings {

        private static final int BASE_RAM_SIZE = 65536; // KB
        private static final int VRAM_SIZE = 32768; // KB

        public static Map<String, Integer> getVideoDeviceSettings(VmBase vmBase) {
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

    private static Map<String, Integer> getVideoDeviceSettings(VmBase vmBase) {
        Version vmVersion = vmBase.getCustomCompatibilityVersion();
        Supplier<Version> clusterVersionSupplier = () -> ClusterUtils.getCompatibilityVersion(vmBase);
        if (CompatibilityVersionUtils.getEffective(vmVersion, clusterVersionSupplier).greaterOrEquals(Version.v3_6)) {
            if (vmBase.getDefaultDisplayType() == DisplayType.qxl) {
                return VgamemVideoSettings.getQxlVideoDeviceSettings(vmBase);
            } else {
                return VgamemVideoSettings.getVgaVideoDeviceSettings();
            }
        } else {
            return LegacyVideoSettings.getVideoDeviceSettings(vmBase);
        }
    }

    /**
     * Returns video device spec params.
     *
     * @return a map of device parameters
     */
    public static Map<String, Object> getVideoDeviceSpecParams(VmBase vmBase) {
        return getVideoDeviceSettings(vmBase).entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
    }

    /**
     * Returns total video RAM size to be allocated for the given VM.
     *
     * @param vm the VM to compute the video RAM size for
     * @return size of the video RAM in MiB
     */
    public static int totalVideoRAMSizeMb(VM vm) {
        Map<String, Integer> settings = getVideoDeviceSettings(vm.getStaticData());
        return (settings.getOrDefault(VdsProperties.VIDEO_RAM, 0) +
                settings.getOrDefault(VdsProperties.VIDEO_VRAM, 0) + 1023) / 1024;
    }

}
