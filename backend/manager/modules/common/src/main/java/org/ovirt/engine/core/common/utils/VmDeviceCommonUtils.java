package org.ovirt.engine.core.common.utils;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmBase;
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
    public static void updateVmDevicesBootOrder(VmBase vmBase,
            List<VmDevice> devices,
            BootSequence bootSequence,
            boolean isOldCluster) {
        int bootOrder = 0;
        switch (bootSequence) {
        case C:
            bootOrder = setDiskBootOrder(vmBase, devices, bootOrder, isOldCluster);
            break;
        case CD:
            bootOrder = setDiskBootOrder(vmBase, devices, bootOrder, isOldCluster);
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case CDN:
            bootOrder = setDiskBootOrder(vmBase, devices, bootOrder, isOldCluster);
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            break;
        case CN:
            bootOrder = setDiskBootOrder(vmBase, devices, bootOrder, isOldCluster);
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            break;
        case CND:
            bootOrder = setDiskBootOrder(vmBase, devices, bootOrder, isOldCluster);
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case D:
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case DC:
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(vmBase, devices, bootOrder, isOldCluster);
            break;
        case DCN:
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(vmBase, devices, bootOrder, isOldCluster);
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            break;
        case DN:
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            break;
        case DNC:
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(vmBase, devices, bootOrder, isOldCluster);
            break;
        case N:
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            break;
        case NC:
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(vmBase, devices, bootOrder, isOldCluster);
            break;
        case NCD:
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(vmBase, devices, bootOrder, isOldCluster);
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case ND:
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case NDC:
            bootOrder = setNetworkBootOrder(devices, bootOrder);
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(vmBase, devices, bootOrder, isOldCluster);
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
    private static int setDiskBootOrder(VmBase vmBase, List<VmDevice> devices, int bootOrder, boolean isOldCluster) {
        int i = 0;
        for (VmDevice device : devices) {
            if (device.getType()
                    .equals(VmDeviceType.DISK.getName())
                    && device.getDevice().equals(
                            VmDeviceType.DISK.getName())) {
                Guid id = device.getDeviceId();
                if (id != null && !id.equals(Guid.Empty)) {
                    if (isOldCluster) { // Only one system disk can be bootable in
                                        // old version.
                        DiskImage diskImage = null;
                        // gets the image disk
                        for (DiskImage image : vmBase.getDiskList()) {
                            if (image.getimage_group_id().equals(id)) {
                                diskImage = image;
                                break;
                            }
                        }
                        if (diskImage != null && diskImage.getboot()) {
                            device.setBootOrder(++bootOrder);
                            break;
                        }
                    } else { // supporting more than 1 bootable disk in 3.1 and up.
                        device.setBootOrder(++bootOrder);
                    }
                }
            }
        }
        return bootOrder;
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

    public static boolean isOldClusterVersion(Version version) {
        return (!(version.getMajor() >= 3 && version.getMinor() >= 1));
    }

    public static boolean isInWhiteList(String type, String device) {
        String expr = getDeviceTypeSearchExpr(type, device);
        String whiteList = Config.GetValue(ConfigValues.ManagedDevicesWhiteList);
        return (whiteList.indexOf(expr) >= 0);
    }

    /**
     * appends device id key/value to specParams
     * @param deviceId
     * @param specParams
     * @return
     */
    public static String appendDeviceIdToSpecParams(Guid deviceId, String specParams) {
        StringBuilder sb = new StringBuilder();
        final String DEVICE = "deviceId=";
        final String SEP = ",";
        // device id is included in special parameters
        // this is done in order to recognize the device when changes are passed from VDSM
        if (specParams.indexOf(DEVICE) >= 0) {
            return specParams;
        } else {
            if (specParams.length() > 0) {
                sb.append(specParams);
                sb.append(SEP);
            }
            sb.append(DEVICE);
            sb.append(deviceId);
        }
        return sb.toString();
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
}
