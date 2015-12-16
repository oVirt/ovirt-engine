package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
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
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.CinderConnectionInfo;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeDriver;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.archstrategy.ArchStrategyFactory;
import org.ovirt.engine.core.vdsbroker.architecture.CreateAdditionalControllers;
import org.ovirt.engine.core.vdsbroker.architecture.GetBootableDiskIndex;
import org.ovirt.engine.core.vdsbroker.architecture.GetControllerIndices;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStringUtils;

@SuppressWarnings({"rawtypes", "unchecked"})
public class VmInfoBuilder extends VmInfoBuilderBase {

    private static final String DEVICES = "devices";
    private static final String USB_BUS = "usb";
    private final static String FIRST_MASTER_MODEL = "ich9-ehci1";
    private static final String CLOUD_INIT_VOL_ID = "config-2";
    private static final Base64 BASE_64 = new Base64(0, null);
    private final List<Map<String, Object>> devices = new ArrayList<>();
    private List<VmDevice> managedDevices = null;
    private final boolean hasNonDefaultBootOrder;
    private Guid vdsId;
    private int numOfReservedScsiIndexes = 0;

    public VmInfoBuilder(VM vm, Guid vdsId, Map createInfo) {
        this.vm = vm;
        this.vdsId = vdsId;
        this.createInfo = createInfo;
        hasNonDefaultBootOrder = (vm.getBootSequence() != vm.getDefaultBootSequence());
        if (hasNonDefaultBootOrder) {
            managedDevices = new ArrayList<>();
        }
    }

    @Override
    protected void buildVmVideoCards() {
        List<VmDevice> vmVideoDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.VIDEO);
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
        boolean usesGraphicsAsDevice = FeatureSupported.graphicsDeviceEnabled(vm.getClusterCompatibilityVersion());

        Map<GraphicsType, GraphicsInfo> infos = vm.getGraphicsInfos();
        Map<String, Object> specParamsFromVm = buildVmGraphicsSpecParamsFromVm(infos);

        if (graphicsOverriden) {
            buildVmGraphicsDevicesOverriden(infos, usesGraphicsAsDevice, specParamsFromVm);
        } else {
            buildVmGraphicsDevicesFromDb(usesGraphicsAsDevice, specParamsFromVm);
        }
    }

    /**
     * Some vm-level settings need to be translated as graphic device specParams.
     * This information must take precedence over that stored on the DB - because these parameters aren't expected
     * to be stored in the DB at all!
     */
    private Map<String, Object> buildVmGraphicsSpecParamsFromVm(Map<GraphicsType, GraphicsInfo> infos) {
        Map<String, Object> specParamsFromVm = null;
        if (infos != null && infos.containsKey(GraphicsType.SPICE)) {
            // harmless if added to VNC devices. Just noise.
            specParamsFromVm = new HashMap();
            addVmSpiceOptions(infos, specParamsFromVm);
        }
        return specParamsFromVm;
    }

    /**
     * Creates graphics devices from graphics info - this will override the graphics devices from the db.
     * Used when vm is run via run once.
     *
     * @param graphicsInfos - vm graphics
     * @param usesGraphicsAsDevice - true if vdsm understands graphics as a separate device, false when vdsm creates
     *                             video and graphics from conf.
     */
    private void buildVmGraphicsDevicesOverriden(Map<GraphicsType, GraphicsInfo> graphicsInfos, boolean usesGraphicsAsDevice, Map<String, Object> extraSpecParams) {
        if (usesGraphicsAsDevice) {
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
     *
     * @param usesGraphicsAsDevice - true if vdsm understands graphics as a separate device, false when vdsm creates
     *                             video and graphics from conf.
     */
    private void buildVmGraphicsDevicesFromDb(boolean usesGraphicsAsDevice, Map<String, Object> extraSpecParams) {
        if (usesGraphicsAsDevice) {
            buildVmDevicesFromDb(VmDeviceGeneralType.GRAPHICS, false, extraSpecParams);
        }

        String legacyDisplay = deriveDisplayTypeLegacy();
        if (legacyDisplay != null) {
            createInfo.put(VdsProperties.display, legacyDisplay);
        }
    }

    private static OsRepository getOsRepository() {
        return SimpleDependencyInjector.getInstance().get(OsRepository.class);
    }

    @Override
    protected void buildVmCD() {
        Map<String, Object> struct;
        boolean hasPayload = vm.getVmPayload() != null && vm.getVmPayload().getDeviceType() == VmDeviceType.CDROM;
        // check if we have payload CD
        if (hasPayload) {
            struct = new HashMap<>();
            addCdDetails(vm.getVmPayload(), struct, vm);
            addDevice(struct, vm.getVmPayload(), "");
        }
        // check first if CD was given as a parameter
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
            addCdDetails(vmDevice, struct, vm);
            addDevice(struct, vmDevice, vm.getCdPath());
        } else {
            // get vm device for this CD from DB
            List<VmDevice> vmDevices =
                    DbFacade.getInstance()
                            .getVmDeviceDao()
                            .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
                                    VmDeviceGeneralType.DISK,
                                    VmDeviceType.CDROM.getName());
            for (VmDevice vmDevice : vmDevices) {
                // skip unamanged devices (handled separtely)
                if (!vmDevice.getIsManaged()) {
                    continue;
                }
                // The Payload is loaded in via RunVmCommand to VM.Payload
                // and its handled at the beginning of the method, so no
                // need to add the device again
                if (VmPayload.isPayload(vmDevice.getSpecParams())) {
                    continue;
                }
                struct = new HashMap<>();
                String cdPath = vm.getCdPath();
                addCdDetails(vmDevice, struct, vm);
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
            addFloppyDetails(vm.getVmPayload(), struct);
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
            addFloppyDetails(vmDevice, struct);
            addDevice(struct, vmDevice, vm.getFloppyPath());
        } else {
            // get vm device for this Floppy from DB
            List<VmDevice> vmDevices =
                    DbFacade.getInstance()
                            .getVmDeviceDao()
                            .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
                                    VmDeviceGeneralType.DISK,
                                    VmDeviceType.FLOPPY.getName());
            for (VmDevice vmDevice : vmDevices) {
                // skip unamanged devices (handled separtely)
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
                addFloppyDetails(vmDevice, struct);
                addDevice(struct, vmDevice, file);
            }
        }
    }

    @Override
    protected void buildVmDrives() {
        boolean bootDiskFound = false;
        List<Disk> disks = getSortedDisks();
        Map<VmDevice, Integer> vmDeviceVirtioScsiUnitMap = getVmDeviceUnitMapForVirtioScsiDisks(vm);

        Map<VmDevice, Integer> vmDeviceSpaprVscsiUnitMap = getVmDeviceUnitMapForSpaprScsiDisks(vm);

        Map<DiskInterface, Integer> controllerIndexMap =
                ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new GetControllerIndices()).returnValue();

        int virtioScsiIndex = controllerIndexMap.get(DiskInterface.VirtIO_SCSI);
        int sPaprVscsiIndex = controllerIndexMap.get(DiskInterface.SPAPR_VSCSI);
        // map to avoid fetching qos object for same disk profile id
        Map<Guid, Guid> diskProfileStorageQosMap = new HashMap<>();
        Map<Guid, Map<String, Long>> storageQosIoTuneMap = new HashMap<>();

        int pinnedDriveIndex = 0;

        for (Disk disk : disks) {
            Map<String, Object> struct = new HashMap<>();
            // get vm device for this disk from DB
            VmDevice vmDevice = getVmDeviceByDiskId(disk.getId(), vm.getId());
            // skip unamanged devices (handled separtely)
            if (!vmDevice.getIsManaged()) {
                continue;
            }
            if (vmDevice.getIsPlugged()) {
                struct.put(VdsProperties.Type, vmDevice.getType().getValue());
                struct.put(VdsProperties.Device, vmDevice.getDevice());
                switch (disk.getDiskInterface()) {
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
                        vmDevice.setAddress(createAddressForScsiDisk(virtioScsiIndex, unit).toString());
                    }
                    break;
                case SPAPR_VSCSI:
                    struct.put(VdsProperties.INTERFACE, VdsProperties.Scsi);

                    if (StringUtils.isEmpty(vmDevice.getAddress())) {
                        // Explicitly define device's address if missing
                        int unit = vmDeviceSpaprVscsiUnitMap.get(vmDevice);
                        vmDevice.setAddress(createAddressForScsiDisk(sPaprVscsiIndex, unit).toString());
                    }
                    break;
                default:
                    logUnsupportedInterfaceType();
                    break;
                }
                // Insure that boot disk is created first
                if (!bootDiskFound && disk.isBoot()) {
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

                        handleIoTune(vm, vmDevice, diskImage, diskProfileStorageQosMap, storageQosIoTuneMap);
                        break;
                    case LUN:
                        LunDisk lunDisk = (LunDisk) disk;
                        struct.put(VdsProperties.Guid, lunDisk.getLun().getLUNId());
                        struct.put(VdsProperties.Format, VolumeFormat.RAW.toString().toLowerCase());
                        struct.put(VdsProperties.PropagateErrors, PropagateErrors.Off.toString().toLowerCase());
                        break;
                    case CINDER:
                        buildCinderDisk((CinderDisk) disk, struct);
                        break;
                }
                addBootOrder(vmDevice, struct);
                struct.put(VdsProperties.Shareable,
                        (vmDevice.getSnapshotId() != null && FeatureSupported.hotPlugDiskSnapshot(vm.getClusterCompatibilityVersion())) ? VdsProperties.Transient
                                : String.valueOf(disk.isShareable()));
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

    public static void buildCinderDisk(CinderDisk cinderDisk, Map<String, Object> struct) {
        CinderConnectionInfo connectionInfo = cinderDisk.getCinderConnectionInfo();
        CinderVolumeDriver cinderVolumeDriver = CinderVolumeDriver.forValue(connectionInfo.getDriverVolumeType());
        if (cinderVolumeDriver == null) {
            log.error("Unsupported Cinder volume driver: '{}' (disk: '{}')",
                    connectionInfo.getDriverVolumeType(), cinderDisk.getDiskAlias());
            return;
        }
        switch (cinderVolumeDriver) {
            case RBD:
                Map<String, Object> connectionInfoData = cinderDisk.getCinderConnectionInfo().getData();
                struct.put(VdsProperties.Path, connectionInfoData.get("name"));
                struct.put(VdsProperties.Format, VolumeFormat.RAW.toString().toLowerCase());
                struct.put(VdsProperties.PropagateErrors, PropagateErrors.Off.toString().toLowerCase());
                struct.put(VdsProperties.Protocol, cinderDisk.getCinderConnectionInfo().getDriverVolumeType());
                struct.put(VdsProperties.DiskType, VdsProperties.NETWORK);

                List<String> hostAddresses = (ArrayList<String>) connectionInfoData.get("hosts");
                List<String> hostPorts = (ArrayList<String>) connectionInfoData.get("ports");
                List<Map<String, Object>> hosts = new ArrayList<>();
                // Looping over hosts addresses to create 'hosts' element
                // (Cinder should ensure that the addresses and ports lists are synced in order).
                for (int i = 0; i < hostAddresses.size(); i++) {
                    Map<String, Object> hostMap = new HashMap<>();
                    hostMap.put(VdsProperties.NetworkDiskName, hostAddresses.get(i));
                    hostMap.put(VdsProperties.NetworkDiskPort, hostPorts.get(i));
                    hostMap.put(VdsProperties.NetworkDiskTransport, VdsProperties.Tcp);
                    hosts.add(hostMap);
                }
                struct.put(VdsProperties.NetworkDiskHosts, hosts);

                boolean authEnabled = (boolean) connectionInfoData.get(VdsProperties.CinderAuthEnabled);
                String secretType = (String) connectionInfoData.get(VdsProperties.CinderSecretType);
                String authUsername = (String) connectionInfoData.get(VdsProperties.CinderAuthUsername);
                String secretUuid = (String) connectionInfoData.get(VdsProperties.CinderSecretUuid);
                if (authEnabled) {
                    Map<String, Object> authMap = new HashMap<>();
                    authMap.put(VdsProperties.NetworkDiskAuthSecretType, secretType);
                    authMap.put(VdsProperties.NetworkDiskAuthUsername, authUsername);
                    authMap.put(VdsProperties.NetworkDiskAuthSecretUuid, secretUuid);
                    struct.put(VdsProperties.NetworkDiskAuth, authMap);
                }
                break;
        }
    }

    /**
     * Prepare the ioTune limits map and add it to the specParams if supported by the cluster
     *
     * @param vm The VM the vmDevice belongs to
     * @param vmDevice The disk device with QoS limits
     * @param diskImage The image that backs up the vmDevice
     * @param diskProfileStorageQosMap Cache object to reuse existing disk profiles entitites when iterating
     * @param storageQosIoTuneMap Cache object to reuse existing ioTune QoS entitites when iterating
     */
    static void handleIoTune(VM vm, VmDevice vmDevice, DiskImage diskImage, Map<Guid, Guid> diskProfileStorageQosMap, Map<Guid, Map<String, Long>> storageQosIoTuneMap) {
        if (FeatureSupported.storageQoS(vm.getClusterCompatibilityVersion())) {
            Map<String, Long> ioTune = buildIoTune(diskImage, diskProfileStorageQosMap, storageQosIoTuneMap);

            if (ioTune != null) {
                if (vmDevice.getSpecParams() == null) {
                    vmDevice.setSpecParams(new HashMap<>());
                }
                vmDevice.getSpecParams().put(VdsProperties.Iotune, ioTune);
            }
        }
    }

    private static Map<String, Long> buildIoTune(DiskImage diskImage,
            Map<Guid, Guid> diskProfileStorageQosMap,
            Map<Guid, Map<String, Long>> storageQosIoTuneMap) {
        Guid diskProfileId = diskImage.getDiskProfileId();
        if (diskProfileId == null) {
            return null;
        }
        Guid storageQosId = diskProfileStorageQosMap.get(diskProfileId);
        if (storageQosId == null) {
            StorageQos storageQos = DbFacade.getInstance().getStorageQosDao().getQosByDiskProfileId(diskProfileId);
            if (storageQos == null) {
                return null;
            }
            storageQosId = storageQos.getId();
            diskProfileStorageQosMap.put(diskProfileId, storageQosId);
            storageQosIoTuneMap.put(storageQosId, buildIoTuneMap(storageQos));
        }

        Map<String, Long> ioTuneMap = storageQosIoTuneMap.get(storageQosId);
        // return map with values
        if (!ioTuneMap.isEmpty()) {
            return ioTuneMap;
        }
        return null;
    }

    private static Map<String, Long> buildIoTuneMap(StorageQos storageQos) {
        // build map
        Map<String, Long> ioTuneMap = new HashMap<>();
        if (storageQos.getMaxThroughput() != null) {
            // Convert MiB/s to B/s vdsm is expecting
            ioTuneMap.put(VdsProperties.TotalBytesSec, storageQos.getMaxThroughput() * 1024 * 1024L);
        }
        if (storageQos.getMaxReadThroughput() != null) {
            // Convert MiB/s to B/s vdsm is expecting

            ioTuneMap.put(VdsProperties.ReadBytesSec, storageQos.getMaxReadThroughput() * 1024 * 1024L);
        }
        if (storageQos.getMaxWriteThroughput() != null) {
            // Convert MiB/s to B/s vdsm is expecting
            ioTuneMap.put(VdsProperties.WriteBytesSec, storageQos.getMaxWriteThroughput() * 1024 * 1024L);
        }
        if (storageQos.getMaxIops() != null) {
            ioTuneMap.put(VdsProperties.TotalIopsSec, storageQos.getMaxIops().longValue());
        }
        if (storageQos.getMaxReadIops() != null) {
            ioTuneMap.put(VdsProperties.ReadIopsSec, storageQos.getMaxReadIops().longValue());
        }
        if (storageQos.getMaxWriteIops() != null) {
            ioTuneMap.put(VdsProperties.WriteIopsSec, storageQos.getMaxWriteIops().longValue());
        }

        return ioTuneMap;
    }

    @Override
    protected void buildVmNetworkInterfaces() {
        Map<VmDeviceId, VmDevice> devicesByDeviceId =
                Entities.businessEntitiesById(DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
                                VmDeviceGeneralType.INTERFACE,
                                VmDeviceType.BRIDGE.getName()));

        devicesByDeviceId.putAll(Entities.businessEntitiesById(DbFacade.getInstance()
                .getVmDeviceDao()
                .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
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
                    addNetworkVirtualFunctionProperties(struct, vmInterface, vmDevice, vfDeviceName, vm);
                } else {
                    addNetworkInterfaceProperties(struct,
                            vmInterface,
                            vmDevice,
                            VmInfoBuilder.evaluateInterfaceType(ifaceType, vm.getHasAgent()),
                            vm.getClusterCompatibilityVersion());
                }

                devices.add(struct);
                addToManagedDevices(vmDevice);
            }
        }
    }

    public static String evaluateInterfaceType(VmInterfaceType ifaceType, boolean vmHasAgent) {
        return ifaceType == VmInterfaceType.rtl8139_pv
                ? (vmHasAgent ? VmInterfaceType.pv.name() : VmInterfaceType.rtl8139.name())
                : ifaceType.getInternalName();
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
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdAndType(vm.getId(),
                                generalType);

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
        Map<String, String> customMap = (createInfo.containsKey(VdsProperties.Custom)) ?
                (Map<String, String>) createInfo.get(VdsProperties.Custom) : new HashMap<>();
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getUnmanagedDevicesByVmId(vm.getId());
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
                    managedDevices,
                    VmDeviceCommonUtils.isOldClusterVersion(vm.getClusterCompatibilityVersion()));
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
        vmPayload.getFiles().put(getOsRepository().getSysprepFileName(vm.getOs(), vm.getClusterCompatibilityVersion()),
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
        addFloppyDetails(vmDevice, struct);
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
        addCdDetails(vmDevice, struct, vm);
        addDevice(struct, vmDevice, "");
    }

    private static void addBootOrder(VmDevice vmDevice, Map<String, Object> struct) {
        String s = String.valueOf(vmDevice.getBootOrder());
        if (!StringUtils.isEmpty(s) && !s.equals("0")) {
            struct.put(VdsProperties.BootOrder, s);
        }
    }

    private static void addAddress(VmDevice vmDevice, Map<String, Object> struct) {
        Map<String, String> addressMap = XmlRpcStringUtils.string2Map(vmDevice.getAddress());
        if (!addressMap.isEmpty()) {
            struct.put(VdsProperties.Address, addressMap);
        }
    }

    private void addNetworkInterfaceProperties(Map<String, Object> struct,
            VmNic vmInterface,
            VmDevice vmDevice,
            String nicModel,
            Version clusterVersion) {
        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());

        if (FeatureSupported.networkLinking(clusterVersion)) {
            struct.put(VdsProperties.LINK_ACTIVE, String.valueOf(vmInterface.isLinked()));
        }

        addAddress(vmDevice, struct);
        struct.put(VdsProperties.MAC_ADDR, vmInterface.getMacAddress());
        addBootOrder(vmDevice, struct);
        struct.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
        struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
        struct.put(VdsProperties.NIC_TYPE, nicModel);

        addProfileDataToNic(struct, vm, vmDevice, vmInterface);
        addNetworkFiltersToNic(struct, clusterVersion);
    }

    static void addNetworkVirtualFunctionProperties(Map<String, Object> struct,
            VmNic vmInterface,
            VmDevice vmDevice,
            String vfName,
            VM vm) {
        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());
        struct.put(VdsProperties.HostDev, vfName);

        addAddress(vmDevice, struct);
        struct.put(VdsProperties.MAC_ADDR, vmInterface.getMacAddress());
        addBootOrder(vmDevice, struct);
        struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));

        Map<String, Object> specParams = new HashMap<>();

        VnicProfile vnicProfile = DbFacade.getInstance().getVnicProfileDao().get(vmInterface.getVnicProfileId());
        Network network = DbFacade.getInstance().getNetworkDao().get(vnicProfile.getNetworkId());
        if (NetworkUtils.isVlan(network)) {
            specParams.put(VdsProperties.VLAN_ID, network.getVlanId());
        }
        struct.put(VdsProperties.SpecParams, specParams);

        addCustomPropertiesForDevice(struct,
                vm,
                vmDevice,
                vm.getClusterCompatibilityVersion(),
                getVnicCustomProperties(vnicProfile));
    }

    public static void addProfileDataToNic(Map<String, Object> struct,
            VM vm,
            VmDevice vmDevice,
            VmNic nic) {
        VnicProfile vnicProfile = null;
        Network network = null;
        String networkName = "";
        List<VnicProfileProperties> unsupportedFeatures = new ArrayList<>();
        if (nic.getVnicProfileId() != null) {
            vnicProfile = DbFacade.getInstance().getVnicProfileDao().get(nic.getVnicProfileId());
            if (vnicProfile != null) {
                network = DbFacade.getInstance().getNetworkDao().get(vnicProfile.getNetworkId());
                networkName = network.getName();
                log.debug("VNIC '{}' is using profile '{}' on network '{}'",
                        nic.getName(), vnicProfile, networkName);
                if (!addQosForDevice(struct, vnicProfile, vm.getClusterCompatibilityVersion())) {
                    unsupportedFeatures.add(VnicProfileProperties.NETWORK_QOS);
                }
            }
        }

        struct.put(VdsProperties.NETWORK, networkName);

        if (!addPortMirroringToVmInterface(struct, vnicProfile, vm.getClusterCompatibilityVersion(), network)) {
            unsupportedFeatures.add(VnicProfileProperties.PORT_MIRRORING);
        }

        if (!addCustomPropertiesForDevice(struct,
                vm,
                vmDevice,
                vm.getClusterCompatibilityVersion(),
                getVnicCustomProperties(vnicProfile))) {
            unsupportedFeatures.add(VnicProfileProperties.CUSTOM_PROPERTIES);
        }

        reportUnsupportedVnicProfileFeatures(vm, nic, vnicProfile, unsupportedFeatures);
    }

    private static boolean addPortMirroringToVmInterface(Map<String, Object> struct,
            VnicProfile vnicProfile,
            Version version,
            Network network) {

        if (vnicProfile != null && vnicProfile.isPortMirroring()) {
            if (FeatureSupported.portMirroring(version)) {
                struct.put(VdsProperties.PORT_MIRRORING, network == null ? Collections.<String> emptyList()
                        : Collections.singletonList(network.getName()));
            } else {
                return false;
            }
        }

        return true;
    }

    private static boolean addQosForDevice(Map<String, Object> struct,
            VnicProfile vnicProfile,
            Version clusterCompatibilityVersion) {

        Guid qosId = vnicProfile.getNetworkQosId();
        if (!FeatureSupported.networkQoS(clusterCompatibilityVersion)) {
            return qosId == null;
        }

        Map<String, Object> specParams = (Map<String, Object>) struct.get(VdsProperties.SpecParams);
        if (specParams == null) {
            specParams = new HashMap<>();
            struct.put(VdsProperties.SpecParams, specParams);
        }
        NetworkQoS networkQoS = (qosId == null) ? new NetworkQoS() : DbFacade.getInstance().getNetworkQosDao().get(qosId);
        NetworkQosMapper qosMapper =
                new NetworkQosMapper(specParams, VdsProperties.QOS_INBOUND, VdsProperties.QOS_OUTBOUND);
        qosMapper.serialize(networkQoS);

        return true;
    }

    public static Map<String, String> getVnicCustomProperties(VnicProfile vnicProfile) {
        Map<String, String> customProperties = null;

        if (vnicProfile != null) {
            customProperties = vnicProfile.getCustomProperties();
        }

        return customProperties == null ? new HashMap<>() : customProperties;
    }

    public static boolean addCustomPropertiesForDevice(Map<String, Object> struct,
            VM vm,
            VmDevice vmDevice,
            Version clusterVersion,
            Map<String, String> customProperties) {

        if (customProperties == null) {
            customProperties = new HashMap<>();
        }

        customProperties.putAll(vmDevice.getCustomProperties());
        Map<String, String> runtimeCustomProperties = vm.getRuntimeDeviceCustomProperties().get(vmDevice.getId());
        if (runtimeCustomProperties != null) {
            customProperties.putAll(runtimeCustomProperties);
        }

        if (!customProperties.isEmpty()) {
            if (FeatureSupported.deviceCustomProperties(clusterVersion)) {
                struct.put(VdsProperties.Custom, customProperties);
            } else {
                return false;
            }
        }

        return true;
    }

    public static void addNetworkFiltersToNic(Map<String, Object> struct, Version clusterVersion) {
        if (FeatureSupported.antiMacSpoofing(clusterVersion)
                && Config.<Boolean> getValue(ConfigValues.EnableMACAntiSpoofingFilterRules)) {
            struct.put(VdsProperties.NW_FILTER, NetworkFilters.NO_MAC_SPOOFING.getFilterName());
        }
    }

    private static void addFloppyDetails(VmDevice vmDevice, Map<String, Object> struct) {
        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());
        struct.put(VdsProperties.Index, "0"); // IDE slot 2 is reserved by VDSM to CDROM
        struct.put(VdsProperties.INTERFACE, VdsProperties.Fdc);
        struct.put(VdsProperties.ReadOnly, String.valueOf(vmDevice.getIsReadOnly()));
        struct.put(VdsProperties.Shareable, Boolean.FALSE.toString());
    }

    private static void addCdDetails(VmDevice vmDevice, Map<String, Object> struct, VM vm) {
        OsRepository osRepository = getOsRepository();

        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());

        String cdInterface = osRepository.getCdInterface(vm.getOs(),
                vm.getClusterCompatibilityVersion());

        if ("scsi".equals(cdInterface)) {
            struct.put(VdsProperties.Index, "0"); // SCSI unit 0 is reserved by VDSM to CDROM
            struct.put(VdsProperties.Address, createAddressForScsiDisk(0, 0));
        } else if ("ide".equals(cdInterface)) {
            struct.put(VdsProperties.Index, "2"); // IDE slot 2 is reserved by VDSM to CDROM
        }

        struct.put(VdsProperties.INTERFACE, cdInterface);

        struct.put(VdsProperties.ReadOnly, Boolean.TRUE.toString());
        struct.put(VdsProperties.Shareable, Boolean.FALSE.toString());
    }

    private void addDevice(Map<String, Object> struct, VmDevice vmDevice, String path) {
        boolean isPayload = (VmPayload.isPayload(vmDevice.getSpecParams()) &&
                vmDevice.getDevice().equals(VmDeviceType.CDROM.getName()));
        Map<String, Object> specParams =
                (vmDevice.getSpecParams() == null) ? Collections.<String, Object> emptyMap() : vmDevice.getSpecParams();
        if (path != null) {
            struct.put(VdsProperties.Path, (isPayload) ? "" : path);
        }
        if (isPayload) {
            String cdInterface = osRepository.getCdInterface(vm.getOs(),
                    vm.getClusterCompatibilityVersion());

            if ("scsi".equals(cdInterface)) {
                struct.put(VdsProperties.Index, "1"); // SCSI unit 1 is reserved for payload
                struct.put(VdsProperties.Address, createAddressForScsiDisk(0, 1));
            } else if ("ide".equals(cdInterface)) {
                // 3 is magic number for payload - we are using it as hdd
                struct.put(VdsProperties.Index, "3");
            }
        }
        struct.put(VdsProperties.SpecParams, specParams);
        struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
        addBootOrder(vmDevice, struct);
        devices.add(struct);
        addToManagedDevices(vmDevice);
    }

    private void addToManagedDevices(VmDevice vmDevice) {
        if (managedDevices != null) {
            managedDevices.add(vmDevice);
        }
    }

    private void buildVmUsbControllers() {
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
                                VmDeviceGeneralType.CONTROLLER,
                                VmDeviceType.USB.getName());
        for (VmDevice vmDevice : vmDevices) {
            Map struct = new HashMap();
            struct.put(VdsProperties.Type, vmDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmDevice.getDevice());
            setVdsPropertiesFromSpecParams(vmDevice.getSpecParams(), struct);
            struct.put(VdsProperties.SpecParams, new HashMap<String, Object>());
            struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));
            addAddress(vmDevice, struct);
            String model = (String) struct.get(VdsProperties.Model);

            // This is a workaround until libvirt will fix the requirement to order these controllers
            if (model != null && isFirstMasterController(model)) {
                devices.add(0, struct);
            } else {
                devices.add(struct);
            }
        }
    }

    private void buildVmUsbSlots() {
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
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
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
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
                    DbFacade.getInstance()
                            .getVmDeviceDao()
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

    private static void setVdsPropertiesFromSpecParams(Map<String, Object> specParams, Map<String, Object> struct) {
        Set<Entry<String, Object>> values = specParams.entrySet();
        for (Entry<String, Object> currEntry : values) {
            if (currEntry.getValue() instanceof String) {
                struct.put(currEntry.getKey(), currEntry.getValue());
            } else if (currEntry.getValue() instanceof Map) {
                struct.put(currEntry.getKey(), currEntry.getValue());
            }
        }
    }

    /**
     * This method returns true if it is the first master model It is used due to the requirement to send this device
     * before the other controllers. There is an open bug on libvirt on that. Until then we make sure it is passed
     * first.
     */
    private static boolean isFirstMasterController(String model) {
        return model.equalsIgnoreCase(FIRST_MASTER_MODEL);
    }

    @Override
    protected void buildVmWatchdog() {
        List<VmDevice> watchdogs =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdAndType(vm.getId(),
                                VmDeviceGeneralType.WATCHDOG);
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
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
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
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                .getVmDeviceDao()
                .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
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

    /**
     * @return a map containing an appropriate unit (disk's index in VirtIO-SCSI controller) for each vm device.
     */
    public static Map<VmDevice, Integer> getVmDeviceUnitMapForVirtioScsiDisks(VM vm) {
        return getVmDeviceUnitMapForScsiDisks(vm, DiskInterface.VirtIO_SCSI, false);
    }

    /**
     * @return a map containing an appropriate unit (disk's index in sPAPR VSCSI controller) for each vm device.
     */
    public static Map<VmDevice, Integer> getVmDeviceUnitMapForSpaprScsiDisks(VM vm) {
        return getVmDeviceUnitMapForScsiDisks(vm, DiskInterface.SPAPR_VSCSI, true);
    }

    public static Map<VmDevice, Integer> getVmDeviceUnitMapForScsiDisks(VM vm,
            DiskInterface scsiInterface,
            boolean reserveFirstTwoLuns) {
        List<Disk> disks = new ArrayList<>(vm.getDiskMap().values());
        Map<VmDevice, Integer> vmDeviceUnitMap = new HashMap<>();
        Map<VmDevice, Disk> vmDeviceDiskMap = new HashMap<>();

        for (Disk disk : disks) {
            if (disk.getDiskInterface() == scsiInterface) {
                VmDevice vmDevice = getVmDeviceByDiskId(disk.getId(), vm.getId());
                Map<String, String> address = XmlRpcStringUtils.string2Map(vmDevice.getAddress());
                String unitStr = address.get(VdsProperties.Unit);

                // If unit property is available adding to 'vmDeviceUnitMap';
                // Otherwise, adding to 'vmDeviceDiskMap' for setting the unit property later.
                if (StringUtils.isNotEmpty(unitStr)) {
                    vmDeviceUnitMap.put(vmDevice, Integer.valueOf(unitStr));
                }
                else {
                    vmDeviceDiskMap.put(vmDevice, disk);
                }
            }
        }

        // Find available unit (disk's index in VirtIO-SCSI controller) for disks with empty address
        for (Entry<VmDevice, Disk> entry : vmDeviceDiskMap.entrySet()) {
            int unit = getAvailableUnitForScsiDisk(vmDeviceUnitMap, reserveFirstTwoLuns);
            vmDeviceUnitMap.put(entry.getKey(), unit);
        }

        return vmDeviceUnitMap;
    }

    public static int getAvailableUnitForScsiDisk(Map<VmDevice, Integer> vmDeviceUnitMap, boolean reserveFirstTwoLuns) {
        int unit = reserveFirstTwoLuns ? 2 : 0;
        while (vmDeviceUnitMap.containsValue(unit)) {
            unit++;
        }
        return unit;
    }

    public static Map<String, String> createAddressForScsiDisk(int controller, int unit) {
        Map<String, String> addressMap = new HashMap<>();
        addressMap.put(VdsProperties.Type, "drive");
        addressMap.put(VdsProperties.Controller, String.valueOf(controller));
        addressMap.put(VdsProperties.Bus, "0");
        addressMap.put(VdsProperties.target, "0");
        addressMap.put(VdsProperties.Unit, String.valueOf(unit));
        return addressMap;
    }

    protected void buildVmRngDevice() {
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
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
        final String compatibilityVersion = vm.getClusterCompatibilityVersion().toString();
        addNumaSetting(compatibilityVersion);
    }

    /**
     * Numa will use the same compatibilityVersion as cpu pinning since
     * numa may also add cpu pinning configuration and the two features
     * have almost the same libvirt version support
     */
    private void addNumaSetting(final String compatibilityVersion) {
        if (Boolean.TRUE.equals(Config.<Boolean> getValue(ConfigValues.CpuPinningEnabled,
                compatibilityVersion))) {
            List<VmNumaNode> vmNumaNodes = DbFacade.getInstance().getVmNumaNodeDao().getAllVmNumaNodeByVmId(vm.getId());
            List<VdsNumaNode> totalVdsNumaNodes = DbFacade.getInstance().getVdsNumaNodeDao()
                    .getAllVdsNumaNodeByVdsId(vdsId);
            if (totalVdsNumaNodes.isEmpty()) {
                log.warn("No NUMA nodes found for host {} for vm {} {}",  vdsId, vm.getName(), vm.getId());
                return;
            }

            // if user didn't set specific NUMA conf
            // create a default one with one guest numa node
            if (vmNumaNodes.isEmpty()) {
                if (FeatureSupported.hotPlugMemory(vm.getClusterCompatibilityVersion(), vm.getClusterArch())) {
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
                        NumaSettingFactory.buildVmNumatuneSetting(numaTune, vmNumaNodes, totalVdsNumaNodes);
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
    }

    @Override
    protected void buildVmHostDevices() {
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.HOSTDEV);

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
