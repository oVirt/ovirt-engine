package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import java.io.IOException;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.UsbControllerModel;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VgpuPlacement;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmInit;
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
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.MDevTypesUtils;
import org.ovirt.engine.core.common.utils.PDIVMapBuilder;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.compat.WindowsJavaTimezoneMapping;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.dao.VmDao;
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
import org.ovirt.engine.core.vdsbroker.monitoring.VmDevicesMonitoring;
import org.ovirt.engine.core.vdsbroker.vdsbroker.CloudInitHandler;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IgnitionHandler;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IoTuneUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.NetworkQosMapper;
import org.ovirt.engine.core.vdsbroker.vdsbroker.NumaSettingFactory;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmSerialNumberBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmInfoBuildUtils {
    private static final Logger log = LoggerFactory.getLogger(VmInfoBuildUtils.class);

    private static final String FIRST_MASTER_MODEL = "ich9-ehci1";
    private static final String CLOUD_INIT_VOL_ID = "config-2";
    private static final Base64 BASE_64 = new Base64(0, null);
    private static final int DEFAULT_HUGEPAGESIZE_X86_64 = 2048;
    private static final int DEFAULT_HUGEPAGESIZE_PPC64LE = 16384;
    private static final List<String> SCSI_HOST_DEV_WITH_PREDETERMINED_ADDRESS;

    public static final String VDSM_LIBGF_CAP_NAME = "libgfapi_supported";

    static {
        SCSI_HOST_DEV_WITH_PREDETERMINED_ADDRESS = new ArrayList<>(LibvirtVmXmlBuilder.SCSI_HOST_DEV_DRIVERS);
        SCSI_HOST_DEV_WITH_PREDETERMINED_ADDRESS.remove(LibvirtVmXmlBuilder.SCSI_VIRTIO_BLK_PCI);
    }

    private final NetworkClusterDao networkClusterDao;
    private final NetworkDao networkDao;
    private final NetworkFilterDao networkFilterDao;
    private final NetworkQoSDao networkQosDao;
    private final StorageQosDao storageQosDao;
    private final VmDeviceDao vmDeviceDao;
    private final VmDao vmDao;
    private final VnicProfileDao vnicProfileDao;
    private final VmNicFilterParameterDao vmNicFilterParameterDao;
    private final AuditLogDirector auditLogDirector;
    private final ClusterFeatureDao clusterFeatureDao;
    private final VmNumaNodeDao vmNumaNodeDao;
    private final OsRepository osRepository;
    private final StorageDomainStaticDao storageDomainStaticDao;
    private final StorageServerConnectionDao storageServerConnectionDao;
    private final VdsNumaNodeDao vdsNumaNodeDao;
    private final VdsStaticDao vdsStaticDao;
    private final VdsDynamicDao vdsDynamicDao;
    private final VdsStatisticsDao vdsStatisticsDao;
    private final HostDeviceDao hostDeviceDao;
    private final DiskVmElementDao diskVmElementDao;
    private final VmDevicesMonitoring vmDevicesMonitoring;
    private final VmSerialNumberBuilder vmSerialNumberBuilder;
    private final MultiQueueUtils multiQueueUtils;

    private static final String BLOCK_DOMAIN_DISK_PATH = "/rhev/data-center/mnt/blockSD/%s/images/%s/%s";
    private static final String FILE_DOMAIN_DISK_PATH = "/rhev/data-center/%s/%s/images/%s/%s";

    private static final Pattern BLOCK_DOMAIN_MATCHER =
            Pattern.compile(String.format(BLOCK_DOMAIN_DISK_PATH, ValidationUtils.GUID,
                    ValidationUtils.GUID, ValidationUtils.GUID));

    public static final int NVDIMM_LABEL_SIZE = 128 * 1024;

    @Inject
    VmInfoBuildUtils(
            NetworkDao networkDao,
            NetworkFilterDao networkFilterDao,
            NetworkQoSDao networkQosDao,
            StorageQosDao storageQosDao,
            VmDeviceDao vmDeviceDao,
            VmDao vmDao,
            VnicProfileDao vnicProfileDao,
            VmNicFilterParameterDao vmNicFilterParameterDao,
            NetworkClusterDao networkClusterDao,
            AuditLogDirector auditLogDirector,
            ClusterFeatureDao clusterFeatureDao,
            VmNumaNodeDao vmNumaNodeDao,
            OsRepository osRepository,
            StorageDomainStaticDao storageDomainStaticDao,
            StorageServerConnectionDao storageServerConnectionDao,
            VdsNumaNodeDao vdsNumaNodeDao,
            VdsStaticDao vdsStaticDao,
            VdsDynamicDao vdsDynamicDao,
            VdsStatisticsDao vdsStatisticsDao,
            HostDeviceDao hostDeviceDao,
            VmSerialNumberBuilder vmSerialNumberBuilder,
            DiskVmElementDao diskVmElementDao,
            VmDevicesMonitoring vmDevicesMonitoring,
            MultiQueueUtils multiQueueUtils) {
        this.networkDao = Objects.requireNonNull(networkDao);
        this.networkFilterDao = Objects.requireNonNull(networkFilterDao);
        this.networkQosDao = Objects.requireNonNull(networkQosDao);
        this.storageQosDao = Objects.requireNonNull(storageQosDao);
        this.vmDeviceDao = Objects.requireNonNull(vmDeviceDao);
        this.vmDao = Objects.requireNonNull(vmDao);
        this.vnicProfileDao = Objects.requireNonNull(vnicProfileDao);
        this.vmNicFilterParameterDao = Objects.requireNonNull(vmNicFilterParameterDao);
        this.networkClusterDao = Objects.requireNonNull(networkClusterDao);
        this.auditLogDirector = Objects.requireNonNull(auditLogDirector);
        this.clusterFeatureDao = Objects.requireNonNull(clusterFeatureDao);
        this.vmNumaNodeDao = Objects.requireNonNull(vmNumaNodeDao);
        this.osRepository = Objects.requireNonNull(osRepository);
        this.storageDomainStaticDao = Objects.requireNonNull(storageDomainStaticDao);
        this.storageServerConnectionDao = Objects.requireNonNull(storageServerConnectionDao);
        this.vdsNumaNodeDao = Objects.requireNonNull(vdsNumaNodeDao);
        this.vdsStaticDao = Objects.requireNonNull(vdsStaticDao);
        this.vdsDynamicDao = Objects.requireNonNull(vdsDynamicDao);
        this.vdsStatisticsDao = Objects.requireNonNull(vdsStatisticsDao);
        this.hostDeviceDao = Objects.requireNonNull(hostDeviceDao);
        this.vmSerialNumberBuilder = Objects.requireNonNull(vmSerialNumberBuilder);
        this.diskVmElementDao = Objects.requireNonNull(diskVmElementDao);
        this.vmDevicesMonitoring = Objects.requireNonNull(vmDevicesMonitoring);
        this.multiQueueUtils = Objects.requireNonNull(multiQueueUtils);
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

        VnicProfile vnicProfile = getVnicProfile(vmInterface.getVnicProfileId());
        Network network = getNetwork(vnicProfile.getNetworkId());
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
        if (nic.getVnicProfileId() != null) {
            vnicProfile = getVnicProfile(nic.getVnicProfileId());
        }
        addProfileDataToNic(struct, vm, vmDevice, nic, vnicProfile);
    }

    public void addProfileDataToNic(Map<String, Object> struct,
            VM vm,
            VmDevice vmDevice,
            VmNic nic,
            VnicProfile vnicProfile) {
        Network network = null;
        String networkName = "";
        String vdsmName = "";
        List<VnicProfileProperties> unsupportedFeatures = new ArrayList<>();
        if (vnicProfile != null) {
            network = getNetwork(vnicProfile.getNetworkId());
            networkName = network.getName();
            vdsmName = network.getVdsmName();
            log.debug("VNIC '{}' is using profile '{}' on network '{}' with vdsmName '{}'",
                    nic.getName(),
                    vnicProfile,
                    networkName,
                    vdsmName);
            addQosForDevice(struct, vnicProfile);
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
                            : Collections.singletonList(network.getVdsmName()));
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
            final List<VmNicFilterParameter> vmNicFilterParameters = getAllNetworkFiltersForVmNic(vmNic.getId());
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
            VnicProfile vnicProfile = getVnicProfile(vmNic.getVnicProfileId());
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

    public VmDevice createCdRomDevice(VM vm) {
        return new VmDevice(
                new VmDeviceId(Guid.newGuid(), vm.getId()),
                VmDeviceGeneralType.DISK,
                VmDeviceType.CDROM.getName(),
                "",
                Collections.singletonMap(VdsProperties.Path, ""),
                true,
                true,
                true,
                "",
                null,
                null,
                null);
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
        return getVmDeviceUnitMapForScsiDisks(vm, DiskInterface.VirtIO_SCSI, false, false);
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
        return getVmDeviceUnitMapForScsiDisks(vm, DiskInterface.SPAPR_VSCSI, true, true);
    }

    protected Map<Integer, Map<VmDevice, Integer>> getVmDeviceUnitMapForScsiDisks(VM vm,
            DiskInterface scsiInterface,
            boolean reserveFirstTwoLuns,
            boolean reserveForScsiCd) {
        Map<Integer, Map<VmDevice, Integer>> vmDeviceUnitMap = new HashMap<>();
        vmDeviceUnitMap.putAll(getVmDeviceUnitMapForHostdevScsiDisks(vm));

        LinkedList<VmDevice> vmDeviceList = new LinkedList<>();
        getSortedPluggedDisks(vm).forEachOrdered(disk -> {
            DiskVmElement dve = disk.getDiskVmElementForVm(vm.getId());
            if (dve.getDiskInterface() == scsiInterface) {
                VmDevice vmDevice = getVmDeviceByDiskId(disk.getId(), vm.getId());
                Map<String, String> address = StringMapUtils.string2Map(vmDevice.getAddress());
                final String unitStr = address.get(VdsProperties.Unit);
                final String controllerStr = address.get(VdsProperties.Controller);

                // If unit property is available adding to 'vmDeviceUnitMap';
                // Otherwise, adding to 'vmDeviceList' for setting the unit property later.
                if (StringUtils.isNotEmpty(unitStr) && StringUtils.isNotEmpty(controllerStr)) {
                    Integer controllerInt = Integer.valueOf(controllerStr);

                    boolean controllerOutOfRange = controllerInt >= vm.getNumOfIoThreads() + getDefaultVirtioScsiIndex(vm, dve.getDiskInterface());
                    boolean ioThreadsEnabled = vm.getNumOfIoThreads() > 0;

                    if ((ioThreadsEnabled && !controllerOutOfRange) ||
                            (controllerInt == getDefaultVirtioScsiIndex(vm, dve.getDiskInterface()))) {
                        vmDeviceUnitMap.computeIfAbsent(controllerInt, i -> new HashMap<>());
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
        });

        // Find available unit (disk's index in VirtIO-SCSI controller) for disks with empty address
        IntStream.range(0, vmDeviceList.size()).forEach(index -> {
            VmDevice vmDevice = vmDeviceList.get(index);
            // TODO: consider changing this so that it will search for the next available and
            // less used controller instead of always starting from index.
            int controller = getControllerForScsiDisk(vmDevice.getAddress(), vm, scsiInterface, index);
            vmDeviceUnitMap.computeIfAbsent(controller, i -> new HashMap<>());
            int unit = getAvailableUnitForScsiDisk(vmDeviceUnitMap.get(controller), reserveFirstTwoLuns, reserveForScsiCd && controller == 0);
            vmDeviceUnitMap.get(controller).put(vmDevice, unit);
        });

        return vmDeviceUnitMap;
    }

    private Map<Integer, Map<VmDevice, Integer>> getVmDeviceUnitMapForHostdevScsiDisks(VM vm) {
        List<HostDeviceView> hostDevices = hostDeviceDao.getVmExtendedHostDevicesByVmId(vm.getId());
        List<HostDeviceView> hostScsiDevices = hostDevices.stream()
                .filter(dev -> "scsi".equals(dev.getCapability()))
                .collect(Collectors.toList());
        if (hostScsiDevices.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> vmCustomProperties = VmPropertiesUtils.getInstance().getVMProperties(
                vm.getCompatibilityVersion(),
                vm.getStaticData());
        String scsiHostdevProperty = vmCustomProperties.get("scsi_hostdev");

        var vmHostDevices = vmDeviceDao.getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.HOSTDEV);
        var nameToHostScsiDevice = hostScsiDevices.stream().collect(Collectors.toMap(HostDevice::getDeviceName, d -> d));

        return !SCSI_HOST_DEV_WITH_PREDETERMINED_ADDRESS.contains(scsiHostdevProperty) ?
                // if the address of the vm's host device is dynamically allocated by libvirt, take it from the vm device
                getHostScsiDeviceAddressByVmHostDevice(vmHostDevices, nameToHostScsiDevice)
                // otherwise, the address is determined according to the corresponding host device
                : getHostScsiDeviceAddressByHostDevice(vmHostDevices, nameToHostScsiDevice);
    }

    private Map<Integer, Map<VmDevice, Integer>> getHostScsiDeviceAddressByVmHostDevice(
            List<VmDevice> vmHostDevices, Map<String, HostDeviceView> nameToHostScsiDevice) {
        Map<Integer, Map<VmDevice, Integer>> hostDeviceUnitMap = new HashMap<>();
        vmHostDevices.stream().filter(dev -> nameToHostScsiDevice.containsKey(dev.getDevice())).forEach(dev -> {
            Map<String, String> address = StringMapUtils.string2Map(dev.getAddress());
            String unitStr = address.get(VdsProperties.Unit);
            String controllerStr = address.get(VdsProperties.Controller);
            if (StringUtils.isNotEmpty(unitStr) && StringUtils.isNotEmpty(controllerStr)) {
                int controller = Integer.parseInt(controllerStr);
                hostDeviceUnitMap.computeIfAbsent(controller, i -> new HashMap<>());
                hostDeviceUnitMap.get(controller).put(dev, Integer.parseInt(unitStr));
            }
        });
        return hostDeviceUnitMap;
    }

    private Map<Integer, Map<VmDevice, Integer>> getHostScsiDeviceAddressByHostDevice(
            List<VmDevice> vmHostDevices, Map<String, HostDeviceView> nameToHostScsiDevice) {
        Map<Integer, Map<VmDevice, Integer>> hostDeviceUnitMap = new HashMap<>();
        vmHostDevices.stream().filter(dev -> nameToHostScsiDevice.containsKey(dev.getDevice())).forEach(dev -> {
            var hostDev = nameToHostScsiDevice.get(dev.getDevice());
            Map<String, String> address = hostDev.getAddress();
            String unitStr = address.get("lun");
            String controllerStr = address.get("host");
            if (StringUtils.isNotEmpty(unitStr) && StringUtils.isNotEmpty(controllerStr)) {
                int controller = Integer.parseInt(controllerStr);
                hostDeviceUnitMap.computeIfAbsent(controller, i -> new HashMap<>());
                hostDeviceUnitMap.get(controller).put(dev, Integer.parseInt(unitStr));
            }
        });
        return hostDeviceUnitMap;
    }

    private int getDefaultVirtioScsiIndex(VM vm, DiskInterface diskInterface) {
        Map<DiskInterface, Integer> controllerIndexMap =
                ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new GetControllerIndices()).returnValue();

        return diskInterface == DiskInterface.SPAPR_VSCSI ?
                controllerIndexMap.get(DiskInterface.SPAPR_VSCSI) :
                controllerIndexMap.get(DiskInterface.VirtIO_SCSI);
    }

    /**
     * Generates the next controller id using round robin.
     * If the disk already has an controller id, returns it.
     *
     * @param address the PCI address of the disk for which the controller id has to be generated
     * @param vm a VM to which this disk is attached
     * @param diskInterface the interface type of the disk
     * @param increment a number from 0..N to let the round robin cycle
     * @return a controller id
     */
    public int getControllerForScsiDisk(String address, VM vm, DiskInterface diskInterface, int increment) {
        Map<String, String> addressMap = StringMapUtils.string2Map(address);
        String controllerStr = addressMap.get(VdsProperties.Controller);

        int defaultIndex = getDefaultVirtioScsiIndex(vm, diskInterface);

        if (StringUtils.isNotEmpty(controllerStr)) {
            int controllerInt = Integer.parseInt(controllerStr);
            boolean controllerOutOfRange = controllerInt > vm.getNumOfIoThreads() + getDefaultVirtioScsiIndex(vm, diskInterface);

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

    public List<Entry<Disk, VmDevice>> getSortedDisks(Map<Disk, VmDevice> disksToDevices, Guid vmId) {
        // Order the drives as following:
        // - Boot devices of non-snapshot disks
        // - Device address of the disk
        // - Boot devices of snapshot disks (i.e., boot disks of other VMs plugged to this one
        // - Then by the disk alias
        List<Entry<Disk, VmDevice>> diskAndDevicePairs = new ArrayList<>(disksToDevices.entrySet());
        diskAndDevicePairs.sort(
                Comparator.comparing((Entry<Disk, VmDevice> e) -> !e.getKey().getDiskVmElementForVm(vmId).isBoot())
                .thenComparing(e -> StringUtils.isEmpty(e.getValue().getAddress()))
                .thenComparing(e -> e.getKey().getDiskVmElementForVm(vmId).isBoot() && e.getKey().isDiskSnapshot())
                .thenComparing(e -> e.getKey(), new LexoNumericNameableComparator<>()));
        return diskAndDevicePairs;
    }

    private Stream<Disk> getSortedPluggedDisks(VM vm) {
        // Order the drives as following:
        // - Boot devices of non-snapshot disks
        // - Boot devices of snapshot disks (i.e., boot disks of other VMs plugged to this one
        // - Then by the disk alias
        return vm.getDiskMap().values().stream()
                .filter(disk -> disk.getDiskVmElementForVm(vm.getId()).isPlugged())
                .sorted(Comparator.comparing((Disk d) -> !d.getDiskVmElementForVm(vm.getId()).isBoot())
                        .thenComparing(d -> d.getDiskVmElementForVm(vm.getId()).isBoot() && d.isDiskSnapshot())
                        .thenComparing(new LexoNumericNameableComparator<>()));
    }

    public int getAvailableUnitForScsiDisk(Map<VmDevice, Integer> vmDeviceUnitMap, boolean reserveFirstTwoLuns, boolean reserveForScsiCd) {
        int unit = reserveFirstTwoLuns ? 2 : 0;
        int cdPayloadUnitIndex = VmDeviceCommonUtils.getCdPayloadDeviceIndex("scsi");
        int cdUnitIndex = VmDeviceCommonUtils.getCdDeviceIndex("scsi");

        while (reserveForScsiCd && unit == cdPayloadUnitIndex ||
               reserveForScsiCd && unit == cdUnitIndex ||
               (vmDeviceUnitMap != null && vmDeviceUnitMap.containsValue(unit))) {
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

    public boolean needsIommuCachingMode(VM vm, MemoizingSupplier<Map<String, HostDevice>> hostDevicesSupplier,
            MemoizingSupplier<List<VmDevice>> vmDevicesSupplier) {
        if (!MDevTypesUtils.getMDevTypes(vm).isEmpty()) {
            return true;
        }
        for (VmDevice device : vmDevicesSupplier.get()) {
            if (device.isPlugged() && device.getType() == VmDeviceGeneralType.HOSTDEV) {
                HostDevice hostDevice = hostDevicesSupplier.get().get(device.getDevice());
                if (hostDevice != null && "pci".equals(hostDevice.getCapability())) {
                    return true;
                }
            }
        }
        return false;
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

    public String getTimeZoneForVm(VM vm) {
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
        String timeZone = getTimeZoneForVm(vm);
        String javaZoneId = osRepository.isWindows(vm.getOs()) ? WindowsJavaTimezoneMapping.get(timeZone) : timeZone;
        long now = new Date().getTime();
        return javaZoneToOffset(javaZoneId, now);
    }

    public static int javaZoneToOffset(String javaZoneId, long now) {
        return javaZoneId != null ? TimeZone.getTimeZone(javaZoneId).getOffset(now) / 1000 : 0;
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

    public String diskInterfaceToDevName(String iface) {
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
        boolean cdromPayload = !osRepository.isFloppySupported(vm.getOs(), vm.getCompatibilityVersion());
        VmPayload vmPayload = new VmPayload();
        if (cdromPayload) {
            vmPayload.setDeviceType(VmDeviceType.CDROM);
        } else {
            vmPayload.setDeviceType(VmDeviceType.FLOPPY);
        }
        vmPayload.getFiles().put(
                osRepository.getSysprepFileName(vm.getOs(), vm.getCompatibilityVersion()),
                new String(BASE_64.encode(sysPrepContent.getBytes()), StandardCharsets.UTF_8));

        return new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                VmDeviceGeneralType.DISK,
                cdromPayload ? VmDeviceType.CDROM.getName() : VmDeviceType.FLOPPY.getName(),
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

    public String getDiskType(VM vm, DiskImage diskImage, VmDevice device) {
        if (device.getSnapshotId() != null) {
            return "file"; // transient disks are always files
        }
        StorageType storageType = diskImage.getStorageTypes().get(0);
        Optional<String> diskType = getNetworkDiskType(vm, storageType);
        return diskType.orElseGet(() -> storageType.isBlockDomain() ? "block" : "file");
    }

    private boolean isFeatureSupportedAsAdditionalFeature(Guid clusterId, String featureName) {
        return clusterFeatureDao.getAllByClusterId(clusterId).stream()
        .filter(SupportedAdditionalClusterFeature::isEnabled)
        .anyMatch(f -> f.getFeature().getName().equals(featureName));
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
            String finalMsgReason = Stream.of(msgReason1, msgReason2, msgReason3)
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
        return path != null && BLOCK_DOMAIN_MATCHER.matcher(path).matches();
    }

    public List<VdsNumaNode> getVdsNumaNodes(Guid vdsId) {
        return vdsNumaNodeDao.getAllVdsNumaNodeByVdsId(vdsId);
    }

    public List<VmNumaNode> getVmNumaNodes(VM vm) {
        int onlineCpus = VmCpuCountHelper.getDynamicNumOfCpu(vm);
        int vcpus = FeatureSupported.supportedInConfig(ConfigValues.HotPlugCpuSupported, vm.getCompatibilityVersion(), vm.getClusterArch()) && !VmCpuCountHelper.isAutoPinning(vm)?
                VmCpuCountHelper.calcMaxVCpu(vm, vm.getCompatibilityVersion())
                : onlineCpus;
        int offlineCpus = vcpus - onlineCpus;
        List<VmNumaNode> vmNumaNodes = vmNumaNodeDao.getAllVmNumaNodeByVmId(vm.getId());
        if (!vmNumaNodes.isEmpty()) {
            int totalCpusInNodes = 0;
            for (VmNumaNode vmNode : vmNumaNodes) {
                totalCpusInNodes += vmNode.getCpuIds().size();
            }

            if (onlineCpus != totalCpusInNodes) {
                offlineCpus = vcpus - totalCpusInNodes;
            }

            // When the NUMA Configuration is provided, distribute the remaining
            // offline vCPUs evenly across all nodes
            if (offlineCpus > 0) {
                Comparator<VmNumaNode> compareBySize =
                        Comparator.comparingInt(o -> o.getCpuIds().size());
                Collections.sort(vmNumaNodes, compareBySize);
                int numaCount = vmNumaNodes.size();
                int start = totalCpusInNodes-1;
                for (VmNumaNode vmNode : vmNumaNodes) {
                    int index = start = start+1;
                    while (index < vcpus) {
                        vmNode.getCpuIds().add(index);
                        index += numaCount;
                    }
                }
            }
            return vmNumaNodes;
        }

        // if user didn't set specific NUMA conf
        // create a default one with one guest numa node
        // and assign also offline vCPUs to it when CPU
        // hotplug or memory hotplug is supported.
        if (offlineCpus > 0 || vm.getMaxMemorySizeMb() > vm.getMemSizeMb()) {
            VmNumaNode vmNode = new VmNumaNode();
            vmNode.setIndex(0);
            vmNode.setMemTotal(vm.getMemSizeMb());
            for (int i = 0; i < vcpus; i++) {
                vmNode.getCpuIds().add(i);
            }
            return Collections.singletonList(vmNode);
        }

        // no need to send numa if memory hotplug not supported
        return Collections.emptyList();
    }

    public Map<String, Object> parseCpuPinning(String cpuPinning) {
        if (StringUtils.isEmpty(cpuPinning)) {
            return Collections.emptyMap();
        }
        return Arrays.stream(cpuPinning.split("_"))
                .map(pin -> pin.split("#"))
                .collect(Collectors.toMap(split -> split[0], split -> split[1]));
    }

    public boolean isNumaEnabled(MemoizingSupplier<List<VdsNumaNode>> hostNumaNodesSupplier,
            MemoizingSupplier<List<VmNumaNode>> vmNumaNodesSupplier, VM vm) {
        List<VdsNumaNode> hostNumaNodes = hostNumaNodesSupplier.get();
        if (hostNumaNodes.isEmpty()) {
            log.warn("No host NUMA nodes found for vm {} ({})", vm.getName(), vm.getId());
            return false;
        }

        List<VmNumaNode> vmNumaNodes = vmNumaNodesSupplier.get();
        if (vmNumaNodes.isEmpty()) {
            return false;
        }

        return true;
    }

    public String getMatchingNumaNode(Map<String, Object> numaTuneSetting,
            MemoizingSupplier<List<VmNumaNode>> vmNumaNodesSupplier, String preferredNode) {
        String node = null;
        if (numaTuneSetting != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> memNodes = (List<Map<String, String>>) numaTuneSetting.get(VdsProperties.NUMA_TUNE_MEMNODES);
            if (memNodes != null) {
                for (Map<String, String> memnode : memNodes) {
                    if (memnode.get(VdsProperties.NUMA_TUNE_NODESET).equals(preferredNode)) {
                        node = memnode.get(VdsProperties.NUMA_TUNE_VM_NODE_INDEX);
                        break;
                    }
                }
            }
        }
        if (node == null) {
            // Matching node not found, use any other node.
            List<Map<String, Object>> numaNodes = NumaSettingFactory.buildVmNumaNodeSetting(vmNumaNodesSupplier.get());
            if (!numaNodes.isEmpty()) {
                node = numaNodes.get(0).get(VdsProperties.NUMA_NODE_INDEX).toString();
            }
        }
        return node;
    }

    public String tpmData(Guid vmId) {
        return vmDao.getTpmData(vmId).getFirst();
    }

    public String nvramData(Guid vmId) {
        return vmDao.getNvramData(vmId).getFirst();
    }

    public List<VmDevice> getVmDevices(Guid vmId) {
        return vmDeviceDao.getVmDeviceByVmId(vmId);
    }

    public Network getNetwork(Guid networkId) {
        return networkDao.get(networkId);
    }

    public boolean isHypervEnabled(int osId, Version version) {
        return osRepository.isHypervEnabled(osId, version);
    }

    public String getCdInterface(int osId, Version version, ChipsetType chipset) {
        return osRepository.getCdInterface(osId, version, chipset);
    }

    public boolean isLegacyVirtio(int osId, ChipsetType chipset) {
        return osRepository.requiresLegacyVirtio(osId, chipset);
    }

    public boolean isOvirtGuestAgent(int osId) {
        return osRepository.requiresOvirtGuestAgentChannel(osId);
    }

    public boolean isKASLRDumpEnabled(int osId) {
        return osRepository.isLinux(osId) && Config.<Boolean> getValue(ConfigValues.EnableKASLRDump);
    }

    public List<VmNicFilterParameter> getAllNetworkFiltersForVmNic(Guid nicId)  {
        return vmNicFilterParameterDao.getAllForVmNic(nicId);
    }

    public VnicProfile getVnicProfile(Guid vnicProfileId) {
        return vnicProfileDao.get(vnicProfileId);
    }

    public List<VnicProfile> getAllVnicProfiles() {
        return vnicProfileDao.getAll();
    }

    public VdsStatistics getVdsStatistics(Guid hostId) {
        return vdsStatisticsDao.get(hostId);
    }

    public Map<String, HostDevice> getHostDevices(Guid hostId) {
        return hostDeviceDao.getHostDevicesByHostId(hostId)
                .stream()
                .collect(Collectors.toMap(HostDevice::getDeviceName, device -> device));
    }

    public void refreshVmDevices(Guid vmId) {
        vmDevicesMonitoring.refreshVmDevices(vmId);
    }

    public int pinToIoThreads(VM vm, int pinnedDriveIndex) {
        // simple round robin e.g. for 2 threads and 4 disks it will be pinned like this:
        // disk 0 -> iothread 1
        // disk 1 -> iothread 2
        // disk 2 -> iothread 1
        // disk 3 -> iothread 2
        return vm.getNumOfIoThreads() != 0 ? pinnedDriveIndex % vm.getNumOfIoThreads() + 1 : 0;
    }

    public int nextIoThreadToPinTo(VM vm) {
        if (vm.getNumOfIoThreads() == 0) {
            return 0;
        }
        List<DiskVmElement> diskVmElements = diskVmElementDao.getAllPluggedToVm(vm.getId());
        int numOfAttachedVirtioInterfaces = (int) diskVmElements.stream()
                .filter(dve -> dve.getDiskInterface() == DiskInterface.VirtIO)
                .count();
        return pinToIoThreads(vm, numOfAttachedVirtioInterfaces);
    }

    public void calculateAddressForScsiDisk(VM vm,
            Disk disk,
            VmDevice device,
            Map<Integer, Map<VmDevice, Integer>> vmDeviceSpaprVscsiUnitMap,
            Map<Integer, Map<VmDevice, Integer>> vmDeviceVirtioScsiUnitMap) {

        Map<DiskInterface, Integer> controllerIndexMap =
                ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new GetControllerIndices()).returnValue();
        int defaultSpaprVscsiControllerIndex = controllerIndexMap.get(DiskInterface.SPAPR_VSCSI);
        int defaultVirtioScsiControllerIndex = controllerIndexMap.get(DiskInterface.VirtIO_SCSI);

        switch (disk.getDiskVmElementForVm(vm.getId()).getDiskInterface()) {
        case SPAPR_VSCSI:
            if (StringUtils.isEmpty(device.getAddress())) {
                Integer unitIndex = vmDeviceSpaprVscsiUnitMap.get(defaultSpaprVscsiControllerIndex).get(device);
                device.setAddress(createAddressForScsiDisk(defaultSpaprVscsiControllerIndex, unitIndex).toString());
            }
            break;
        case VirtIO_SCSI:
            Integer unitIndex = null;
            int controllerIndex = defaultVirtioScsiControllerIndex;
            VmDevice deviceFromMap = device;
            for (Map.Entry<Integer, Map<VmDevice, Integer>> controllerToDevices : vmDeviceVirtioScsiUnitMap.entrySet()) {
                Optional<VmDevice> maybeDeviceFromMap = controllerToDevices.getValue().keySet().stream()
                        .filter(d -> d.getId().equals(device.getId()))
                        .findFirst();
                if (maybeDeviceFromMap.isPresent()) {
                    deviceFromMap = maybeDeviceFromMap.get();
                    controllerIndex = controllerToDevices.getKey();
                    unitIndex = controllerToDevices.getValue().get(deviceFromMap);
                    break;
                }
            }

            if (StringUtils.isEmpty(deviceFromMap.getAddress())) {
                if (unitIndex == null) {
                    // should never get here, but for safety having this fallback and generating a new unit id
                    unitIndex = getAvailableUnitForScsiDisk(vmDeviceVirtioScsiUnitMap.get(controllerIndex), false, false);
                    log.debug("The unit was null for disk '{}' on controller '{}', generating a new one '{}'", disk.getId(), controllerIndex, unitIndex);
                }
                device.setAddress(createAddressForScsiDisk(controllerIndex, unitIndex).toString());
            }
            break;
        default:
        }
    }

    public Map<String, String> createDiskUuidsMap(VM vm, String cdPath) {
        Matcher m = Pattern.compile(ValidationUtils.GUID).matcher(cdPath);
        m.find();
        Guid domainId = Guid.createGuidFromString(m.group());
        m.find();
        Guid imageId = Guid.createGuidFromString(m.group());
        m.find();
        Guid volumeId = Guid.createGuidFromString(m.group());
        return createDiskUuidsMap(vm.getStoragePoolId(), domainId, imageId, volumeId);
    }

    public Map<String, String> createDiskUuidsMap(DiskImage diskImage) {
        return createDiskUuidsMap(
                diskImage.getStoragePoolId(),
                diskImage.getStorageIds().get(0),
                diskImage.getId(),
                diskImage.getImageId());
    }

    private Map<String, String> createDiskUuidsMap(Guid poolId, Guid domainId, Guid imageId, Guid volumeId) {
        return PDIVMapBuilder.create()
                .setPoolId(poolId)
                .setDomainId(domainId)
                .setImageGroupId(imageId)
                .setVolumeId(volumeId).build();
    }

    public int getDefaultHugepageSize(VM vm) {
        switch(vm.getClusterArch().getFamily()) {
        case ppc:
            return DEFAULT_HUGEPAGESIZE_PPC64LE;
        default:
            return DEFAULT_HUGEPAGESIZE_X86_64;
        }
    }

    public String getVmSerialNumber(VM vm, String defaultValue) {
        String uuid = vmSerialNumberBuilder.buildVmSerialNumber(vm);
        return uuid != null ? uuid : defaultValue;
    }

    public int getOptimalNumOfQueuesPerVnic(int numOfCpus) {
        return multiQueueUtils.getOptimalNumOfQueuesPerVnic(numOfCpus);
    }

    public int getNumOfScsiQueues(int numOfDisks, int numOfCpus) {
        return multiQueueUtils.getNumOfScsiQueues(numOfDisks, numOfCpus);
    }

    public boolean isInterfaceQueuable(VmDevice vmDevice, VmNic vmNic) {
        return multiQueueUtils.isInterfaceQueuable(vmDevice, vmNic);
    }

    public boolean hasUsbController(VM vm) {
        List<VmDevice> vmUsbDevicesList = vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(
                vm.getId(),
                VmDeviceGeneralType.CONTROLLER,
                VmDeviceType.USB);
        return !(vmUsbDevicesList.size() == 1
                && UsbControllerModel.NONE.libvirtName.equals(vmUsbDevicesList.get(0).getSpecParams().get(VdsProperties.Model)));
    }

    private UsbControllerModel getUsbControllerModelForVm(VM vm) {
        return osRepository.getOsUsbControllerModel(
                vm.getVmOsId(),
                vm.getCompatibilityVersion(),
                vm.getBiosType().getChipsetType());
    }

    public boolean isTabletEnabled(VM vm) {
        return hasUsbController(vm) // when we have USB controller
                && getUsbControllerModelForVm(vm) != UsbControllerModel.NONE // when there is USB controller for this OS
                && vm.getGraphicsInfos().containsKey(GraphicsType.VNC); // and VNC is requested (VNC or SPICE+VNC)
    }

    public boolean shouldUseNativeIO(VM vm, DiskImage diskImage, VmDevice device) {
        StorageType storageType = diskImage.getStorageTypes().get(0);
        String diskType = getDiskType(vm, diskImage, device);
        return (!"file".equals(diskType) || (storageType == StorageType.GLUSTERFS
                && FeatureSupported.useNativeIOForGluster(vm.getCompatibilityVersion())))
                && device.getSnapshotId() == null;
        // marked as transient disk (file type) and uses cache when snapshotId is not null
        // so native io should not be used
    }

    public VgpuPlacement vgpuPlacement(Guid hostId) {
        return VgpuPlacement.forValue(vdsStaticDao.get(hostId).getVgpuPlacement());
    }

    public void setCinderDriverType(ManagedBlockStorageDisk disk) {
        Map<String, Object> conn_info = disk.getConnectionInfo();

        if (CinderVolumeDriver.RBD.getName().equals(conn_info.get(ManagedBlockStorageDisk.DRIVER_VOLUME_TYPE))) {
            disk.setCinderVolumeDriver(CinderVolumeDriver.RBD);
        } else {
            disk.setCinderVolumeDriver(CinderVolumeDriver.BLOCK);
        }
    }

    boolean isKernelFipsMode(VdsDynamic vds) {
        boolean fips = vds.isFipsEnabled();
        log.debug("Kernel FIPS - Guid: {} fips: {}", vds.getId(), fips);
        return fips;
    }

    /**
     * buildPayload will create a proper payload to pass to libvirt.
     * It supports cloud-init. Cloud init payload is supported before 4.0
     * vmInit - holding all the init data for a VM - hostname, script, root password etc.
     **/
    public Map<String, byte[]> buildPayloadCloudInit(VmInit vmInit) throws IOException {
        return new CloudInitHandler(vmInit).getFileData();
    }

    /**
     * buildPayload will create a proper payload to pass to libvirt.
     * It supports ignition. Ignition support is experimental, and is implicit.
     * vmInit - holding all the init data for a VM - hostname, script, root password etc.
     **/
    public Map<String, byte[]> buildPayloadIgnition(VmInit vmInit, Version ignitionVersion) throws IOException {
        return new IgnitionHandler(vmInit, ignitionVersion).getFileData();
    }

    VdsDynamic getVdsDynamic(Guid vdsGuid) {
        return vdsDynamicDao.get(vdsGuid);
    }

    private Long alignDown(Long size, Long alignment) {
        return (size / alignment) * alignment;
    }

    public Long getNvdimmAlignedSize(VM vm, HostDevice hostDevice) {
        // Beware: The sizes computed here cannot be changed otherwise data corruption/loss may happen!
        Map<String, Object> specParams = hostDevice.getSpecParams();
        final Long hotplugAlignment = new Long(vm.getClusterArch().getHotplugMemorySizeFactorMb() * 1024 * 1024);
        final Long nvdimmAlignment = (Long)specParams.getOrDefault(VdsProperties.ALIGN_SIZE, hotplugAlignment);
        // Libvirt performs size alignments, sometimes up rather than down, let's make an initial alignment down
        // here to be safe.
        Long size = alignDown((Long)specParams.get(VdsProperties.DEVICE_SIZE), hotplugAlignment);
        // Libvirt subtracts label size on POWER before checking for alignments.
        if (vm.getClusterArch().getFamily() == ArchitectureType.ppc) {
            size += NVDIMM_LABEL_SIZE;
        }
        // After QEMU subtracts label size from the NVDIMM size and aligns it down to the align size,
        // the resulting size must be aligned to hot plug memory size factor in order to make regular
        // memory hot plug working.
        // See https://github.com/qemu/qemu/blob/v5.0.0/hw/mem/nvdimm.c#L143 for the related QEMU computations.
        final Long qemu_memory_size = alignDown(size - NVDIMM_LABEL_SIZE, nvdimmAlignment);
        if (nvdimmAlignment != null && hotplugAlignment % nvdimmAlignment == 0) {
            // Additional alignment on hot plug size needed
            size -= qemu_memory_size % hotplugAlignment;
        } else if (nvdimmAlignment != null && nvdimmAlignment % hotplugAlignment != 0) {
            // We can't align this
            log.error("Memory ({}) or NVDIMM ({}) alignment not a power of 2", hotplugAlignment, nvdimmAlignment);
            return null;
        }  // else: NVDIMM alignment aligned with hot plug alignment, no adjustment needed
        return size;
    }

    public Long getNvdimmTotalSize(VM vm, MemoizingSupplier<Map<String, HostDevice>> hostDevicesSupplier) {
        long size = 0;
        for (VmDevice device : getVmDevices(vm.getId())) {
            if (device.isPlugged() && device.getType() == VmDeviceGeneralType.HOSTDEV) {
                HostDevice hostDevice = hostDevicesSupplier.get().get(device.getDevice());
                if (hostDevice != null && "nvdimm".equals(hostDevice.getCapability())) {
                    size += getNvdimmAlignedSize(vm, hostDevice);
                }
            }
        }
        return size;
    }

    public static int maxNumberOfVcpus(VM vm) {
        return FeatureSupported.supportedInConfig(ConfigValues.HotPlugCpuSupported, vm.getCompatibilityVersion(),
                vm.getClusterArch()) && !VmCpuCountHelper.isAutoPinning(vm) ? VmCpuCountHelper.calcMaxVCpu(vm, vm.getCompatibilityVersion())
                        : VmCpuCountHelper.getDynamicNumOfCpu(vm);
    }

    public static boolean isVmWithHighNumberOfX86Vcpus(VM vm) {
        return vm.getClusterArch().getFamily() == ArchitectureType.x86
                && (VmCpuCountHelper.isDynamicCpuTopologySet(vm) ?
                vm.getCurrentNumOfCpus() : maxNumberOfVcpus(vm)) >= VmCpuCountHelper.HIGH_NUMBER_OF_X86_VCPUS;
    }
}
