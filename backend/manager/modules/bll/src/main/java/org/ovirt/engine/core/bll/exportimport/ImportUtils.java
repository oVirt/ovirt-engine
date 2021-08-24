package org.ovirt.engine.core.bll.exportimport;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

@Singleton
public class ImportUtils {
    @Inject
    private OsRepository osRepository;

    /**
     * Checks whether imported VM/Template has Graphics devices.
     *  - If the VM/Template has Video devices and no Graphics devices, this method
     *    adds compatible graphics device to devices map (This could mean
     *    the exported VM/Template has been created when we didn't have Graphics
     *    devices).
     *  - If the VM/Template has no Video devices, no Graphics devices are added
     *    (we assume headless VM/Template).
     */
    public void updateGraphicsDevices(VmBase vmBase, Version clusterVersion) {
        if (vmBase == null || vmBase.getManagedDeviceMap() == null || clusterVersion == null) {
            return;
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

    private List<VmDevice> getDevicesOfType(VmDeviceGeneralType type, Map<Guid, VmDevice> managedDevicesMap) {
        return managedDevicesMap.values().stream().filter(device -> device.getType() == type).collect(toList());
    }

    private GraphicsDevice getCompatibleGraphics(VmDeviceType videoDeviceType, Version clusterVersion, VmBase vmBase) {
        GraphicsDevice graphicsDevice = null;
        GraphicsType compatibleType = null;

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
