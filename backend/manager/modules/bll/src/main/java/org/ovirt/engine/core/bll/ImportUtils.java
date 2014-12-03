package org.ovirt.engine.core.bll;

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
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
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
        List<VmDevice> videoDevs = getDevicesOfType(VmDeviceGeneralType.VIDEO, vmBase.getManagedDeviceMap());
        boolean hasNoGraphics = getDevicesOfType(VmDeviceGeneralType.GRAPHICS, vmBase.getManagedDeviceMap()).isEmpty();

        if (!videoDevs.isEmpty() && hasNoGraphics) {
            GraphicsDevice compatibleGraphics = getCompatibleGraphics(VmDeviceType.getByName(videoDevs.get(0).getDevice()), clusterVersion, vmBase);

            if (compatibleGraphics != null) {
                vmBase.getManagedDeviceMap().put(compatibleGraphics.getDeviceId(), compatibleGraphics);
            }
        }
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

        OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);
        for (Pair<GraphicsType, DisplayType> graphicsDisplayPair : osRepository.getGraphicsAndDisplays().get(vmBase.getOsId()).get(clusterVersion)) {
            if (graphicsDisplayPair.getSecond().getDefaultVmDeviceType() == videoDeviceType) {
                compatibleType = graphicsDisplayPair.getFirst();
            }
        }

        if (compatibleType != null) {
            graphicsDevice = new GraphicsDevice(compatibleType.getCorrespondingDeviceType());
            graphicsDevice.setId(new VmDeviceId(Guid.newGuid(), vmBase.getId()));
        }

        return graphicsDevice;
    }

}
