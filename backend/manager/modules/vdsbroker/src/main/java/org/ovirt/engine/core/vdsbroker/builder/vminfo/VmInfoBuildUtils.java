package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.CinderConnectionInfo;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeDriver;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.WindowsJavaTimezoneMapping;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;
import org.ovirt.engine.core.utils.MemoizingSupplier;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.StringMapUtils;
import org.ovirt.engine.core.utils.archstrategy.ArchStrategyFactory;
import org.ovirt.engine.core.utils.collections.ComparatorUtils;
import org.ovirt.engine.core.vdsbroker.architecture.GetControllerIndices;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IoTuneUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.NetworkQosMapper;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmInfoBuildUtils {
    private static final Logger log = LoggerFactory.getLogger(VmInfoBuildUtils.class);

    private static final String FIRST_MASTER_MODEL = "ich9-ehci1";
    private static final String CLOUD_INIT_VOL_ID = "config-2";
    private static final Base64 BASE_64 = new Base64(0, null);

    public static final String VDSM_LIBGF_CAP_NAME = "libgfapi_supported";

    private final NetworkClusterDao networkClusterDao;
    private final NetworkDao networkDao;
    private final NetworkFilterDao networkFilterDao;
    private final NetworkQoSDao networkQosDao;
    private final StorageQosDao storageQosDao;
    private final VmDeviceDao vmDeviceDao;
    private final VnicProfileDao vnicProfileDao;
    private final VmNicFilterParameterDao vmNicFilterParameterDao;
    private final AuditLogDirector auditLogDirector;
    private final ClusterFeatureDao clusterFeatureDao;
    private final VmNumaNodeDao vmNumaNodeDao;
    private final OsRepository osRepository;
    private final StorageDomainStaticDao storageDomainStaticDao;
    private final StorageServerConnectionDao storageServerConnectionDao;

    private static final String BLOCK_DOMAIN_DISK_PATH = "/rhev/data-center/mnt/blockSD/%s/images/%s/%s";
    private static final String FILE_DOMAIN_DISK_PATH = "/rhev/data-center/%s/%s/images/%s/%s";

    private static final Pattern BLOCK_DOMAIN_MATCHER =
            Pattern.compile(String.format(BLOCK_DOMAIN_DISK_PATH, ValidationUtils.GUID,
                    ValidationUtils.GUID, ValidationUtils.GUID));

    @Inject
    VmInfoBuildUtils(
            NetworkDao networkDao,
            NetworkFilterDao networkFilterDao,
            NetworkQoSDao networkQosDao,
            StorageQosDao storageQosDao,
            VmDeviceDao vmDeviceDao,
            VnicProfileDao vnicProfileDao,
            VmNicFilterParameterDao vmNicFilterParameterDao,
            NetworkClusterDao networkClusterDao,
            AuditLogDirector auditLogDirector,
            ClusterFeatureDao clusterFeatureDao,
            VmNumaNodeDao vmNumaNodeDao,
            OsRepository osRepository,
            StorageDomainStaticDao storageDomainStaticDao,
            StorageServerConnectionDao storageServerConnectionDao) {
        this.networkDao = Objects.requireNonNull(networkDao);
        this.networkFilterDao = Objects.requireNonNull(networkFilterDao);
        this.networkQosDao = Objects.requireNonNull(networkQosDao);
        this.storageQosDao = Objects.requireNonNull(storageQosDao);
        this.vmDeviceDao = Objects.requireNonNull(vmDeviceDao);
        this.vnicProfileDao = Objects.requireNonNull(vnicProfileDao);
        this.vmNicFilterParameterDao = Objects.requireNonNull(vmNicFilterParameterDao);
        this.networkClusterDao = Objects.requireNonNull(networkClusterDao);
        this.auditLogDirector = Objects.requireNonNull(auditLogDirector);
        this.clusterFeatureDao = Objects.requireNonNull(clusterFeatureDao);
        this.vmNumaNodeDao = Objects.requireNonNull(vmNumaNodeDao);
        this.osRepository = Objects.requireNonNull(osRepository);
        this.storageDomainStaticDao = Objects.requireNonNull(storageDomainStaticDao);
        this.storageServerConnectionDao = Objects.requireNonNull(storageServerConnectionDao);
    }

    @SuppressWarnings("unchecked")
    public void buildCinderDisk(CinderDisk cinderDisk, Map<String, Object> struct) {
        CinderConnectionInfo connectionInfo = cinderDisk.getCinderConnectionInfo();
        CinderVolumeDriver cinderVolumeDriver = CinderVolumeDriver.forValue(connectionInfo.getDriverVolumeType());
        if (cinderVolumeDriver == null) {
            log.error("Unsupported Cinder volume driver: '{}' (disk: '{}')",
                    connectionInfo.getDriverVolumeType(),
                    cinderDisk.getDiskAlias());
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
     * @param vmDevice   The disk device with QoS limits
     * @param storageQos StorageQos
     */
    public void handleIoTune(VmDevice vmDevice, StorageQos storageQos) {
        if (storageQos != null) {
            if (vmDevice.getSpecParams() == null) {
                vmDevice.setSpecParams(new HashMap<>());
            }
            vmDevice.getSpecParams().put(VdsProperties.Iotune, IoTuneUtils.ioTuneMapFrom(storageQos));
        }
    }

    public StorageQos loadStorageQos(DiskImage diskImage) {
        if (diskImage.getDiskProfileId() == null) {
            return null;
        }
        return storageQosDao.getQosByDiskProfileId(diskImage.getDiskProfileId());
    }

    public String evaluateInterfaceType(VmInterfaceType ifaceType, boolean vmHasAgent) {
        return ifaceType == VmInterfaceType.rtl8139_pv
                ? vmHasAgent ? VmInterfaceType.pv.name() : VmInterfaceType.rtl8139.name()
                : ifaceType.getInternalName();
    }

    public void addNetworkVirtualFunctionProperties(Map<String, Object> struct,
            VmNic vmInterface,
            VmDevice vmDevice,
            String vfName,
            VM vm) {
        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());
        struct.put(VdsProperties.HostDev, vfName);

        addAddress(vmDevice, struct);
        struct.put(VdsProperties.MAC_ADDR, vmInterface.getMacAddress());
        struct.put(VdsProperties.DeviceId, String.valueOf(vmDevice.getId().getDeviceId()));

        Map<String, Object> specParams = new HashMap<>();

        VnicProfile vnicProfile = vnicProfileDao.get(vmInterface.getVnicProfileId());
        Network network = networkDao.get(vnicProfile.getNetworkId());
        if (NetworkUtils.isVlan(network)) {
            specParams.put(VdsProperties.VLAN_ID, network.getVlanId());
        }
        struct.put(VdsProperties.SpecParams, specParams);

        addCustomPropertiesForDevice(struct,
                vm,
                vmDevice,
                getVnicCustomProperties(vnicProfile));
    }

    public void addProfileDataToNic(Map<String, Object> struct,
            VM vm,
            VmDevice vmDevice,
            VmNic nic) {
        VnicProfile vnicProfile = null;
        Network network = null;
        String networkName = "";
        String vdsmName = "";
        List<VnicProfileProperties> unsupportedFeatures = new ArrayList<>();
        if (nic.getVnicProfileId() != null) {
            vnicProfile = vnicProfileDao.get(nic.getVnicProfileId());
            if (vnicProfile != null) {
                network = networkDao.get(vnicProfile.getNetworkId());
                networkName = network.getName();
                vdsmName = network.getVdsmName();
                log.debug("VNIC '{}' is using profile '{}' on network '{}' with vdsmName '{}'",
                        nic.getName(),
                        vnicProfile,
                        networkName,
                        vdsmName);
                addQosForDevice(struct, vnicProfile);
            }
        }

        struct.put(VdsProperties.NETWORK, vdsmName);

        addPortMirroringToVmInterface(struct, vnicProfile, network);

        addCustomPropertiesForDevice(struct,
                vm,
                vmDevice,
                getVnicCustomProperties(vnicProfile));

        reportUnsupportedVnicProfileFeatures(vm, nic, vnicProfile, unsupportedFeatures);
    }

    private void addPortMirroringToVmInterface(Map<String, Object> struct,
            VnicProfile vnicProfile,
            Network network) {

        if (vnicProfile != null && vnicProfile.isPortMirroring()) {
            struct.put(
                    VdsProperties.PORT_MIRRORING,
                    network == null
                            ? Collections.<String> emptyList()
                            : Collections.singletonList(network.getName()));
        }

    }

    private void addQosForDevice(Map<String, Object> struct, VnicProfile vnicProfile) {

        Guid qosId = vnicProfile.getNetworkQosId();
        @SuppressWarnings("unchecked")
        Map<String, Object> specParams = (Map<String, Object>) struct.get(VdsProperties.SpecParams);
        if (specParams == null) {
            specParams = new HashMap<>();
            struct.put(VdsProperties.SpecParams, specParams);
        }
        NetworkQoS networkQoS = (qosId == null) ? new NetworkQoS() : networkQosDao.get(qosId);
        NetworkQosMapper qosMapper =
                new NetworkQosMapper(specParams, VdsProperties.QOS_INBOUND, VdsProperties.QOS_OUTBOUND);
        qosMapper.serialize(networkQoS);
    }

    private Map<String, String> getVnicCustomProperties(VnicProfile vnicProfile) {
        Map<String, String> customProperties = null;

        if (vnicProfile != null) {
            customProperties = vnicProfile.getCustomProperties();
        }

        return customProperties == null ? new HashMap<>() : customProperties;
    }

    private void addCustomPropertiesForDevice(Map<String, Object> struct,
            VM vm,
            VmDevice vmDevice,
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
            struct.put(VdsProperties.Custom, customProperties);
        }
    }

    public void addNetworkFiltersToNic(Map<String, Object> struct, VmNic vmNic) {
        final NetworkFilter networkFilter = fetchVnicProfileNetworkFilter(vmNic);
        if (networkFilter != null) {
            final String networkFilterName = networkFilter.getName();
            struct.put(VdsProperties.NW_FILTER, networkFilterName);
            final List<VmNicFilterParameter> vmNicFilterParameters =
                    vmNicFilterParameterDao.getAllForVmNic(vmNic.getId());
            struct.put(VdsProperties.NETWORK_FILTER_PARAMETERS, mapVmNicFilterParameter(vmNicFilterParameters));
        }
    }

    private List<Map<String, Object>> mapVmNicFilterParameter(List<VmNicFilterParameter> vmNicFilterParameters) {
        return vmNicFilterParameters.stream().map(this::mapVmNicFilterParameter).collect(Collectors.toList());
    }

    private Map<String, Object> mapVmNicFilterParameter(VmNicFilterParameter nicFilterParameter) {
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("name", nicFilterParameter.getName());
        parameter.put("value", nicFilterParameter.getValue());
        return parameter;
    }

    protected NetworkFilter fetchVnicProfileNetworkFilter(VmNic vmNic) {
        if (vmNic.getVnicProfileId() != null) {
            VnicProfile vnicProfile = vnicProfileDao.get(vmNic.getVnicProfileId());
            if (vnicProfile != null) {
                final Guid networkFilterId = vnicProfile.getNetworkFilterId();
                return networkFilterId == null ? null : networkFilterDao.getNetworkFilterById(networkFilterId);
            }
        }
        return null;
    }

    Map<String, Object> buildFloppyDetails(VmDevice vmDevice) {
        Map<String, Object> struct = new HashMap<>();
        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());
        struct.put(VdsProperties.Index, "0"); // IDE slot 2 is reserved by VDSM to CDROM
        struct.put(VdsProperties.INTERFACE, VdsProperties.Fdc);
        struct.put(VdsProperties.ReadOnly, String.valueOf(vmDevice.getReadOnly()));
        struct.put(VdsProperties.Shareable, Boolean.FALSE.toString());
        return struct;
    }

    Map<String, Object> buildCdDetails(VmDevice vmDevice, VM vm) {
        Map<String, Object> struct = new HashMap<>();
        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());

        String cdInterface = osRepository.getCdInterface(
                vm.getOs(),
                vm.getCompatibilityVersion(),
                ChipsetType.fromMachineType(vm.getEmulatedMachine()));

        struct.put(VdsProperties.INTERFACE, cdInterface);

        int index = VmDeviceCommonUtils.getCdDeviceIndex(cdInterface);
        struct.put(VdsProperties.Index, Integer.toString(index));

        if ("scsi".equals(cdInterface)) {
            struct.put(VdsProperties.Address, createAddressForScsiDisk(0, index));
        }

        struct.put(VdsProperties.ReadOnly, Boolean.TRUE.toString());
        struct.put(VdsProperties.Shareable, Boolean.FALSE.toString());
        return struct;
    }

    void setVdsPropertiesFromSpecParams(Map<String, Object> specParams, Map<String, Object> struct) {
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
    boolean isFirstMasterController(String model) {
        return model.equalsIgnoreCase(FIRST_MASTER_MODEL);
    }

    /**
     * @return A map of VirtIO-SCSI index to a map of disk's index in that VirtIO-SCSI controller:
     * for example:
     * (0 -> (disk1 -> 0, disk2 -> 1)),
     * (1 -> (disk3 -> 0, disk4 -> 1))
     * means that there are two controllers, 0 and 1. On the 0 there are 2 disks, first mapped to 0 and second to 1
     * inside that particular controller. Similar for second controller.
     */
    public Map<Integer, Map<VmDevice, Integer>> getVmDeviceUnitMapForVirtioScsiDisks(VM vm) {
        return getVmDeviceUnitMapForScsiDisks(vm, DiskInterface.VirtIO_SCSI, false);
    }

    /**
     * @return A map of sPAPR VSCSI index to a map of disk's index in that sPAPR VSCSI controller:
     * for example:
     * (0 -> (disk1 -> 0, disk2 -> 1)),
     * (1 -> (disk3 -> 0, disk4 -> 1))
     * means that there are two controllers, 0 and 1. On the 0 there are 2 disks, first mapped to 0 and second to 1
     * inside that particular controller. Similar for second controller.
     */
    public Map<Integer, Map<VmDevice, Integer>> getVmDeviceUnitMapForSpaprScsiDisks(VM vm) {
        return getVmDeviceUnitMapForScsiDisks(vm, DiskInterface.SPAPR_VSCSI, true);
    }

    protected Map<Integer, Map<VmDevice, Integer>> getVmDeviceUnitMapForScsiDisks(VM vm,
            DiskInterface scsiInterface,
            boolean reserveFirstTwoLuns) {
        List<Disk> disks = getSortedDisks(vm);
        Map<Integer, Map<VmDevice, Integer>> vmDeviceUnitMap = new HashMap<>();
        LinkedList<VmDevice> vmDeviceList = new LinkedList<>();

        for (Disk disk : disks) {
            DiskVmElement dve = disk.getDiskVmElementForVm(vm.getId());
            if (dve.getDiskInterface() == scsiInterface) {
                VmDevice vmDevice = getVmDeviceByDiskId(disk.getId(), vm.getId());
                Map<String, String> address = StringMapUtils.string2Map(vmDevice.getAddress());
                String unitStr = address.get(VdsProperties.Unit);
                String controllerStr = address.get(VdsProperties.Controller);

                // If unit property is available adding to 'vmDeviceUnitMap';
                // Otherwise, adding to 'vmDeviceList' for setting the unit property later.
                if (StringUtils.isNotEmpty(unitStr) && StringUtils.isNotEmpty(controllerStr)) {
                    Integer controllerInt = Integer.valueOf(controllerStr);

                    boolean controllerOutOfRange = controllerInt >= vm.getNumOfIoThreads() + getDefaultVirtioScsiIndex(vm);
                    boolean ioThreadsEnabled = vm.getNumOfIoThreads() > 0 &&
                            FeatureSupported.virtioScsiIoThread(vm.getCompatibilityVersion());

                    if ((ioThreadsEnabled && !controllerOutOfRange) ||
                            (controllerInt == getDefaultVirtioScsiIndex(vm))) {
                        if (!vmDeviceUnitMap.containsKey(controllerInt)) {
                            vmDeviceUnitMap.put(controllerInt, new HashMap<>());
                        }
                        vmDeviceUnitMap.get(controllerInt).put(vmDevice, Integer.valueOf(unitStr));
                    } else {
                        // controller id not correct, generate the address again later
                        vmDevice.setAddress(null);
                        vmDeviceList.add(vmDevice);
                    }
                } else {
                    vmDeviceList.add(vmDevice);
                }
            }
        }

        // Find available unit (disk's index in VirtIO-SCSI controller) for disks with empty address\
        IntStream.range(0, vmDeviceList.size()).forEach(index -> {
            VmDevice vmDevice = vmDeviceList.get(index);
            int controller = getControllerForScsiDisk(vmDevice, vm, index);

            if (!vmDeviceUnitMap.containsKey(controller)) {
                vmDeviceUnitMap.put(controller, new HashMap<>());
            }

            int unit = getAvailableUnitForScsiDisk(vmDeviceUnitMap.get(controller), reserveFirstTwoLuns);
            vmDeviceUnitMap.get(controller).put(vmDevice, unit);
        });

        return vmDeviceUnitMap;
    }


    private int getDefaultVirtioScsiIndex(VM vm) {
        Map<DiskInterface, Integer> controllerIndexMap =
                ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new GetControllerIndices()).returnValue();

        return controllerIndexMap.get(DiskInterface.VirtIO_SCSI);
    }

    /**
     * Generates the next controller id using round robin.
     * If the disk already has an controller id, returns it.
     *
     * @param disk the disk for which the controller id has to be generated
     * @param vm a VM to which this disk is attached
     * @param increment a number from 0..N to let the round robin cycle
     * @return a controller id
     */
    public int getControllerForScsiDisk(VmDevice disk, VM vm, int increment) {
        Map<String, String> address = StringMapUtils.string2Map(disk.getAddress());
        String controllerStr = address.get(VdsProperties.Controller);

        int defaultIndex = getDefaultVirtioScsiIndex(vm);
        boolean ioThreadsEnabled = FeatureSupported.virtioScsiIoThread(vm.getCompatibilityVersion());

        if (!ioThreadsEnabled) {
            // no io threads, only 1 controller allowed and it is the default one
            return defaultIndex;
        }

        if (StringUtils.isNotEmpty(controllerStr)) {
            int controllerInt = Integer.parseInt(controllerStr);
            boolean controllerOutOfRange = controllerInt > vm.getNumOfIoThreads() + getDefaultVirtioScsiIndex(vm);

            if (!controllerOutOfRange) {
                // io threads enabled and the controller in range, use it
                return controllerInt;
            }
        }

        // Here it can end up either if the controller has not been set or it has been set but it is out of range.
        // Out of range it can be when:
        // The VM was started with, say, 2 io threads and this disk had the controller id set to 1
        // Than the VM has been turned off, set the io threads to 1 and ran the VM again. In that case this disk will
        // be out of range.
        // In both cases the controller index needs to be generated again.
        if (vm.getNumOfIoThreads() > 0) {
            // the num of IO threads equals to num of controllers
            // the result of this will be a round robin over the controller indexes from the default index
            return increment % vm.getNumOfIoThreads() + defaultIndex;
        }

        return defaultIndex;
    }

    public List<Disk> getSortedDisks(VM vm) {
        // order first by drive numbers and then order by boot for the bootable
        // drive to be first (important for IDE to be index 0) !
        List<Disk> disks = new ArrayList<>(vm.getDiskMap().values());
        Collections.sort(disks, new LexoNumericNameableComparator<>());
        Collections.sort(disks, Collections.reverseOrder(new DiskByBootAndSnapshotComparator(vm.getId())));
        return disks;
    }

    public int getAvailableUnitForScsiDisk(Map<VmDevice, Integer> vmDeviceUnitMap, boolean reserveFirstTwoLuns) {
        int unit = reserveFirstTwoLuns ? 2 : 0;
        if (vmDeviceUnitMap == null) {
            return unit;
        }
        while (vmDeviceUnitMap.containsValue(unit)) {
            unit++;
        }
        return unit;
    }

    public Map<String, String> createAddressForScsiDisk(int controller, int unit) {
        Map<String, String> addressMap = new HashMap<>();
        addressMap.put(VdsProperties.Type, "drive");
        addressMap.put(VdsProperties.Controller, String.valueOf(controller));
        addressMap.put(VdsProperties.Bus, "0");
        addressMap.put(VdsProperties.target, "0");
        addressMap.put(VdsProperties.Unit, String.valueOf(unit));
        return addressMap;
    }

    void addAddress(VmDevice vmDevice, Map<String, Object> struct) {
        Map<String, String> addressMap = StringMapUtils.string2Map(vmDevice.getAddress());
        if (!addressMap.isEmpty()) {
            struct.put(VdsProperties.Address, addressMap);
        }
    }

    VmDevice getVmDeviceByDiskId(Guid diskId, Guid vmId) {
        // get vm device for this disk from DB
        return vmDeviceDao.get(new VmDeviceId(diskId, vmId));
    }

    private void reportUnsupportedVnicProfileFeatures(VM vm,
            VmNic nic,
            VnicProfile vnicProfile,
            List<VnicProfileProperties> unsupportedFeatures) {

        if (unsupportedFeatures.isEmpty()) {
            return;
        }

        AuditLogable event = new AuditLogableImpl();
        event.setVmId(vm.getId());
        event.setVmName(vm.getName());
        event.setClusterId(vm.getClusterId());
        event.setClusterName(vm.getClusterName());
        event.setCustomId(nic.getId().toString());
        event.setCompatibilityVersion(vm.getCompatibilityVersion().toString());
        event.addCustomValue("NicName", nic.getName());
        event.addCustomValue("VnicProfile", vnicProfile == null ? null : vnicProfile.getName());
        String[] unsupportedFeatureNames = new String[unsupportedFeatures.size()];
        for (int i = 0; i < unsupportedFeatures.size(); i++) {
            unsupportedFeatureNames[i] = unsupportedFeatures.get(i).getFeatureName();
        }

        event.addCustomValue("UnsupportedFeatures", StringUtils.join(unsupportedFeatureNames, ", "));
        auditLogDirector.log(event, AuditLogType.VNIC_PROFILE_UNSUPPORTED_FEATURES);
    }

    public Network getDisplayNetwork(VM vm) {
        List<NetworkCluster> all = networkClusterDao.getAllForCluster(vm.getClusterId());
        NetworkCluster networkCluster = null;
        for (NetworkCluster tempNetworkCluster : all) {
            if (tempNetworkCluster.isDisplay()) {
                networkCluster = tempNetworkCluster;
                break;
            }
        }
        if (networkCluster != null) {
            List<Network> allNetworks = networkDao.getAll();
            for (Network tempNetwork : allNetworks) {
                if (tempNetwork.getId().equals(networkCluster.getNetworkId())) {
                    return tempNetwork;
                }
            }
        }
        return null;
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

    public int getVmTimeZone(VM vm) {
        // get vm timezone
        String timeZone = getTimeZoneForVm(vm);

        final String javaZoneId;
        if (osRepository.isWindows(vm.getOs())) {
            // convert to java & calculate offset
            javaZoneId = WindowsJavaTimezoneMapping.get(timeZone);
        } else {
            javaZoneId = timeZone;
        }

        int offset = 0;
        if (javaZoneId != null) {
            offset = TimeZone.getTimeZone(javaZoneId).getOffset(
                    new Date().getTime()) / 1000;
        }
        return offset;
    }

    public String getEmulatedMachineByClusterArch(ArchitectureType arch) {
        switch(arch) {
        case ppc64:
        case ppc64le:
            return "pseries";
        case x86_64:
        default:
            return "pc";
        }
    }

    public VmDevice createVideoDeviceByDisplayType(DisplayType displayType, Guid vmId) {
        VmDevice vmDevice = new VmDevice();
        vmDevice.setId(new VmDeviceId(Guid.newGuid(), vmId));
        vmDevice.setType(VmDeviceGeneralType.VIDEO);
        vmDevice.setDevice(displayType.getDefaultVmDeviceType().getName());
        vmDevice.setPlugged(true);
        vmDevice.setAddress("");
        return vmDevice;
    }

    public void addVmGraphicsOptions(Map<GraphicsType, GraphicsInfo> infos, Map<String, Object> params, VM vm) {
        if (infos.containsKey(GraphicsType.SPICE)) {
            params.put(VdsProperties.spiceFileTransferEnable,
                    Boolean.toString(vm.isSpiceFileTransferEnabled()));
            params.put(VdsProperties.spiceCopyPasteEnable,
                    Boolean.toString(vm.isSpiceCopyPasteEnabled()));

            if (Config.getValue(ConfigValues.SSLEnabled)) {
                params.put(VdsProperties.SpiceSecureChannels, Config.getValue(
                        ConfigValues.SpiceSecureChannels, vm.getCompatibilityVersion().toString()));
            }
        }

        if (infos.containsKey(GraphicsType.VNC)) {
            String keyboardLayout = vm.getDynamicData().getVncKeyboardLayout();
            if (keyboardLayout == null) {
                keyboardLayout = vm.getDefaultVncKeyboardLayout();
                if (keyboardLayout == null) {
                    keyboardLayout = Config.getValue(ConfigValues.VncKeyboardLayout);
                }
            }

            params.put(VdsProperties.KeyboardMap, keyboardLayout);
        }
    }

    public List<VmDevice> createGraphicsDevices(
            Map<GraphicsType, GraphicsInfo> graphicsInfos,
            Map<String, Object> extraSpecParams,
            Guid vmId) {
        final Comparator<GraphicsType> spiceLastComparator =
                ComparatorUtils.sortLast(GraphicsType.SPICE);
        final List<Entry<GraphicsType, GraphicsInfo>> sortedGraphicsInfos = graphicsInfos.entrySet().stream()
                .sorted(Comparator.comparing(Entry::getKey, spiceLastComparator))
                .collect(Collectors.toList());

        List<VmDevice> result = new ArrayList<>();
        for (Entry<GraphicsType, GraphicsInfo> graphicsInfo : sortedGraphicsInfos) {
            VmDevice device = new VmDevice();
            device.setId(new VmDeviceId(Guid.newGuid(), vmId));
            device.setType(VmDeviceGeneralType.GRAPHICS);
            device.setDevice(graphicsInfo.getKey().name().toLowerCase());
            device.setPlugged(true);
            device.setAddress("");
            if (extraSpecParams != null) {
                device.setSpecParams(extraSpecParams);
            }
            result.add(device);
        }

        return result;
    }

    /**
     * See {@link VmInfoBuildUtilsTest#testMakeDiskName()}
     */
    public String makeDiskName(String diskInterface, int index) {
        String devIndex = "";
        while (index > 0) {
            devIndex = (char)('a' + (index % 26)) + devIndex;
            index /= 26;
        }
        return diskInterfaceToDevName(diskInterface) + (devIndex.isEmpty() ? 'a' : devIndex);
    }

    private String diskInterfaceToDevName(String iface) {
        switch(iface) {
        case "virtio":
            return "vd";
        case "fdc":
            return "fd";
        case "scsi":
        case "sata":
            return "sd";
        case "ide":
        default:
            return "hd";
        }
    }

    public VmDevice createSysprepPayloadDevice(String sysPrepContent, VM vm) {
        // We do not validate the size of the content being passed to the VM payload by VmPayload.isPayloadSizeLegal().
        // The sysprep file size isn't being verified for 3.0 clusters and below, so we maintain the same behavior here.
        VmPayload vmPayload = new VmPayload();
        vmPayload.setDeviceType(VmDeviceType.FLOPPY);
        vmPayload.getFiles().put(
                osRepository.getSysprepFileName(vm.getOs(), vm.getCompatibilityVersion()),
                new String(BASE_64.encode(sysPrepContent.getBytes()), StandardCharsets.UTF_8));

        return new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                VmDeviceGeneralType.DISK,
                VmDeviceType.FLOPPY.getName(),
                "",
                vmPayload.getSpecParams(),
                true,
                true,
                true,
                "",
                null,
                null,
                null);
    }

    public VmDevice createCloudInitPayloadDevice(Map<String, byte[]> cloudInitContent, VM vm) {
        VmPayload vmPayload = new VmPayload();
        vmPayload.setDeviceType(VmDeviceType.CDROM);
        vmPayload.setVolumeId(CLOUD_INIT_VOL_ID);
        for (Entry<String, byte[]> entry : cloudInitContent.entrySet()) {
            vmPayload.getFiles().put(entry.getKey(),
                    new String(BASE_64.encode(entry.getValue()), StandardCharsets.UTF_8));
        }

        return new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                VmDeviceGeneralType.DISK,
                VmDeviceType.CDROM.getName(),
                "",
                vmPayload.getSpecParams(),
                true,
                true,
                true,
                "",
                null,
                null,
                null);
    }

    public VmDevice createFloppyDevice(VM vm) {
        return new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                VmDeviceGeneralType.DISK,
                VmDeviceType.FLOPPY.getName(),
                "",
                null,
                true,
                true,
                true,
                "",
                null,
                null,
                null);
    }

    public Optional<String> getNetworkDiskType(VM vm, StorageType storageType) {
        if (storageType == StorageType.GLUSTERFS) {
            if (FeatureSupported.libgfApiSupported(vm.getCompatibilityVersion())
                    || isFeatureSupportedAsAdditionalFeature(vm.getClusterId(), VDSM_LIBGF_CAP_NAME)) {
                return Optional.of(VdsProperties.NETWORK);
            }
        }
        return Optional.empty();
    }

    public String getDiskType(VM vm, DiskImage diskImage) {
        StorageType storageType = diskImage.getStorageTypes().get(0);
        Optional<String> diskType = getNetworkDiskType(vm, storageType);
        return diskType.orElseGet(() -> storageType.isBlockDomain() ? "block" : "file");
    }

    private boolean isFeatureSupportedAsAdditionalFeature(Guid clusterId, String featureName) {
        return clusterFeatureDao.getSupportedFeaturesByClusterId(clusterId).stream()
        .filter(SupportedAdditionalClusterFeature::isEnabled)
        .filter(f -> f.getFeature().getName().equals(featureName))
        .findAny()
        .isPresent();
    }

    /**
     * Calculate and generates a string list of pCPUs to use in libvirt xml for IO threads and emulator threads pinning.
     * Used for High Performance VM types only.
     * In case the VM type is not "High performance" or in case prerequisites are not set, null value is returned.
     *
     * @param vm a VM to which iothreads and emulator threads pinning is calculated
     * @param cpuPinning a map of CPU pinning topology (vCPU,vCPU set)
     * @param hostNumaNodesSupplier a list of the assigned VM's host NUMA nodes
     * @param vdsCpuThreads number of Logical CPU Cores
     * @return a string list of pCPUs ids to pin to
     */
    public String getIoThreadsAndEmulatorPinningCpus(VM vm, Map<String, Object> cpuPinning, MemoizingSupplier<List<VdsNumaNode>> hostNumaNodesSupplier, int vdsCpuThreads) {
        if (vm.getVmType() != VmType.HighPerformance) {
            return null;
        }

        List<VmNumaNode> vmNumaNodes = vmNumaNodeDao.getAllVmNumaNodeByVmId(vm.getId());
        Optional<VmNumaNode> pinnedVmNumaNode = vmNumaNodes.stream().filter(d -> !d.getVdsNumaNodeList().isEmpty()).findAny();

        if (MapUtils.isEmpty(cpuPinning) || !pinnedVmNumaNode.isPresent() || vm.getNumOfIoThreads() == 0) {
            String msgReason1 = MapUtils.isEmpty(cpuPinning) ? "CPU Pinning topology is not set": null;
            String msgReason2 = vm.getNumOfIoThreads() == 0 ? "IO Threads is not enabled": null;
            String msgReason3 = !pinnedVmNumaNode.isPresent() ? "vm's virtual NUMA nodes are not pinned to host's NUMA nodes": null;
            String finalMsgReason = Arrays.asList(msgReason1, msgReason2, msgReason3).stream()
                    .filter(Objects::nonNull).collect(Collectors.joining(", ")) + ".";

            log.warn("No IO thread(s) pinning and Emulator thread(s) pinning for High Performance VM {} {} due to wrong configuration: {}",
                    vm.getName(), vm.getId(), finalMsgReason);
            return null;
        }

        return findCpusToPinIoAndEmulator(vm, cpuPinning, hostNumaNodesSupplier, vdsCpuThreads);
    }

    /**
     * Finds Storage Domain by disk and extracts gluster host and volume info.
     * @param disk Disk located on Gluster's SD
     * @return volume info array. First element is a brick's hostname, second element - volume name.
     */
    public String[] getGlusterVolInfo(Disk disk) {
        StorageDomainStatic dom = this.storageDomainStaticDao.get(((DiskImage) disk).getStorageIds().get(0));
        StorageServerConnections con = this.storageServerConnectionDao.getAllForDomain(dom.getId()).get(0);
        String path = con.getConnection(); // host:/volume
        String[] volInfo = path.split(":");
        if (volInfo.length != 2) {
            log.error("Invalid volInfo value: {}", path);
            return null;
        }
        volInfo[1] = volInfo[1].replaceFirst("^/", "");
        return volInfo;
    }

    private String findCpusToPinIoAndEmulator(VM vm, Map<String, Object> cpuPinning, MemoizingSupplier<List<VdsNumaNode>> hostNumaNodesSupplier, int vdsCpuThreads) {
        List<VdsNumaNode> vdsNumaNodes = hostNumaNodesSupplier.get();
        Set<Integer> vdsPinnedCpus = getAllPinnedPCpus(cpuPinning);
        VdsNumaNode mostPinnedPnumaNode = vdsNumaNodes.isEmpty() ? null : vdsNumaNodes.get(0);
        int maxNumOfPinnedCpusForNode = 0;
        // Go over all Host's NUMA nodes and find the node with most pinned CPU's in order to pin the
        // IO threads and emulator threads to
        for (VdsNumaNode pNode : vdsNumaNodes) {
            if (pNode.getCpuIds().isEmpty()) {
                continue;
            }

            int numOfPinnedCpus = CollectionUtils.intersection(pNode.getCpuIds(), vdsPinnedCpus).size();

            if (maxNumOfPinnedCpusForNode < numOfPinnedCpus) {
                maxNumOfPinnedCpusForNode = numOfPinnedCpus;
                mostPinnedPnumaNode = pNode;
            } else if (maxNumOfPinnedCpusForNode == numOfPinnedCpus && !mostPinnedPnumaNode.getCpuIds().isEmpty()) {
                // choose the NUMA node with lower CPU ids
                mostPinnedPnumaNode = getNumaNodeWithLowerCpuIds(mostPinnedPnumaNode, pNode);
            }
        }

        // Prepare the list of one or two CPUs to pin Io and emulator threads to
        List<Integer> retCpusList = new LinkedList<>();
        if (mostPinnedPnumaNode == null || mostPinnedPnumaNode.getCpuIds().isEmpty()) {
            // in case no NUMA node found or no CPU's for the NUMA node,
            // set pinned CPU's to be {0,1} or just {0) (depends on the number of CPUs in host)
            retCpusList.add(0);
            if (vdsCpuThreads > 1) {
                retCpusList.add(1);
            }
        } else {
            retCpusList.add(mostPinnedPnumaNode.getCpuIds().get(0));
            if (mostPinnedPnumaNode.getCpuIds().size() > 1) {
                retCpusList.add(mostPinnedPnumaNode.getCpuIds().get(1));
            }
        }

        String overridenPinCpus = getOverriddenPinnedCpusList(vdsPinnedCpus, retCpusList);
        if (!overridenPinCpus.isEmpty()) {
            log.warn("IO thread(s), Emulator thread(s) and few CPU thread(s) are pinned to the same physical CPU(s): [{}], for High Performance "
                            + "VM {} {}. Please consider changing the CPU pinning topology to avoid that overlapping.",
                    overridenPinCpus, vm.getName(), vm.getId());
        }

        return retCpusList.size() == 2 ?
                retCpusList.get(0) + "," + retCpusList.get(1) :
                retCpusList.get(0).toString();
    }

    // collect all pinned cpus and merge them into one set
    private static Set<Integer> getAllPinnedPCpus(Map<String, Object> cpuPinning) {
        final Set<Integer> pinnedCpus = new LinkedHashSet<>();
        cpuPinning.forEach((vcpu, cpuSet) -> {
            pinnedCpus.addAll(parsePCpuPinningNumbers((String)cpuSet));
        });
        return pinnedCpus;
    }

    private static Collection<Integer> parsePCpuPinningNumbers(final String text) {
        try {
            Set<Integer> include = new HashSet<>();
            Set<Integer> exclude = new HashSet<>();
            String[] splitText = text.split(",");
            for (String section : splitText) {
                if (section.startsWith("^")) {
                    exclude.add(Integer.parseInt(section.substring(1)));
                } else if (section.contains("-")) {
                    // include range
                    String[] numbers = section.split("-");
                    int start = Integer.parseInt(numbers[0]);
                    int end = Integer.parseInt(numbers[1]);
                    List<Integer> range = createRange(start, end);
                    if (range != null) {
                        include.addAll(range);
                    } else {
                        return Arrays.asList();
                    }
                } else {
                    // include one
                    include.add(Integer.parseInt(section));
                }
            }
            include.removeAll(exclude);
            return include;
        } catch (NumberFormatException ex) {
            return Arrays.asList();
        }
    }

    private static List<Integer> createRange(int start, int end) {
        if (start >= 0 && start < end) {
            List<Integer> returnList = new LinkedList<>();
            for (int i = start; i <= end; i++) {
                returnList.add(i);
            }
            return returnList;
        } else {
            return null;
        }
    }

    // Get list of pCPUs used both for CPU pinning and IO/emulator pinning
    private String getOverriddenPinnedCpusList(Set<Integer> vdsPinnedCpus, List<Integer> ioEmulatorPinnedCpus) {
        List<Integer> overriddenCpus = (List<Integer>)CollectionUtils.intersection(vdsPinnedCpus, ioEmulatorPinnedCpus);

        if (overriddenCpus.isEmpty()) {
            return "";
        } else {
            return overriddenCpus.size() == 2 ?
                    overriddenCpus.get(0) + "," + overriddenCpus.get(1) :
                    overriddenCpus.get(0).toString();
        }
    }

    private static VdsNumaNode getNumaNodeWithLowerCpuIds(VdsNumaNode mostPinnedPnumaNode, VdsNumaNode currNode) {
        return Objects.compare(currNode.getCpuIds(), mostPinnedPnumaNode.getCpuIds(), Comparator.comparing(Collections::min)) < 0 ?
                currNode: mostPinnedPnumaNode;
    }


    public String getPathToImage(DiskImage diskImage) {
        if (diskImage.getStorageTypes().get(0).isBlockDomain()) {
            return String.format(BLOCK_DOMAIN_DISK_PATH,
                    diskImage.getStorageIds().get(0),
                    diskImage.getId(),
                    diskImage.getImageId());
        }
        return String.format(FILE_DOMAIN_DISK_PATH,
                diskImage.getStoragePoolId(),
                diskImage.getStorageIds().get(0),
                diskImage.getId(),
                diskImage.getImageId());
    }

    public boolean isBlockDomainPath(String path) {
        return BLOCK_DOMAIN_MATCHER.matcher(path).matches();
    }
}
