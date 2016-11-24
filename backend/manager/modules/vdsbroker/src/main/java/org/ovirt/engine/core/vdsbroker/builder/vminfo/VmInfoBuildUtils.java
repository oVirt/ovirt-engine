package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
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
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.StringMapUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IoTuneUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.NetworkQosMapper;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class VmInfoBuildUtils {
    private static final Logger log = LoggerFactory.getLogger(VmInfoBuildUtils.class);

    private static final String FIRST_MASTER_MODEL = "ich9-ehci1";

    private final NetworkDao networkDao;
    private final NetworkFilterDao networkFilterDao;
    private final NetworkQoSDao networkQosDao;
    private final StorageQosDao storageQosDao;
    private final VmDeviceDao vmDeviceDao;
    private final VnicProfileDao vnicProfileDao;

    @Inject
    VmInfoBuildUtils(
            NetworkDao networkDao,
            NetworkFilterDao networkFilterDao,
            NetworkQoSDao networkQosDao,
            StorageQosDao storageQosDao,
            VmDeviceDao vmDeviceDao,
            VnicProfileDao vnicProfileDao) {
        this.networkDao = Objects.requireNonNull(networkDao);
        this.networkFilterDao = Objects.requireNonNull(networkFilterDao);
        this.networkQosDao = Objects.requireNonNull(networkQosDao);
        this.storageQosDao = Objects.requireNonNull(storageQosDao);
        this.vmDeviceDao = Objects.requireNonNull(vmDeviceDao);
        this.vnicProfileDao = Objects.requireNonNull(vnicProfileDao);
    }

    OsRepository getOsRepository() {
        return SimpleDependencyInjector.getInstance().get(OsRepository.class);
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
        addBootOrder(vmDevice, struct);
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
        List<VnicProfileProperties> unsupportedFeatures = new ArrayList<>();
        if (nic.getVnicProfileId() != null) {
            vnicProfile = vnicProfileDao.get(nic.getVnicProfileId());
            if (vnicProfile != null) {
                network = networkDao.get(vnicProfile.getNetworkId());
                networkName = network.getName();
                log.debug("VNIC '{}' is using profile '{}' on network '{}'",
                        nic.getName(),
                        vnicProfile,
                        networkName);
                addQosForDevice(struct, vnicProfile);
            }
        }

        struct.put(VdsProperties.NETWORK, networkName);

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
        }
    }

    private NetworkFilter fetchVnicProfileNetworkFilter(VmNic vmNic) {
        if (vmNic.getVnicProfileId() != null) {
            VnicProfile vnicProfile = vnicProfileDao.get(vmNic.getVnicProfileId());
            if (vnicProfile != null) {
                final Guid networkFilterId = vnicProfile.getNetworkFilterId();
                return networkFilterId == null ? null : networkFilterDao.getNetworkFilterById(networkFilterId);
            }
        }
        return null;
    }

    void addFloppyDetails(VmDevice vmDevice, Map<String, Object> struct) {
        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());
        struct.put(VdsProperties.Index, "0"); // IDE slot 2 is reserved by VDSM to CDROM
        struct.put(VdsProperties.INTERFACE, VdsProperties.Fdc);
        struct.put(VdsProperties.ReadOnly, String.valueOf(vmDevice.getIsReadOnly()));
        struct.put(VdsProperties.Shareable, Boolean.FALSE.toString());
    }

    void addCdDetails(VmDevice vmDevice, Map<String, Object> struct, VM vm) {
        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());

        String cdInterface = getOsRepository().getCdInterface(
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
     * @return a map containing an appropriate unit (disk's index in VirtIO-SCSI controller) for each vm device.
     */
    public Map<VmDevice, Integer> getVmDeviceUnitMapForVirtioScsiDisks(VM vm) {
        return getVmDeviceUnitMapForScsiDisks(vm, DiskInterface.VirtIO_SCSI, false);
    }

    /**
     * @return a map containing an appropriate unit (disk's index in sPAPR VSCSI controller) for each vm device.
     */
    public Map<VmDevice, Integer> getVmDeviceUnitMapForSpaprScsiDisks(VM vm) {
        return getVmDeviceUnitMapForScsiDisks(vm, DiskInterface.SPAPR_VSCSI, true);
    }

    private Map<VmDevice, Integer> getVmDeviceUnitMapForScsiDisks(VM vm,
            DiskInterface scsiInterface,
            boolean reserveFirstTwoLuns) {
        List<Disk> disks = new ArrayList<>(vm.getDiskMap().values());
        Map<VmDevice, Integer> vmDeviceUnitMap = new HashMap<>();
        Map<VmDevice, Disk> vmDeviceDiskMap = new HashMap<>();

        for (Disk disk : disks) {
            DiskVmElement dve = disk.getDiskVmElementForVm(vm.getId());
            if (dve.getDiskInterface() == scsiInterface) {
                VmDevice vmDevice = getVmDeviceByDiskId(disk.getId(), vm.getId());
                Map<String, String> address = StringMapUtils.string2Map(vmDevice.getAddress());
                String unitStr = address.get(VdsProperties.Unit);

                // If unit property is available adding to 'vmDeviceUnitMap';
                // Otherwise, adding to 'vmDeviceDiskMap' for setting the unit property later.
                if (StringUtils.isNotEmpty(unitStr)) {
                    vmDeviceUnitMap.put(vmDevice, Integer.valueOf(unitStr));
                } else {
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

    public int getAvailableUnitForScsiDisk(Map<VmDevice, Integer> vmDeviceUnitMap, boolean reserveFirstTwoLuns) {
        int unit = reserveFirstTwoLuns ? 2 : 0;
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

    void addBootOrder(VmDevice vmDevice, Map<String, Object> struct) {
        String s = String.valueOf(vmDevice.getBootOrder());
        if (!StringUtils.isEmpty(s) && !s.equals("0")) {
            struct.put(VdsProperties.BootOrder, s);
        }
    }

    private void reportUnsupportedVnicProfileFeatures(VM vm,
            VmNic nic,
            VnicProfile vnicProfile,
            List<VnicProfileProperties> unsupportedFeatures) {

        if (unsupportedFeatures.isEmpty()) {
            return;
        }

        AuditLogableBase event = Injector.injectMembers(new AuditLogableBase());
        event.setVmId(vm.getId());
        event.setClusterId(vm.getClusterId());
        event.setCustomId(nic.getId().toString());
        event.setCompatibilityVersion(vm.getCompatibilityVersion().toString());
        event.addCustomValue("NicName", nic.getName());
        event.addCustomValue("VnicProfile", vnicProfile == null ? null : vnicProfile.getName());
        String[] unsupportedFeatureNames = new String[unsupportedFeatures.size()];
        for (int i = 0; i < unsupportedFeatures.size(); i++) {
            unsupportedFeatureNames[i] = unsupportedFeatures.get(i).getFeatureName();
        }

        event.addCustomValue("UnsupportedFeatures", StringUtils.join(unsupportedFeatureNames, ", "));
        new AuditLogDirector().log(event, AuditLogType.VNIC_PROFILE_UNSUPPORTED_FEATURES);
    }

}
