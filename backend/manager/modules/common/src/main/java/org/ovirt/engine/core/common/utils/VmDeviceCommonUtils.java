package org.ovirt.engine.core.common.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;

public class VmDeviceCommonUtils {

    static final String NETWORK_CHAR = "N";
    static final String CDROM_CHAR = "D";
    static final String DRIVE_CHAR = "C";

    /** Expected unit: MiB */
    public static final String SPEC_PARAM_SIZE = "size";
    public static final String SPEC_PARAM_NODE = "node";
    public static final String VIDEO_HEADS = "heads";

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

    public static boolean isMemory(VmDevice device) {
        return VmDeviceGeneralType.MEMORY == device.getType()
                && VmDeviceType.MEMORY.getName().equals(device.getDevice());
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
        devices.forEach(device -> device.setBootOrder(0));

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
    public static boolean isSpecialDevice(String device, VmDeviceGeneralType type, boolean includeHostDev) {
        if (VmDeviceType.USB.getName().equals(device)) {
            return true;
        }

        switch(type) {
        case SOUND:
            return true;
        case CONSOLE:
            return VmDeviceType.CONSOLE.getName().equals(device);
        case SMARTCARD:
            return VmDeviceType.SMARTCARD.getName().equals(device);
        case REDIR:
            return VmDeviceType.SPICEVMC.getName().equals(device);
        case BALLOON:
            return VmDeviceType.MEMBALLOON.getName().equals(device);
        case WATCHDOG:
            return VmDeviceType.WATCHDOG.getName().equals(device);
        case RNG:
            return VmDeviceType.VIRTIO.getName().equals(device);
        case CONTROLLER:
            return VmDeviceType.VIRTIOSERIAL.getName().equals(device)
                    || VmDeviceType.VIRTIOSCSI.getName().equals(device);
        case HOSTDEV:
            return includeHostDev;
        default:
            return false;
        }
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

    public static boolean isVirtIoScsiDiskInterfaceExists(VmBase vmBase) {
        return vmBase.getImages().stream().anyMatch(i -> i.getDiskVmElementForVm(vmBase.getId())
                .getDiskInterface() == DiskInterface.VirtIO_SCSI);
    }

    public static boolean isVirtIoScsiDeviceExists(Collection<VmDevice> devices) {
        for (VmDevice device : devices) {
            if (device.getType() == VmDeviceGeneralType.CONTROLLER
                    && device.getDevice().equals(VmDeviceType.VIRTIOSCSI.getName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCdDeviceExists(Collection<VmDevice> devices) {
        return devices.stream().anyMatch(VmDeviceCommonUtils::isCD);
    }

    public static boolean hasCdDevice(VmBase vmBase) {
        return isCdDeviceExists(vmBase.getManagedDeviceMap().values());
    }

    public static void addVirtIoScsiDevice(VmBase vmBase) {
        VmDevice vmDevice = new VmDevice();
        vmDevice.setId(new VmDeviceId(Guid.newGuid(), vmBase.getId()));
        vmDevice.setType(VmDeviceGeneralType.CONTROLLER);
        vmDevice.setDevice(VmDeviceType.VIRTIOSCSI.getName());
        vmDevice.setManaged(true);
        vmDevice.setPlugged(true);
        vmDevice.setReadOnly(false);
        vmDevice.setAddress("");
        vmBase.getManagedDeviceMap().put(vmDevice.getDeviceId(), vmDevice);
    }

    public static void setDiskInterfaceForVm(VmBase vmBase, DiskInterface diskInterface) {
        vmBase.getImages().forEach(d -> d.getDiskVmElementForVm(vmBase.getId()).setDiskInterface(diskInterface));
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

    public static void addCdDevice(VmBase vmBase) {
        VmDevice vmDevice = new VmDevice();
        vmDevice.setId(new VmDeviceId(Guid.newGuid(), vmBase.getId()));
        vmDevice.setType(VmDeviceGeneralType.DISK);
        vmDevice.setDevice(VmDeviceType.CDROM.getName());
        vmDevice.setManaged(true);
        vmDevice.setPlugged(true);
        vmDevice.setReadOnly(true);
        vmDevice.setAddress("");
        vmBase.getManagedDeviceMap().put(vmDevice.getDeviceId(), vmDevice);
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
        case "sata":
            return 1;
        case "ide":
            return 3;
        default:
            return -1;
        }
    }

    /**
     * Spec param "size" may not always be present. See BZ#1452631.
     *
     * @return size of memory device in MB
     */
    public static Optional<Integer> getSizeOfMemoryDeviceMb(VmDevice memoryDevice) {
        if (!isMemory(memoryDevice)) {
            throw new RuntimeException("Memory device expected but device "
                    + memoryDevice
                    + " passed of type "
                    + memoryDevice.getType());
        }
        return getSpecParamsIntValue(memoryDevice, SPEC_PARAM_SIZE);
    }

    public static Optional<Integer> getSpecParamsIntValue(VmDevice device, String specParamsKey) {
        final Object value = device.getSpecParams().get(specParamsKey);
        if (!(value instanceof String)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt((String) value));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    /**
     * Libvirt doesn't specify what values of xml snippet it actually uses to identify memory device to hot unplug. Thus
     * all values need to be provided to build complete xml representation of memory device.
     */
    public static boolean isMemoryDeviceHotUnpluggable(VmDevice memoryDevice) {
        if (!isMemory(memoryDevice)) {
            throw new IllegalArgumentException("Memory device expected but device " + memoryDevice + " obtained.");
        }
        return getSpecParamsIntValue(memoryDevice, SPEC_PARAM_SIZE).isPresent()
                && getSpecParamsIntValue(memoryDevice, SPEC_PARAM_NODE).isPresent();
    }

    public static boolean isSingleQxlPci(VmBase vmBase) {
        OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
        return osRepository.isLinux(vmBase.getOsId()) && vmBase.getDefaultDisplayType() == DisplayType.qxl
                && vmBase.getOrigin() != OriginType.KUBEVIRT && !vmBase.isHostedEngine();
    }


    public static VmDevice createFailoverVmDevice(Guid failoverId, Guid vmId) {
        VmDevice failoverDevice = new VmDevice();
        failoverDevice.setDevice("bridge");
        failoverDevice.setId(new VmDeviceId(failoverId, vmId));
        Map<String, String> customProperties = new HashMap<>();
        customProperties.put("failover", "failover");
        failoverDevice.setCustomProperties(customProperties);
        failoverDevice.setManaged(true);
        return failoverDevice;
    }

    public static VmNic createFailoverVmNic(Guid failoverId, Guid vmId, String macAddress) {
        VmNic failoverNic = new VmNic();
        failoverNic.setVmId(vmId);
        failoverNic.setLinked(true);
        failoverNic.setVnicProfileId(failoverId);
        failoverNic.setType(VmInterfaceType.pv.getValue());
        failoverNic.setMacAddress(macAddress);
        return failoverNic;
    }

}
