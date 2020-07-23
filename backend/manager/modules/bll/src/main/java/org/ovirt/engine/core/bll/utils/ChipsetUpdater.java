package org.ovirt.engine.core.bll.utils;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.utils.BiosTypeUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;

public class ChipsetUpdater {

    public static boolean updateChipset(VM vm, ChipsetType oldChipsetType, Cluster cluster) {
        return updateChipset(vm.getStaticData(), oldChipsetType, cluster);
    }

    public static boolean updateChipset(VmBase vmBase, ChipsetType oldChipsetType, Cluster cluster) {
        if (oldChipsetType == null) {
            return false;
        }
        ChipsetType newChipsetType = BiosTypeUtils.getEffective(vmBase, cluster).getChipsetType();
        if (oldChipsetType == newChipsetType) {
            return false;
        }

        boolean managedUpdated = updateDevicesForChipset(vmBase.getManagedDeviceMap().values(), newChipsetType);
        boolean unmanagedUpdated = updateDevicesForChipset(vmBase.getUnmanagedDeviceList(), newChipsetType);
        return managedUpdated || unmanagedUpdated;
    }

    private static boolean updateDevicesForChipset(Collection<VmDevice> devices, ChipsetType chipsetType) {
        boolean updated = false;
        for (VmDevice device : devices) {
            if (device.getType() == VmDeviceGeneralType.CONTROLLER) {
                if (VmDeviceType.IDE.getName().equals(device.getDevice())) {
                    if (ChipsetType.Q35.equals(chipsetType)) {
                        device.setDevice(VmDeviceType.SATA.getName());
                        updated = true;
                    }
                } else if (VmDeviceType.SATA.getName().equals(device.getDevice())) {
                    if (ChipsetType.I440FX.equals(chipsetType)) {
                        device.setDevice(VmDeviceType.IDE.getName());
                        updated = true;
                    }
                }
            }
            if (!StringUtils.isEmpty(device.getAddress())) {
                device.setAddress("");
                updated = true;
            }
        }
        return updated;
    }

}
