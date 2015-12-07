package org.ovirt.engine.core.bll.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;

public class VideoDeviceSettings {

    private interface VideoRAMSettings {
        /**
         * Returns video device spec params.
         *
         * @param vmBase
         * @return a map of device parameters
         */
        public abstract Map<String, Object> getVideoDeviceSpecParams(VmBase vmBase);
    }

    /**
     * Video settings for clusters 3.6 and newer (they may change in future versions).
     */
    private static class VgamemVideoRAMSettings implements VideoRAMSettings {

        // Displays up to 4 Mpix fit into this; higher resolutions are currently
        // not supported by the drivers.
        private final static int BASE_RAM_SIZE = 16384; // KB
        private final static int DEFAULT_VRAM_SIZE = 8192; // KB
        private final static int RAM_MULTIPLIER = 4;

        @Override
        public Map<String, Object> getVideoDeviceSpecParams(VmBase vmBase) {
            if (vmBase.getDefaultDisplayType() == DisplayType.qxl) {
                return getQxlVideoDeviceSpecParams(vmBase);
            } else {
                return getVgaVideoDeviceSpecParams();
            }
        }

        /**
         * Returns video device spec params for QXL devices.
         *
         * @param vmBase
         * @return a map of device parameters
         */
        private Map<String, Object> getQxlVideoDeviceSpecParams(VmBase vmBase) {
            // Things are likely to completely change in future, so let's keep this
            // computation as simple as possible for now.
            Map<String, Object> specParams = new HashMap<>();
            int heads = vmBase.getSingleQxlPci() ? vmBase.getNumOfMonitors() : 1;
            int vgamem = BASE_RAM_SIZE * heads;
            int vramMultiplier = getVramMultiplier(vmBase);
            int vram = (vramMultiplier == 0 ? DEFAULT_VRAM_SIZE : vramMultiplier * vgamem);
            specParams.put(VdsProperties.VIDEO_HEADS, String.valueOf(heads));
            specParams.put(VdsProperties.VIDEO_VGAMEM, String.valueOf(vgamem));
            specParams.put(VdsProperties.VIDEO_RAM, String.valueOf(RAM_MULTIPLIER * vgamem));
            specParams.put(VdsProperties.VIDEO_VRAM, String.valueOf(vram));
            return specParams;
        }

        /**
         * Returns video device spec params for VGA and Cirrus devices.
         *
         * @param vmBase
         * @return a map of device parameters
         */
        private Map<String, Object> getVgaVideoDeviceSpecParams() {
            // No multihead, no special driver requirements, the base value should
            // just work.  Except for high resolutions on Wayland (requires twice
            // as much video RAM as other systems due to page flipping); not likely
            // to be used with vga or cirrus so we don't care about that here.
            return Collections.singletonMap(VdsProperties.VIDEO_VRAM, String.valueOf(BASE_RAM_SIZE));
        }

        private int getVramMultiplier(VmBase vmBase) {
            OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
            return osRepository.getVramMultiplier(vmBase.getOsId());
        }
    }

    /**
     * Legacy video settings, for clusters older than 3.6.
     * Not necesessarily correct but this is what the engine used to send to the hosts,
     * so it's best to reuse the values in order not to break old setups.
     */
    private static class LegacyVideoRAMSettings implements VideoRAMSettings {

        private final static int BASE_RAM_SIZE = 65536; // KB
        private final static int VRAM_SIZE = 32768; // KB

        @Override
        public Map<String, Object> getVideoDeviceSpecParams(VmBase vmBase) {
            Map<String, Object> specParams = new HashMap<>();
            boolean singleQxlPci = vmBase.getSingleQxlPci();
            int numOfMonitors = vmBase.getNumOfMonitors();
            int heads = singleQxlPci ? numOfMonitors : 1;
            specParams.put(VdsProperties.VIDEO_HEADS, String.valueOf(heads));
            specParams.put(VdsProperties.VIDEO_VRAM, String.valueOf(VRAM_SIZE));
            if (singleQxlPci) {
                specParams.put(VdsProperties.VIDEO_RAM, String.valueOf(BASE_RAM_SIZE * heads));
            }
            return specParams;
        }
    }

    /**
     * Returns video device spec params.
     *
     * @param vmBase
     * @return a map of device parameters
     */
    public static Map<String, Object> getVideoDeviceSpecParams(VmBase vmBase) {
        return selectVideoRAMSettings(vmBase).getVideoDeviceSpecParams(vmBase);
    }

    private static VideoRAMSettings selectVideoRAMSettings(VmBase vmBase) {
        Version vmVersion = vmBase.getCustomCompatibilityVersion();
        Supplier<Version> clusterVersionSupplier = () -> ClusterUtils.getCompatibilityVersion(vmBase);
        if (CompatibilityVersionUtils.getEffective(vmVersion, clusterVersionSupplier).greaterOrEquals(new Version(3, 6))) {
            return new VgamemVideoRAMSettings();
        } else {
            return new LegacyVideoRAMSettings();
        }
    }
}
