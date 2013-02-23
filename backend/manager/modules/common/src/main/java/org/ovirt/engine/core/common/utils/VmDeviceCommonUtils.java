package org.ovirt.engine.core.common.utils;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.BaseDisk;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class VmDeviceCommonUtils {

    public final static String LOW_VIDEO_MEM = "32768";
    public final static String HIGH_VIDEO_MEM = "65536";

    final static String NETWORK_CHAR = "N";
    final static String CDROM_CHAR = "D";
    final static String DRIVE_CHAR = "C";

    public final static String CDROM_IMAGE_ID = "11111111-1111-1111-1111-111111111111";

    public static boolean isNetwork(VmDevice device) {
        return (device.getType().equals(VmDeviceType.INTERFACE.getName()));
    }

    public static boolean isDisk(VmDevice device) {
        return (device.getType().equals(VmDeviceType.DISK.getName()) && device.getDevice()
                .equals(VmDeviceType.DISK.getName()));
    }

    public static boolean isCD(VmDevice device) {
        return (device.getType().equals(VmDeviceType.DISK.getName()) && device.getDevice()
                .equals(VmDeviceType.CDROM.getName()));
    }

    /**
     * updates given devices boot order
     *
     * @param devices
     * @param bootSequence
     * @param isOldCluster
     */
    public static void updateVmDevicesBootOrder(VM vm,
            List<VmDevice> devices,
            boolean isOldCluster) {
        int bootOrder = 0;

        // reset current boot order of all relevant devices before recomputing it.
        for (VmDevice device : devices) {
            if (isBootable(device)) {
                // a boot order of 0 prevents it from being sent to VDSM
                device.setBootOrder(0);
            }
        }
        BootSequence bootSequence =
                (vm.isRunOnce()) ? vm.getBootSequence() : vm.getDefaultBootSequence();
        switch (bootSequence) {
        case C:
            bootOrder = setDiskBootOrder(vm, devices, bootOrder, isOldCluster);
            break;
        case CD:
            bootOrder = setDiskBootOrder(vm, devices, bootOrder, isOldCluster);
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case CDN:
            bootOrder = setDiskBootOrder(vm, devices, bootOrder, isOldCluster);
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            break;
        case CN:
            bootOrder = setDiskBootOrder(vm, devices, bootOrder, isOldCluster);
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            break;
        case CND:
            bootOrder = setDiskBootOrder(vm, devices, bootOrder, isOldCluster);
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case D:
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case DC:
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(vm, devices, bootOrder, isOldCluster);
            break;
        case DCN:
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(vm, devices, bootOrder, isOldCluster);
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            break;
        case DN:
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            break;
        case DNC:
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(vm, devices, bootOrder, isOldCluster);
            break;
        case N:
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            break;
        case NC:
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(vm, devices, bootOrder, isOldCluster);
            break;
        case NCD:
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(vm, devices, bootOrder, isOldCluster);
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case ND:
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case NDC:
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(vm, devices, bootOrder, isOldCluster);
            break;
        }
    }

    /**
     * updates network devices boot order
     *
     * @param devices
     * @param bootOrder
     * @return
     */
    private static int setNetworkBootOrder(List<VmDevice> devices, int bootOrder) {
        for (VmDevice device : devices) {
            if (device.getType().equals(
                    VmDeviceType.INTERFACE.getName())
                    && device.getDevice().equals(
                            VmDeviceType.BRIDGE.getName())) {
                if (device.getIsPlugged()) {
                    device.setBootOrder(++bootOrder);
                }
            }
        }
        return bootOrder;
    }

    /**
     * updates CD boot order
     *
     * @param devices
     * @param bootOrder
     * @return
     */
    private static int setCDBootOrder(List<VmDevice> devices, int bootOrder) {
        for (VmDevice device : devices) {
            if (device.getType()
                    .equals(VmDeviceType.DISK.getName())
                    && device.getDevice().equals(
                            VmDeviceType.CDROM.getName())) {
                if (device.getIsPlugged()) {
                    device.setBootOrder(++bootOrder);
                }
                break; // only one CD is currently supported.
            }
        }
        return bootOrder;
    }

    /**
     * updates disk boot order
     *
     * @param vmBase
     * @param devices
     * @param bootOrder
     * @param isOldCluster
     * @return
     */
    private static int setDiskBootOrder(VM vm,
            List<VmDevice> devices,
            int bootOrder,
            boolean isOldCluster) {
        for (VmDevice device : devices) {
            if (device.getType()
                    .equals(VmDeviceType.DISK.getName())
                    && device.getDevice().equals(
                            VmDeviceType.DISK.getName())) {
                Guid id = device.getDeviceId();
                if (id != null && !id.equals(Guid.Empty)) {
                    // gets the image disk
                    BaseDisk disk = getDisk(vm, id);
                    if (disk != null && disk.isBoot()) {
                        device.setBootOrder(++bootOrder);
                        if (isOldCluster) { // Only one system disk can be bootable in old version.
                            break;
                        }
                    }
                }
            }
        }
        return bootOrder;
    }

    private static Disk getDisk(VM vm, Guid id) {
        for (Disk disk : vm.getDiskMap().values()) {
            if (disk.getId().equals(id)) {
                return disk;
            }
        }
        return null;
    }

    /**
     * Computes old boot sequence enum value from the given list of devices.
     *
     * @param devices
     * @return
     */
    public static BootSequence getBootSequence(List<VmDevice> devices) {
        StringBuilder sb = new StringBuilder();
        BootSequence ret = BootSequence.C;
        for (VmDevice device : devices) {
            if (device.getBootOrder() > 0) {
                if (isNetwork(device) && sb.indexOf(NETWORK_CHAR) < 0) {
                    sb.append(NETWORK_CHAR);
                }
                if (isDisk(device) && sb.indexOf(DRIVE_CHAR) < 0) {
                    sb.append(DRIVE_CHAR);
                }
                if (isCD(device) && sb.indexOf(CDROM_CHAR) < 0) {
                    sb.append(CDROM_CHAR);
                }
                // maximum string is 3 characters, so, if reached , exit loop.
                if (sb.length() == 3) {
                    break;
                }
            }
        }

        for (BootSequence bs : BootSequence.values()) {
            if (bs.name().equals(sb.toString())) {
                ret = bs;
                break;
            }
        }
        return ret;
    }

    /**
     * "old" level is considered anything lower than 3.1 at the moment
     *
     * @param version
     * @return
     */
    public static boolean isOldClusterVersion(Version version) {
        return (version.compareTo(Version.v3_1) < 0);
    }

    public static boolean isInWhiteList(String type, String device) {
        String expr = getDeviceTypeSearchExpr(type, device);
        String whiteList = Config.GetValue(ConfigValues.ManagedDevicesWhiteList);
        return (whiteList.indexOf(expr) >= 0);
    }

    private static boolean isBootable(VmDevice device) {
        return (VmDeviceType.DISK.getName().equals(device.getType()) || VmDeviceType.INTERFACE.getName()
                .equals(device.getType()));
    }

    private static String getDeviceTypeSearchExpr(String type, String device) {
        StringBuilder sb = new StringBuilder();
        sb.append("type=");
        sb.append(type);
        sb.append(" device=");
        sb.append(device);
        sb.append(" ");
        return sb.toString();
    }

    /**
     * is special device - device which is managed, but contains the general properties
     */
    public static boolean isSpecialDevice(String device, String type) {
        return (VmDeviceType.SOUND.getName().equals(type) ||
                VmDeviceType.USB.getName().equals(device) ||
                (VmDeviceType.SMARTCARD.getName().equals(device) && VmDeviceType.SMARTCARD.getName().equals(type)) ||
                (VmDeviceType.SPICEVMC.getName().equals(device) && VmDeviceType.REDIR.getName().equals(type)) || (VmDeviceType.MEMBALLOON.getName()
                        .equals(device) && VmDeviceType.BALLOON.getName().equals(type)));
    }
}
