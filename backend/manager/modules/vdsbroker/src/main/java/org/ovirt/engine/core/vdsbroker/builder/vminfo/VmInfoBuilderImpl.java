package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
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
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.Network;
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
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.archstrategy.ArchStrategyFactory;
import org.ovirt.engine.core.utils.collections.ComparatorUtils;
import org.ovirt.engine.core.vdsbroker.architecture.CreateAdditionalControllers;
import org.ovirt.engine.core.vdsbroker.architecture.GetBootableDiskIndex;
import org.ovirt.engine.core.vdsbroker.architecture.GetControllerIndices;
import org.ovirt.engine.core.vdsbroker.architecture.MemoryUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.NumaSettingFactory;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmSerialNumberBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class VmInfoBuilderImpl implements VmInfoBuilder {

    private static final Logger log = LoggerFactory.getLogger(VmInfoBuilderImpl.class);

    private static final String DEVICES = "devices";
    private static final String USB_BUS = "usb";

    private final VmInfoBuildUtils vmInfoBuildUtils;
    private final VmDeviceDao vmDeviceDao;
    private final ClusterDao clusterDao;

    private final List<Map<String, Object>> devices = new ArrayList<>();
    private final Map<String, Object> createInfo;
    private final VM vm;

    private OsRepository osRepository;
    private List<VmDevice> bootableDevices = null;
    private Guid vdsId;
    private Cluster cluster;
    private int numOfReservedScsiIndexes = 0;

    VmInfoBuilderImpl(
            VM vm,
            Guid vdsId,
            Map<String, Object> createInfo,
            ClusterDao clusterDao,
            NetworkDao networkDao,
            VmDeviceDao vmDeviceDao,
            VmInfoBuildUtils vmInfoBuildUtils,
            OsRepository osRepository) {
        this.clusterDao = Objects.requireNonNull(clusterDao);
        this.vmDeviceDao = Objects.requireNonNull(vmDeviceDao);
        this.vmInfoBuildUtils = Objects.requireNonNull(vmInfoBuildUtils);
        this.osRepository = Objects.requireNonNull(osRepository);

        this.vdsId = vdsId;
        this.vm = vm;
        this.createInfo = createInfo;
        bootableDevices = new ArrayList<>();
    }

    @Override
    public void buildVmVideoCards() {
        boolean videoCardOverridden = vm.isRunOnce() && vm.getDefaultDisplayType() != null;

        if (videoCardOverridden) {
            if (vm.getDefaultDisplayType() == DisplayType.none) {
                return;
            }
            buildVmVideoDeviceOverridden();
        } else {
            buildVmVideoDevicesFromDb();
        }
    }

    @Override
    public void buildVmGraphicsDevices() {
        if (vm.getDefaultDisplayType() == DisplayType.none) {
            // headless mode, no graphics device is needed
            return;
        }

        Map<GraphicsType, GraphicsInfo> infos = vm.getGraphicsInfos();
        Map<String, Object> specParamsFromVm = new HashMap<>();
        vmInfoBuildUtils.addVmGraphicsOptions(infos, specParamsFromVm, vm);

        if (vm.isRunOnce() && !infos.isEmpty()) {
            // graphics devices that are in the database are overridden
            // by those specified in the run once configuration
            buildVmGraphicsDevicesOverridden(infos, specParamsFromVm);
        } else {
            buildVmGraphicsDevicesFromDb(specParamsFromVm);
        }
    }

    @Override
    public void buildVmCD(VmPayload vmPayload) {
        boolean hasPayload = vmPayload != null && vmPayload.getDeviceType() == VmDeviceType.CDROM;
        // check if we have payload CD
        if (hasPayload) {
            Map<String, Object> struct = vmInfoBuildUtils.buildCdDetails(vmPayload, vm);
            addDevice(struct, vmPayload, "");
        }
        // check first if CD was given as a RunOnce parameter
        if (vm.isRunOnce() && !StringUtils.isEmpty(vm.getCdPath())) {
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                            VmDeviceGeneralType.DISK,
                            VmDeviceType.CDROM.getName(),
                            "",
                            null,
                            true,
                            true,
                            true,
                            "",
                            null,
                            null,
                            null);
            Map<String, Object> struct = vmInfoBuildUtils.buildCdDetails(vmDevice, vm);
            addDevice(struct, vmDevice, vm.getCdPath());
        } else {
            // get vm device for this CD from DB
            List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                    vm.getId(),
                    VmDeviceGeneralType.DISK,
                    VmDeviceType.CDROM);
            for (VmDevice vmDevice : vmDevices) {
                // skip unmanaged devices (handled separately)
                if (!vmDevice.isManaged()) {
                    continue;
                }
                // The Payload is loaded in via RunVmCommand to VM.
                // Payload and its device are handled at the beginning of
                // the method, so no need to add the device again,
                if (VmPayload.isPayload(vmDevice.getSpecParams())) {
                    continue;
                }
                String cdPath = vm.getCdPath();
                Map<String, Object> struct = vmInfoBuildUtils.buildCdDetails(vmDevice, vm);
                vmInfoBuildUtils.addAddress(vmDevice, struct);
                addDevice(struct, vmDevice, cdPath == null ? "" : cdPath);
            }
        }
        numOfReservedScsiIndexes++;
    }

    @Override
    public void buildVmFloppy(VmPayload vmPayload) {
        // check if we have payload Floppy
        boolean hasPayload = vmPayload != null && vmPayload.getDeviceType() == VmDeviceType.FLOPPY;
        if (hasPayload) {
            Map<String, Object>struct = vmInfoBuildUtils.buildFloppyDetails(vmPayload);
            addDevice(struct, vmPayload, "");
        // check first if Floppy was given as a parameter
        } else if (vm.isRunOnce() && !StringUtils.isEmpty(vm.getFloppyPath())) {
            VmDevice vmDevice = vmInfoBuildUtils.createFloppyDevice(vm);
            Map<String, Object> struct = vmInfoBuildUtils.buildFloppyDetails(vmDevice);
            addDevice(struct, vmDevice, vm.getFloppyPath());
        } else {
            // get vm device for this Floppy from DB
            List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                    vm.getId(),
                    VmDeviceGeneralType.DISK,
                    VmDeviceType.FLOPPY);
            for (VmDevice vmDevice : vmDevices) {
                // skip unmanaged devices (handled separately)
                if (!vmDevice.isManaged()) {
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
                String file = vm.getFloppyPath();
                Map<String, Object> struct = vmInfoBuildUtils.buildFloppyDetails(vmDevice);
                addDevice(struct, vmDevice, file);
            }
        }
    }

    @Override
    public void buildVmDrives() {
        boolean bootDiskFound = false;
        List<Disk> disks = vmInfoBuildUtils.getSortedDisks(vm);
        Map<Integer, Map<VmDevice, Integer>> vmDeviceVirtioScsiUnitMap =
                vmInfoBuildUtils.getVmDeviceUnitMapForVirtioScsiDisks(vm);

        Map<Integer, Map<VmDevice, Integer>> vmDeviceSpaprVscsiUnitMap =
                vmInfoBuildUtils.getVmDeviceUnitMapForSpaprScsiDisks(vm);

        Map<Guid, StorageQos> qosCache = new HashMap<>();

        int pinnedDriveIndex = 0;

        for (Disk disk : disks) {
            Map<String, Object> struct = new HashMap<>();
            // get vm device for this disk from DB
            VmDevice vmDevice = vmInfoBuildUtils.getVmDeviceByDiskId(disk.getId(), vm.getId());
            DiskVmElement dve = disk.getDiskVmElementForVm(vm.getId());
            // skip unmanaged devices (handled separately)
            if (!vmDevice.isManaged()) {
                continue;
            }
            if (vmDevice.isPlugged()) {
                struct.put(VdsProperties.Type, vmDevice.getType().getValue());
                struct.put(VdsProperties.Device, vmDevice.getDevice());
                switch (dve.getDiskInterface()) {
                case IDE:
                    struct.put(VdsProperties.INTERFACE, VdsProperties.Ide);
                    break;
                case VirtIO:
                    struct.put(VdsProperties.INTERFACE, VdsProperties.Virtio);
                    int pinTo = vmInfoBuildUtils.pinToIoThreads(vm, pinnedDriveIndex++);
                    if (pinTo > 0) {
                        vmDevice.getSpecParams().put(VdsProperties.pinToIoThread, pinTo);
                    }
                    break;
                case VirtIO_SCSI:
                    // If SCSI pass-through is enabled (DirectLUN disk and SGIO is defined),
                    // set device type as 'lun' (instead of 'disk') and set the specified SGIO.
                    if (disk.getDiskStorageType() == DiskStorageType.LUN && disk.isScsiPassthrough()) {
                        struct.put(VdsProperties.Device, VmDeviceType.LUN.getName());
                        struct.put(VdsProperties.Sgio, disk.getSgio().toString().toLowerCase());
                    }
                case SPAPR_VSCSI:
                    struct.put(VdsProperties.INTERFACE, VdsProperties.Scsi);
                    vmInfoBuildUtils.calculateAddressForScsiDisk(vm, disk, vmDevice, vmDeviceSpaprVscsiUnitMap, vmDeviceVirtioScsiUnitMap);
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
                if (FeatureSupported.passDiscardSupported(vm.getCompatibilityVersion())) {
                    struct.put(VdsProperties.DISCARD, dve.isPassDiscard());
                }
                vmInfoBuildUtils.addAddress(vmDevice, struct);
                switch (disk.getDiskStorageType()) {
                case IMAGE:
                    DiskImage diskImage = (DiskImage) disk;
                    struct.put(VdsProperties.DiskType, vmInfoBuildUtils.getDiskType(vm, diskImage, vmDevice));
                    struct.put(VdsProperties.PoolId, diskImage.getStoragePoolId().toString());
                    struct.put(VdsProperties.DomainId, diskImage.getStorageIds().get(0).toString());
                    struct.put(VdsProperties.ImageId, diskImage.getId().toString());
                    struct.put(VdsProperties.VolumeId, diskImage.getImageId().toString());
                    struct.put(VdsProperties.Format, diskImage.getVolumeFormat()
                            .toString()
                            .toLowerCase());
                    struct.put(VdsProperties.PropagateErrors, disk.getPropagateErrors()
                            .toString()
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
                struct.put(VdsProperties.Shareable,
                        (vmDevice.getSnapshotId() != null)
                                ? VdsProperties.Transient : String.valueOf(disk.isShareable()));
                struct.put(VdsProperties.Optional, Boolean.FALSE.toString());
                struct.put(VdsProperties.ReadOnly, String.valueOf(vmDevice.getReadOnly()));
                struct.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
                struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
                devices.add(struct);
                bootableDevices.add(vmDevice);
            }
        }

        ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new CreateAdditionalControllers(devices));
    }

    @Override
    public void buildVmNetworkInterfaces(Map<Guid, String> passthroughVnicToVfMap) {
        Map<VmDeviceId, VmDevice> devicesByDeviceId =
                Entities.businessEntitiesById(
                        vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                                vm.getId(),
                                VmDeviceGeneralType.INTERFACE,
                                VmDeviceType.BRIDGE));

        devicesByDeviceId.putAll(Entities.businessEntitiesById(
                vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                        vm.getId(),
                        VmDeviceGeneralType.INTERFACE,
                        VmDeviceType.HOST_DEVICE)));

        for (VmNic vmInterface : vm.getInterfaces()) {
            // get vm device for this nic from DB
            VmDevice vmDevice =
                    devicesByDeviceId.get(new VmDeviceId(vmInterface.getId(), vmInterface.getVmId()));

            if (vmDevice != null && vmDevice.isManaged() && vmDevice.isPlugged()) {

                Map<String, Object> struct = new HashMap<>();
                VmInterfaceType ifaceType = VmInterfaceType.rtl8139;

                if (vmInterface.getType() != null) {
                    ifaceType = VmInterfaceType.forValue(vmInterface.getType());
                }

                if (vmInterface.isPassthrough()) {
                    String vfDeviceName = passthroughVnicToVfMap.get(vmInterface.getId());
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
                bootableDevices.add(vmDevice);
            }
        }
    }

    @Override
    public void buildVmSoundDevices() {
        buildVmDevicesFromDb(VmDeviceGeneralType.SOUND, true, null);
    }

    @Override
    public void buildVmConsoleDevice() {
        buildVmDevicesFromDb(VmDeviceGeneralType.CONSOLE, false, null);
    }

    @Override
    public void buildUnmanagedDevices(String hibernationVolHandle) {
        @SuppressWarnings("unchecked")
        Map<String, String> customMap =
                (Map<String, String>) createInfo.getOrDefault(VdsProperties.Custom, new HashMap<>());
        List<VmDevice> vmDevices = vmDeviceDao.getUnmanagedDevicesByVmId(vm.getId());
        if (!vmDevices.isEmpty()) {
            StringBuilder id = new StringBuilder();
            for (VmDevice vmDevice : vmDevices) {
                Map<String, Object> struct = new HashMap<>();
                id.append(VdsProperties.Device);
                id.append("_");
                id.append(vmDevice.getDeviceId());
                if (VmDeviceCommonUtils.isMemory(vmDevice)) {
                    handleMemoryDevice(vmDevice, hibernationVolHandle, devices);
                    continue;
                }
                if (VmDeviceCommonUtils.isInWhiteList(vmDevice.getType(), vmDevice.getDevice())) {
                    struct.put(VdsProperties.Type, vmDevice.getType().getValue());
                    struct.put(VdsProperties.Device, vmDevice.getDevice());
                    vmInfoBuildUtils.addAddress(vmDevice, struct);
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

    /**
     * Memory devices (originates from hotplugs) are sent to VDSM only if VM is being resumed
     * from a snapshot with memory.
     *
     * @param vmDevice memory device
     * @param hibernationVolHandle memory volume identifier if exists, empty string otherwise
     * @param devices devices to be sent to VDSM
     */
    private void handleMemoryDevice(VmDevice vmDevice, String hibernationVolHandle, List<Map<String, Object>> devices) {
        if (StringUtils.isEmpty(hibernationVolHandle)) {
            return;
        }
        devices.add(MemoryUtils.createVmMemoryDeviceMap(vmDevice, true));
    }

    @Override
    public void buildVmBootSequence() {
        // recalculate boot order from source devices and set it to target devices
        VmDeviceCommonUtils.updateVmDevicesBootOrder(
                vm.getBootSequence(),
                bootableDevices,
                vm.getInterfaces(),
                VmDeviceCommonUtils.extractDiskVmElements(vm));
        for (VmDevice vmDevice : bootableDevices) {
            for (Map<String, Object> struct : devices) {
                String deviceId = (String) struct.get(VdsProperties.DeviceId);
                if (deviceId != null && deviceId.equals(vmDevice.getDeviceId().toString())) {
                    if (vmDevice.getBootOrder() > 0) {
                        struct.put(VdsProperties.BootOrder, String.valueOf(vmDevice.getBootOrder()));
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void buildSysprepVmPayload(String sysPrepContent) {
        VmDevice vmDevice = vmInfoBuildUtils.createSysprepPayloadDevice(sysPrepContent, vm);
        Map<String, Object>struct = vmInfoBuildUtils.buildFloppyDetails(vmDevice);
        addDevice(struct, vmDevice, vm.getFloppyPath());
    }

    @Override
    public void buildCloudInitVmPayload(Map<String, byte[]> cloudInitContent) {
        VmDevice vmDevice = vmInfoBuildUtils.createCloudInitPayloadDevice(cloudInitContent, vm);
        Map<String, Object> struct = vmInfoBuildUtils.buildCdDetails(vmDevice, vm);
        addDevice(struct, vmDevice, "");
    }

    @Override
    public void buildVmUsbDevices() {
        buildVmUsbControllers();
        buildVmUsbSlots();
        buildSmartcardDevice();
    }

    @Override
    public void buildVmMemoryBalloon() {
        // get vm device for this Balloon from DB
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.BALLOON,
                VmDeviceType.MEMBALLOON);
        for (VmDevice vmDevice : vmDevices) {
            // skip unamanged devices (handled separtely)
            if (!vmDevice.isManaged()) {
                continue;
            }
            addMemBalloonDevice(vmDevice);
            break; // only one memory balloon should exist
        }
    }

    @Override
    public void buildVmWatchdog() {
        List<VmDevice> watchdogs = vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.WATCHDOG);
        for (VmDevice watchdog : watchdogs) {
            Map<String, Object> watchdogFromRpc = new HashMap<>();
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
    public void buildVmVirtioScsi() {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.VIRTIOSCSI);

        Map<DiskInterface, Integer> controllerIndexMap =
                ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new GetControllerIndices()).returnValue();

        int virtioScsiIndex = controllerIndexMap.get(DiskInterface.VirtIO_SCSI);

        for (VmDevice vmDevice : vmDevices) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, VmDeviceGeneralType.CONTROLLER.getValue());
            struct.put(VdsProperties.Device, VdsProperties.Scsi);
            struct.put(VdsProperties.Model, VdsProperties.VirtioScsi);
            struct.put(VdsProperties.Index, Integer.toString(virtioScsiIndex));
            vmInfoBuildUtils.addAddress(vmDevice, struct);

            virtioScsiIndex++;

            addDevice(struct, vmDevice, null);
        }
    }

    @Override
    public void buildVmVirtioSerial() {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.VIRTIOSERIAL);

        for (VmDevice vmDevice : vmDevices) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, VmDeviceGeneralType.CONTROLLER.getValue());
            struct.put(VdsProperties.Device, VdsProperties.VirtioSerial);
            vmInfoBuildUtils.addAddress(vmDevice, struct);

            addDevice(struct, vmDevice, null);
        }
    }

    @Override
    public void buildVmRngDevice() {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.RNG,
                VmDeviceType.VIRTIO);

        for (VmDevice vmDevice : vmDevices) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, VmDeviceGeneralType.RNG.getValue());
            struct.put(VdsProperties.Device, VmDeviceType.VIRTIO.getName());
            struct.put(VdsProperties.Model, VdsProperties.Virtio);
            struct.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
            addDevice(struct, vmDevice, null);
        }
    }

    /**
     * Numa will use the same compatibilityVersion as cpu pinning since numa may also add cpu pinning configuration and
     * the two features have almost the same libvirt version support
     */
    @Override
    public void buildVmNumaProperties() {
        List<VdsNumaNode> totalVdsNumaNodes = vmInfoBuildUtils.getVdsNumaNodes(vdsId);
        if (totalVdsNumaNodes.isEmpty()) {
            log.warn("No NUMA nodes found for host {} for vm {} {}", vdsId, vm.getName(), vm.getId());
            return;
        }

        List<VmNumaNode> vmNumaNodes = vmInfoBuildUtils.getVmNumaNodes(vm);
        if (vmNumaNodes.isEmpty()) {
            return;
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
    public void buildVmProperties(String hibernationVolHandle) {
        createInfo.put(VdsProperties.vm_guid, vm.getId().toString());
        createInfo.put(VdsProperties.vm_name, vm.getName());
        createInfo.put(VdsProperties.mem_size_mb, vm.getVmMemSizeMb());

        if (FeatureSupported.hotPlugMemory(vm.getCompatibilityVersion(), vm.getClusterArch())) {
            // because QEMU fails if memory and maxMemory are the same
            if (vm.getVmMemSizeMb() != vm.getMaxMemorySizeMb()) {
                createInfo.put(VdsProperties.maxMemSize, vm.getMaxMemorySizeMb());
            }
            createInfo.put(VdsProperties.maxMemSlots, Config.getValue(ConfigValues.MaxMemorySlots));
        }

        createInfo.put(VdsProperties.mem_guaranteed_size_mb, vm.getMinAllocatedMem());
        createInfo.put(VdsProperties.smartcardEnabled, Boolean.toString(vm.isSmartcardEnabled()));
        createInfo.put(VdsProperties.num_of_cpus, String.valueOf(vm.getNumOfCpus()));
        if (vm.getNumOfIoThreads() != 0) {
            createInfo.put(VdsProperties.numOfIoThreads, vm.getNumOfIoThreads());
        }

        if (Config.getValue(ConfigValues.SendSMPOnRunVm)) {
            createInfo.put(VdsProperties.cores_per_socket, Integer.toString(vm.getCpuPerSocket()));
            createInfo.put(VdsProperties.threads_per_core, Integer.toString(vm.getThreadsPerCpu()));
            if (FeatureSupported.supportedInConfig(
                    ConfigValues.HotPlugCpuSupported,
                    vm.getCompatibilityVersion(),
                    vm.getClusterArch())) {
                createInfo.put(
                        VdsProperties.max_number_of_cpus,
                        calcMaxVCpu().toString());
            }
        }

        Map<String, Object> cpuPinning = vmInfoBuildUtils.parseCpuPinning(vm.getCpuPinning());
        if (!cpuPinning.isEmpty()) {
            createInfo.put(VdsProperties.cpuPinning, cpuPinning);
        }

        if (vm.getEmulatedMachine() != null) {
            createInfo.put(VdsProperties.emulatedMachine, vm.getEmulatedMachine());
        }

        createInfo.put(VdsProperties.kvmEnable, "true");
        createInfo.put(VdsProperties.acpiEnable, vm.getAcpiEnable()
                .toString()
                .toLowerCase());
        createInfo.put(VdsProperties.BOOT_MENU_ENABLE, Boolean.toString(vm.isBootMenuEnabled()));

        createInfo.put(VdsProperties.Custom,
                VmPropertiesUtils.getInstance().getVMProperties(vm.getCompatibilityVersion(),
                        vm.getStaticData()));
        createInfo.put(VdsProperties.vm_type, "kvm"); // "qemu", "kvm"
        if (vm.isRunAndPause()) {
            createInfo.put(VdsProperties.launch_paused_param, "true");
        }
        if (vm.isUseHostCpuFlags()) {
            createInfo.put(VdsProperties.cpuType,
                    "hostPassthrough");
        } else if (vm.getCpuName() != null) { // uses dynamic vm data which was already updated by runVmCommand
            createInfo.put(VdsProperties.cpuType, vm.getCpuName());
        }
        createInfo.put(VdsProperties.niceLevel,
                String.valueOf(vm.getNiceLevel()));
        if (vm.getCpuShares() > 0) {
            createInfo.put(VdsProperties.cpuShares,
                    String.valueOf(vm.getCpuShares()));
        }
        if (!StringUtils.isEmpty(hibernationVolHandle)) {
            createInfo.put(VdsProperties.hiberVolHandle, hibernationVolHandle);
        }

        if (osRepository.isLinux(vm.getVmOsId())) {
            createInfo.put(VdsProperties.PitReinjection, "false");
        }

        // Avoid adding Tablet device for High Performance VMs since no USB devices are set
        if (vm.getVmType() != VmType.HighPerformance && vm.getGraphicsInfos().size() == 1 && vm.getGraphicsInfos().containsKey(GraphicsType.VNC)) {
            createInfo.put(VdsProperties.TabletEnable, "true");
        }
        createInfo.put(VdsProperties.transparent_huge_pages,
                vm.isTransparentHugePages() ? "true" : "false");

        if (osRepository.isHypervEnabled(vm.getVmOsId(), vm.getCompatibilityVersion())) {
            createInfo.put(VdsProperties.hypervEnable, "true");
        }

        if (vm.getLeaseStorageDomainId() != null) {
            buildVmLease();
        }

        if (FeatureSupported.isAgentChannelNamingSupported(vm.getCompatibilityVersion())) {
            createInfo.put(VdsProperties.agentChannelName, "ovirt-guest-agent.0");
        }
    }

    public void buildVmLease() {
        Map<String, Object> device = new HashMap<>();
        device.put(VdsProperties.Type, VdsProperties.VmLease);
        device.put(VdsProperties.Device, VdsProperties.VmLease);
        device.put(VdsProperties.DeviceId, Guid.newGuid().toString());
        device.put(VdsProperties.VmLeaseSdId, vm.getLeaseStorageDomainId().toString());
        device.put(VdsProperties.VmLeaseId, vm.getId().toString());
        devices.add(device);
    }

    @Override
    public void buildVmNetworkCluster() {
        // set Display network
        Network net = vmInfoBuildUtils.getDisplayNetwork(vm);
        if (net != null) {
            createInfo.put(VdsProperties.DISPLAY_NETWORK, net.getName());
        }
    }

    @Override
    public void buildVmBootOptions() {
        // Boot Options
        if (!StringUtils.isEmpty(vm.getInitrdUrl())) {
            createInfo.put(VdsProperties.InitrdUrl, vm.getInitrdUrl());
        }
        if (!StringUtils.isEmpty(vm.getKernelUrl())) {
            createInfo.put(VdsProperties.KernelUrl, vm.getKernelUrl());

            if (!StringUtils.isEmpty(vm.getKernelParams())) {
                createInfo.put(VdsProperties.KernelParams,
                        vm.getKernelParams());
            }
        }
    }

    @Override
    public void buildVmTimeZone() {
        // get vm timezone
        createInfo.put(VdsProperties.utc_diff, "" + vmInfoBuildUtils.getVmTimeZone(vm));
    }

    @Override
    public void buildVmSerialNumber() {
        new VmSerialNumberBuilder(vm, getCluster(), createInfo).buildVmSerialNumber();
    }

    @Override
    public void buildVmHostDevices() {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.HOSTDEV);

        for (VmDevice vmDevice : vmDevices) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, VmDeviceType.HOST_DEVICE.getName());
            struct.put(VdsProperties.Device, vmDevice.getDevice());
            struct.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
            struct.put(VdsProperties.DeviceId, vmDevice.getId().getDeviceId().toString());
            vmInfoBuildUtils.addAddress(vmDevice, struct);
            devices.add(struct);
        }
    }

    /**
     * Creates graphics devices from graphics info - this will override the graphics devices from the db. Used when vm
     * is run via run once.
     *
     * @param graphicsInfos
     *            - vm graphics
     *
     * @see #buildVmGraphicsDevicesFromDb(Map)
     */
    private void buildVmGraphicsDevicesOverridden(
            Map<GraphicsType, GraphicsInfo> graphicsInfos,
            Map<String, Object> extraSpecParams) {
        final Comparator<GraphicsType> spiceLastComparator =
                ComparatorUtils.sortLast(GraphicsType.SPICE);
        final List<Entry<GraphicsType, GraphicsInfo>> sortedGraphicsInfos = graphicsInfos.entrySet().stream()
                .sorted(Comparator.comparing(Entry::getKey, spiceLastComparator))
                .collect(Collectors.toList());

        for (Entry<GraphicsType, GraphicsInfo> graphicsInfo : sortedGraphicsInfos) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, VmDeviceGeneralType.GRAPHICS.getValue());
            struct.put(VdsProperties.Device, graphicsInfo.getKey().name().toLowerCase());
            struct.put(VdsProperties.DeviceId, String.valueOf(Guid.newGuid()));
            if (extraSpecParams != null) {
                struct.put(VdsProperties.SpecParams, extraSpecParams);
            }
            devices.add(struct);
        }

        if (!graphicsInfos.isEmpty() && FeatureSupported.isLegacyDisplaySupported(vm.getCompatibilityVersion())) {
            String legacyGraphicsType = (graphicsInfos.size() == 2)
                    ? VdsProperties.QXL
                    : graphicsTypeToLegacyDisplayType(graphicsInfos.keySet().iterator().next());

            createInfo.put(VdsProperties.display, legacyGraphicsType);
        }
    }

    private void buildVmVideoDevicesFromDb() {
        List<VmDevice> vmVideoDevices = vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.VIDEO);
        for (VmDevice vmVideoDevice : vmVideoDevices) {
            // skip unmanaged devices (handled separately)
            if (!vmVideoDevice.isManaged()) {
                continue;
            }

            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, vmVideoDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmVideoDevice.getDevice());
            vmInfoBuildUtils.addAddress(vmVideoDevice, struct);
            struct.put(VdsProperties.SpecParams, vmVideoDevice.getSpecParams());
            struct.put(VdsProperties.DeviceId, String.valueOf(vmVideoDevice.getId().getDeviceId()));
            devices.add(struct);
        }
    }

    private void buildVmVideoDeviceOverridden() {
        Map<String, Object> struct = new HashMap<>();
        struct.put(VdsProperties.Type, VmDeviceGeneralType.VIDEO.getValue());
        struct.put(VdsProperties.Device, vm.getDefaultDisplayType().getDefaultVmDeviceType().getName());
        struct.put(VdsProperties.DeviceId, String.valueOf(Guid.newGuid()));

        devices.add(struct);
    }

    /**
     * Builds vm graphics from database.
     *
     * <p>SPICE device is put at the end of the graphics device list. Libvirt sets QEMU
     * environment variable QEMU_AUDIO_DRV according to the last graphics device and we
     * want to allow sound for SPICE if graphics protocol "SPICE+VNC" is selected.</p>
     */
    private void buildVmGraphicsDevicesFromDb(Map<String, Object> extraSpecParams) {
        Comparator<VmDevice> spiceLastDeviceComparator = Comparator.comparing(
                VmDevice::getDevice,
                ComparatorUtils.sortLast(VmDeviceType.SPICE.getName()));
        buildVmDevicesFromDb(VmDeviceGeneralType.GRAPHICS, false, extraSpecParams, spiceLastDeviceComparator);

        if (FeatureSupported.isLegacyDisplaySupported(vm.getCompatibilityVersion())) {
            String legacyDisplay = deriveDisplayTypeLegacy();
            if (legacyDisplay != null) {
                createInfo.put(VdsProperties.display, legacyDisplay);
            }
        }
    }

    private void buildVmDevicesFromDb(VmDeviceGeneralType generalType,
            boolean addAddress,
            Map<String, Object> extraSpecParams) {
        buildVmDevicesFromDb(generalType, addAddress, extraSpecParams, null);
    }

    /**
     * @param deviceComparator allows to sort devices in resulting {@link #devices} list and thus
     *                         also in libvirt domain xml. {@code null} indicates no special
     *                         ordering.
     */
    private void buildVmDevicesFromDb(VmDeviceGeneralType generalType,
            boolean addAddress,
            Map<String, Object> extraSpecParams,
            Comparator<VmDevice> deviceComparator) {
        final List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), generalType);
        List<VmDevice> sortedDevices = deviceComparator == null
                ? vmDevices
                : vmDevices.stream().sorted(deviceComparator).collect(Collectors.toList());

        for (VmDevice vmDevice : sortedDevices) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, vmDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmDevice.getDevice());

            Map<String, Object> specParams = vmDevice.getSpecParams();
            if (extraSpecParams != null) {
                specParams.putAll(extraSpecParams);
            }
            struct.put(VdsProperties.SpecParams, specParams);

            struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
            if (addAddress) {
                vmInfoBuildUtils.addAddress(vmDevice, struct);
            }
            devices.add(struct);
        }
    }

    private int getBootableDiskIndex(Disk disk) {
        int index = ArchStrategyFactory.getStrategy(vm.getClusterArch())
                .run(new GetBootableDiskIndex(numOfReservedScsiIndexes))
                .returnValue();
        log.info("Bootable disk '{}' set to index '{}'", disk.getId(), index);
        return index;
    }

    private void addNetworkInterfaceProperties(Map<String, Object> struct,
            VmNic vmInterface,
            VmDevice vmDevice,
            String nicModel) {
        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());
        struct.put(VdsProperties.LINK_ACTIVE, String.valueOf(vmInterface.isLinked()));

        vmInfoBuildUtils.addAddress(vmDevice, struct);
        struct.put(VdsProperties.MAC_ADDR, vmInterface.getMacAddress());
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
        devices.add(struct);
        bootableDevices.add(vmDevice);
    }

    private void buildVmUsbControllers() {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.USB);
        for (VmDevice vmDevice : vmDevices) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, vmDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmDevice.getDevice());
            vmInfoBuildUtils.setVdsPropertiesFromSpecParams(vmDevice.getSpecParams(), struct);
            struct.put(VdsProperties.SpecParams, new HashMap<String, Object>());
            struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
            vmInfoBuildUtils.addAddress(vmDevice, struct);
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
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.REDIR,
                VmDeviceType.SPICEVMC);

        for (VmDevice vmDevice : vmDevices) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, vmDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmDevice.getDevice());
            struct.put(VdsProperties.Bus, USB_BUS);
            struct.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
            struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
            vmInfoBuildUtils.addAddress(vmDevice, struct);
            devices.add(struct);
        }
    }

    private void buildSmartcardDevice() {
        List<VmDevice> vmDevices = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.SMARTCARD,
                VmDeviceType.SMARTCARD);

        for (VmDevice vmDevice : vmDevices) {
            Map<String, Object> struct = new HashMap<>();
            struct.put(VdsProperties.Type, vmDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmDevice.getDevice());
            addDevice(struct, vmDevice, null);
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
        vmInfoBuildUtils.addAddress(vmDevice, struct);
        addDevice(struct, vmDevice, null);
    }

    private Integer calcMaxVCpu() {
        return VmCpuCountHelper.calcMaxVCpu(vm, vm.getClusterCompatibilityVersion());
    }

    private String getTimeZoneForVm(VM vm) {
        if (!StringUtils.isEmpty(vm.getTimeZone())) {
            return vm.getTimeZone();
        }

        // else fallback to engine config default for given OS type
        if (osRepository.isWindows(vm.getOs())) {
            return Config.getValue(ConfigValues.DefaultWindowsTimeZone);
        } else {
            return "Etc/GMT";
        }
    }

    private void logUnsupportedInterfaceType() {
        log.error("Unsupported interface type, ISCSI interface type is not supported.");
    }

    private Cluster getCluster() {
        if (cluster == null) {
            cluster = clusterDao.get(vm.getClusterId());
        }
        return cluster;
    }

    /**
     * Derives display type from vm configuration, used with legacy vdsm.
     *
     * @return either "vnc" or "qxl" string or null if the vm is headless
     */
    private String deriveDisplayTypeLegacy() {
        List<VmDevice> vmDevices =
                vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.GRAPHICS);

        if (vmDevices.isEmpty()) {
            return null;
        } else if (vmDevices.size() == 2) { // we have spice & vnc together, we prioritize SPICE
            return VdsProperties.QXL;
        }

        GraphicsType deviceType = GraphicsType.fromString(vmDevices.get(0).getDevice());
        return graphicsTypeToLegacyDisplayType(deviceType);
    }

    private String graphicsTypeToLegacyDisplayType(GraphicsType graphicsType) {
        switch (graphicsType) {
        case SPICE:
            return VdsProperties.QXL;
        case VNC:
            return VdsProperties.VNC;
        default:
            return null;
        }
    }
}
