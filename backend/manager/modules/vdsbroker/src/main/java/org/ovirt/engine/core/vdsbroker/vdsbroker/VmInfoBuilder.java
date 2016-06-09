package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.utils.archstrategy.ArchStrategyFactory;
import org.ovirt.engine.core.vdsbroker.architecture.CreateAdditionalControllers;
import org.ovirt.engine.core.vdsbroker.architecture.GetBootableDiskIndex;
import org.ovirt.engine.core.vdsbroker.architecture.GetControllerIndices;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStringUtils;

@SuppressWarnings({"rawtypes", "unchecked"})
public class VmInfoBuilder extends VmInfoBuilderBase {

    private static final String DEVICES = "devices";
    private static final String USB_BUS = "usb";
    private static final String CLOUD_INIT_VOL_ID = "config-2";
    private static final Base64 BASE_64 = new Base64(0, null);

    private final VdsNumaNodeDao vdsNumaNodeDao;
    private final VmDeviceDao vmDeviceDao;
    private final VmNumaNodeDao vmNumaNodeDao;
    private final VmInfoBuildUtils vmInfoBuildUtils;

    private final List<Map<String, Object>> devices = new ArrayList<>();
    private List<VmDevice> managedDevices = null;
    private Guid vdsId;
    private int numOfReservedScsiIndexes = 0;

    public VmInfoBuilder(VM vm,
            Guid vdsId,
            Map createInfo,
            VdsNumaNodeDao vdsNumaNodeDao,
            VmDeviceDao vmDeviceDao,
            VmNumaNodeDao vmNumaNodeDao,
            VmInfoBuildUtils vmInfoBuildUtils) {
        this.vdsNumaNodeDao = Objects.requireNonNull(vdsNumaNodeDao);
        this.vmDeviceDao = Objects.requireNonNull(vmDeviceDao);
        this.vmNumaNodeDao = Objects.requireNonNull(vmNumaNodeDao);
        this.vmInfoBuildUtils = Objects.requireNonNull(vmInfoBuildUtils);

        this.vm = vm;
        this.vdsId = vdsId;
        this.createInfo = createInfo;
        final boolean hasNonDefaultBootOrder = vm.getBootSequence() != vm.getDefaultBootSequence();
        if (hasNonDefaultBootOrder) {
            managedDevices = new ArrayList<>();
        }
    }

    @Override
    protected void buildVmVideoCards() {
        List<VmDevice> vmVideoDevices = vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.VIDEO);
        for (VmDevice vmVideoDevice : vmVideoDevices) {
            // skip unmanaged devices (handled separately)
            if (!vmVideoDevice.getIsManaged()) {
                continue;
            }

            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, vmVideoDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmVideoDevice.getDevice());
            addAddress(vmVideoDevice, struct);
            struct.put(VdsProperties.SpecParams, vmVideoDevice.getSpecParams());
            struct.put(VdsProperties.DeviceId, String.valueOf(vmVideoDevice.getId().getDeviceId()));
            addToManagedDevices(vmVideoDevice);
            devices.add(struct);
        }
    }

    /**
     * Builds graphics cards for a vm.
     * If there is a pre-filled information about graphics in graphics info (this means vm is run via run once ),
     * this information is used to create graphics devices. Otherwise graphics devices are build from database.
     */
    @Override
    protected void buildVmGraphicsDevices() {
        boolean graphicsOverriden = vm.isRunOnce() && vm.getGraphicsInfos() != null && !vm.getGraphicsInfos().isEmpty();

        Map<GraphicsType, GraphicsInfo> infos = vm.getGraphicsInfos();

        Map<String, Object> specParamsFromVm = null;
        if (infos != null) {
            specParamsFromVm = new HashMap();
            addVmGraphicsOptions(infos, specParamsFromVm);
        }

        if (graphicsOverriden) {
            buildVmGraphicsDevicesOverriden(infos, specParamsFromVm);
        } else {
            buildVmGraphicsDevicesFromDb(specParamsFromVm);
        }
    }

    /**
     * Creates graphics devices from graphics info - this will override the graphics devices from the db.
     * Used when vm is run via run once.
     *
     * @param graphicsInfos - vm graphics
     */
    private void buildVmGraphicsDevicesOverriden(Map<GraphicsType, GraphicsInfo> graphicsInfos, Map<String, Object> extraSpecParams) {
        for (Entry<GraphicsType, GraphicsInfo> graphicsInfo : graphicsInfos.entrySet()) {
            Map struct = new HashMap();
            struct.put(VdsProperties.Type, VmDeviceGeneralType.GRAPHICS.getValue());
            struct.put(VdsProperties.Device, graphicsInfo.getKey().name().toLowerCase());
            struct.put(VdsProperties.DeviceId, String.valueOf(Guid.newGuid()));
            if (extraSpecParams != null) {
                struct.put(VdsProperties.SpecParams, extraSpecParams);
            }
            devices.add(struct);
        }

        if (!graphicsInfos.isEmpty()) {
            String legacyGraphicsType = (graphicsInfos.size() == 2)
                    ? VdsProperties.QXL
                    : graphicsTypeToLegacyDisplayType(graphicsInfos.keySet().iterator().next());

            createInfo.put(VdsProperties.display, legacyGraphicsType);
        }
    }

    /**
     * Builds vm graphics from database.
     */
    private void buildVmGraphicsDevicesFromDb(Map<String, Object> extraSpecParams) {
        buildVmDevicesFromDb(VmDeviceGeneralType.GRAPHICS, false, extraSpecParams);

        String legacyDisplay = deriveDisplayTypeLegacy();
        if (legacyDisplay != null) {
            createInfo.put(VdsProperties.display, legacyDisplay);
        }
    }

    @Override
    protected void buildVmCD() {
        Map<String, Object> struct;
        boolean hasPayload = vm.getVmPayload() != null && vm.getVmPayload().getDeviceType() == VmDeviceType.CDROM;
        // check if we have payload CD
        if (hasPayload) {
            struct = new HashMap<>();
            vmInfoBuildUtils.addCdDetails(vm.getVmPayload(), struct, vm);
            addDevice(struct, vm.getVmPayload(), "");
        }
        // check first if CD was given as a RunOnce parameter
        if (vm.isRunOnce() && !StringUtils.isEmpty(vm.getCdPath())) {
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                            VmDeviceGeneralType.DISK,
                            VmDeviceType.CDROM.getName(),
                            "",
                            0,
                            null,
                            true,
                            true,
                            true,
                            "",
                            null,
                            null,
                            null);
            struct = new HashMap<>();
            vmInfoBuildUtils.addCdDetails(vmDevice, struct, vm);
            addDevice(struct, vmDevice, vm.getCdPath());
        } else {
            // get vm device for this CD from DB
            List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                    vm.getId(),
                    VmDeviceGeneralType.DISK,
                    VmDeviceType.CDROM.getName());
            for (VmDevice vmDevice : vmDevices) {
                // skip unmanaged devices (handled separately)
                if (!vmDevice.getIsManaged()) {
                    continue;
                }
                // The Payload is loaded in via RunVmCommand to VM.
                // Payload and its device are handled at the beginning of
                // the method, so no need to add the device again,
                if (VmPayload.isPayload(vmDevice.getSpecParams())) {
                    continue;
                }
                struct = new HashMap<>();
                String cdPath = vm.getCdPath();
                vmInfoBuildUtils.addCdDetails(vmDevice, struct, vm);
                addAddress(vmDevice, struct);
                addDevice(struct, vmDevice, cdPath == null ? "" : cdPath);
            }
        }
        numOfReservedScsiIndexes++;
    }

    @Override
    protected void buildVmFloppy() {
        // check if we have payload Floppy
        boolean hasPayload = vm.getVmPayload() != null && vm.getVmPayload().getDeviceType() == VmDeviceType.FLOPPY;
        if (hasPayload) {
            Map<String, Object> struct = new HashMap<>();
            vmInfoBuildUtils.addFloppyDetails(vm.getVmPayload(), struct);
            addDevice(struct, vm.getVmPayload(), "");
        }
        // check first if Floppy was given as a parameter
        else if (vm.isRunOnce() && !StringUtils.isEmpty(vm.getFloppyPath())) {
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                            VmDeviceGeneralType.DISK,
                            VmDeviceType.FLOPPY.getName(),
                            "",
                            0,
                            null,
                            true,
                            true,
                            true,
                            "",
                            null,
                            null,
                            null);
            Map<String, Object> struct = new HashMap<>();
            vmInfoBuildUtils.addFloppyDetails(vmDevice, struct);
            addDevice(struct, vmDevice, vm.getFloppyPath());
        } else {
            // get vm device for this Floppy from DB
            List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                    vm.getId(),
                    VmDeviceGeneralType.DISK,
                    VmDeviceType.FLOPPY.getName());
            for (VmDevice vmDevice : vmDevices) {
                // skip unmanaged devices (handled separately)
                if (!vmDevice.getIsManaged()) {
                    continue;
                }
                // more than one device mean that we have payload and should use it
                // instead of the blank cd
                if (!VmPayload.isPayload(vmDevice.getSpecParams()) && vmDevices.size() > 1) {
                    continue;
                }
                // The Payload is loaded in via RunVmCommand to VM.Payload
                // and its handled at the beginning of the method, so no
                // need to add the device again
                if (VmPayload.isPayload(vmDevice.getSpecParams())) {
                    continue;
                }
                Map<String, Object> struct = new HashMap<>();
                String file = vm.getFloppyPath();
                vmInfoBuildUtils.addFloppyDetails(vmDevice, struct);
                addDevice(struct, vmDevice, file);
            }
        }
    }

    @Override
    protected void buildVmDrives() {
        boolean bootDiskFound = false;
        List<Disk> disks = getSortedDisks();
        Map<VmDevice, Integer> vmDeviceVirtioScsiUnitMap =
                vmInfoBuildUtils.getVmDeviceUnitMapForVirtioScsiDisks(vm);

        Map<VmDevice, Integer> vmDeviceSpaprVscsiUnitMap =
                vmInfoBuildUtils.getVmDeviceUnitMapForSpaprScsiDisks(vm);

        Map<DiskInterface, Integer> controllerIndexMap =
                ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new GetControllerIndices()).returnValue();

        int virtioScsiIndex = controllerIndexMap.get(DiskInterface.VirtIO_SCSI);
        int sPaprVscsiIndex = controllerIndexMap.get(DiskInterface.SPAPR_VSCSI);

        Map<Guid, StorageQos> qosCache = new HashMap<>();

        int pinnedDriveIndex = 0;

        for (Disk disk : disks) {
            Map<String, Object> struct = new HashMap<>();
            // get vm device for this disk from DB
            VmDevice vmDevice = getVmDeviceByDiskId(disk.getId(), vm.getId());
            DiskVmElement dve = disk.getDiskVmElementForVm(vm.getId());
            // skip unamanged devices (handled separtely)
            if (!vmDevice.getIsManaged()) {
                continue;
            }
            if (vmDevice.getIsPlugged()) {
                struct.put(VdsProperties.Type, vmDevice.getType().getValue());
                struct.put(VdsProperties.Device, vmDevice.getDevice());
                switch (dve.getDiskInterface()) {
                case IDE:
                    struct.put(VdsProperties.INTERFACE, VdsProperties.Ide);
                    break;
                case VirtIO:
                    struct.put(VdsProperties.INTERFACE, VdsProperties.Virtio);
                    if (disk.getDiskStorageType() == DiskStorageType.LUN) {
                        struct.put(VdsProperties.Device, VmDeviceType.LUN.getName());
                    }

                    if (vm.getNumOfIoThreads() != 0) {
                        // simple round robin e.g. for 2 threads and 4 disks it will be pinned like this:
                        // disk 0 -> iothread 1
                        // disk 1 -> iothread 2
                        // disk 2 -> iothread 1
                        // disk 3 -> iothread 2
                        int pinTo = pinnedDriveIndex % vm.getNumOfIoThreads() + 1;
                        pinnedDriveIndex ++;
                        vmDevice.getSpecParams().put(VdsProperties.pinToIoThread, pinTo);
                    }

                    break;
                case VirtIO_SCSI:
                    struct.put(VdsProperties.INTERFACE, VdsProperties.Scsi);
                    // If SCSI pass-through is enabled (DirectLUN disk and SGIO is defined),
                    // set device type as 'lun' (instead of 'disk') and set the specified SGIO.
                    if (disk.getDiskStorageType() == DiskStorageType.LUN && disk.isScsiPassthrough()) {
                        struct.put(VdsProperties.Device, VmDeviceType.LUN.getName());
                        struct.put(VdsProperties.Sgio, disk.getSgio().toString().toLowerCase());
                    }
                    if (StringUtils.isEmpty(vmDevice.getAddress())) {
                        // Explicitly define device's address if missing
                        int unit = vmDeviceVirtioScsiUnitMap.get(vmDevice);
                        vmDevice.setAddress(
                                vmInfoBuildUtils.createAddressForScsiDisk(virtioScsiIndex, unit).toString());
                    }
                    break;
                case SPAPR_VSCSI:
                    struct.put(VdsProperties.INTERFACE, VdsProperties.Scsi);

                    if (StringUtils.isEmpty(vmDevice.getAddress())) {
                        // Explicitly define device's address if missing
                        int unit = vmDeviceSpaprVscsiUnitMap.get(vmDevice);
                        vmDevice.setAddress(vmInfoBuildUtils.createAddressForScsiDisk(sPaprVscsiIndex, unit).toString());
                    }
                    break;
                default:
                    logUnsupportedInterfaceType();
                    break;
                }
                // Insure that boot disk is created first
                if (!bootDiskFound && dve.isBoot()) {
                    bootDiskFound = true;
                    struct.put(VdsProperties.Index, getBootableDiskIndex(disk));
                }
                addAddress(vmDevice, struct);
                switch (disk.getDiskStorageType()) {
                    case IMAGE:
                        DiskImage diskImage = (DiskImage) disk;
                        struct.put(VdsProperties.PoolId, diskImage.getStoragePoolId().toString());
                        struct.put(VdsProperties.DomainId, diskImage.getStorageIds().get(0).toString());
                        struct.put(VdsProperties.ImageId, diskImage.getId().toString());
                        struct.put(VdsProperties.VolumeId, diskImage.getImageId().toString());
                        struct.put(VdsProperties.Format, diskImage.getVolumeFormat().toString()
                                .toLowerCase());
                        struct.put(VdsProperties.PropagateErrors, disk.getPropagateErrors().toString()
                                .toLowerCase());

                        if (!qosCache.containsKey(diskImage.getDiskProfileId())) {
                            qosCache.put(diskImage.getDiskProfileId(), vmInfoBuildUtils.loadStorageQos(diskImage));
                        }
                        vmInfoBuildUtils.handleIoTune(vmDevice, qosCache.get(diskImage.getDiskProfileId()));
                        break;
                    case LUN:
                        LunDisk lunDisk = (LunDisk) disk;
                        struct.put(VdsProperties.Guid, lunDisk.getLun().getLUNId());
                        struct.put(VdsProperties.Format, VolumeFormat.RAW.toString().toLowerCase());
                        struct.put(VdsProperties.PropagateErrors, PropagateErrors.Off.toString().toLowerCase());
                        break;
                    case CINDER:
                        vmInfoBuildUtils.buildCinderDisk((CinderDisk) disk, struct);
                        break;
                }
                vmInfoBuildUtils.addBootOrder(vmDevice, struct);
                struct.put(VdsProperties.Shareable,
                        (vmDevice.getSnapshotId() != null)
                                ? VdsProperties.Transient : String.valueOf(disk.isShareable()));
                struct.put(VdsProperties.Optional, Boolean.FALSE.toString());
                struct.put(VdsProperties.ReadOnly, String.valueOf(vmDevice.getIsReadOnly()));
                struct.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
                struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
                devices.add(struct);
                addToManagedDevices(vmDevice);
            }
        }

        ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new CreateAdditionalControllers(devices));
    }

    private int getBootableDiskIndex(Disk disk) {
        int index = ArchStrategyFactory.getStrategy(vm.getClusterArch()).
                run(new GetBootableDiskIndex(numOfReservedScsiIndexes)).returnValue();
        log.info("Bootable disk '{}' set to index '{}'", disk.getId(), index);
        return index;
    }

    @Override
    protected void buildVmNetworkInterfaces() {
        Map<VmDeviceId, VmDevice> devicesByDeviceId =
                Entities.businessEntitiesById(
                        vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                                vm.getId(),
                                VmDeviceGeneralType.INTERFACE,
                                VmDeviceType.BRIDGE.getName()));

        devicesByDeviceId.putAll(Entities.businessEntitiesById(
                vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                        vm.getId(),
                        VmDeviceGeneralType.INTERFACE,
                        VmDeviceType.HOST_DEVICE.getName())));

        for (VmNic vmInterface : vm.getInterfaces()) {
            // get vm device for this nic from DB
            VmDevice vmDevice =
                    devicesByDeviceId.get(new VmDeviceId(vmInterface.getId(), vmInterface.getVmId()));

            if (vmDevice != null && vmDevice.getIsManaged() && vmDevice.getIsPlugged()) {

                Map struct = new HashMap();
                VmInterfaceType ifaceType = VmInterfaceType.rtl8139;

                if (vmInterface.getType() != null) {
                    ifaceType = VmInterfaceType.forValue(vmInterface.getType());
                }

                if (vmInterface.isPassthrough()) {
                    String vfDeviceName = vm.getPassthroughVnicToVfMap().get(vmInterface.getId());
                    vmInfoBuildUtils.addNetworkVirtualFunctionProperties(struct,
                            vmInterface,
                            vmDevice,
                            vfDeviceName,
                            vm);
                } else {
                    addNetworkInterfaceProperties(struct,
                            vmInterface,
                            vmDevice,
                            vmInfoBuildUtils.evaluateInterfaceType(ifaceType, vm.getHasAgent()));
                }

                devices.add(struct);
                addToManagedDevices(vmDevice);
            }
        }
    }

    @Override
    protected void buildVmSoundDevices() {
        buildVmDevicesFromDb(VmDeviceGeneralType.SOUND, true, null);
    }

    @Override
    protected void buildVmConsoleDevice() {
        buildVmDevicesFromDb(VmDeviceGeneralType.CONSOLE, false, null);
    }

    private void buildVmDevicesFromDb(VmDeviceGeneralType generalType, boolean addAddress, Map<String, Object> extraSpecParams) {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), generalType);

        for (VmDevice vmDevice : vmDevices) {
            Map struct = new HashMap();
            struct.put(VdsProperties.Type, vmDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmDevice.getDevice());

            Map<String, Object> specParams = vmDevice.getSpecParams();
            if (extraSpecParams != null) {
                specParams.putAll(extraSpecParams);
            }
            struct.put(VdsProperties.SpecParams, specParams);

            struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
            if (addAddress) {
                addAddress(vmDevice, struct);
            }
            devices.add(struct);
        }
    }

    @Override
    protected void buildUnmanagedDevices() {
        Map<String, String> customMap = createInfo.containsKey(VdsProperties.Custom) ?
                (Map<String, String>) createInfo.get(VdsProperties.Custom) : new HashMap<>();
        List<VmDevice> vmDevices = vmDeviceDao.getUnmanagedDevicesByVmId(vm.getId());
        if (!vmDevices.isEmpty()) {
            StringBuilder id = new StringBuilder();
            for (VmDevice vmDevice : vmDevices) {
                Map struct = new HashMap();
                id.append(VdsProperties.Device);
                id.append("_");
                id.append(vmDevice.getDeviceId());
                if (VmDeviceCommonUtils.isInWhiteList(vmDevice.getType(), vmDevice.getDevice())) {
                    struct.put(VdsProperties.Type, vmDevice.getType().getValue());
                    struct.put(VdsProperties.Device, vmDevice.getDevice());
                    addAddress(vmDevice, struct);
                    struct.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
                    struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
                    devices.add(struct);
                } else {
                    customMap.put(id.toString(), vmDevice.toString());
                }
            }
        }
        createInfo.put(VdsProperties.Custom, customMap);
        createInfo.put(DEVICES, devices);
    }

    @Override
    protected void buildVmBootSequence() {
        // Check if boot sequence in parameters is different from default boot sequence
        if (managedDevices != null) {
            // recalculate boot order from source devices and set it to target devices
            VmDeviceCommonUtils.updateVmDevicesBootOrder(
                    vm,
                    vm.isRunOnce() ? vm.getBootSequence() : vm.getDefaultBootSequence(),
                    managedDevices);
            for (VmDevice vmDevice : managedDevices) {
                for (Map struct : devices) {
                    String deviceId = (String) struct.get(VdsProperties.DeviceId);
                    if (deviceId != null && deviceId.equals(vmDevice.getDeviceId().toString())) {
                        if (vmDevice.getBootOrder() > 0) {
                            struct.put(VdsProperties.BootOrder, String.valueOf(vmDevice.getBootOrder()));
                        } else {
                            struct.keySet().remove(VdsProperties.BootOrder);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void buildSysprepVmPayload(String sysPrepContent) {

        // We do not validate the size of the content being passed to the VM payload by VmPayload.isPayloadSizeLegal().
        // The sysprep file size isn't being verified for 3.0 clusters and below, so we maintain the same behavior here.
        VmPayload vmPayload = new VmPayload();
        vmPayload.setDeviceType(VmDeviceType.FLOPPY);
        vmPayload.getFiles().put(
                vmInfoBuildUtils.getOsRepository().getSysprepFileName(vm.getOs(), vm.getCompatibilityVersion()),
                new String(BASE_64.encode(sysPrepContent.getBytes()), Charset.forName(CharEncoding.UTF_8)));

        VmDevice vmDevice =
                new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                        VmDeviceGeneralType.DISK,
                        VmDeviceType.FLOPPY.getName(),
                        "",
                        0,
                        vmPayload.getSpecParams(),
                        true,
                        true,
                        true,
                        "",
                        null,
                        null,
                        null);
        Map<String, Object> struct = new HashMap<>();
        vmInfoBuildUtils.addFloppyDetails(vmDevice, struct);
        addDevice(struct, vmDevice, vm.getFloppyPath());
    }

    @Override
    protected void buildCloudInitVmPayload(Map<String, byte[]> cloudInitContent) {
        VmPayload vmPayload = new VmPayload();
        vmPayload.setDeviceType(VmDeviceType.CDROM);
        vmPayload.setVolumeId(CLOUD_INIT_VOL_ID);
        for (Map.Entry<String, byte[]> entry : cloudInitContent.entrySet()) {
            vmPayload.getFiles().put(entry.getKey(), new String(BASE_64.encode(entry.getValue()), Charset.forName(CharEncoding.UTF_8)));
        }

        VmDevice vmDevice =
                new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                        VmDeviceGeneralType.DISK,
                        VmDeviceType.CDROM.getName(),
                        "",
                        0,
                        vmPayload.getSpecParams(),
                        true,
                        true,
                        true,
                        "",
                        null,
                        null,
                        null);
        Map<String, Object> struct = new HashMap<>();
        vmInfoBuildUtils.addCdDetails(vmDevice, struct, vm);
        addDevice(struct, vmDevice, "");
    }

    static void addAddress(VmDevice vmDevice, Map<String, Object> struct) {
        Map<String, String> addressMap = XmlRpcStringUtils.string2Map(vmDevice.getAddress());
        if (!addressMap.isEmpty()) {
            struct.put(VdsProperties.Address, addressMap);
        }
    }

    private void addNetworkInterfaceProperties(Map<String, Object> struct,
            VmNic vmInterface,
            VmDevice vmDevice,
            String nicModel) {
        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());
        struct.put(VdsProperties.LINK_ACTIVE, String.valueOf(vmInterface.isLinked()));

        addAddress(vmDevice, struct);
        struct.put(VdsProperties.MAC_ADDR, vmInterface.getMacAddress());
        vmInfoBuildUtils.addBootOrder(vmDevice, struct);
        struct.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
        struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
        struct.put(VdsProperties.NIC_TYPE, nicModel);

        vmInfoBuildUtils.addProfileDataToNic(struct, vm, vmDevice, vmInterface);
        vmInfoBuildUtils.addNetworkFiltersToNic(struct, vmInterface);
    }

    private void addDevice(Map<String, Object> struct, VmDevice vmDevice, String path) {
        boolean isPayload = VmPayload.isPayload(vmDevice.getSpecParams()) &&
                vmDevice.getDevice().equals(VmDeviceType.CDROM.getName());
        Map<String, Object> specParams =
                (vmDevice.getSpecParams() == null) ? Collections.emptyMap() : vmDevice.getSpecParams();
        if (path != null) {
            struct.put(VdsProperties.Path, isPayload ? "" : path);
        }
        if (isPayload) {
            String cdInterface = osRepository.getCdInterface(
                    vm.getOs(),
                    vm.getCompatibilityVersion(),
                    ChipsetType.fromMachineType(vm.getEmulatedMachine()));

            int index = VmDeviceCommonUtils.getCdPayloadDeviceIndex(cdInterface);
            struct.put(VdsProperties.Index, Integer.toString(index));

            if ("scsi".equals(cdInterface)) {
                struct.put(VdsProperties.Address, vmInfoBuildUtils.createAddressForScsiDisk(0, index));
            }
        }
        struct.put(VdsProperties.SpecParams, specParams);
        struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
        vmInfoBuildUtils.addBootOrder(vmDevice, struct);
        devices.add(struct);
        addToManagedDevices(vmDevice);
    }

    private void addToManagedDevices(VmDevice vmDevice) {
        if (managedDevices != null) {
            managedDevices.add(vmDevice);
        }
    }

    private void buildVmUsbControllers() {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.USB.getName());
        for (VmDevice vmDevice : vmDevices) {
            Map struct = new HashMap();
            struct.put(VdsProperties.Type, vmDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmDevice.getDevice());
            vmInfoBuildUtils.setVdsPropertiesFromSpecParams(vmDevice.getSpecParams(), struct);
            struct.put(VdsProperties.SpecParams, new HashMap<String, Object>());
            struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
            addAddress(vmDevice, struct);
            String model = (String) struct.get(VdsProperties.Model);

            // This is a workaround until libvirt will fix the requirement to order these controllers
            if (model != null && vmInfoBuildUtils.isFirstMasterController(model)) {
                devices.add(0, struct);
            } else {
                devices.add(struct);
            }
        }
    }

    private void buildVmUsbSlots() {
        List<VmDevice> vmDevices =
                vmDeviceDao
                        .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
                                VmDeviceGeneralType.REDIR,
                                VmDeviceType.SPICEVMC.getName());
        for (VmDevice vmDevice : vmDevices) {
            Map struct = new HashMap();
            struct.put(VdsProperties.Type, vmDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmDevice.getDevice());
            struct.put(VdsProperties.Bus, USB_BUS);
            struct.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
            struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
            addAddress(vmDevice, struct);
            devices.add(struct);
        }
    }

    @Override
    protected void buildVmUsbDevices() {
        buildVmUsbControllers();
        buildVmUsbSlots();
        buildSmartcardDevice();
    }

    private void buildSmartcardDevice() {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.SMARTCARD,
                VmDeviceType.SMARTCARD.getName());

        for (VmDevice vmDevice : vmDevices) {
            Map struct = new HashMap();
            struct.put(VdsProperties.Type, vmDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmDevice.getDevice());
            addDevice(struct, vmDevice, null);
        }
    }

    @Override
    protected void buildVmMemoryBalloon() {
        if (vm.isRunOnce() && vm.isBalloonEnabled()) {
            Map<String, Object> specParams = new HashMap<>();
            specParams.put(VdsProperties.Model, VdsProperties.Virtio);
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                            VmDeviceGeneralType.BALLOON,
                            VmDeviceType.MEMBALLOON.getName(),
                            "",
                            0,
                            specParams,
                            true,
                            true,
                            true,
                            "",
                            null,
                            null,
                            null);
            addMemBalloonDevice(vmDevice);
        } else {
            // get vm device for this Balloon from DB
            List<VmDevice> vmDevices =
                    vmDeviceDao
                            .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
                                    VmDeviceGeneralType.BALLOON,
                                    VmDeviceType.MEMBALLOON.getName());
            for (VmDevice vmDevice : vmDevices) {
                // skip unamanged devices (handled separtely)
                if (!vmDevice.getIsManaged()) {
                    continue;
                }
                addMemBalloonDevice(vmDevice);
                break; // only one memory balloon should exist
            }
        }
    }

    private void addMemBalloonDevice(VmDevice vmDevice) {
        Map<String, Object> struct = new HashMap<>();
        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());
        Map<String, Object> specParams = vmDevice.getSpecParams();
        // validate & set spec params for balloon device
        if (specParams == null) {
            specParams = new HashMap<>();
            vmDevice.setSpecParams(specParams);
        }
        specParams.put(VdsProperties.Model, VdsProperties.Virtio);
        addAddress(vmDevice, struct);
        addDevice(struct, vmDevice, null);
    }

    @Override
    protected void buildVmWatchdog() {
        List<VmDevice> watchdogs = vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.WATCHDOG);
        for (VmDevice watchdog : watchdogs) {
            HashMap watchdogFromRpc = new HashMap();
            watchdogFromRpc.put(VdsProperties.Type, VmDeviceGeneralType.WATCHDOG.getValue());
            watchdogFromRpc.put(VdsProperties.Device, watchdog.getDevice());
            Map<String, Object> specParams = watchdog.getSpecParams();
            if (specParams == null) {
                specParams = new HashMap<>();
            }
            watchdogFromRpc.put(VdsProperties.SpecParams, specParams);
            addDevice(watchdogFromRpc, watchdog, null);
        }
    }

    @Override
    protected void buildVmVirtioScsi() {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.VIRTIOSCSI.getName());

        Map<DiskInterface, Integer> controllerIndexMap =
                ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new GetControllerIndices()).returnValue();

        int virtioScsiIndex = controllerIndexMap.get(DiskInterface.VirtIO_SCSI);

        for (VmDevice vmDevice : vmDevices) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, VmDeviceGeneralType.CONTROLLER.getValue());
            struct.put(VdsProperties.Device, VdsProperties.Scsi);
            struct.put(VdsProperties.Model, VdsProperties.VirtioScsi);
            struct.put(VdsProperties.Index, Integer.toString(virtioScsiIndex));
            addAddress(vmDevice, struct);

            virtioScsiIndex++;

            addDevice(struct, vmDevice, null);
        }
    }

    @Override
    protected void buildVmVirtioSerial() {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.VIRTIOSERIAL.getName());

        for (VmDevice vmDevice : vmDevices) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, VmDeviceGeneralType.CONTROLLER.getValue());
            struct.put(VdsProperties.Device, VdsProperties.VirtioSerial);
            addAddress(vmDevice, struct);

            addDevice(struct, vmDevice, null);
        }
    }

    protected void buildVmRngDevice() {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.RNG,
                VmDeviceType.VIRTIO.getName());

        for (VmDevice vmDevice : vmDevices) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, VmDeviceGeneralType.RNG.getValue());
            struct.put(VdsProperties.Device, VmDeviceType.VIRTIO.getName());
            struct.put(VdsProperties.Model, VdsProperties.Virtio);
            struct.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
            addDevice(struct, vmDevice, null);
        }
    }

    protected void buildVmNumaProperties() {
        addNumaSetting();
    }

    /**
     * Numa will use the same compatibilityVersion as cpu pinning since
     * numa may also add cpu pinning configuration and the two features
     * have almost the same libvirt version support
     */
    private void addNumaSetting() {
        List<VmNumaNode> vmNumaNodes = vmNumaNodeDao.getAllVmNumaNodeByVmId(vm.getId());
        List<VdsNumaNode> totalVdsNumaNodes = vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(vdsId);
        if (totalVdsNumaNodes.isEmpty()) {
            log.warn("No NUMA nodes found for host {} for vm {} {}",  vdsId, vm.getName(), vm.getId());
            return;
        }

        // if user didn't set specific NUMA conf
        // create a default one with one guest numa node
        if (vmNumaNodes.isEmpty()) {
            if (FeatureSupported.hotPlugMemory(vm.getCompatibilityVersion(), vm.getClusterArch())) {
                VmNumaNode vmNode = new VmNumaNode();
                vmNode.setIndex(0);
                vmNode.setMemTotal(vm.getMemSizeMb());
                for (int i = 0; i < vm.getNumOfCpus(); i++) {
                    vmNode.getCpuIds().add(i);
                }
                vmNumaNodes.add(vmNode);
            } else {
                // no need to send numa if memory hotplug not supported
                return;
            }
        }
        NumaTuneMode numaTune = vm.getNumaTuneMode();

        if (numaTune != null) {
            Map<String, Object> numaTuneSetting =
                    NumaSettingFactory.buildVmNumatuneSetting(numaTune, vmNumaNodes);
            if (!numaTuneSetting.isEmpty()) {
                createInfo.put(VdsProperties.NUMA_TUNE, numaTuneSetting);
            }
        }
        List<Map<String, Object>> createVmNumaNodes = NumaSettingFactory.buildVmNumaNodeSetting(vmNumaNodes);
        if (!createVmNumaNodes.isEmpty()) {
            createInfo.put(VdsProperties.VM_NUMA_NODES, createVmNumaNodes);
        }
        if (StringUtils.isEmpty(vm.getCpuPinning())) {
            Map<String, Object> cpuPinDict =
                    NumaSettingFactory.buildCpuPinningWithNumaSetting(vmNumaNodes, totalVdsNumaNodes);
            if (!cpuPinDict.isEmpty()) {
                createInfo.put(VdsProperties.cpuPinning, cpuPinDict);
            }
        }
    }

    @Override
    protected void buildVmHostDevices() {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.HOSTDEV);

        for (VmDevice vmDevice : vmDevices) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, VmDeviceType.HOST_DEVICE.getName());
            struct.put(VdsProperties.Device, vmDevice.getDevice());
            struct.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
            struct.put(VdsProperties.DeviceId, vmDevice.getId().getDeviceId().toString());
            addAddress(vmDevice, struct);
            devices.add(struct);
        }
    }
}
