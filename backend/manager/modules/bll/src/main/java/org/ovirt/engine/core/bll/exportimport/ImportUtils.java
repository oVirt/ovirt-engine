package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class ImportUtils {

    /**
     * Checks whether imported VM/Template has Graphics devices.
     *  - If the VM/Template has Video devices and no Graphics devices, this method
     *    adds compatible graphics device to devices map (This could mean
     *    the exported VM/Template has been created when we didn't have Graphics
     *    devices).
     *  - If the VM/Template has no Video devices, no Graphics devices are added
     *    (we assume headless VM/Template).
     */
    public static void updateGraphicsDevices(VmBase vmBase, Version clusterVersion) {
        if (vmBase == null || vmBase.getManagedDeviceMap() == null || clusterVersion == null) {
            return;
        }

        if (Version.v4_0.lessOrEquals(clusterVersion)) {
            if (removeVideoDevice(VmDeviceType.VNC, VmDeviceType.CIRRUS, vmBase.getManagedDeviceMap())) {
                vmBase.setDefaultDisplayType(DisplayType.vga);
                VmDeviceCommonUtils.addVideoDevice(vmBase);
            }
        }

        List<VmDevice> videoDevs = getDevicesOfType(VmDeviceGeneralType.VIDEO, vmBase.getManagedDeviceMap());
        boolean hasNoGraphics = getDevicesOfType(VmDeviceGeneralType.GRAPHICS, vmBase.getManagedDeviceMap()).isEmpty();

        if (!videoDevs.isEmpty() && hasNoGraphics) {
            GraphicsDevice compatibleGraphics = getCompatibleGraphics(VmDeviceType.getByName(videoDevs.get(0).getDevice()), clusterVersion, vmBase);

            if (compatibleGraphics != null) {
                vmBase.getManagedDeviceMap().put(compatibleGraphics.getDeviceId(), compatibleGraphics);
            }
        }
    }

    private static boolean removeVideoDevice(VmDeviceType whenGraphicsExists, VmDeviceType videoToRemove,
                                                Map<Guid, VmDevice> managedDevicesMap) {
        if (VmDeviceCommonUtils.isVmDeviceExists(managedDevicesMap, whenGraphicsExists)) {
            Guid key = null;
            for (Map.Entry<Guid, VmDevice> graphicsDevice : managedDevicesMap.entrySet()) {
                if (videoToRemove.getName().equals(graphicsDevice.getValue().getDevice())) {
                    key = graphicsDevice.getKey();
                    break;
                }
            }
            if (key != null) {
                managedDevicesMap.remove(key);
                return true;
            }
        }
        return false;
    }

    private static List<VmDevice> getDevicesOfType(VmDeviceGeneralType type, Map<Guid, VmDevice> managedDevicesMap) {
        List<VmDevice> devices = new ArrayList<>();

        for (VmDevice vmDevice : managedDevicesMap.values()) {
            if (vmDevice.getType() == type) {
                devices.add(vmDevice);
            }
        }

        return devices;
    }

    private static GraphicsDevice getCompatibleGraphics(VmDeviceType videoDeviceType, Version clusterVersion, VmBase vmBase) {
        GraphicsDevice graphicsDevice = null;
        GraphicsType compatibleType = null;

        OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
        for (Pair<GraphicsType, DisplayType> graphicsDisplayPair : osRepository.getGraphicsAndDisplays(vmBase.getOsId(), clusterVersion)) {
            if (graphicsDisplayPair.getSecond().getDefaultVmDeviceType() == videoDeviceType) {
                compatibleType = graphicsDisplayPair.getFirst();

                // previously to spice+vnc, QXL was only used by spice, so prefer spice if available
                if (videoDeviceType == VmDeviceType.QXL && compatibleType == GraphicsType.SPICE) {
                    break;
                }
            }
        }

        if (compatibleType != null) {
            graphicsDevice = new GraphicsDevice(compatibleType.getCorrespondingDeviceType());
            graphicsDevice.setId(new VmDeviceId(Guid.newGuid(), vmBase.getId()));
        }

        return graphicsDevice;
    }

}
