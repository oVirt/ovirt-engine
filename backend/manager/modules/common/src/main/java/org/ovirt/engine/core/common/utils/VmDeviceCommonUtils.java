package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;

public class VmDeviceCommonUtils {

    static final String NETWORK_CHAR = "N";
    static final String CDROM_CHAR = "D";
    static final String DRIVE_CHAR = "C";
    public static final String SPEC_PARAM_SIZE = "size";

    public static boolean isNetwork(VmDevice device) {
        return device.getType() == VmDeviceGeneralType.INTERFACE;
    }

    public static boolean isDisk(VmDevice device) {
        return device.getType() == VmDeviceGeneralType.DISK
                && device.getDevice().equals(VmDeviceType.DISK.getName());
    }

    public static boolean isCD(VmDevice device) {
        return device.getType() == VmDeviceGeneralType.DISK
                && device.getDevice().equals(VmDeviceType.CDROM.getName());
    }

    public static boolean isSound(VmDevice device) {
        return device.getType() == VmDeviceGeneralType.SOUND;
    }

    public static boolean isMemoryBalloon(VmDevice device) {
        return device.getType() == VmDeviceGeneralType.BALLOON;
    }

    public static boolean isGraphics(VmDevice device) {
        return device.getType() == VmDeviceGeneralType.GRAPHICS;
    }

    public static boolean isBridge(VmDevice device) {
        return device.getType() == VmDeviceGeneralType.INTERFACE
                && device.getDevice().equals(VmDeviceType.BRIDGE.getName());
    }

    public static boolean isHostDevInterface(VmDevice device) {
        return device.getType() == VmDeviceGeneralType.INTERFACE
                && device.getDevice().equals(VmDeviceType.HOST_DEVICE.getName());
    }

    public static VmDevice createVirtioSerialDeviceForVm(Guid vmId) {
        return new VmDevice(new VmDeviceId(Guid.newGuid(), vmId),
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.VIRTIOSERIAL.getName(),
                "",
                new HashMap<String, Object>(),
                true,
                true,
                false,
                "",
                null,
                null,
                null);
    }

    /**
     * Updates given devices boot order in accordance with bootSequence given.
     */
    public static void updateVmDevicesBootOrder(
            BootSequence bootSequence,
            Collection<VmDevice> devices,
            List<VmNetworkInterface> interfaces,
            Map<VmDeviceId, DiskVmElement> deviceIdToDiskVmElement) {

        int bootOrder = 0;

        // reset current boot order of all relevant devices before recomputing it.
        for (VmDevice device : devices) {
            device.setBootOrder(0);
        }

        switch (bootSequence) {
        case C:
            bootOrder = setDiskBootOrder(devices, bootOrder, deviceIdToDiskVmElement);
            break;
        case CD:
            bootOrder = setDiskBootOrder(devices, bootOrder, deviceIdToDiskVmElement);
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case CDN:
            bootOrder = setDiskBootOrder(devices, bootOrder, deviceIdToDiskVmElement);
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setNetworkBootOrder(devices, bootOrder, interfaces);
            break;
        case CN:
            bootOrder = setDiskBootOrder(devices, bootOrder, deviceIdToDiskVmElement);
            bootOrder = setNetworkBootOrder(devices, bootOrder, interfaces);
            break;
        case CND:
            bootOrder = setDiskBootOrder(devices, bootOrder, deviceIdToDiskVmElement);
            bootOrder = setNetworkBootOrder(devices, bootOrder, interfaces);
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case D:
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case DC:
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(devices, bootOrder, deviceIdToDiskVmElement);
            break;
        case DCN:
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(devices, bootOrder, deviceIdToDiskVmElement);
            bootOrder = setNetworkBootOrder(devices, bootOrder, interfaces);
            break;
        case DN:
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setNetworkBootOrder(devices, bootOrder, interfaces);
            break;
        case DNC:
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setNetworkBootOrder(devices, bootOrder, interfaces);
            bootOrder = setDiskBootOrder(devices, bootOrder, deviceIdToDiskVmElement);
            break;
        case N:
            bootOrder = setNetworkBootOrder(devices, bootOrder, interfaces);
            break;
        case NC:
            bootOrder = setNetworkBootOrder(devices, bootOrder, interfaces);
            bootOrder = setDiskBootOrder(devices, bootOrder, deviceIdToDiskVmElement);
            break;
        case NCD:
            bootOrder = setNetworkBootOrder(devices, bootOrder, interfaces);
            bootOrder = setDiskBootOrder(devices, bootOrder, deviceIdToDiskVmElement);
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case ND:
            bootOrder = setNetworkBootOrder(devices, bootOrder, interfaces);
            bootOrder = setCDBootOrder(devices, bootOrder);
            break;
        case NDC:
            bootOrder = setNetworkBootOrder(devices, bootOrder, interfaces);
            bootOrder = setCDBootOrder(devices, bootOrder);
            bootOrder = setDiskBootOrder(devices, bootOrder, deviceIdToDiskVmElement);
            break;
        }
    }

    /**
     * updates network devices boot order
     */
    private static int setNetworkBootOrder(Collection<VmDevice> devices, int bootOrder, List<VmNetworkInterface> interfaces) {
        for (VmDevice pluggedInterface : sortInterfacesByName(getPluggedManagedInterfaces(devices), interfaces)) {
            pluggedInterface.setBootOrder(++bootOrder);
        }

        return bootOrder;
    }

    private static List<VmDevice> getPluggedManagedInterfaces(Collection<VmDevice> devices) {
        List<VmDevice> result = new ArrayList<>();
        for (VmDevice device : devices) {
            if ((isHostDevInterface(device) || isBridge(device)) && device.isPlugged() && device.isManaged()) {
                result.add(device);
            }
        }

        return result;
    }

    private static List<VmDevice> sortInterfacesByName(List<VmDevice> pluggedInterfaces, List<VmNetworkInterface> interfaces) {
        if (pluggedInterfaces.size() < 2) {
            return pluggedInterfaces;
        }

        final Map<Guid, String> deviceIdToIfaceName = new HashMap<>();
        for (VmNetworkInterface iface : interfaces) {
            deviceIdToIfaceName.put(iface.getId(), iface.getName());
        }

        Collections.sort(pluggedInterfaces, Comparator.comparing(d -> deviceIdToIfaceName.get(d.getId().getDeviceId())));

        return pluggedInterfaces;
    }

    /**
     * updates CD boot order
     */
    private static int setCDBootOrder(Collection<VmDevice> devices, int bootOrder) {
        for (VmDevice device : devices) {
            if (isCD(device) && device.isPlugged()) {
                device.setBootOrder(++bootOrder);
            }
        }
        return bootOrder;
    }

    /**
     * updates disk boot order
     * snapshot disk devices always will have lower priority than regular attached disks.
     */
    private static int setDiskBootOrder(
            Collection<VmDevice> devices,
            int bootOrder,
            Map<VmDeviceId, DiskVmElement> deviceIdTodiskVmElement) {
        LinkedList<VmDevice> diskDevices = new LinkedList<>();
        for (VmDevice device : devices) {
            if (isDisk(device)) {
                Guid id = device.getDeviceId();
                if (id != null && !id.equals(Guid.Empty)) {
                    if (device.getSnapshotId() == null) {
                        diskDevices.addFirst(device);
                    } else {
                        diskDevices.addLast(device);
                    }
                }
            }
        }

        for (VmDevice device : diskDevices) {
            DiskVmElement dve = deviceIdTodiskVmElement.get(device.getId());
            if (dve != null && dve.isBoot()) {
                device.setBootOrder(++bootOrder);
            }
        }

        return bootOrder;
    }

    public static Map<VmDeviceId, DiskVmElement> extractDiskVmElements(VM vm) {
        Map<VmDeviceId, DiskVmElement> result = new HashMap<>();
        for(Disk disk : vm.getDiskMap().values()) {
            DiskVmElement element = disk.getDiskVmElementForVm(vm.getId());
            if (element != null) {
                result.put(element.getId(), element);
            }
        }
        return result;
    }

    public static boolean isInWhiteList(VmDeviceGeneralType type, String device) {
        String expr = getDeviceTypeSearchExpr(type, device);
        String whiteList = Config.getValue(ConfigValues.ManagedDevicesWhiteList);
        return whiteList.indexOf(expr) >= 0;
    }

    private static String getDeviceTypeSearchExpr(VmDeviceGeneralType type, String device) {
        StringBuilder sb = new StringBuilder();
        sb.append("type=");
        sb.append(type.getValue());
        sb.append(" device=");
        sb.append(device);
        sb.append(" ");
        return sb.toString();
    }

    /**
     * is special device - device which is managed, but contains the general properties
     */
    public static boolean isSpecialDevice(String device, VmDeviceGeneralType type) {
        return (VmDeviceGeneralType.SOUND == type || VmDeviceType.USB.getName().equals(device)
                || (VmDeviceType.CONSOLE.getName().equals(device) && VmDeviceGeneralType.CONSOLE == type)
                || (VmDeviceType.SMARTCARD.getName().equals(device) && VmDeviceGeneralType.SMARTCARD == type)
                || (VmDeviceType.SPICEVMC.getName().equals(device) && VmDeviceGeneralType.REDIR == type)
                || (VmDeviceType.MEMBALLOON.getName().equals(device) && VmDeviceGeneralType.BALLOON == type))
                || (VmDeviceType.WATCHDOG.getName().equals(device) && VmDeviceGeneralType.WATCHDOG == type)
                || (VmDeviceType.VIRTIO.getName().equals(device) && VmDeviceGeneralType.RNG == type)
                || (VmDeviceType.VIRTIOSERIAL.getName().equals(device) && VmDeviceGeneralType.CONTROLLER == type)
                || (VmDeviceType.VIRTIOSCSI.getName().equals(device) && VmDeviceGeneralType.CONTROLLER == type);
    }

    /**
     * Find a device in the map with the given type.
     */
    public static VmDevice findVmDeviceByType(Map<?, VmDevice> vmManagedDeviceMap, VmDeviceType type) {
        return findVmDeviceByType(vmManagedDeviceMap, type.getName());
    }

    /**
     * Find a device in the map with the given type.
     */
    public static VmDevice findVmDeviceByType(Map<?, VmDevice> vmManagedDeviceMap, String typeName) {
        for (VmDevice vmDevice : vmManagedDeviceMap.values()) {
            if (vmDevice.getDevice().equals(typeName)) {
                return vmDevice;
            }
        }

        return null;
    }

    /**
     * Find a device in the map with the given general type.
     */
    public static VmDevice findVmDeviceByGeneralType(Map<Guid, VmDevice> vmManagedDeviceMap,
                                                     VmDeviceGeneralType generalType) {
        for (VmDevice vmDevice : vmManagedDeviceMap.values()) {
            if (vmDevice.getType() == generalType) {
                return vmDevice;
            }
        }

        return null;
    }

    /**
     * Check if device with the type given exists in the map.
     */
    public static boolean isVmDeviceExists(Map<Guid, VmDevice> vmManagedDeviceMap, VmDeviceType type) {
        return findVmDeviceByType(vmManagedDeviceMap, type) != null;
    }

    /**
     * Check if device with the general type given exists in the map.
     */
    public static boolean isVmDeviceExists(Map<Guid, VmDevice> vmManagedDeviceMap, VmDeviceGeneralType generalType) {
        return findVmDeviceByGeneralType(vmManagedDeviceMap, generalType) != null;
    }

    /**
     * Check if the given collection of devices contains balloon
     *
     * @param devices - collection of VM devices to look in
     */
    public static boolean isBalloonDeviceExists(Collection<VmDevice> devices) {
        for (VmDevice device : devices) {
            if (isMemoryBalloon(device)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isSoundDeviceExists(Collection<VmDevice> devices) {
        for (VmDevice device : devices) {
            if (isSound(device)) {
                return true;
            }
        }

        return false;
    }

    public static void addVideoDevice(VmBase vmBase) {
        if (vmBase.getDefaultDisplayType().getDefaultVmDeviceType() == null) {
            return;
        }
        VmDevice vmDevice = new VmDevice();
        vmDevice.setId(new VmDeviceId(Guid.newGuid(), vmBase.getId()));
        vmDevice.setType(VmDeviceGeneralType.VIDEO);
        vmDevice.setDevice(vmBase.getDefaultDisplayType().getDefaultVmDeviceType().getName());
        vmDevice.setManaged(true);
        vmDevice.setPlugged(true);
        vmDevice.setReadOnly(false);
        vmDevice.setAddress("");
        vmBase.getManagedDeviceMap().put(vmDevice.getDeviceId(), vmDevice);
    }

    public static void addGraphicsDevice(VmBase vmBase, VmDeviceType vmDeviceType)  {
        GraphicsDevice graphicsDevice = new GraphicsDevice(vmDeviceType);
        graphicsDevice.setId(new VmDeviceId(Guid.newGuid(), vmBase.getId()));
        vmBase.getManagedDeviceMap().put(graphicsDevice.getDeviceId(), graphicsDevice);
    }

    /**
     * Get unit/slot number reserved by VDSM for CD-ROM.
     *
     * @param cdInterface name of the interface ("ide"/"scsi"/"sata")
     * @return the index
     */
    public static int getCdDeviceIndex(String cdInterface) {
        switch (cdInterface) {
        case "scsi":
        case "ide":
        case "sata":
            return 2;
        default:
            return -1;
        }
    }

    /**
     * Get unit/slot number reserved by VDSM for payload.
     *
     * @param cdInterface name of the interface ("ide"/"scsi"/"sata")
     * @return the index
     */
    public static int getCdPayloadDeviceIndex(String cdInterface) {
        switch (cdInterface) {
        case "scsi":
            return 1;
        case "ide":
            return 3;
        case "sata":
            return 1;
        default:
            return -1;
        }
    }

    public static Integer getSizeOfMemoryDeviceMb(VmDevice memoryDevice) {
        if (memoryDevice.getType() != VmDeviceGeneralType.MEMORY) {
            throw new RuntimeException("Memory device expected but device "
                    + memoryDevice
                    + " passed of type "
                    + memoryDevice.getType());
        }
        return (Integer) memoryDevice.getSpecParams().get(SPEC_PARAM_SIZE);
    }

}
