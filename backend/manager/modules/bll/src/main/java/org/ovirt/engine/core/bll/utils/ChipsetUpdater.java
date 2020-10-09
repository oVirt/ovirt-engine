package org.ovirt.engine.core.bll.utils;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.utils.VmDeviceType;

public class ChipsetUpdater {

    public static boolean updateChipset(VM vm, Cluster cluster) {
        return updateChipset(vm.getStaticData(), cluster);
    }

    public static boolean updateChipset(VmBase vmBase, Cluster cluster) {

        BiosType oldBiosType = vmBase.getEffectiveBiosType();
        BiosType newBiosType = vmBase.getCustomBiosType() != BiosType.CLUSTER_DEFAULT ? vmBase.getCustomBiosType() : cluster.getBiosType();

        if (oldBiosType.getChipsetType() == newBiosType.getChipsetType()) {
            return false;
        }

        vmBase.setEffectiveBiosType(newBiosType);
        vmBase.setUnmanagedDeviceList(new ArrayList<>());
        return updateDevicesForChipset(vmBase.getManagedDeviceMap().values(), newBiosType.getChipsetType());
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
