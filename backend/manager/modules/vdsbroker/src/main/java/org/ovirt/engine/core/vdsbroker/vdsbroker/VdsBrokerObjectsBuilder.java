package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.AutoNumaBalanceStatus;
import org.ovirt.engine.core.common.businessentities.CpuStatistics;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.GuestContainer;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.HugePage;
import org.ovirt.engine.core.common.businessentities.KdumpStatus;
import org.ovirt.engine.core.common.businessentities.LeaseStatus;
import org.ovirt.engine.core.common.businessentities.MDevType;
import org.ovirt.engine.core.common.businessentities.NumaNodeStatistics;
import org.ovirt.engine.core.common.businessentities.OsType;
import org.ovirt.engine.core.common.businessentities.SessionState;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.V2VJobInfo;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSDomainsData;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.businessentities.VmBalloonInfo;
import org.ovirt.engine.core.common.businessentities.VmBlockJob;
import org.ovirt.engine.core.common.businessentities.VmBlockJobType;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmExitReason;
import org.ovirt.engine.core.common.businessentities.VmExitStatus;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.common.businessentities.VmJobState;
import org.ovirt.engine.core.common.businessentities.VmJobType;
import org.ovirt.engine.core.common.businessentities.VmPauseStatus;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.BondMode;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NameServer;
import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.Vlan;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LeaseJobStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.network.DnsResolverConfigurationDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.CollectionUtils;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.ovirt.engine.core.utils.network.predicate.InterfaceByAddressPredicate;
import org.ovirt.engine.core.utils.network.predicate.IpAddressPredicate;
import org.ovirt.engine.core.vdsbroker.NetworkStatisticsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class encapsulate the knowledge of how to create objects from the VDS RPC protocol response.
 * This class has methods that receive struct and construct the following Classes: VmDynamic VdsDynamic VdsStatic.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
@Singleton
public class VdsBrokerObjectsBuilder {

    private static final Logger log = LoggerFactory.getLogger(VdsBrokerObjectsBuilder.class);

    private static final int VNC_START_PORT = 5900;
    private static final double NANO_SECONDS = 1000000000;

    private static final Comparator<VdsNumaNode> numaNodeComparator = Comparator.comparing(VdsNumaNode::getIndex);
    private static final Pattern IPV6_ADDRESS_CAPTURE_PREFIX_PATTERN = Pattern.compile("^.*?/(\\d+)?$");
    private static final Pattern IPV6_ADDRESS_CAPTURE_PATTERN = Pattern.compile("^([^/]+)(:?/\\d{1,3})?$");

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private DnsResolverConfigurationDao dnsResolverConfigurationDao;

    public VM buildVmsDataFromExternalProvider(Map<String, Object> struct) {
        VmStatic vmStatic = buildVmStaticDataFromExternalProvider(struct);
        if (vmStatic == null) {
            return null;
        }

        VM vm = new VM(vmStatic, buildVMDynamicDataFromList(struct), new VmStatistics());
        vm.getImages().forEach(image -> vm.getDiskMap().put(Guid.newGuid(), image));

        try {
            vm.setClusterArch(parseArchitecture(struct));
        } catch (IllegalArgumentException ex) {
            log.warn("Illegal architecture type: {}, replacing with x86_64", struct.get(VdsProperties.vm_arch));
            vm.setClusterArch(ArchitectureType.x86_64);
        } catch (NullPointerException ex) {
            log.warn("null architecture type, replacing with x86_64, {}", vm);
            vm.setClusterArch(ArchitectureType.x86_64);
        }

        return vm;
    }

    /**
     * Convert the devices map and make a list of {@linkplain DiskImage}
     * Mainly used to import the Hosted Engine Vm disks.
     *
     * @return A List of Disk Images {@linkplain DiskImage}
     */
    public ArrayList<DiskImage> buildDiskImagesFromDevices(Map<String, Object> vmStruct, Guid vmId) {
        ArrayList<DiskImage> diskImages = new ArrayList<>();
        Object[] devices = (Object[]) vmStruct.get("devices");
        if (devices != null) {
            for (Object device : devices) {
                Map <String, Object> deviceMap = (Map<String, Object>) device;
                if (VdsProperties.Disk.equals(deviceMap.get(VdsProperties.Device))) {
                    DiskImage image = new DiskImage();
                    image.setDiskAlias((String) deviceMap.get(VdsProperties.Alias));
                    Long size = assignLongValue(deviceMap, VdsProperties.disk_apparent_size);
                    image.setSize(size != null ? size : 0);
                    Double actualSize = assignDoubleValue(deviceMap, VdsProperties.disk_true_size);
                    image.setActualSize(actualSize != null ? actualSize : 0);
                    image.setVolumeFormat(VolumeFormat.valueOf(((String) deviceMap.get(VdsProperties.Format)).toUpperCase()));
                    image.setShareable(false);
                    String id = assignStringValue(deviceMap, VdsProperties.DeviceId);
                    if (id == null) {
                        id = assignStringValue(deviceMap, VdsProperties.ImageId);
                    }
                    image.setId(Guid.createGuidFromString(id));
                    image.setImageId(Guid.createGuidFromString((String) deviceMap.get(VdsProperties.VolumeId)));
                    Guid domainId = Guid.createGuidFromString((String) deviceMap.get(VdsProperties.DomainId));
                    List<Guid> domainIds = Collections.singletonList(domainId);
                    image.setStorageIds(new ArrayList<>(domainIds));
                    // TODO not sure how to extract that info
                    image.setVolumeType(VolumeType.Preallocated);

                    DiskVmElement dve = new DiskVmElement(image.getId(), vmId);
                    image.setDiskVmElements(Collections.singletonList(dve));
                    switch ((String) deviceMap.get("iface")) {
                    case "virtio":
                        dve.setDiskInterface(DiskInterface.VirtIO);
                        break;
                    case "iscsi":
                    case "scsi":
                        dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
                        break;
                    case "ide":
                        dve.setDiskInterface(DiskInterface.IDE);
                        break;
                    case "sata":
                        dve.setDiskInterface(DiskInterface.SATA);
                        break;
                    }
                    diskImages.add(image);
                }
            }
        }
        return diskImages;
    }
    /**
     * Convert the devices map and make a list of {@linkplain VmNetworkInterface}
     * Mainly used to import the Hosted Engine Vm disks.
     *
     * @return A List of VM network interfaces {@linkplain VmNetworkInterface}
     */
    public ArrayList<VmNetworkInterface> buildVmNetworkInterfacesFromDevices(Map<String, Object> vmStruct) {
        ArrayList<VmNetworkInterface> nics = new ArrayList<>();
        Object[] devices = (Object[]) vmStruct.get(VdsProperties.Devices);
        if (devices != null) {
            for (Object device : devices) {
                Map<String, Object> deviceMap = (Map<String, Object>) device;
                if (VdsProperties.VM_INTERFACE_DEVICE_TYPE.equals(deviceMap.get(VdsProperties.Type))) {
                    VmNetworkInterface nic = new VmNetworkInterface();
                    nic.setId(Guid.createGuidFromString((String)deviceMap.get(VdsProperties.DeviceId)));
                    nic.setMacAddress((String) deviceMap.get(VdsProperties.MAC_ADDR));
                    nic.setName((String) deviceMap.get(VdsProperties.Name));
                    // FIXME we can't deduce the network profile by the network name. its many to many.
                    nic.setNetworkName((String) deviceMap.get(VdsProperties.NETWORK));
                    String nicModel = (String) deviceMap.get(VdsProperties.NIC_TYPE);
                    if ("virtio".equals(nicModel)) {
                        nicModel = "pv";
                    }
                    nic.setType(VmInterfaceType.valueOf(nicModel).getValue());
                    if (deviceMap.containsKey(VdsProperties.Model)) {
                        String model = (String) deviceMap.get(VdsProperties.Model);
                        for (VmInterfaceType type : VmInterfaceType.values()) {
                            if (model.equals(type.getInternalName())) {
                                nic.setType(type.getValue());
                                break;
                            }
                        }
                    }
                    nics.add(nic);
                }
            }
        }
        return nics;
    }

    public VmDevice buildConsoleDevice(Map<String, Object> vmStruct, Guid vmId){
        Object[] devices = (Object[]) vmStruct.get(VdsProperties.Devices);
        if (devices != null) {
            for (Object device : devices) {
                Map<String, Object> vdsmDevice = (Map<String, Object>) device;
                String deviceName = (String) vdsmDevice.get(VdsProperties.Device);
                if (VmDeviceType.CONSOLE.getName().equals(deviceName)) {
                    String typeName = (String) vdsmDevice.get(VdsProperties.Type);
                    String alias = StringUtils.defaultString((String) vdsmDevice.get(VdsProperties.Alias));
                    Guid newDeviceId = Guid.createGuidFromString((String) vdsmDevice.get(VdsProperties.DeviceId));
                    VmDeviceId id = new VmDeviceId(newDeviceId, vmId);
                    VmDevice consoleDevice = new VmDevice(id,
                            VmDeviceGeneralType.forValue(typeName),
                            deviceName,
                            "",
                            new HashMap<>(),
                            false,
                            true,
                            false,
                            alias,
                            null,
                            null,
                            null);
                    return consoleDevice;
                }
            }
        }
        return null;
    }

    private VmStatic buildVmStaticDataFromExternalProvider(Map<String, Object> struct) {
        if (!struct.containsKey(VdsProperties.vm_guid) || !struct.containsKey(VdsProperties.vm_name)
                || !struct.containsKey(VdsProperties.mem_size_mb)
                || !struct.containsKey(VdsProperties.num_of_cpus)) {
            return null;
        }

        VmStatic vmStatic = new VmStatic();
        vmStatic.setId(Guid.createGuidFromString((String) struct.get(VdsProperties.vm_guid)));
        vmStatic.setName((String) struct.get(VdsProperties.vm_name));
        vmStatic.setMemSizeMb(parseIntVdsProperty(struct.get(VdsProperties.mem_size_mb)));
        vmStatic.setNumOfSockets(parseIntVdsProperty(struct.get(VdsProperties.num_of_cpus)));
        vmStatic.setCustomCpuName((String) struct.get(VdsProperties.cpu_model));
        vmStatic.setCustomEmulatedMachine((String) struct.get(VdsProperties.emulatedMachine));
        addGraphicsDeviceFromExternalProvider(vmStatic, struct);

        if (struct.containsKey(VdsProperties.vm_disks)) {
            for (Object disk : (Object[]) struct.get(VdsProperties.vm_disks)) {
                Map<String, Object> diskMap = (Map<String, Object>) disk;
                if (VdsProperties.Disk.equals(diskMap.get(VdsProperties.type))) {
                    DiskImage image = buildDiskImageFromExternalProvider(diskMap, vmStatic.getId());
                    vmStatic.getImages().add(image);
                }
            }
        }

        if (struct.containsKey(VdsProperties.NETWORKS)) {
            int idx = 0;
            for (Object networkMap : (Object[]) struct.get(VdsProperties.NETWORKS)) {
                VmNetworkInterface nic = buildNetworkInterfaceFromExternalProvider((Map<String, Object>) networkMap);
                nic.setName(String.format("nic%d", ++idx));
                nic.setVmName(vmStatic.getName());
                nic.setVmId(vmStatic.getId());
                vmStatic.getInterfaces().add(nic);
            }
        }

        return vmStatic;
    }

    /**
     *  libvirt video: "vga", "cirrus", "vmvga", "xen", "vbox", "qxl"
     *  ovirt video: "vga", "cirrus", "qxl"
     *  libvirt grahics: sdl, vnc, spice, rdp or desktop
     *  ovirt graphics: cirrus, spice, vnc
     *  try to add the displaytype and graphics if ovirt support the channels
     */
    private static void addGraphicsDeviceFromExternalProvider(VmStatic vmStatic, Map<String, Object> struct) {
        Object graphicsName = struct.get(VdsProperties.GRAPHICS_DEVICE);
        Object videoName =  struct.get(VdsProperties.VIDEO_DEVICE);
        if (graphicsName == null || videoName == null) {
            return;
        }
        try {
            vmStatic.setDefaultDisplayType(DisplayType.valueOf(videoName.toString()));
        } catch (IllegalArgumentException ex) {
            log.error("Illegal video name '{}'.", videoName.toString());
            return;
        }
        GraphicsType graphicsType = GraphicsType.fromString(graphicsName.toString());
        if (graphicsType == null) {
            log.error("Illegal graphics name '{}'.", graphicsName.toString());
            return;
        }

        VmDeviceCommonUtils.addGraphicsDevice(vmStatic, graphicsType.getCorrespondingDeviceType());
        VmDeviceCommonUtils.addVideoDevice(vmStatic);
    }

    private static DiskImage buildDiskImageFromExternalProvider(Map<String, Object> map, Guid vmId) {
        DiskImage image = new DiskImage();
        image.setDiskAlias((String) map.get(VdsProperties.Alias));
        image.setSize(Long.parseLong((String) map.get(VdsProperties.DISK_VIRTUAL_SIZE)));
        image.setActualSizeInBytes(Long.parseLong((String) map.get(VdsProperties.DISK_ALLOCATION)));
        image.setId(Guid.newGuid());
        if (map.containsKey(VdsProperties.Format)) {
            image.setVolumeFormat(VolumeFormat.valueOf(((String) map.get(VdsProperties.Format)).toUpperCase()));
        }

        DiskVmElement dve = buildDiskVmElementWithDiskInterfaceFromExternalProvider(map, image, vmId);
        image.setDiskVmElements(Collections.singletonList(dve));

        return image;
    }

    /**
     * TODO: NOTE that currently there is a limitation of VDSM sending only the
     * disk target dev name per each disk, therefore it will send "sdx" for 3 different
     * disk interfaces: SCSI,USB and SATA.
     * As a result, all those 3 mentioned disk interfaces will be mapped to SCSI
     * interface in target VM.
     * We need to receive the target bus name for each disk in order to differentiate.
     */
    private static DiskVmElement buildDiskVmElementWithDiskInterfaceFromExternalProvider(Map<String, Object> map, DiskImage image, Guid vmId) {
        DiskVmElement dve = new DiskVmElement(image.getId(), vmId);
        String diskDevName = (String) map.get(VdsProperties.DISK_TARGET_DEV_NAME);
        diskDevName = (diskDevName == null || diskDevName.length() < 3) ? "" : diskDevName.substring(0, 2);
        switch (diskDevName) {
        case "sd":
            dve.setDiskInterface(DiskInterface.VirtIO_SCSI);
            break;
        case "hd":
            dve.setDiskInterface(DiskInterface.IDE);
            break;
        case "vd":
        default:
            dve.setDiskInterface(DiskInterface.VirtIO);
        }
        return dve;
    }

    private static VmNetworkInterface buildNetworkInterfaceFromExternalProvider(Map<String, Object> map) {
        VmNetworkInterface nic = new VmNetworkInterface();
        nic.setMacAddress((String) map.get(VdsProperties.MAC_ADDR));
        nic.setRemoteNetworkName((String) map.get(VdsProperties.BRIDGE));

        nic.setType(VmInterfaceType.pv.getValue());
        if (map.containsKey(VdsProperties.Model)) {
            String model = (String) map.get(VdsProperties.Model);
            for (VmInterfaceType type : VmInterfaceType.values()) {
                if (model.equals(type.getInternalName())) {
                    nic.setType(type.getValue());
                    break;
                }
            }
        }

        return nic;
    }

    public VmDynamic buildVMDynamicDataFromList(Map<String, Object> struct) {
        VmDynamic vmdynamic = new VmDynamic();
        if (struct.containsKey(VdsProperties.vm_guid)) {
            vmdynamic.setId(new Guid((String) struct.get(VdsProperties.vm_guid)));
        }
        if (struct.containsKey(VdsProperties.status)) {
            vmdynamic.setStatus(convertToVmStatus((String) struct.get(VdsProperties.status)));
        }
        return vmdynamic;
    }

    public Double getVdsmCallTimestamp(Map<String, Object> struct) {
        if (struct.containsKey(VdsProperties.statusTime)) {
            return assignDoubleValue(struct, VdsProperties.statusTime);
        }
        return -1d;
    }

    public String getVmDevicesHash(Map<String, Object> struct) {
        if (struct.containsKey(VdsProperties.hash)) {
            return (String) struct.get(VdsProperties.hash);
        }
        return null;
    }

    public String getTpmDataHash(Map<String, Object> struct) {
        return (String) struct.get(VdsProperties.tpmHash);
    }

    public String getNvramDataHash(Map<String, Object> struct) {
        return (String) struct.get(VdsProperties.nvramHash);
    }

    public VmDynamic buildVMDynamicData(Map<String, Object> struct, VDS host) {
        VmDynamic vmdynamic = new VmDynamic();
        updateVMDynamicData(vmdynamic, struct, host);
        return vmdynamic;
    }

    public StoragePool buildStoragePool(Map<String, Object> struct) {
        StoragePool sPool = new StoragePool();
        if (struct.containsKey("type")) {
            sPool.setIsLocal(StorageType.valueOf(struct.get("type").toString()).isLocal());
        }
        sPool.setName(assignStringValue(struct, "name"));
        Integer masterVersion = assignIntValue(struct, "master_ver");
        if (masterVersion != null) {
            sPool.setMasterDomainVersion(masterVersion);
        }
        return sPool;
    }

    public VmStatistics buildVMStatisticsData(Map<String, Object> struct) {
        VmStatistics vmStatistics = new VmStatistics();
        updateVMStatisticsData(vmStatistics, struct);
        return vmStatistics;
    }

    public Map<String, LUNs> buildVmLunDisksData(Map<String, Object> struct) {
        Map<String, Object> disks = (Map<String, Object>) struct.get(VdsProperties.vm_disks);
        if (disks == null) {
            return Collections.emptyMap();
        }

        Map<String, LUNs> lunsMap = new HashMap<>();
        for (Object diskAsObj : disks.values()) {
            Map<String, Object> disk = (Map<String, Object>) diskAsObj;

            String lunGuidString = assignStringValue(disk, VdsProperties.lun_guid);
            if (!StringUtils.isEmpty(lunGuidString)) {
                LUNs lun = new LUNs();
                lun.setLUNId(lunGuidString);

                if (disk.containsKey(VdsProperties.disk_true_size)) {
                    long sizeInBytes = assignLongValue(disk, VdsProperties.disk_true_size);
                    int sizeInGB = SizeConverter.convert(
                            sizeInBytes, SizeConverter.SizeUnit.BYTES, SizeConverter.SizeUnit.GiB).intValue();
                    lun.setDeviceSize(sizeInGB);
                }

                lunsMap.put(lunGuidString, lun);
            }
        }
        return lunsMap;
    }

    public void updateVMDynamicData(VmDynamic vm, Map<String, Object> struct, VDS host) {
        if (struct.containsKey(VdsProperties.vm_guid)) {
            vm.setId(new Guid((String) struct.get(VdsProperties.vm_guid)));
        }
        if (struct.containsKey(VdsProperties.session)) {
            String session = (String) struct.get(VdsProperties.session);
            try {
                vm.setSession(SessionState.valueOf(session));
            } catch (Exception e) {
                log.error("Illegal vm session '{}'.", session);
            }
        }
        if (struct.containsKey(VdsProperties.acpiEnable)) {
            vm.setAcpiEnable(Boolean.parseBoolean((String) struct.get(VdsProperties.acpiEnable)));
        }
        if (struct.containsKey(VdsProperties.status)) {
            vm.setStatus(convertToVmStatus((String) struct.get(VdsProperties.status)));
        }

        boolean hasGraphicsInfo = updateGraphicsInfo(vm, struct);
        if (!hasGraphicsInfo) {
            updateGraphicsInfoFromConf(vm, struct);
        }

        adjustDisplayIp(vm.getGraphicsInfos(), host);

        if (struct.containsKey(VdsProperties.utc_diff)) {
            String utc_diff = struct.get(VdsProperties.utc_diff).toString();
            if (utc_diff.startsWith("+")) {
                utc_diff = utc_diff.substring(1);
            }
            try {
                vm.setUtcDiff(Integer.parseInt(utc_diff));
            } catch (NumberFormatException e) {
                log.error("Illegal vm offset (utc_diff) '{}'.", utc_diff);
            }
        }

        // ------------- vm internal agent data
        if (struct.containsKey(VdsProperties.vm_host)) {
            vm.setVmHost(assignStringValue(struct, VdsProperties.vm_host));
        }

        if (struct.containsKey(VdsProperties.guest_cur_user_name)) {
            vm.setGuestCurrentUserName(assignStringValue(struct, VdsProperties.guest_cur_user_name));
        }

        initAppsList(struct, vm);
        initGuestContainers(struct, vm);

        if (struct.containsKey(VdsProperties.guest_os)) {
            vm.setGuestOs(assignStringValue(struct, VdsProperties.guest_os));
        }
        if (struct.containsKey(VdsProperties.VM_FQDN)) {
            vm.setFqdn(assignStringValue(struct, VdsProperties.VM_FQDN));
            String fqdn = vm.getFqdn().trim();
            if ("localhost".equalsIgnoreCase(fqdn) || "localhost.localdomain".equalsIgnoreCase(fqdn)) {
                vm.setFqdn(null);
            } else {
                vm.setFqdn(fqdn);
            }
        }

        if (struct.containsKey(VdsProperties.exit_code)) {
            String exitCodeStr = struct.get(VdsProperties.exit_code).toString();
            vm.setExitStatus(VmExitStatus.forValue(Integer.parseInt(exitCodeStr)));
        }
        if (struct.containsKey(VdsProperties.exit_message)) {
            String exitMsg = (String) struct.get(VdsProperties.exit_message);
            vm.setExitMessage(exitMsg);
        }
        if (struct.containsKey(VdsProperties.exit_reason)) {
            String exitReasonStr = struct.get(VdsProperties.exit_reason).toString();
            VmExitReason exitReason = VmExitReason.forValue(Integer.parseInt(exitReasonStr));
            if (exitReason == null) {
                log.warn("Illegal exit reason: {}, replacing with Unknown", exitReasonStr);
                exitReason = VmExitReason.Unknown;
            }
            vm.setExitReason(exitReason);
        }

        // negative monitorResponse means it is erroneous
        if (struct.containsKey(VdsProperties.monitorResponse)) {
            int response = Integer.parseInt(struct.get(VdsProperties.monitorResponse).toString());
            if (response < 0 && vm.getStatus().isGuestCpuRunning()) {
                vm.setStatus(VMStatus.NotResponding);
            }
        }
        if (struct.containsKey(VdsProperties.clientIp)) {
            vm.setClientIp(struct.get(VdsProperties.clientIp).toString());
        }

        if (struct.containsKey(VdsProperties.pauseCode)) {
            String pauseCodeStr = (String) struct.get(VdsProperties.pauseCode);
            try {
                vm.setPauseStatus(VmPauseStatus.valueOf(pauseCodeStr));

            } catch (IllegalArgumentException ex) {
                log.error("Error in parsing vm pause status. Setting value to NONE");
            }
        }

        if (struct.containsKey(VdsProperties.watchdogEvent)) {
            Map<String, Object> watchdogStruct = (Map<String, Object>) struct.get(VdsProperties.watchdogEvent);
            double time = Double.parseDouble(watchdogStruct.get(VdsProperties.time).toString());
            // vdsm may not send the action http://gerrit.ovirt.org/14134
            String action =
                    watchdogStruct.containsKey(VdsProperties.action) ? watchdogStruct.get(VdsProperties.action)
                            .toString() : null;
            vm.setLastWatchdogEvent((long) time);
            vm.setLastWatchdogAction(action);
        }

        if (struct.containsKey(VdsProperties.CDRom)) {
            Path fileName = Paths.get((String) struct.get(VdsProperties.CDRom)).getFileName();
            if (fileName != null) {
                String isoName = fileName.toString();
                vm.setCurrentCd(isoName);
            }
        }

        if (struct.containsKey(VdsProperties.GUEST_CPU_COUNT)) {
            vm.setGuestCpuCount(assignIntValue(struct, VdsProperties.GUEST_CPU_COUNT));
        }

        // Guest OS Info
        if (struct.containsKey(VdsProperties.GUEST_OS_INFO)) {
            updateGuestOsInfo(vm, struct);
        }

        // Guest Timezone
        if (struct.containsKey(VdsProperties.GUEST_TIMEZONE)) {
            Map<String, Object> guestTimeZoneStruct =
                    (Map<String, Object>) struct.get(VdsProperties.GUEST_TIMEZONE);
            vm.setGuestOsTimezoneName(assignStringValue(guestTimeZoneStruct, VdsProperties.GUEST_TIMEZONE_ZONE));
            vm.setGuestOsTimezoneOffset(assignIntValue(guestTimeZoneStruct, VdsProperties.GUEST_TIMEZONE_OFFSET));
        }
    }

    /**
     * Adjusts displayIp for graphicsInfos:
     *  - if displayIp is overriden on cluster level then overriden address is used,
     *   or
     *  - if current displayIp starts with "0" then host's hostname is used.
     *
     * @param graphicsInfos - graphicsInfo to adjust
     */
    private static void adjustDisplayIp(Map<GraphicsType, GraphicsInfo> graphicsInfos, VDS host) {
        if (graphicsInfos == null) {
            return;
        }
        for (GraphicsInfo graphicsInfo : graphicsInfos.values()) {
            if (graphicsInfo == null) {
                continue;
            }

            if (host.getConsoleAddress() != null) {
                graphicsInfo.setIp(host.getConsoleAddress());
            } else if (graphicsInfo.getIp() != null && graphicsInfo.getIp().startsWith("0")) {
                graphicsInfo.setIp(host.getHostName());
            }
        }
    }

    private static void updateGuestOsInfo(VmDynamic vm, Map<String, Object> struct) {
        Map<String, Object> guestOsInfoStruct = (Map<String, Object>) struct.get(VdsProperties.GUEST_OS_INFO);
        if(guestOsInfoStruct.containsKey(VdsProperties.GUEST_OS_INFO_ARCH)) {
            String arch = assignStringValue(guestOsInfoStruct, VdsProperties.GUEST_OS_INFO_ARCH);
            try {
                vm.setGuestOsArch(arch);
            } catch(IllegalArgumentException e) {
                log.warn("Invalid or unknown guest architecture type '{}' received from guest agent", arch);
            }
        }

        vm.setGuestOsCodename(assignStringValue(guestOsInfoStruct, VdsProperties.GUEST_OS_INFO_CODENAME));
        vm.setGuestOsDistribution(assignStringValue(guestOsInfoStruct, VdsProperties.GUEST_OS_INFO_DISTRIBUTION));
        vm.setGuestOsKernelVersion(assignStringValue(guestOsInfoStruct, VdsProperties.GUEST_OS_INFO_KERNEL));
        if(guestOsInfoStruct.containsKey(VdsProperties.GUEST_OS_INFO_TYPE)) {
            String osType = assignStringValue(guestOsInfoStruct, VdsProperties.GUEST_OS_INFO_TYPE);
            try {
                OsType type = EnumUtils.valueOf(OsType.class, osType, true);
                vm.setGuestOsType(type);
            } catch(IllegalArgumentException e) {
                log.warn("Invalid or unknown guest os type '{}' received from guest agent", osType);
            }
        } else {
            log.warn("Guest OS type not reported by guest agent but expected.");
        }
        vm.setGuestOsVersion(assignStringValue(guestOsInfoStruct, VdsProperties.GUEST_OS_INFO_VERSION));
    }

    /**
     * Updates graphics runtime information according displayInfo VDSM structure if it exists.
     *
     * @param vm - VmDynamic to update
     * @param struct - data from VDSM
     * @return true if displayInfo exists, false otherwise
     */
    private static boolean updateGraphicsInfo(VmDynamic vm, Map<String, Object> struct) {
        Object displayInfo = struct.get(VdsProperties.displayInfo);

        if (displayInfo == null) {
            return false;
        }

        for (Object info : (Object[]) displayInfo) {
            Map<String, String> infoMap = (Map<String, String>) info;
            GraphicsType graphicsType = GraphicsType.fromString(infoMap.get(VdsProperties.type));

            GraphicsInfo graphicsInfo = new GraphicsInfo();
            graphicsInfo.setIp(infoMap.get(VdsProperties.ipAddress))
                        .setPort(parseIntegerOrNull(infoMap.get(VdsProperties.port)))
                        .setTlsPort(parseIntegerOrNull(infoMap.get(VdsProperties.tlsPort)));

            if (graphicsInfo.getPort() != null || graphicsInfo.getTlsPort() != null) {
                vm.getGraphicsInfos().put(graphicsType, graphicsInfo);
            }
        }
        return true;
    }

    /**
     * Updates graphics runtime information according to vm.conf vdsm structure. It's used with legacy VDSMs that have
     * no notion about graphics device.
     * @param vm - VmDynamic to update
     * @param struct - data from VDSM
     */
    private static void updateGraphicsInfoFromConf(VmDynamic vm, Map<String, Object> struct) {
        GraphicsType vmGraphicsType = parseGraphicsType(struct);
        if (vmGraphicsType == null) {
            log.debug("graphics data missing in XML.");
            return;
        }

        GraphicsInfo graphicsInfo = new GraphicsInfo();
        if (struct.containsKey(VdsProperties.display_port)) {
            try {
                graphicsInfo.setPort(Integer.parseInt(struct.get(VdsProperties.display_port).toString()));
            } catch (NumberFormatException e) {
                log.error("vm display_port value illegal : {0}", struct.get(VdsProperties.display_port));
            }
        } else if (struct.containsKey(VdsProperties.display)) {
            try {
                graphicsInfo
                        .setPort(VNC_START_PORT + Integer.parseInt(struct.get(VdsProperties.display).toString()));
            } catch (NumberFormatException e) {
                log.error("vm display value illegal : {0}", struct.get(VdsProperties.display));
            }
        }
        if (struct.containsKey(VdsProperties.display_secure_port)) {
            try {
                graphicsInfo
                        .setTlsPort(Integer.parseInt(struct.get(VdsProperties.display_secure_port).toString()));
            } catch (NumberFormatException e) {
                log.error("vm display_secure_port value illegal : {0}",
                        struct.get(VdsProperties.display_secure_port));
            }
        }
        if (struct.containsKey(VdsProperties.displayIp)) {
            graphicsInfo.setIp((String) struct.get(VdsProperties.displayIp));
        }

        vm.getGraphicsInfos().put(vmGraphicsType, graphicsInfo);
    }

    /**
     * Retrieves graphics type from xml.
     * @return
     *  - graphics type derived from xml on success
     *  - null on error
     */
    private static GraphicsType parseGraphicsType(Map<String, Object> struct) {
        GraphicsType result = null;

        try {
            String displayTypeStr = struct.get(VdsProperties.displayType).toString();
            switch (displayTypeStr) {
                case VdsProperties.VNC:
                    result = GraphicsType.VNC;
                    break;
                case VdsProperties.QXL:
                    result = GraphicsType.SPICE;
                    break;
            }
        } catch (Exception ignore) {
        }

        return result;
    }

    private static Integer parseIntegerOrNull(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Some properties were changed recently from String to Integer
     * This method checks what type is the property, and returns int
     */
    public int parseIntVdsProperty(Object vdsProperty) {
        if (vdsProperty instanceof Integer) {
            return (Integer) vdsProperty;
        } else {
            return Integer.parseInt((String) vdsProperty);
        }
    }

    protected static ArchitectureType parseArchitecture(Map<String, Object> struct) {
        return ArchitectureType.valueOf((String) struct.get(VdsProperties.vm_arch));
    }

    public List<VmNetworkInterface> buildInterfaceStatisticsData(Map<String, Object> struct) {
        // ------------- vm network statistics -----------------------
        if (!struct.containsKey(VdsProperties.VM_NETWORK)) {
            return null;
        }

        Map networkStruct = (Map) struct.get(VdsProperties.VM_NETWORK);
        List<VmNetworkInterface> interfaceStatistics = new ArrayList<>();
        for (Object tempNic : networkStruct.values()) {
            Map nic = (Map) tempNic;
            VmNetworkInterface stats = new VmNetworkInterface();

            if (nic.containsKey(VdsProperties.VM_INTERFACE_NAME)) {
                stats.setName((String) ((nic.get(VdsProperties.VM_INTERFACE_NAME) instanceof String) ? nic
                        .get(VdsProperties.VM_INTERFACE_NAME) : null));
            }
            extractInterfaceStatistics(nic, stats);
            stats.setMacAddress((String) ((nic.get(VdsProperties.MAC_ADDR) instanceof String) ? nic
                    .get(VdsProperties.MAC_ADDR) : null));
            interfaceStatistics.add(stats);
        }
        return interfaceStatistics;
    }

    public void updateVMStatisticsData(VmStatistics vm, Map<String, Object> struct) {
        if (struct.containsKey(VdsProperties.vm_guid)) {
            vm.setId(new Guid((String) struct.get(VdsProperties.vm_guid)));
        }

        vm.setElapsedTime(assignDoubleValue(struct, VdsProperties.elapsed_time));

        if (struct.containsKey(VdsProperties.VM_DISKS_USAGE)) {
            initDisksUsage(struct, vm);
        }

        // ------------- vm cpu statistics -----------------------
        vm.setCpuSys(assignDoubleValue(struct, VdsProperties.cpu_sys));
        vm.setCpuUser(assignDoubleValue(struct, VdsProperties.cpu_user));

        // ------------- vm memory statistics -----------------------
        vm.setUsageMemPercent(assignIntValue(struct, VdsProperties.vm_usage_mem_percent));

        if (struct.containsKey(VdsProperties.vm_guest_mem_stats)) {
            Map<String, Object> sub = (Map<String, Object>)struct.get(VdsProperties.vm_guest_mem_stats);
            if (sub.containsKey(VdsProperties.vm_guest_mem_buffered)) {
                vm.setGuestMemoryBuffered(Long.parseLong(sub.get(VdsProperties.vm_guest_mem_buffered).toString()));
            }
            if (sub.containsKey(VdsProperties.vm_guest_mem_cached)) {
                vm.setGuestMemoryCached(Long.parseLong(sub.get(VdsProperties.vm_guest_mem_cached).toString()));
            }
            if (sub.containsKey(VdsProperties.vm_guest_mem_free)) {
                vm.setGuestMemoryFree(Long.parseLong(sub.get(VdsProperties.vm_guest_mem_free).toString()));
            }
            if (sub.containsKey(VdsProperties.vm_guest_mem_unused)) {
                vm.setGuestMemoryUnused(Long.parseLong(sub.get(VdsProperties.vm_guest_mem_unused).toString()));
            }
        }

        // ------------- vm migration statistics -----------------------
        Integer migrationProgress = assignIntValue(struct, VdsProperties.vm_migration_progress_percent);
        vm.setMigrationProgressPercent(migrationProgress != null ? migrationProgress : 0);
    }

    public VmBalloonInfo buildVmBalloonInfo(Map<String, Object> struct) {
        VmBalloonInfo vmBalloonInfo = new VmBalloonInfo();
        Map<String, Object> balloonInfo = (Map<String, Object>) struct.get(VdsProperties.vm_balloonInfo);
        if (balloonInfo != null && !balloonInfo.isEmpty()) {
            vmBalloonInfo.setCurrentMemory(assignLongValue(balloonInfo, VdsProperties.vm_balloon_cur));
            vmBalloonInfo.setBalloonMaxMemory(assignLongValue(balloonInfo, VdsProperties.vm_balloon_max));
            vmBalloonInfo.setBalloonTargetMemory(assignLongValue(balloonInfo, VdsProperties.vm_balloon_target));
            vmBalloonInfo.setBalloonMinMemory(assignLongValue(balloonInfo, VdsProperties.vm_balloon_min));
            // only if all 4 properties are found the balloon is considered enabled (available from 3.3)
            vmBalloonInfo.setBalloonDeviceEnabled(balloonInfo.size() >= 4);
        }
        return vmBalloonInfo;
    }

    public List<VmJob> buildVmJobsData(Map<String, Object> struct) {
        if (!struct.containsKey(VdsProperties.vmJobs)) {
            return null;
        }
        Guid vmId = new Guid((String) struct.get(VdsProperties.vm_guid));
        return ((Map<String, Object>) struct.get(VdsProperties.vmJobs)).values().stream()
                .map(jobMap -> buildVmJobData(vmId, (Map<String, Object>) jobMap))
                .collect(Collectors.toList());
    }

    private static VmJob buildVmJobData(Guid vmId, Map<String, Object> struct) {
        VmJob ret;
        VmJobType jobType = VmJobType.getByName(assignStringValue(struct, VdsProperties.vmJobType));
        if (jobType == null) {
            jobType = VmJobType.UNKNOWN;
        }

        switch (jobType) {
        case BLOCK:
            VmBlockJob blockJob = new VmBlockJob();
            blockJob.setBlockJobType(VmBlockJobType.getByName(assignStringValue(struct, VdsProperties.vmBlockJobType)));
            blockJob.setCursorCur(assignLongValue(struct, VdsProperties.vmJobCursorCur));
            blockJob.setCursorEnd(assignLongValue(struct, VdsProperties.vmJobCursorEnd));
            blockJob.setBandwidth(assignLongValue(struct, VdsProperties.vmJobBandwidth));
            blockJob.setImageGroupId(new Guid(assignStringValue(struct, VdsProperties.vmJobImageUUID)));
            ret = blockJob;
            break;
        default:
            ret = new VmJob();
            break;
        }

        ret.setVmId(vmId);
        ret.setId(new Guid(assignStringValue(struct, VdsProperties.vmJobId)));
        ret.setJobState(VmJobState.NORMAL);
        ret.setJobType(jobType);
        return ret;
    }

    public void updateVDSDynamicData(VDS vds, Map<String, String> vdsmNameMap, Map<String, Object> struct) {
        vds.setSupportedClusterLevels(assignStringValueFromArray(struct, VdsProperties.supported_cluster_levels));

        setDnsResolverConfigurationData(vds, struct);

        updateNetworkData(vds, vdsmNameMap, struct);
        updateNumaNodesData(vds, struct);

        vds.setCpuThreads(assignIntValue(struct, VdsProperties.cpuThreads));
        vds.setCpuCores(assignIntValue(struct, VdsProperties.cpu_cores));
        vds.setCpuSockets(assignIntValue(struct, VdsProperties.cpu_sockets));
        vds.setCpuModel(assignStringValue(struct, VdsProperties.cpu_model));
        vds.setOnlineCpus(assignStringValue(struct, VdsProperties.online_cpus));
        vds.setCpuSpeedMh(assignDoubleValue(struct, VdsProperties.cpu_speed_mh));
        vds.setPhysicalMemMb(assignIntValue(struct, VdsProperties.physical_mem_mb));
        vds.setKernelArgs(assignStringValue(struct, VdsProperties.kernel_args));

        vds.setKvmEnabled(assignBoolValue(struct, VdsProperties.kvm_enabled));

        vds.setReservedMem(assignIntValue(struct, VdsProperties.reservedMem));
        Integer guestOverhead = assignIntValue(struct, VdsProperties.guestOverhead);
        vds.setGuestOverhead(guestOverhead != null ? guestOverhead : 0);

        vds.setCpuFlags(assignStringValue(struct, VdsProperties.cpu_flags));

        updatePackagesVersions(vds, struct);

        vds.setSupportedEngines(assignStringValueFromArray(struct, VdsProperties.supported_engines));
        vds.setIScsiInitiatorName(assignStringValue(struct, VdsProperties.iSCSIInitiatorName));

        vds.setSupportedEmulatedMachines(assignStringValueFromArray(struct, VdsProperties.emulatedMachines));

        setRngSupportedSourcesToVds(vds, struct);

        String hooksStr = ""; // default value if hooks is not in the xml rpc struct
        if (struct.containsKey(VdsProperties.hooks)) {
            hooksStr = struct.get(VdsProperties.hooks).toString();
        }
        vds.setHooksStr(hooksStr);

        // parse out the HBAs available in this host
        Map<String, List<Map<String, String>>> hbas = new HashMap<>();
        for (Map.Entry<String, Object[]> el: ((Map<String, Object[]>)struct.get(VdsProperties.HBAInventory)).entrySet()) {
            List<Map<String, String>> devicesList = new ArrayList<>();

            for (Object device: el.getValue()) {
                devicesList.add((Map<String, String>)device);
            }

            hbas.put(el.getKey(), devicesList);
        }
        vds.setHBAs(hbas);
        vds.setBootTime(assignLongValue(struct, VdsProperties.bootTime));
        vds.setKdumpStatus(KdumpStatus.valueOfNumber(assignIntValue(struct, VdsProperties.KDUMP_STATUS)));
        vds.setHostDevicePassthroughEnabled(assignBoolValue(struct, VdsProperties.HOST_DEVICE_PASSTHROUGH));

        Map<String, Object> selinux = (Map<String, Object>) struct.get(VdsProperties.selinux);
        if (selinux != null) {
            vds.setSELinuxEnforceMode(assignIntValue(selinux, VdsProperties.selinux_mode));
        } else {
            vds.setSELinuxEnforceMode(null);
        }

        vds.setHostedEngineConfigured(assignBoolValue(struct, VdsProperties.hosted_engine_configured));

        updateAdditionalFeatures(vds, struct);
        vds.setKernelFeatures((Map<String, Object>) struct.get(VdsProperties.kernelFeatures));

        vds.setOpenstackBindingHostIds((Map<String, Object>) struct.get(VdsProperties.OPENSTACK_BINDING_HOST_IDS));
        vds.setVncEncryptionEnabled(assignBoolValue(struct, VdsProperties.vnc_encryption_enabled));
        vds.setConnectorInfo((Map<String, Object>) struct.get(VdsProperties.CONNECTOR_INFO));
        vds.setKvmEnabled(assignBoolValue(struct, VdsProperties.kvm_enabled));
        vds.setBackupEnabled(assignBoolValue(struct, VdsProperties.BACKUP_ENABLED));
        vds.setColdBackupEnabled(assignBoolValue(struct, VdsProperties.COLD_BACKUP_ENABLED));
        vds.setClearBitmapsEnabled(assignBoolValue(struct, VdsProperties.CLEAR_BITMAPS_ENABLED));
        if (struct.containsKey(VdsProperties.domain_versions)) { //Older VDSMs do not return that
            Set<StorageFormatType> domain_versions = Stream.of((Object[]) struct.get(VdsProperties.domain_versions))
                    .map(o -> (Integer) o)
                    .map(Object::toString)
                    .map(StorageFormatType::forValue)
                    .collect(Collectors.toSet());
            vds.setSupportedDomainVersions(domain_versions);
        }
        vds.setSupportedBlockSize((Map<String, Object>) struct.get(VdsProperties.supported_block_size));
        vds.setTscFrequency(assignStringValue(struct, VdsProperties.TSC_FREQUENCY));
        vds.setKernelCmdlineFips(assignBoolValue(struct, VdsProperties.FIPS_MODE));
        vds.setTscScalingEnabled(assignBoolValue(struct, VdsProperties.TSC_SCALING));
        vds.setFipsEnabled(assignBoolValue(struct, VdsProperties.FIPS_MODE));
        vds.setBootUuid(assignStringValue(struct, VdsProperties.BOOT_UUID));
        vds.setCdChangePdiv(assignBoolValue(struct, VdsProperties.CD_CHANGE_PDIV));
        vds.setOvnConfigured(assignBoolValue(struct, VdsProperties.OVN_CONFIGURED));
    }

    private void setDnsResolverConfigurationData(VDS vds, Map<String, Object> struct) {
        String[] nameServersAddresses = assignStringArrayValue(struct, VdsProperties.name_servers);
        if (nameServersAddresses != null) {
            List<NameServer> nameServers = Stream.of(nameServersAddresses)
                    .distinct()
                    .map(NameServer::new)
                    .collect(Collectors.toList());

            DnsResolverConfiguration reportedDnsResolverConfiguration = new DnsResolverConfiguration();
            reportedDnsResolverConfiguration.setNameServers(nameServers);

            DnsResolverConfiguration oldDnsResolverConfiguration = dnsResolverConfigurationDao.get(vds.getId());
            if (oldDnsResolverConfiguration != null) {
                reportedDnsResolverConfiguration.setId(oldDnsResolverConfiguration.getId());
            }

            vds.getDynamicData().setReportedDnsResolverConfiguration(reportedDnsResolverConfiguration);
        }
    }

    private static void updateAdditionalFeatures(VDS vds, Map<String, Object> struct) {
        String[] addtionalFeaturesSupportedByHost =
                        assignStringArrayValue(struct, VdsProperties.ADDITIONAL_FEATURES);
        if (addtionalFeaturesSupportedByHost != null) {
            for (String feature : addtionalFeaturesSupportedByHost) {
                vds.getAdditionalFeatures().add(feature);
            }
        }
    }

    private static void setRngSupportedSourcesToVds(VDS vds, Map<String, Object> struct) {
        vds.getSupportedRngSources().clear();
        String rngSourcesFromStruct = assignStringValueFromArray(struct, VdsProperties.rngSources);
        if (rngSourcesFromStruct != null) {
            vds.getSupportedRngSources().addAll(VmRngDevice.csvToSourcesSet(rngSourcesFromStruct.toUpperCase()));
        }
    }

    public void checkTimeDrift(VDS vds, Map<String, Object> struct) {
        Boolean isHostTimeDriftEnabled = Config.getValue(ConfigValues.EnableHostTimeDrift);
        if (isHostTimeDriftEnabled) {
            Integer maxTimeDriftAllowed = Config.getValue(ConfigValues.HostTimeDriftInSec);
            Date hostDate = assignDatetimeValue(struct, VdsProperties.hostDatetime);
            if (hostDate != null) {
                Long timeDrift =
                        TimeUnit.MILLISECONDS.toSeconds(Math.abs(hostDate.getTime() - System.currentTimeMillis()));
                if (timeDrift > maxTimeDriftAllowed) {
                    AuditLogable logable = createAuditLogableForHost(vds);
                    logable.addCustomValue("Actual", timeDrift.toString());
                    logable.addCustomValue("Max", maxTimeDriftAllowed.toString());
                    auditLogDirector.log(logable, AuditLogType.VDS_TIME_DRIFT_ALERT);
                }
            } else {
                log.error("Time Drift validation: failed to get Host or Engine time.");
            }
        }
    }

    private static void initDisksUsage(Map<String, Object> vmStruct, VmStatistics vm) {
        Object[] vmDisksUsage = (Object[]) vmStruct.get(VdsProperties.VM_DISKS_USAGE);
        if (vmDisksUsage != null) {
            ArrayList<Object> disksUsageList = new ArrayList<>(Arrays.asList(vmDisksUsage));
            vm.setDisksUsage(SerializationFactory.getSerializer().serializeUnformattedJson(disksUsageList));
        }
    }

    private static void updatePackagesVersions(VDS vds, Map<String, Object> struct) {

        vds.setVersionName(assignStringValue(struct, VdsProperties.version_name));
        vds.setSoftwareVersion(assignStringValue(struct, VdsProperties.software_version));
        vds.setBuildName(assignStringValue(struct, VdsProperties.build_name));
        if (struct.containsKey(VdsProperties.host_os)) {
            Map<String, Object> hostOsMap = (Map<String, Object>) struct.get(VdsProperties.host_os);
            vds.setHostOs(getPackageVersionFormated(hostOsMap, true));
            if (hostOsMap.containsKey(VdsProperties.pretty_name)) {
                vds.setPrettyName(assignStringValue(hostOsMap, VdsProperties.pretty_name));
            }
        }
        if (struct.containsKey(VdsProperties.packages)) {
            // packages is an array of struct (that each is a name, ver,
            // release.. of a package)
            for (Object hostPackageMap : (Object[]) struct.get(VdsProperties.packages)) {
                Map<String, Object> hostPackage = (Map<String, Object>) hostPackageMap;
                String packageName = assignStringValue(hostPackage, VdsProperties.package_name);
                if (VdsProperties.kvmPackageName.equals(packageName)) {
                    vds.setKvmVersion(getPackageVersionFormated(hostPackage, false));
                } else if (VdsProperties.spicePackageName.equals(packageName)) {
                    vds.setSpiceVersion(getPackageVersionFormated(hostPackage, false));
                } else if (VdsProperties.kernelPackageName.equals(packageName)) {
                    vds.setKernelVersion(getPackageVersionFormated(hostPackage, false));
                }
            }
        } else if (struct.containsKey(VdsProperties.packages2)) {
            Map<String, Object> packages = (Map<String, Object>) struct.get(VdsProperties.packages2);

            if (packages.containsKey(VdsProperties.vdsmPackageName)) {
                Map<String, Object> vdsm = (Map<String, Object>) packages.get(VdsProperties.vdsmPackageName);
                vds.setVersion(getPackageRpmVersion("vdsm", vdsm));
            }
            if (packages.containsKey(VdsProperties.qemuKvmPackageName)) {
                Map<String, Object> kvm = (Map<String, Object>) packages.get(VdsProperties.qemuKvmPackageName);
                vds.setKvmVersion(getPackageVersionFormated2(kvm));
            }
            if (packages.containsKey(VdsProperties.libvirtPackageName)) {
                Map<String, Object> libvirt = (Map<String, Object>) packages.get(VdsProperties.libvirtPackageName);
                vds.setLibvirtVersion(getPackageRpmVersion("libvirt", libvirt));
            }
            if (packages.containsKey(VdsProperties.librbdPackageName)) {
                Map<String, Object> librbd1 = (Map<String, Object>) packages.get(VdsProperties.librbdPackageName);
                vds.setLibrbdVersion(getPackageRpmVersion(VdsProperties.librbdPackageName, librbd1));
            }
            if (packages.containsKey(VdsProperties.glusterfsCliPackageName)) {
                Map<String, Object> glusterfsCli = (Map<String, Object>) packages.get(VdsProperties.glusterfsCliPackageName);
                vds.setGlusterfsCliVersion(getPackageRpmVersion(VdsProperties.glusterfsCliPackageName, glusterfsCli));
            }
            if (packages.containsKey(VdsProperties.spiceServerPackageName)) {
                Map<String, Object> spice = (Map<String, Object>) packages.get(VdsProperties.spiceServerPackageName);
                vds.setSpiceVersion(getPackageVersionFormated2(spice));
            }
            if (packages.containsKey(VdsProperties.kernelPackageName)) {
                Map<String, Object> kernel = (Map<String, Object>) packages.get(VdsProperties.kernelPackageName);
                vds.setKernelVersion(getPackageVersionFormated2(kernel));
            }
            if (packages.containsKey(VdsProperties.GLUSTER_PACKAGE_NAME)) {
                Map<String, Object> gluster = (Map<String, Object>) packages.get(VdsProperties.GLUSTER_PACKAGE_NAME);
                vds.setGlusterVersion(getPackageRpmVersion("glusterfs", gluster));
            }
            if (packages.containsKey(VdsProperties.OPENVSWITCH)) {
                Map<String, Object> ovs = (Map<String, Object>) packages.get(VdsProperties.OPENVSWITCH);
                vds.setOvsVersion(getPackageRpmVersion(VdsProperties.OPENVSWITCH, ovs));
            }
            if (packages.containsKey(VdsProperties.NMSTATE)) {
                Map<String, Object> nmstate = (Map<String, Object>) packages.get(VdsProperties.NMSTATE);
                vds.setNmstateVersion(getPackageRpmVersion(VdsProperties.NMSTATE, nmstate));
            }
        }
    }

    // Version 2 of GetPackageVersionFormated2:
    // from 2.3 we get dictionary and not a flat list.
    // from now the packages names (of spice, kernel, qemu and libvirt) are the same as far as VDSM and ENGINE.
    // (VDSM use to report packages name of rpm so in RHEL6 when it change it broke our interface)
    private static String getPackageVersionFormated2(Map<String, Object> hostPackage) {

        String packageVersion = (hostPackage.get(VdsProperties.package_version) != null) ? (String) hostPackage
                .get(VdsProperties.package_version) : null;
        String packageRelease = (hostPackage.get(VdsProperties.package_release) != null) ? (String) hostPackage
                .get(VdsProperties.package_release) : null;

        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(packageVersion)) {
            sb.append(packageVersion);
        }
        if (!StringUtils.isEmpty(packageRelease)) {
            if (sb.length() > 0) {
                sb.append(String.format(" - %1$s", packageRelease));
            } else {
                sb.append(packageRelease);
            }
        }
        return sb.toString();
    }

    private static RpmVersion getPackageRpmVersion(String packageName, Map<String, Object> hostPackage) {

        String packageVersion = (hostPackage.get(VdsProperties.package_version) != null) ? (String) hostPackage
                .get(VdsProperties.package_version) : null;
        String packageRelease = (hostPackage.get(VdsProperties.package_release) != null) ? (String) hostPackage
                .get(VdsProperties.package_release) : null;

        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(packageName)) {
            sb.append(packageName);
        }
        boolean hasPackageVersion = StringUtils.isEmpty(packageVersion);
        boolean hasPackageRelease = StringUtils.isEmpty(packageRelease);
        if (!hasPackageVersion || !hasPackageRelease) {
            sb.append("-");
        }
        if (!hasPackageVersion) {
            sb.append(packageVersion);
        }
        if (!hasPackageRelease) {
            if (sb.length() > 0) {
                sb.append(String.format("-%1$s", packageRelease));
            } else {
                sb.append(packageRelease);
            }
        }
        return new RpmVersion(sb.toString());
    }

    public void updateHardwareSystemInformation(Map<String, Object> hwInfo, VDS vds){
        vds.setHardwareManufacturer(assignStringValue(hwInfo, VdsProperties.hwManufacturer));
        vds.setHardwareProductName(assignStringValue(hwInfo, VdsProperties.hwProductName));
        vds.setHardwareVersion(assignStringValue(hwInfo, VdsProperties.hwVersion));
        vds.setHardwareSerialNumber(assignStringValue(hwInfo, VdsProperties.hwSerialNumber));
        vds.setHardwareUUID(assignStringValue(hwInfo, VdsProperties.hwUUID));
        vds.setHardwareFamily(assignStringValue(hwInfo, VdsProperties.hwFamily));
    }

    private static String getPackageVersionFormated(Map<String, Object> hostPackage, boolean getName) {
        String packageName = assignStringValue(hostPackage, VdsProperties.package_name);
        String packageVersion = assignStringValue(hostPackage, VdsProperties.package_version);
        String packageRelease = assignStringValue(hostPackage, VdsProperties.package_release);
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(packageName) && getName) {
            sb.append(packageName);
        }
        if (!StringUtils.isEmpty(packageVersion)) {
            if (sb.length() > 0) {
                sb.append(String.format(" - %1$s", packageVersion));
            } else {
                sb.append(packageVersion);
            }
        }
        if (!StringUtils.isEmpty(packageRelease)) {
            if (sb.length() > 0) {
                sb.append(String.format(" - %1$s", packageRelease));
            } else {
                sb.append(packageRelease);
            }
        }
        return sb.toString();
    }

    public void updateVDSStatisticsData(VDS vds, Map<String, Object> struct) {
        // ------------- vds memory usage ---------------------------
        vds.setUsageMemPercent(assignIntValue(struct, VdsProperties.mem_usage));

        // ------------- vds network statistics ---------------------
        Map<String, Object> interfaces = (Map<String, Object>) struct.get(VdsProperties.NETWORK);
        if (interfaces != null) {
            int networkUsage = 0;
            Map<String, VdsNetworkInterface> nicsByName = Entities.entitiesByName(vds.getInterfaces());
            NetworkStatisticsBuilder statsBuilder = new NetworkStatisticsBuilder();
            for (Entry<String, Object> entry : interfaces.entrySet()) {
                if (nicsByName.containsKey(entry.getKey())) {
                    VdsNetworkInterface existingIface = nicsByName.get(entry.getKey());
                    existingIface.setVdsId(vds.getId());

                    Map<String, Object> dict = (Map<String, Object>) entry.getValue();
                    VdsNetworkInterface reportedIface = new VdsNetworkInterface();
                    extractInterfaceStatistics(dict, reportedIface);

                    statsBuilder.updateExistingInterfaceStatistics(existingIface, reportedIface);
                    existingIface.getStatistics()
                            .setStatus(assignInterfaceStatusValue(dict, VdsProperties.iface_status));

                    if (!NetworkCommonUtils.isVlan(existingIface) && !existingIface.isPartOfBond()) {
                        Double ifaceUsage = computeInterfaceUsage(existingIface);
                        if (ifaceUsage != null) {
                            networkUsage = (int) Math.max(networkUsage, ifaceUsage);
                        }
                    }
                }
            }
            vds.setUsageNetworkPercent(networkUsage);
        }

        // ----------- vds cpu statistics info ---------------------
        vds.setCpuSys(assignDoubleValue(struct, VdsProperties.cpu_sys));
        vds.setCpuUser(assignDoubleValue(struct, VdsProperties.cpu_user));
        if (vds.getCpuSys() != null && vds.getCpuUser() != null) {
            vds.setUsageCpuPercent((int) (vds.getCpuSys() + vds.getCpuUser()));
        }
        // CPU load reported by VDSM is in uptime-style format, i.e. normalized
        // to unity, so that say an 8% load is reported as 0.08

        Double d = assignDoubleValue(struct, VdsProperties.cpu_load);
        d = (d != null) ? d : 0;
        vds.setCpuLoad(d * 100.0);
        vds.setCpuIdle(assignDoubleValue(struct, VdsProperties.cpu_idle));
        vds.setMemFree(assignLongValue(struct, VdsProperties.memFree));
        vds.setMemShared(assignLongValue(struct, VdsProperties.mem_shared));

        vds.setSwapFree(assignLongValue(struct, VdsProperties.swap_free));
        vds.setSwapTotal(assignLongValue(struct, VdsProperties.swap_total));
        vds.setKsmCpuPercent(assignIntValue(struct, VdsProperties.ksm_cpu_percent));
        vds.setKsmPages(assignLongValue(struct, VdsProperties.ksm_pages));
        vds.setKsmState(assignBoolValue(struct, VdsProperties.ksm_state));

        // dynamic data got from GetVdsStats
        if (struct.containsKey(VdsProperties.transparent_huge_pages_state)) {
            vds.setTransparentHugePagesState(EnumUtils.valueOf(VdsTransparentHugePagesState.class, struct
                    .get(VdsProperties.transparent_huge_pages_state).toString(), true));
        }
        if (struct.containsKey(VdsProperties.anonymous_transparent_huge_pages)) {
            vds.setAnonymousHugePages(assignIntValue(struct, VdsProperties.anonymous_transparent_huge_pages));
        }

        if (struct.containsKey(VdsProperties.hugepages)) {
            Object hugepages = struct.get(VdsProperties.hugepages);
            if (hugepages instanceof Map) {
                Map<String, Map<String, String>> hugepagesMap = (Map<String, Map<String, String>>) hugepages;

                List<HugePage> parsedHugePages = hugepagesMap.entrySet()
                        .stream()
                        .map(entry -> new HugePage(
                                Integer.parseInt(entry.getKey()),
                                assignIntValue(entry.getValue(), VdsProperties.free_hugepages),
                                assignIntValue(entry.getValue(), VdsProperties.total_hugepages)))
                        .collect(Collectors.toList());

                vds.setHugePages(parsedHugePages);
            }
        }
        vds.setNetConfigDirty(assignBoolValue(struct, VdsProperties.netConfigDirty));

        vds.setImagesLastCheck(assignDoubleValue(struct, VdsProperties.images_last_check));
        vds.setImagesLastDelay(assignDoubleValue(struct, VdsProperties.images_last_delay));

        Integer vm_count = assignIntValue(struct, VdsProperties.vm_count);
        vds.setVmCount(vm_count == null ? 0 : vm_count);
        vds.setVmActive(assignIntValue(struct, VdsProperties.vm_active));
        vds.setVmMigrating(assignIntValue(struct, VdsProperties.vm_migrating));

        Integer inOutMigrations;
        inOutMigrations = assignIntValue(struct, VdsProperties.INCOMING_VM_MIGRATIONS);
        if (inOutMigrations != null) {
            vds.setIncomingMigrations(inOutMigrations);
        } else {
            // TODO remove in 4.x when all hosts will send in/out migrations separately
            vds.setIncomingMigrations(-1);
        }
        inOutMigrations = assignIntValue(struct, VdsProperties.OUTGOING_VM_MIGRATIONS);
        if (inOutMigrations != null) {
            vds.setOutgoingMigrations(inOutMigrations);
        } else {
            // TODO remove in 4.x when all hosts will send in/out migrations separately
            vds.setOutgoingMigrations(-1);
        }

        updateVDSDomainData(vds, struct);
        updateLocalDisksUsage(vds, struct);

        // hosted engine
        Integer haScore = null;
        Boolean haIsConfigured = null;
        Boolean haIsActive = null;
        Boolean haGlobalMaint = null;
        Boolean haLocalMaint = null;
        if (struct.containsKey(VdsProperties.ha_stats)) {
            Map<String, Object> haStats = (Map<String, Object>) struct.get(VdsProperties.ha_stats);
            if (haStats != null) {
                haScore = assignIntValue(haStats, VdsProperties.ha_stats_score);
                haIsConfigured = assignBoolValue(haStats, VdsProperties.ha_stats_is_configured);
                haIsActive = assignBoolValue(haStats, VdsProperties.ha_stats_is_active);
                haGlobalMaint = assignBoolValue(haStats, VdsProperties.ha_stats_global_maintenance);
                haLocalMaint = assignBoolValue(haStats, VdsProperties.ha_stats_local_maintenance);
            }
        } else {
            haScore = assignIntValue(struct, VdsProperties.ha_score);
            // prior to 3.4, haScore was returned if ha was installed; assume active if > 0
            if (haScore != null) {
                haIsConfigured = true;
                haIsActive = haScore > 0;
            }
        }
        vds.setHighlyAvailableScore(haScore != null ? haScore : 0);
        vds.setHighlyAvailableIsConfigured(haIsConfigured != null ? haIsConfigured : false);
        vds.setHighlyAvailableIsActive(haIsActive != null ? haIsActive : false);
        vds.setHighlyAvailableGlobalMaintenance(haGlobalMaint != null ? haGlobalMaint : false);
        vds.setHighlyAvailableLocalMaintenance(haLocalMaint != null ? haLocalMaint : false);

        vds.setBootTime(assignLongValue(struct, VdsProperties.bootTime));

        updateNumaStatisticsData(vds, struct);
        updateV2VJobs(vds, struct);
    }

    private static void extractInterfaceStatistics(Map<String, Object> dict, NetworkInterface<?> iface) {
        NetworkStatistics stats = iface.getStatistics();
        stats.setReceiveDrops(assignBigIntegerValueWithNullProtection(dict, VdsProperties.rx_dropped));
        stats.setReceivedBytes(assignBigIntegerValue(dict, VdsProperties.rx_total));
        stats.setTransmitDrops(assignBigIntegerValueWithNullProtection(dict, VdsProperties.tx_dropped));
        stats.setTransmittedBytes(assignBigIntegerValue(dict, VdsProperties.tx_total));
        stats.setSampleTime(assignDoubleValue(dict, VdsProperties.sample_time));

        iface.setSpeed(assignIntValue(dict, VdsProperties.INTERFACE_SPEED));
    }

    private static Double computeInterfaceUsage(VdsNetworkInterface iface) {
        Double receiveRate = iface.getStatistics().getReceiveRate();
        Double transmitRate = iface.getStatistics().getTransmitRate();

        if (receiveRate == null) {
            return transmitRate;
        } else if (transmitRate == null) {
            return receiveRate;
        } else {
            return Math.max(receiveRate, transmitRate);
        }
    }

    public void updateNumaStatisticsData(VDS vds, Map<String, Object> struct) {
        List<VdsNumaNode> vdsNumaNodes = new ArrayList<>();
        if (vds.getNumaNodeList() != null && !vds.getNumaNodeList().isEmpty()) {
            vdsNumaNodes.addAll(vds.getNumaNodeList());
        }
        List<CpuStatistics> cpuStatsData = new ArrayList<>();
        if (struct.containsKey(VdsProperties.CPU_STATS)) {
            Map<String, Map<String, Object>> cpuStats = (Map<String, Map<String, Object>>)
                    struct.get(VdsProperties.CPU_STATS);
            Map<Integer, List<CpuStatistics>> numaNodeCpuStats = new HashMap<>();
            for (Map.Entry<String, Map<String, Object>> item : cpuStats.entrySet()) {
                CpuStatistics data = buildVdsCpuStatistics(item);
                cpuStatsData.add(data);
                int numaNodeIndex = assignIntValue(item.getValue(), VdsProperties.NUMA_NODE_INDEX);
                if (!numaNodeCpuStats.containsKey(numaNodeIndex)) {
                    numaNodeCpuStats.put(numaNodeIndex, new ArrayList<>());
                }
                numaNodeCpuStats.get(numaNodeIndex).add(data);
            }
            DecimalFormat percentageFormatter = new DecimalFormat("#.##");
            for (Map.Entry<Integer, List<CpuStatistics>> item : numaNodeCpuStats.entrySet()) {
                VdsNumaNode nodeWithStatistics = buildVdsNumaNodeStatistics(percentageFormatter, item);
                if (vdsNumaNodes.isEmpty()) {
                    vdsNumaNodes.add(nodeWithStatistics);
                } else {
                    boolean foundNumaNode = false;
                    // append the statistics to the correct numaNode (search by its Index.)
                    for (VdsNumaNode currNumaNode : vdsNumaNodes) {
                        if (currNumaNode.getIndex() == nodeWithStatistics.getIndex()) {
                            currNumaNode.setNumaNodeStatistics(nodeWithStatistics.getNumaNodeStatistics());
                            foundNumaNode = true;
                            break;
                        }
                    }
                    // append new numaNode (contains only statistics) if not found existing
                    if (!foundNumaNode) {
                        vdsNumaNodes.add(nodeWithStatistics);
                    }
                }
            }
        }
        if (struct.containsKey(VdsProperties.NUMA_NODE_FREE_MEM_STAT)) {
            Map<String, Map<String, Object>> memStats = (Map<String, Map<String, Object>>)
                    struct.get(VdsProperties.NUMA_NODE_FREE_MEM_STAT);
            for (Map.Entry<String, Map<String, Object>> item : memStats.entrySet()) {

                int nodeIndex = Integer.parseInt(item.getKey());
                VdsNumaNode node = vdsNumaNodes.stream()
                        .filter(n -> n.getIndex() == nodeIndex)
                        .findAny().orElse(null);

                if (node != null && node.getNumaNodeStatistics() != null) {
                    node.getNumaNodeStatistics().setMemFree(assignLongValue(item.getValue(),
                            VdsProperties.NUMA_NODE_FREE_MEM));
                    node.getNumaNodeStatistics().setMemUsagePercent(assignIntValue(item.getValue(),
                            VdsProperties.NUMA_NODE_MEM_PERCENT));

                    Map<String, Map<String, Object>> hugepagesMap =
                            (Map<String, Map<String, Object>>) item.getValue().get(VdsProperties.NUMA_NODE_HUGEPAGES);

                    if (hugepagesMap == null) {
                        continue;
                    }

                    List<HugePage> parsedHugePages = hugepagesMap.entrySet()
                            .stream()
                            .map(entry -> new HugePage(
                                    Integer.parseInt(entry.getKey()),
                                    assignIntValue(entry.getValue(), VdsProperties.NUMA_NODE_HUGEPAGES_FREE)))
                            .collect(Collectors.toList());

                    node.getNumaNodeStatistics().setHugePages(parsedHugePages);
                }
            }
        }
        vds.getNumaNodeList().clear();
        vds.getNumaNodeList().addAll(vdsNumaNodes);
        vds.getStatisticsData().getCpuCoreStatistics().clear();
        vds.getStatisticsData().getCpuCoreStatistics().addAll(cpuStatsData);
    }

    private static VdsNumaNode buildVdsNumaNodeStatistics(DecimalFormat percentageFormatter,
            Map.Entry<Integer, List<CpuStatistics>> item) {
        VdsNumaNode node = new VdsNumaNode();
        NumaNodeStatistics nodeStat = new NumaNodeStatistics();
        double nodeCpuUser = 0.0;
        double nodeCpuSys = 0.0;
        double nodeCpuIdle = 0.0;
        for (CpuStatistics cpuStat : item.getValue()) {
            nodeCpuUser += cpuStat.getCpuUser();
            nodeCpuSys += cpuStat.getCpuSys();
            nodeCpuIdle += cpuStat.getCpuIdle();
        }
        nodeStat.setCpuUser(Double.parseDouble(percentageFormatter.format(nodeCpuUser / item.getValue().size())));
        nodeStat.setCpuSys(Double.parseDouble(percentageFormatter.format(nodeCpuSys / item.getValue().size())));
        nodeStat.setCpuIdle(Double.parseDouble(percentageFormatter.format(nodeCpuIdle / item.getValue().size())));
        nodeStat.setCpuUsagePercent((int) (nodeStat.getCpuSys() + nodeStat.getCpuUser()));
        node.setIndex(item.getKey());
        node.setNumaNodeStatistics(nodeStat);
        return node;
    }

    private static CpuStatistics buildVdsCpuStatistics(Map.Entry<String, Map<String, Object>> item) {
        CpuStatistics data = new CpuStatistics();
        data.setCpuId(Integer.parseInt(item.getKey()));
        data.setCpuUser(assignDoubleValue(item.getValue(), VdsProperties.NUMA_CPU_USER));
        data.setCpuSys(assignDoubleValue(item.getValue(), VdsProperties.NUMA_CPU_SYS));
        data.setCpuIdle(assignDoubleValue(item.getValue(), VdsProperties.NUMA_CPU_IDLE));
        data.setCpuUsagePercent((int) (data.getCpuSys() + data.getCpuUser()));
        return data;
    }

    /**
     * Update {@link VDS#setLocalDisksUsage(Map)} with map of paths usage extracted from the returned returned value. The
     * usage is reported in MB.
     *
     * @param vds
     *            The VDS object to update.
     * @param struct
     *            The struct to extract the usage from.
     */
    protected void updateLocalDisksUsage(VDS vds, Map<String, Object> struct) {
        if (struct.containsKey(VdsProperties.DISK_STATS)) {
            Map<String, Object> diskStatsStruct = (Map<String, Object>) struct.get(VdsProperties.DISK_STATS);
            Map<String, Long> diskStats = new HashMap<>();

            // collect(Collectors.toMap(...)) will not work here as it uses merge() internally and
            // will fail on null values
            diskStatsStruct.entrySet()
                    .forEach(e -> diskStats.put(e.getKey(),
                            assignLongValue((Map<String, Object>) e.getValue(), VdsProperties.DISK_STATS_FREE)));
            vds.setLocalDisksUsage(diskStats);
        }
    }

    private static void updateVDSDomainData(VDS vds, Map<String, Object> struct) {
        if (struct.containsKey(VdsProperties.domains)) {
            Map<String, Object> domains = (Map<String, Object>)
                    struct.get(VdsProperties.domains);
            ArrayList<VDSDomainsData> domainsData = new ArrayList<>();
            for (Map.Entry<String, ?> value : domains.entrySet()) {
                try {
                    VDSDomainsData data = new VDSDomainsData();
                    data.setDomainId(new Guid(value.getKey().toString()));
                    Map<String, Object> internalValue = (Map<String, Object>) value.getValue();
                    double lastCheck = 0;
                    data.setCode((Integer) internalValue.get(VdsProperties.code));
                    if (internalValue.containsKey(VdsProperties.lastCheck)) {
                        lastCheck = Double.parseDouble((String) internalValue.get(VdsProperties.lastCheck));
                    }
                    data.setLastCheck(lastCheck);
                    double delay = 0;
                    if (internalValue.containsKey(VdsProperties.delay)) {
                        delay = Double.parseDouble((String) internalValue.get(VdsProperties.delay));
                    }
                    data.setDelay(delay);
                    Boolean actual = Boolean.TRUE;
                    if (internalValue.containsKey(VdsProperties.actual)) {
                        actual = (Boolean)internalValue.get(VdsProperties.actual);
                    }
                    data.setActual(actual);
                    Boolean acquired = Boolean.FALSE;
                    if (internalValue.containsKey(VdsProperties.acquired)) {
                        acquired = (Boolean)internalValue.get(VdsProperties.acquired);
                    }
                    data.setAcquired(acquired);
                    domainsData.add(data);
                } catch (Exception e) {
                    log.error("failed building domains: {}", e.getMessage());
                    log.debug("Exception", e);
                }
            }
            vds.setDomains(domainsData);
        }
    }

    private static InterfaceStatus assignInterfaceStatusValue(Map<String, Object> input, String name) {
        InterfaceStatus ifaceStatus = InterfaceStatus.NONE;
        if (input.containsKey(name)) {
            String stringValue = (String) ((input.get(name) instanceof String) ? input.get(name) : null);
            if (!StringUtils.isEmpty(stringValue)) {
                if (stringValue.toLowerCase().trim().equals("up")) {
                    ifaceStatus = InterfaceStatus.UP;
                } else {
                    ifaceStatus = InterfaceStatus.DOWN;
                }
            }
        }
        return ifaceStatus;
    }

    private static Double assignDoubleValue(Map<String, Object> input, String name) {
        Object value = input.get(name);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof String) {
            return Double.parseDouble((String) value);
        }
        return null;
    }

    /**
     * Do the same logic as assignDoubleValue does, but instead, in case of null we return 0.
     * @param input - the Input xml
     * @param name - The name of the field we want to cast it to double.
     * @return - the double value.
     */
    private static Double assignDoubleValueWithNullProtection(Map<String, Object> input, String name) {
        Double doubleValue = assignDoubleValue(input, name);
        return doubleValue == null ? Double.valueOf(0.0) : doubleValue;
    }

    private static Integer assignIntValue(Map input, String name) {
        if (input.containsKey(name)) {
            if (input.get(name) instanceof Integer) {
                return (Integer) input.get(name);
            }
            String stringValue = (String) input.get(name);
            if (StringUtils.isNotEmpty(stringValue)) { // in case the input
                                                       // is decimal and we
                                                       // need int.
                stringValue = stringValue.split("[.]", -1)[0];

                try {
                    return Integer.parseInt(stringValue);
                } catch (NumberFormatException nfe) {
                    log.error("Failed to parse '{}' value '{}' to integer: {}", name, stringValue, nfe.getMessage());
                }
            }
        }
        return null;
    }

    private static Long assignLongValue(Map<String, Object> input, String name) {
        if (input.containsKey(name)) {
            if (input.get(name) instanceof Long || input.get(name) instanceof Integer) {
                return Long.parseLong(input.get(name).toString());
            }
            String stringValue = (String) ((input.get(name) instanceof String) ? input.get(name) : null);
            if (!StringUtils.isEmpty(stringValue)) { // in case the input
                                                     // is decimal and we
                                                     // need int.
                stringValue = stringValue.split("[.]", -1)[0];

                try {
                    return Long.parseLong(stringValue);
                } catch (NumberFormatException e) {
                    log.error("Failed to parse '{}' value '{}' to long: {}", name, stringValue, e.getMessage());
                }
            }
        }
        return null;
    }

    private static BigInteger assignBigIntegerValueWithNullProtection(Map<String, Object> input, String name) {
        var bigIntValue = assignBigIntegerValue(input, name);
        return bigIntValue == null ? BigInteger.ZERO : bigIntValue;
    }

    private static BigInteger assignBigIntegerValue(Map<String, Object> input, String name) {
        if (input.containsKey(name)) {
            Object inputName = input.get(name);
            if (inputName instanceof Long || inputName instanceof Integer) {
                return new BigInteger(inputName.toString());
            }
            String stringValue = (String) ((inputName instanceof String) ? inputName : null);
            if (!StringUtils.isEmpty(stringValue)) { // in case the input
                                                     // is decimal and we
                                                     // need int.
                stringValue = stringValue.split("[.]", -1)[0];

                try {
                    return new BigInteger(stringValue);
                } catch (NumberFormatException e) {
                    log.error("Failed to parse '{}' value '{}' to BigInteger: {}", name, stringValue, e.getMessage());
                }
            }
        }
        return null;
    }

    private static String assignStringValue(Map<String, Object> input, String name) {
        if (input.containsKey(name)) {
            return (String) ((input.get(name) instanceof String) ? input.get(name) : null);
        }
        return null;
    }

    private static String[] assignStringArrayValue(Map<String, Object> input, String name) {
        String[] array = null;
        if (input.containsKey(name)) {
            array = (String[]) ((input.get(name) instanceof String[]) ? input.get(name) : null);
            if (array == null) {
                Object[] arr2 = (Object[]) ((input.get(name) instanceof Object[]) ? input.get(name) : null);
                if (arr2 != null) {
                    array = new String[arr2.length];
                    for (int i = 0; i < arr2.length; i++) {
                        array[i] = arr2[i].toString();
                    }
                }
            }
        }
        return array;
    }

    private static String assignStringValueFromArray(Map<String, Object> input, String name) {
        String[] arr = assignStringArrayValue(input, name);
        if (arr != null) {
            return StringUtils.join(arr, ',');
        }
        return null;
    }

    private static Date assignDatetimeValue(Map<String, Object> input, String name) {
        if (input.containsKey(name)) {
            if (input.get(name) instanceof Date) {
                return (Date) input.get(name);
            }
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
            String dateStr = input.get(name).toString().replaceFirst("T", " ").trim();
            try {
                return formatter.parse(dateStr);
            } catch (ParseException e) {
                log.error("Failed parsing {}", dateStr, e);
            }
        }
        return null;
    }

    private static Boolean assignBoolValue(Map<String, Object> input, String name) {
        if (input.containsKey(name)) {
            if (input.get(name) instanceof Boolean) {
                return (Boolean) input.get(name);
            }
            return Boolean.parseBoolean(input.get(name).toString());
        }
        return Boolean.FALSE;
    }

    public List<DiskImageDynamic> buildVmDiskStatistics(Map<String, Object> vmStruct) {
        Map<String, Object> disks = (Map<String, Object>) vmStruct.get(VdsProperties.vm_disks);
        if (disks == null) {
            return Collections.emptyList();
        }

        List<DiskImageDynamic> disksData = new ArrayList<>();
        for (Object diskAsObj : disks.values()) {
            Map<String, Object> disk = (Map<String, Object>) diskAsObj;
            DiskImageDynamic diskData = new DiskImageDynamic();
            String imageGroupIdString = assignStringValue(disk, VdsProperties.image_group_id);
            if (!StringUtils.isEmpty(imageGroupIdString)) {
                Guid imageGroupIdGuid = new Guid(imageGroupIdString);
                diskData.setId(imageGroupIdGuid);
                diskData.setReadRate(assignIntValue(disk, VdsProperties.vm_disk_read_rate));
                diskData.setReadOps(assignLongValue(disk, VdsProperties.vm_disk_read_ops));
                diskData.setWriteRate(assignIntValue(disk, VdsProperties.vm_disk_write_rate));
                diskData.setWriteOps(assignLongValue(disk, VdsProperties.vm_disk_write_ops));

                if (disk.containsKey(VdsProperties.disk_true_size)) {
                    Long size = assignLongValue(disk, VdsProperties.disk_true_size);
                    diskData.setActualSize(size != null ? size : 0);
                }
                if (disk.containsKey(VdsProperties.vm_disk_read_latency)) {
                    diskData.setReadLatency(assignDoubleValueWithNullProtection(disk,
                            VdsProperties.vm_disk_read_latency) / NANO_SECONDS);
                }
                if (disk.containsKey(VdsProperties.vm_disk_write_latency)) {
                    diskData.setWriteLatency(assignDoubleValueWithNullProtection(disk,
                            VdsProperties.vm_disk_write_latency) / NANO_SECONDS);
                }
                if (disk.containsKey(VdsProperties.vm_disk_flush_latency)) {
                    diskData.setFlushLatency(assignDoubleValueWithNullProtection(disk,
                            VdsProperties.vm_disk_flush_latency) / NANO_SECONDS);
                }
                disksData.add(diskData);
            }
        }
        return disksData;
    }

    private static void initAppsList(Map<String, Object> vmStruct, VmDynamic vm) {
        if (vmStruct.containsKey(VdsProperties.app_list)) {
            Object tempAppsList = vmStruct.get(VdsProperties.app_list);
            if (tempAppsList instanceof Object[]) {
                Object[] apps = (Object[]) tempAppsList;
                StringBuilder builder = new StringBuilder();
                boolean firstTime = true;
                for (Object app : apps) {
                    String appString = (String) ((app instanceof String) ? app : null);
                    if (app == null) {
                        log.warn("Failed to convert app: [null] to string");
                        continue; // Don't process this
                    }
                    if(appString == null) {
                        // Note: app cannot be null here anymore
                        log.warn("Failed to convert app: [" + app.getClass().getName() + "] is not a string");
                        continue; // Don't process this
                    }
                    if (!firstTime) {
                        builder.append(",");
                    } else {
                        firstTime = false;
                    }
                    builder.append(appString);
                }
                vm.setAppList(builder.toString());
            } else {
                vm.setAppList("");
            }
        }
    }

    private static void initGuestContainers(Map<String, Object> vmStruct, VmDynamic vm) {
        if (vmStruct.containsKey(VdsProperties.guest_containers)) {
            vm.setGuestContainers(new ArrayList<>());
            Object obj = vmStruct.get(VdsProperties.guest_containers);
            if (obj instanceof Object[]) {
                Object[] containers = (Object[])obj;
                for (Object containerObj : containers) {
                    Map<String, Object> container = (Map<String, Object>) containerObj;
                    ArrayList<String> names = new ArrayList<>();
                    for(Object o : (Object[]) container.get(VdsProperties.guest_container_names)) {
                        names.add((String)o);
                    }
                    vm.getGuestContainers().add(new GuestContainer(
                            (String)container.get(VdsProperties.guest_container_id),
                            names,
                            (String)container.get(VdsProperties.guest_container_image),
                            (String)container.get(VdsProperties.guest_container_command),
                            (String)container.get(VdsProperties.guest_container_status)
                    ));
                }
            }
        } else {
            vm.setGuestContainers(Collections.emptyList());
        }
    }

    public VMStatus convertToVmStatus(String status) {
        switch(status) {
        case VdsProperties.MIGRATION_SOURCE:
            return VMStatus.MigratingFrom;

        case VdsProperties.MIGRATION_DESTINATION:
            return VMStatus.MigratingTo;

        default:
            status = status.replace(" ", "");
            try {
                return EnumUtils.valueOf(VMStatus.class, status, true);
            } catch (Exception e) {
                log.error("Illegal VM status: '{}'.", status);
                return VMStatus.Unassigned;
            }
        }
    }

    /**
     * Updates the host network data with the network data reported by the host
     *
     * @param vds
     *            The host to update
     * @param struct
     *            A nested map contains network interfaces data
     */
    public void updateNetworkData(VDS vds, Map<String, String> vdsmNameMap, Map<String, Object> struct) {
        List<VdsNetworkInterface> oldInterfaces = interfaceDao.getAllInterfacesForVds(vds.getId());
        vds.getInterfaces().clear();

        addHostNetworkInterfaces(vds, struct);

        addHostVlanDevices(vds, struct);

        addHostBondDevices(vds, struct);

        addHostNetworksAndUpdateInterfaces(vds, vdsmNameMap, struct);

        // set bonding options
        setBondingOptions(vds, oldInterfaces);

        // This information was added in 3.1, so don't use it if it's not there.
        if (struct.containsKey(VdsProperties.netConfigDirty)) {
            vds.setNetConfigDirty(assignBoolValue(struct, VdsProperties.netConfigDirty));
        }

        setVlanSpeeds(vds);
    }

    private static void setVlanSpeeds(VDS vds) {
        List<VdsNetworkInterface> interfaces = vds.getInterfaces();
        List<VdsNetworkInterface> vlans = interfaces
                .stream()
                .filter(iface -> NetworkCommonUtils.isVlan(iface))
                .collect(Collectors.toList());

        for (VdsNetworkInterface vlanIface : vlans) {
            VdsNetworkInterface baseInterface = interfaces
                    .stream()
                    .filter(iface -> iface.getName().equals(vlanIface.getBaseInterface()))
                    .findFirst()
                    .get();
            vlanIface.setSpeed(baseInterface.getSpeed());
        }
    }

    /***
     * resolve the host's interface that is being used to communicate with engine.
     *
     * @return host's interface that being used to communicate with engine, null otherwise
     */
    private static VdsNetworkInterface resolveActiveNic(VDS host, String hostIp) {
        if (hostIp == null) {
            return null;
        }
        VdsNetworkInterface activeIface = host.getInterfaces().stream()
                .filter(new InterfaceByAddressPredicate(hostIp)).findFirst().orElse(null);
        return activeIface;
    }

    private void addHostNetworksAndUpdateInterfaces(VDS host, Map<String, String> vdsmNameMap, Map<String, Object> struct) {

        Map<String, Map<String, Object>> bridges =
                (Map<String, Map<String, Object>>) struct.get(VdsProperties.NETWORK_BRIDGES);

        final String hostActiveNicName = findActiveNicName(host, bridges);
        host.setActiveNic(hostActiveNicName);

        // Networks collection (name point to list of nics or bonds)
        Map<String, Map<String, Object>> networks =
                (Map<String, Map<String, Object>>) struct.get(VdsProperties.NETWORKS);
        Map<String, VdsNetworkInterface> vdsInterfaces = Entities.entitiesByName(host.getInterfaces());
        if (networks != null) {
            host.getNetworkNames().clear();
            for (Entry<String, Map<String, Object>> entry : networks.entrySet()) {
                Map<String, Object> networkProperties = entry.getValue();

                String vdsmName = entry.getKey();
                String networkName = vdsmNameMap.containsKey(vdsmName) ? vdsmNameMap.get(vdsmName) : vdsmName;

                if (networkProperties != null) {
                    String interfaceName = (String) networkProperties.get(VdsProperties.INTERFACE);
                    Map<String, Object> bridgeProperties = (bridges == null) ? null : bridges.get(interfaceName);

                    boolean bridgedNetwork = isBridgedNetwork(networkProperties);
                    SwitchType switchType = getSwitchType(networkProperties);
                    HostNetworkQos qos = new HostNetworkQosMapper(networkProperties).deserialize();

                    /**
                     * TODO: remove overly-defensive code in 4.0 - IP address, subnet, gateway and boot protocol should
                     * only be extracted for bridged networks and from bridge entries (not network entries)
                     **/
                    Map<String, Object> effectiveProperties =
                            (bridgedNetwork && bridgeProperties != null) ?
                                    bridgeProperties : networkProperties;
                    String v4addr = extractAddress(effectiveProperties);
                    String v4Subnet = extractSubnet(effectiveProperties);
                    String v4gateway = extractGateway(effectiveProperties);
                    boolean v4DefaultRoute = assignBoolValue(effectiveProperties, VdsProperties.IPV4_DEFAULT_ROUTE);

                    final String rawIpv6Address = getIpv6Address(effectiveProperties);
                    String v6Addr = extractIpv6Address(rawIpv6Address);
                    Integer v6Prefix = extractIpv6Prefix(rawIpv6Address);
                    String v6gateway = extractIpv6Gateway(effectiveProperties);

                    List<VdsNetworkInterface> interfaces = findNetworkInterfaces(vdsInterfaces, interfaceName, bridgeProperties);
                    for (VdsNetworkInterface iface : interfaces) {
                        iface.setNetworkName(networkName);
                        iface.setIpv4Address(v4addr);
                        iface.setIpv4Subnet(v4Subnet);
                        iface.setIpv4Gateway(v4gateway);
                        iface.setIpv4DefaultRoute(v4DefaultRoute);
                        iface.setIpv6Address(v6Addr);
                        iface.setIpv6Gateway(v6gateway);
                        iface.setIpv6Prefix(v6Prefix);
                        iface.setBridged(bridgedNetwork);
                        iface.setReportedSwitchType(switchType);
                        iface.setQos(qos);

                        // set the management ip
                        if (getManagementNetworkUtil().isManagementNetwork(iface.getNetworkName(), host.getClusterId())) {
                            iface.setType(iface.getType() | VdsInterfaceType.MANAGEMENT.getValue());
                        }

                        if (bridgedNetwork) {
                            addBootProtocol(effectiveProperties, iface);
                        }
                    }

                    host.getNetworkNames().add(networkName);
                    reportInvalidInterfacesForNetwork(interfaces, networkName, host);
                }
            }
        }
    }

    private String findActiveNicName(VDS vds, Map<String, Map<String, Object>> bridges) {
        final String hostIp = NetworkUtils.getHostIp(vds);
        final String activeBridge = findActiveBridge(hostIp, bridges);
        if (activeBridge != null) {
            return activeBridge;
        }
        // by now, if the host is communicating with engine over a valid interface,
        // the interface will have the host's engine IP
        final VdsNetworkInterface activeIface = resolveActiveNic(vds, hostIp);
        String hostActiveNic = (activeIface == null) ? null : activeIface.getName();
        return hostActiveNic;
    }

    /***
     * @return the name of the bridge obtaining ipAddress, null in case no such exist
     */
    private String findActiveBridge(String ipAddress, Map<String, Map<String, Object>> bridges) {
        if (bridges != null) {
            final Predicate<String> ipAddressPredicate = new IpAddressPredicate(ipAddress);
            for (Entry<String, Map<String, Object>> entry : bridges.entrySet()) {
                Map<String, Object> bridgeProperties = entry.getValue();
                String bridgeName = entry.getKey();
                if (bridgeProperties != null) {
                    String bridgeIpv4Address = (String) bridgeProperties.get("addr");
                    String bridgeIpv6Address = extractIpv6Address(getIpv6Address(bridgeProperties));
                    // in case host is communicating with engine over a bridge
                    if (ipAddressPredicate.test(bridgeIpv4Address) || ipAddressPredicate.test(bridgeIpv6Address)) {
                        return bridgeName;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Reports a warning to the audit log if a bridge is connected to more than one interface which is considered bad
     * configuration.
     *
     * @param interfaces
     *            The network's interfaces
     * @param networkName
     *            The network to report for
     * @param vds
     *            The host in which the network is defined
     */
    private void reportInvalidInterfacesForNetwork(List<VdsNetworkInterface> interfaces, String networkName, VDS vds) {
        if (interfaces.isEmpty()) {
            auditLogDirector.log(createHostNetworkAuditLog(networkName, vds), AuditLogType.NETWORK_WITHOUT_INTERFACES);
        } else if (interfaces.size() > 1) {
            AuditLogable logable = createHostNetworkAuditLog(networkName, vds);
            logable.addCustomValue("Interfaces",
                    interfaces.stream().map(VdsNetworkInterface::getName).collect(Collectors.joining(",")));
            auditLogDirector.log(logable, AuditLogType.BRIDGED_NETWORK_OVER_MULTIPLE_INTERFACES);
        }
    }

    protected static AuditLogable createHostNetworkAuditLog(String networkName, VDS vds) {
        AuditLogable logable = createAuditLogableForHost(vds);
        logable.addCustomValue("NetworkName", networkName);
        return logable;
    }

    private static AuditLogable createAuditLogableForHost(VDS vds) {
        AuditLogable logable = new AuditLogableImpl();
        logable.setVdsId(vds.getId());
        logable.setVdsName(vds.getName());
        return logable;
    }

    private static List<VdsNetworkInterface> findNetworkInterfaces(Map<String, VdsNetworkInterface> vdsInterfaces,
            String interfaceName,
            Map<String, Object> bridgeProperties) {

        List<VdsNetworkInterface> interfaces = new ArrayList<>();
        VdsNetworkInterface iface = vdsInterfaces.get(interfaceName);
        if (iface == null) {
            if (bridgeProperties != null) {
                interfaces.addAll(findBridgedNetworkInterfaces(bridgeProperties, vdsInterfaces));
            }
        } else {
            interfaces.add(iface);
        }

        return interfaces;
    }

    private static List<VdsNetworkInterface> findBridgedNetworkInterfaces(Map<String, Object> bridge,
            Map<String, VdsNetworkInterface> vdsInterfaces) {
        Object[] ports = (Object[]) bridge.get("ports");
        if (ports != null) {
            return Arrays.stream(ports).filter(port -> vdsInterfaces.containsKey(port.toString()))
                    .map(port -> vdsInterfaces.get(port.toString())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private void addHostBondDevices(VDS vds, Map<String, Object> struct) {
        Map<String, Map<String, Object>> bonds =
                (Map<String, Map<String, Object>>) struct.get(VdsProperties.NETWORK_BONDINGS);
        if (bonds != null) {
            for (Entry<String, Map<String, Object>> entry : bonds.entrySet()) {
                Bond bond = new Bond();
                updateCommonInterfaceData(bond, vds, entry);
                bond.setBonded(true);

                Map<String, Object> bondProperties = entry.getValue();
                if (bondProperties != null) {
                    bond.setMacAddress((String) bondProperties.get("hwaddr"));
                    if (bondProperties.get("slaves") != null) {
                        addBondDeviceToHost(vds, bond, (Object[]) bondProperties.get("slaves"));
                    }

                    bond.setBondOptions(parseBondOptions((Map<String, Object>) bondProperties.get("opts")));

                    bond.setAdPartnerMac((String) bondProperties.get("ad_partner_mac"));
                    bond.setActiveSlave((String) bondProperties.get("active_slave"));
                }
            }
        }
    }

    /**
     * Retrieves bonding options string from the structure that describes the bond options as reported by VDSM
     * @param struct
     *   a map contains pairs of option name and value
     * @return
     *  - null is returned for null valued `struct`
     *  - else bonding options string
     */
    private static String parseBondOptions(Map<String, Object> struct) {

        if (struct == null) {
            return null;
        }

        Set<String> reservedPositionKeys = new LinkedHashSet<>();
        reservedPositionKeys.add("mode");
        reservedPositionKeys.add("miimon");
        reservedPositionKeys.add(VdsProperties.BOND_XMIT_POLICY);

        List<String> bondOptions = new ArrayList<>();

        for(String key : reservedPositionKeys) {
            String value = (String) struct.get(key);
            if (value != null) {
                bondOptions.add(
                        String.format("%s=%s", key, value));
            }
        }

        for (Entry<String, Object> entry : struct.entrySet()) {
            if (!reservedPositionKeys.contains(entry.getKey())) {
                bondOptions.add(
                        String.format("%s=%s", entry.getKey(), entry.getValue()));
            }
        }

        return normalizeBondOptions(StringUtils.join(bondOptions, ' '));
    }

    private static String normalizeBondOptions(String bondOptions){
        Matcher matcher = Pattern.compile("mode=([\\w-\\.]+)").matcher(bondOptions);
        if (!matcher.find()) {
            return bondOptions;
        }

        BondMode bondMode = BondMode.getBondMode(matcher.group(1));
        if (bondMode != null) {
            return matcher.replaceAll("mode=" + bondMode.getValue());
        }
        return bondOptions;
    }

    /**
     * Updates the host interfaces list with vlan devices
     *
     * @param vds
     *            The host to update
     * @param struct
     *            a map contains pairs of vlan device name and vlan data
     */
    private void addHostVlanDevices(VDS vds, Map<String, Object> struct) {
        // vlans
        Map<String, Map<String, Object>> vlans = (Map<String, Map<String, Object>>) struct.get(VdsProperties.NETWORK_VLANS);
        if (vlans != null) {
            for (Entry<String, Map<String, Object>> entry : vlans.entrySet()) {
                VdsNetworkInterface vlan = new Vlan();
                updateCommonInterfaceData(vlan, vds, entry);

                String vlanDeviceName = entry.getKey();
                Map<String, Object> vlanProperties = entry.getValue();
                if (vlanProperties.get(VdsProperties.VLAN_ID) != null && vlanProperties.get(VdsProperties.BASE_INTERFACE) != null) {
                    vlan.setVlanId((Integer) vlanProperties.get(VdsProperties.VLAN_ID));
                    vlan.setBaseInterface((String) vlanProperties.get(VdsProperties.BASE_INTERFACE));
                } else if (vlanDeviceName.contains(".")) {
                    String[] names = vlanDeviceName.split("[.]", -1);
                    String vlanId = names[1];
                    vlan.setVlanId(Integer.parseInt(vlanId));
                    vlan.setBaseInterface(names[0]);
                }

                vds.getInterfaces().add(vlan);
            }
        }
    }

    /**
     * Updates the host network interfaces with the collected data from the host
     *
     * @param vds
     *            The host to update its interfaces
     * @param struct
     *            A nested map contains network interfaces data
     */
    private void addHostNetworkInterfaces(VDS vds, Map<String, Object> struct) {
        Map<String, Map<String, Object>> nics =
                (Map<String, Map<String, Object>>) struct.get(VdsProperties.NETWORK_NICS);
        if (nics != null) {
            for (Entry<String, Map<String, Object>> entry : nics.entrySet()) {
                VdsNetworkInterface nic = new Nic();
                updateCommonInterfaceData(nic, vds, entry);

                Map<String, Object> nicProperties = entry.getValue();
                if (nicProperties != null) {
                    nic.setMacAddress((String) nicProperties.get("hwaddr"));
                    // if we get "permhwaddr", we are a part of a bond and we use that as the mac address
                    String mac = (String) nicProperties.get("permhwaddr");
                    if (mac != null) {
                        //TODO remove when the minimal supported vdsm version is >=3.6
                        // in older VDSM version, slave's Mac is in upper case
                        nic.setMacAddress(mac.toLowerCase());
                    }
                }

                vds.getInterfaces().add(nic);
            }
        }
    }

    /**
     * Updates a given interface (be it physical, bond or VLAN) by data as collected from the host.
     *
     * @param iface
     *            The interface to update
     * @param host
     *            The host to which the interface belongs.
     * @param ifaceEntry
     *            A pair whose key is the interface's name, and whose value it a map of the interface properties.
     */
    private void updateCommonInterfaceData(VdsNetworkInterface iface,
            VDS host,
            Entry<String, Map<String, Object>> ifaceEntry) {

        iface.setName(ifaceEntry.getKey());
        iface.setId(Guid.newGuid());
        iface.setVdsId(host.getId());

        VdsNetworkStatistics iStats = new VdsNetworkStatistics();
        iStats.setId(iface.getId());
        iStats.setVdsId(host.getId());
        iface.setStatistics(iStats);

        Map<String, Object> nicProperties = ifaceEntry.getValue();
        if (nicProperties != null) {
            Object speed = nicProperties.get("speed");
            if (speed != null) {
                iface.setSpeed((Integer) speed);
            }

            iface.setIpv4Address(extractAddress(nicProperties));
            iface.setIpv4Subnet(extractSubnet(nicProperties));

            final String ipv6Address = getIpv6Address(nicProperties);
            iface.setIpv6Address(extractIpv6Address(ipv6Address));
            iface.setIpv6Prefix(extractIpv6Prefix(ipv6Address));

            final Integer mtu = assignIntValue(nicProperties, VdsProperties.MTU);
            if (mtu != null) {
                iface.setMtu(mtu);
            }
            addBootProtocol(nicProperties, iface);
            addAdAggregatorId(nicProperties, iface);
        }
    }

    private static void addAdAggregatorId(Map<String, Object> nicProperties,
            VdsNetworkInterface iface) {
        Object adAggregatorId = nicProperties.get("ad_aggregator_id");
        if (adAggregatorId != null) {
            iface.setAdAggregatorId(Integer.parseInt((String) adAggregatorId));
        }
    }

    Integer extractIpv6Prefix(String ipv6Address) {
        if (ipv6Address == null) {
            return null;
        }

        final Matcher matcher = IPV6_ADDRESS_CAPTURE_PREFIX_PATTERN.matcher(ipv6Address);
        if (matcher.matches()) {
            final String prefixString = matcher.group(1);
            return Integer.valueOf(prefixString);
        }
        return null;
    }

    String extractAddress(Map<String, Object> properties) {
        return extractNonEmptyProperty(properties, VdsProperties.ADDR);
    }

    String extractSubnet(Map<String, Object> properties) {
        return extractNonEmptyProperty(properties, VdsProperties.NETMASK);
    }

    String extractGateway(Map<String, Object> properties) {
        return extractNonEmptyProperty(properties, VdsProperties.GLOBAL_GATEWAY);
    }

    String extractIpv6Gateway(Map<String, Object> properties) {
        return extractNonEmptyProperty(properties, VdsProperties.IPV6_GLOBAL_GATEWAY);
    }

    private String extractNonEmptyProperty(Map<String, Object> properties, String name) {
        String value = (String) properties.get(name);
        return StringUtils.isEmpty(value) ? null : value;
    }

    private static String getIpv6Address(Map<String, Object> properties) {
        final Object[] ipv6Addresses = (Object[]) properties.get("ipv6addrs");
        if (ipv6Addresses == null || ipv6Addresses.length == 0) {
            return null;
        }
        return (String) ipv6Addresses[0];
    }

    String extractIpv6Address(String address) {
        if (StringUtils.isEmpty(address)) {
            return null;
        }

        final Matcher matcher = IPV6_ADDRESS_CAPTURE_PATTERN.matcher(address);

        return matcher.matches() ? matcher.group(1) : address;
    }

    /**
     * Returns true if vdsm doesn't report the 'bridged' attribute or if reported - its actual value.<br>
     * The assumption is bridge-less network isn't supported if the 'bridged' attribute wasn't reported.<br>
     * Bridge-less networks must report 'false' for this property.
     *
     * @param network
     *            The network to evaluate its bridge attribute
     * @return true is no attribute is reported or its actual value
     */
    private static boolean isBridgedNetwork(Map<String, Object> network) {
        return network.get("bridged") == null || Boolean.parseBoolean(network.get("bridged").toString());
    }

    /**
     * @return {@link SwitchType} obtained from reported network properties.
     * @throws IllegalStateException when switch type is not reported.
     */
    private static SwitchType getSwitchType(Map<String, Object> networkProperties) {
        Object switchType = networkProperties.get(VdsProperties.SWITCH_KEY);

        if (switchType == null) {
            throw new IllegalStateException("Required SwitchType is not reported.");
        }

        return SwitchType.parse(Objects.toString(switchType, SwitchType.LEGACY.getOptionValue()));
    }

    // we check for old bonding options,
    // if we had value for the bonding options, i.e. the user set it by the UI
    // and we have host that is not returning it's bonding options(host below 2.2.4) we override
    // the "new" bonding options with the old one only if we have the new one as null and the old one is not
    private static void setBondingOptions(VDS vds, List<VdsNetworkInterface> oldInterfaces) {
        oldInterfaces.stream().
                filter(iface -> iface.getBondOptions() != null).
                forEach(iface -> vds.getInterfaces()
                        .stream()
                        .filter(newIface -> iface.getName().equals(newIface.getName()))
                        .filter(newIface -> newIface.getBondOptions() == null)
                        .findFirst()
                        .ifPresent(newIface -> newIface.setBondOptions(iface.getBondOptions())));
    }

    private static Ipv4BootProtocolResolver getIpv4BootProtocolResolver() {
        return Injector.get(Ipv4BootProtocolResolver.class);
    }

    private static Ipv6BootProtocolResolver getIpv6BootProtocolResolver() {
        return Injector.get(Ipv6BootProtocolResolver.class);
    }

    private static void addBootProtocol(Map<String, Object> nicProperties, VdsNetworkInterface iface) {
        if (nicProperties == null) {
            return;
        }

        setBootProtocolAndGateway(
                getIpv4BootProtocolResolver(),
                new NoCfgIpv4InfoFetcher(nicProperties, iface.getIpv4Address()),
                bootProtocol -> Ipv4BootProtocol.STATIC_IP == bootProtocol,
                iface::setIpv4BootProtocol,
                iface::setIpv4Gateway);

        setBootProtocolAndGateway(
                getIpv6BootProtocolResolver(),
                new NoCfgIpv6InfoFetcher(nicProperties, iface.getIpv6Address()),
                bootProtocol -> Ipv6BootProtocol.STATIC_IP == bootProtocol,
                iface::setIpv6BootProtocol,
                iface::setIpv6Gateway);
    }

    private static <T, F extends IpInfoFetcher> void setBootProtocolAndGateway(
            BootProtocolResolver<T, F> bootProtocolResolver,
            F infoFetcher,
            Predicate<T> bootProtocolStaticIpPredicate,
            Consumer<T> bootProtocolSetter,
            Consumer<String> gatewaySetter) {

        final T bootProtocol = bootProtocolResolver.resolve(infoFetcher);
        bootProtocolSetter.accept(bootProtocol);
        if (bootProtocolStaticIpPredicate.test(bootProtocol)) {
            String gateway = infoFetcher.fetchGateway();
            if (StringUtils.isNotEmpty(gateway)) {
                gatewaySetter.accept(gateway);
            }
        }
    }

    private static void addBondDeviceToHost(VDS vds, VdsNetworkInterface iface, Object[] interfaces) {
        vds.getInterfaces().add(iface);
        if (interfaces != null) {
            Arrays.stream(interfaces).
                    forEach(name -> vds.getInterfaces()
                            .stream()
                            .filter(tempInterface -> tempInterface.getName().equals(name.toString()))
                            .findFirst()
                            .ifPresent(tempInterface -> tempInterface.setBondName(iface.getName())));
        }
    }

    private static ManagementNetworkUtil getManagementNetworkUtil() {
        final ManagementNetworkUtil managementNetworkUtil = Injector.get(ManagementNetworkUtil.class);
        return managementNetworkUtil;
    }

    /**
     * Creates a list of {@link VmGuestAgentInterface} from the {@link VdsProperties.GuestNetworkInterfaces}
     *
     * @param vmId
     *            the Vm's ID which contains the interfaces
     *
     * @param struct
     *            the structure that describes the VM as reported by VDSM
     * @return a list of {@link VmGuestAgentInterface} or null if no guest vNics were reported
     */
    public List<VmGuestAgentInterface> buildVmGuestAgentInterfacesData(Guid vmId, Map<String, Object> struct) {
        if (!struct.containsKey(VdsProperties.VM_NETWORK_INTERFACES)) {
            return null;
        }

        List<VmGuestAgentInterface> interfaces = new ArrayList<>();
        for (Object ifaceStruct : (Object[]) struct.get(VdsProperties.VM_NETWORK_INTERFACES)) {
            VmGuestAgentInterface nic = new VmGuestAgentInterface();
            Map ifaceMap = (Map) ifaceStruct;
            nic.setInterfaceName(assignStringValue(ifaceMap, VdsProperties.VM_INTERFACE_NAME));
            nic.setMacAddress(getMacAddress(ifaceMap));
            nic.setIpv4Addresses(extractList(ifaceMap, VdsProperties.VM_IPV4_ADDRESSES));
            nic.setIpv6Addresses(extractList(ifaceMap, VdsProperties.VM_IPV6_ADDRESSES));
            nic.setVmId(vmId);
            interfaces.add(nic);
        }
        return interfaces;
    }

    private static String getMacAddress(Map<String, Object> ifaceMap) {
        String macAddress = assignStringValue(ifaceMap, VdsProperties.VM_INTERFACE_MAC_ADDRESS);
        return macAddress != null ? macAddress.replace('-', ':') : null;
    }

    /**
     * Build through the received NUMA nodes information
     */
    private static void updateNumaNodesData(VDS vds, Map<String, Object> struct) {
        if (struct.containsKey(VdsProperties.AUTO_NUMA)) {
            vds.getDynamicData().setAutoNumaBalancing(AutoNumaBalanceStatus.forValue(
                    assignIntValue(struct, VdsProperties.AUTO_NUMA)));
        }
        if (struct.containsKey(VdsProperties.NUMA_NODES)) {
            Map<String, Map<String, Object>> numaNodeMap =
                    (Map<String, Map<String, Object>>) struct.get(VdsProperties.NUMA_NODES);
            Map<String, Object> numaNodeDistanceMap =
                    (Map<String, Object>) struct.get(VdsProperties.NUMA_NODE_DISTANCE);

            List<VdsNumaNode> newNumaNodeList = new ArrayList<>(numaNodeMap.size());

            for (Map.Entry<String, Map<String, Object>> item : numaNodeMap.entrySet()) {
                int index = Integer.parseInt(item.getKey());
                Map<String, Object> itemMap = item.getValue();
                List<Integer> cpuIds = extractList(itemMap, VdsProperties.NUMA_NODE_CPU_LIST);
                long memTotal =  assignLongValue(itemMap, VdsProperties.NUMA_NODE_TOTAL_MEM);
                VdsNumaNode numaNode = new VdsNumaNode();
                numaNode.setIndex(index);
                if (cpuIds != null) {
                    numaNode.setCpuIds(cpuIds);
                }
                numaNode.setMemTotal(memTotal);
                newNumaNodeList.add(numaNode);
            }

            Collections.sort(newNumaNodeList, numaNodeComparator);

            for (VdsNumaNode vdsNumaNode : newNumaNodeList) {
                int index = vdsNumaNode.getIndex();
                Map<Integer, Integer> distanceMap = new HashMap<>();
                List<Integer> distances = Collections.emptyList();

                if (numaNodeDistanceMap != null) {
                    // Save the received NUMA node distances
                    distances = extractList(numaNodeDistanceMap, String.valueOf(index));
                    for (int i = 0; i < distances.size(); i++) {
                        distanceMap.put(newNumaNodeList.get(i).getIndex(), distances.get(i));
                    }
                }

                if (distances.isEmpty()) {
                    // Save faked distances
                    for (VdsNumaNode otherNumaNode : newNumaNodeList) {
                        // There is no distance if the node is the same one
                        if (otherNumaNode.getIndex() == vdsNumaNode.getIndex()) {
                            continue;
                        }

                        distanceMap.put(otherNumaNode.getIndex(), 0);
                    }
                }

                vdsNumaNode.setNumaNodeDistances(distanceMap);
            }

            vds.getDynamicData().setNumaNodeList(newNumaNodeList);
            vds.setNumaSupport(newNumaNodeList.size() > 1);
        }

    }

    private static <T> List<T> extractList(Map<String, Object> struct, String propertyName) {
        if (struct.containsKey(propertyName)){
            Object[] items = (Object[]) struct.get(propertyName);
            if (items.length > 0) {
                return Arrays.stream(items).map(item -> (T) item).collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    /**
     * Parse Host Device Information in the form of
     *
     * {
     *   'computer': {
     *      'params': {'capability': 'system', 'product': 'ProLiant DL160 G6  '}
     *   },
     *   'pci_0000_00_1d_2': {
     *      'params': {
     *        'capability': 'pci',
     *        'iommu_group': '9',
     *        'parent': 'computer',
     *        'product': '82801JI (ICH10 Family) USB UHCI Controller #3',
     *        'product_id': '0x3a36',
     *        'vendor': 'Intel Corporation',
     *        'vendor_id': '0x8086'
     *        'mdev': {
     *          'nvidia-11': {'available_instances': '16', 'name': 'GRID M60-0B', 'description': 'mDev detail description'}
     *           ...
     *        }
     *      }
     *   },
     *   'pci_0000_00_1d_1': {
     *       ...
     *   },
     },

     * }
     */
    public List<HostDevice> buildHostDevices(Map<String, Map<String, Map<String, Object>>> deviceList) {
        List<HostDevice> devices = new ArrayList<>();

        for (Entry<String, Map<String, Map<String, Object>>> entry : deviceList.entrySet()) {

            Map<String, Object> params = entry.getValue().get(VdsProperties.PARAMS);
            String deviceName = entry.getKey();

            HostDevice device = new HostDevice();
            device.setDeviceName(entry.getKey());
            device.setCapability(params.get(VdsProperties.CAPABILITY).toString());

            // special case for root device "computer"
            device.setParentDeviceName(VdsProperties.ROOT_HOST_DEVICE.equals(deviceName) ?
                    VdsProperties.ROOT_HOST_DEVICE  // set parent to self, for DB integrity
                    : params.get(VdsProperties.PARENT_NAME).toString());

            device.setAssignable(params.containsKey(VdsProperties.IS_ASSIGNABLE) ?
                    assignBoolValue(params, VdsProperties.IS_ASSIGNABLE)
                    : true);

            if (params.containsKey(VdsProperties.MDEV)) {
                device.setMdevTypes(buildMDevTypesList((Map<String, Object>) params.get(VdsProperties.MDEV)));
            }
            if (params.containsKey(VdsProperties.IOMMU_GROUP)) {
                device.setIommuGroup(Integer.parseInt(params.get(VdsProperties.IOMMU_GROUP).toString()));
            }
            if (params.containsKey(VdsProperties.PRODUCT_ID)) {
                device.setProductId(params.get(VdsProperties.PRODUCT_ID).toString());
            }
            if (params.containsKey(VdsProperties.PRODUCT_NAME)) {
                device.setProductName(params.get(VdsProperties.PRODUCT_NAME).toString());
            }
            if (params.containsKey(VdsProperties.VENDOR_NAME)) {
                device.setVendorName(params.get(VdsProperties.VENDOR_NAME).toString());
            }
            if (params.containsKey(VdsProperties.VENDOR_ID)) {
                device.setVendorId(params.get(VdsProperties.VENDOR_ID).toString());
            }
            if (params.containsKey(VdsProperties.PHYSICAL_FUNCTION)) {
                device.setParentPhysicalFunction(params.get(VdsProperties.PHYSICAL_FUNCTION).toString());
            }
            if (params.containsKey(VdsProperties.TOTAL_VFS)) {
                device.setTotalVirtualFunctions(Integer.parseInt(params.get(VdsProperties.TOTAL_VFS).toString()));
            }
            if (params.containsKey(VdsProperties.NET_INTERFACE_NAME)) {
                device.setNetworkInterfaceName(params.get(VdsProperties.NET_INTERFACE_NAME).toString());
            }
            if (params.containsKey(VdsProperties.DRIVER)) {
                device.setDriver(params.get(VdsProperties.DRIVER).toString());
            }
            if (params.containsKey(VdsProperties.Address)) {
                device.setAddress((Map<String, String>) params.get(VdsProperties.Address));
            }
            if (params.containsKey(VdsProperties.BlockPath)) {
                device.setBlockPath(Objects.toString(params.get(VdsProperties.BlockPath), null));
            }

            Map<String, Object> specParams = new HashMap<>();
            if (params.containsKey(VdsProperties.DEVICE_PATH)) {
                specParams.put(VdsProperties.DEVICE_PATH, params.get(VdsProperties.DEVICE_PATH).toString());
            }
            if (params.containsKey(VdsProperties.NUMA_NODE)) {
                specParams.put(VdsProperties.NUMA_NODE, params.get(VdsProperties.NUMA_NODE).toString());
            }
            if (params.containsKey(VdsProperties.MODE)) {
                specParams.put(VdsProperties.MODE, params.get(VdsProperties.MODE).toString());
            }
            if (params.containsKey(VdsProperties.DEVICE_SIZE)) {
                specParams.put(VdsProperties.DEVICE_SIZE, assignLongValue(params, VdsProperties.DEVICE_SIZE));
            }
            if (params.containsKey(VdsProperties.ALIGN_SIZE)) {
                specParams.put(VdsProperties.ALIGN_SIZE, assignLongValue(params, VdsProperties.ALIGN_SIZE));
            }
            device.setSpecParams(specParams);

            devices.add(device);
        }

        return devices;
    }

    private static List<MDevType> buildMDevTypesList(Map<String, Object> mDevParams) {
        List<MDevType> mdevs = new ArrayList<>();
        for (String mDevName : mDevParams.keySet()) {
            Integer availableInstances = null;
            String description = null;
            String humanReadableName = null;
            if (mDevParams.get(mDevName) instanceof Map) {
                Map<String, Object> mDevParam = (Map<String, Object>) mDevParams.get(mDevName);
                if (mDevParam.containsKey(VdsProperties.MDEV_AVAILABLE_INSTANCES)) {
                    try {
                        availableInstances = Integer.parseInt(mDevParam.get(VdsProperties.MDEV_AVAILABLE_INSTANCES).toString());
                    } catch (NumberFormatException e) {
                        log.error("mdev_type -> available instances value was illegal : {0}", mDevParam.get(VdsProperties.MDEV_AVAILABLE_INSTANCES));
                    }
                }
                if (mDevParam.containsKey(VdsProperties.MDEV_DESCRIPTION)) {
                    description = mDevParam.get(VdsProperties.MDEV_DESCRIPTION).toString();
                }
                if (mDevParam.containsKey(VdsProperties.MDEV_NAME)) {
                    humanReadableName = mDevParam.get(VdsProperties.MDEV_NAME).toString();
                }
            }
            mdevs.add(new MDevType(mDevName, humanReadableName, availableInstances, description));
        }
        return mdevs;
    }

    private static void updateV2VJobs(VDS vds, Map<String, Object> struct) {
        if (!struct.containsKey(VdsProperties.v2vJobs)) {
            return;
        }

        List<V2VJobInfo> v2vJobs = ((Map<String, Object>) struct.get(VdsProperties.v2vJobs)).entrySet()
                .stream()
                .map(job -> buildV2VJobData(job.getKey(), (Map<String, Object>) job.getValue())).collect(Collectors.toList());

        vds.getStatisticsData().setV2VJobs(v2vJobs);
    }

    private static V2VJobInfo buildV2VJobData(String jobId, Map<String, Object> struct) {
        V2VJobInfo job = new V2VJobInfo();
        job.setId(Guid.createGuidFromString(jobId));
        job.setStatus(getV2VJobStatusValue(struct));
        job.setDescription(assignStringValue(struct, VdsProperties.v2vDescription));
        job.setProgress(assignIntValue(struct, VdsProperties.v2vProgress));
        return job;
    }

    private static V2VJobInfo.JobStatus getV2VJobStatusValue(Map<String, Object> input) {
        String status = (String) input.get(VdsProperties.v2vJobStatus);
        try {
            return V2VJobInfo.JobStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            log.warn("Got invalid status for virt-v2v job: {}", status);
            return V2VJobInfo.JobStatus.UNKNOWN;
        }
    }

    public Double removeNotifyTimeFromVmStatusEvent(Map<String, Object> struct) {
        Object notifyTime = struct.remove(VdsProperties.notify_time);
        // notifyTime may be an integer value or long value, depennding on how
        // much time the host is up, and the monotonic time source used by
        // vdsm.
        if (Number.class.isInstance(notifyTime)) {
            return ((Number) notifyTime).doubleValue();
        }
        return null;
    }

    public LeaseStatus buildLeaseStatus(Map<String, Object> struct) {
        List<Integer> owners = CollectionUtils.emptyListToNull(extractList(struct, "owners"));
        LeaseStatus leaseStatus = new LeaseStatus(owners);

        if (struct.get("metadata") != null) {
            Map<String, Object> metadata = (Map<String, Object>) struct.get("metadata");
            if (metadata != null) {
                int generation = (Integer) metadata.computeIfAbsent("generation", key -> -1);
                leaseStatus.setGeneration(generation);
            }
            if (metadata != null && metadata.get("job_status") != null) {
                LeaseJobStatus jobStatus = LeaseJobStatus.forValue((String) metadata.get("job_status"));
                leaseStatus.setJobStatus(jobStatus);
            }
        }

        return leaseStatus;
    }
}
