package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.archstrategy.ArchStrategyFactory;
import org.ovirt.engine.core.vdsbroker.architecture.CreateAdditionalControllers;
import org.ovirt.engine.core.vdsbroker.architecture.GetControllerIndices;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStringUtils;

@SuppressWarnings({"rawtypes", "unchecked"})
public class VmInfoBuilder extends VmInfoBuilderBase {

    private static final String SYSPREP_FILE_NAME = "sysprep.inf";
    private static final String DEVICES = "devices";
    private static final String USB_BUS = "usb";
    private final static String FIRST_MASTER_MODEL = "ich9-ehci1";
    private static final String CLOUD_INIT_VOL_ID = "config-2";

    private final List<Map<String, Object>> devices = new ArrayList<Map<String, Object>>();
    private List<VmDevice> managedDevices = null;
    private final boolean hasNonDefaultBootOrder;

    public VmInfoBuilder(VM vm, Map createInfo) {
        this.vm = vm;
        this.createInfo = createInfo;
        hasNonDefaultBootOrder = (vm.getBootSequence() != vm.getDefaultBootSequence());
        if (hasNonDefaultBootOrder) {
            managedDevices = new ArrayList<VmDevice>();
        }
    }

    @Override
    protected void buildVmVideoCards() {
        createInfo.put(VdsProperties.display, vm.getDisplayType().toString());
        // the requested display type might be different than the default display of
        // the VM in Run Once scenario, in that case we need to add proper video device.
        if (vm.getDisplayType() != vm.getDefaultDisplayType()) {
            addVideoCardByDisplayType(vm.getDisplayType(), vm.getNumOfMonitors(), vm.getSingleQxlPci());
        }
        else {
            addVideoCardsDefinedForVmInDB(vm.getId());
        }
    }

    /**
     * Add video device according to the given display type
     */
    private void addVideoCardByDisplayType(DisplayType displayType, int numOfMonitors, boolean singleQxlPci) {
        Map<String, Object> struct = new HashMap<String, Object>();
        VmDeviceType vmDeviceType = getOsRepository().getDisplayDevice(vm.getOs(), vm.getVdsGroupCompatibilityVersion(), displayType);
        // create a monitor as an unmanaged device
        struct.put(VdsProperties.Type, VmDeviceGeneralType.VIDEO.getValue());
        struct.put(VdsProperties.Device, vmDeviceType.getName());
        struct.put(VdsProperties.SpecParams, getNewMonitorSpecParams(displayType, numOfMonitors, singleQxlPci));
        struct.put(VdsProperties.DeviceId, String.valueOf(Guid.newGuid()));
        devices.add(struct);
    }

    private static OsRepository getOsRepository() {
        return SimpleDependecyInjector.getInstance().get(OsRepository.class);
    }

    /**
     * Add the video cards defined for the VM with the given id in the DB
     */
    private void addVideoCardsDefinedForVmInDB(Guid vmId) {
        List<VmDevice> vmVideoDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdAndType(vmId, VmDeviceGeneralType.VIDEO);
        for (VmDevice vmVideoDevice : vmVideoDevices) {
            // skip unmanaged devices (handled separately)
            if (!vmVideoDevice.getIsManaged()) {
                continue;
            }

            Map<String, Object> struct = new HashMap<String, Object>();
            struct.put(VdsProperties.Type, vmVideoDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmVideoDevice.getDevice());
            addAddress(vmVideoDevice, struct);
            struct.put(VdsProperties.SpecParams, vmVideoDevice.getSpecParams());
            struct.put(VdsProperties.DeviceId, String.valueOf(vmVideoDevice.getId().getDeviceId()));
            addToManagedDevices(vmVideoDevice);
            devices.add(struct);
        }
    }

    @Override
    protected void buildVmCD() {
        Map<String, Object> struct;
        // check if we have payload CD
        if (vm.getVmPayload() != null && vm.getVmPayload().getType() == VmDeviceType.CDROM) {
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                            VmDeviceGeneralType.DISK,
                            VmDeviceType.CDROM.getName(),
                            "",
                            0,
                            vm.getVmPayload().getSpecParams(),
                            true,
                            true,
                            true,
                            "",
                            null,
                            null);
            struct = new HashMap<String, Object>();
            addCdDetails(vmDevice, struct, vm);
            addDevice(struct, vmDevice, "");
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
                            null);
            struct = new HashMap<String, Object>();
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
                struct = new HashMap<String, Object>();
                String cdPath = vm.getCdPath();
                addCdDetails(vmDevice, struct, vm);
                addAddress(vmDevice, struct);
                addDevice(struct, vmDevice, cdPath == null ? "" : cdPath);
            }
        }
    }

    @Override
    protected void buildVmFloppy() {
        // check if we have payload CD
        if (vm.getVmPayload() != null && vm.getVmPayload().getType() == VmDeviceType.FLOPPY) {
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                            VmDeviceGeneralType.DISK,
                            VmDeviceType.FLOPPY.getName(),
                            "",
                            0,
                            (vm.getVmPayload() == null) ? null : vm.getVmPayload().getSpecParams(),
                            true,
                            true,
                            true,
                            "",
                            null,
                            null);
            Map<String, Object> struct = new HashMap<String, Object>();
            addCdDetails(vmDevice, struct, vm);
            addDevice(struct, vmDevice, "");
        }
        // check first if Floppy was given as a parameter
        else if (vm.isRunOnce() && !StringUtils.isEmpty(vm.getFloppyPath())) {
            VmDevice vmDevice =
                    new VmDevice(new VmDeviceId(Guid.newGuid(), vm.getId()),
                            VmDeviceGeneralType.DISK,
                            VmDeviceType.FLOPPY.getName(),
                            "",
                            0,
                            (vm.getVmPayload() == null) ? null : vm.getVmPayload().getSpecParams(),
                            true,
                            true,
                            true,
                            "",
                            null,
                            null);
            Map<String, Object> struct = new HashMap<String, Object>();
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
                // more then one device mean that we have payload and should use it
                // instead of the blank cd
                if (!VmPayload.isPayload(vmDevice.getSpecParams()) && vmDevices.size() > 1) {
                    continue;
                }
                Map<String, Object> struct = new HashMap<String, Object>();
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

        for (Disk disk : disks) {
            Map<String, Object> struct = new HashMap<String, Object>();
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
                    struct.put(VdsProperties.Index, 0);
                }
                addAddress(vmDevice, struct);
                if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage diskImage = (DiskImage) disk;
                    struct.put(VdsProperties.PoolId, diskImage.getStoragePoolId().toString());
                    struct.put(VdsProperties.DomainId, diskImage.getStorageIds().get(0).toString());
                    struct.put(VdsProperties.ImageId, diskImage.getId().toString());
                    struct.put(VdsProperties.VolumeId, diskImage.getImageId().toString());
                    struct.put(VdsProperties.Format, diskImage.getVolumeFormat().toString()
                            .toLowerCase());
                    struct.put(VdsProperties.PropagateErrors, disk.getPropagateErrors().toString()
                            .toLowerCase());
                } else {
                    LunDisk lunDisk = (LunDisk) disk;
                    struct.put(VdsProperties.Guid, lunDisk.getLun().getLUN_id());
                    struct.put(VdsProperties.Format, VolumeFormat.RAW.toString().toLowerCase());
                    struct.put(VdsProperties.PropagateErrors, PropagateErrors.Off.toString()
                            .toLowerCase());
                }

                addBootOrder(vmDevice, struct);
                struct.put(VdsProperties.Shareable,
                        (vmDevice.getSnapshotId() != null && FeatureSupported.hotPlugDiskSnapshot(vm.getVdsGroupCompatibilityVersion())) ? VdsProperties.Transient
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

    @Override
    protected void buildVmNetworkInterfaces() {
        Map<VmDeviceId, VmDevice> devicesByDeviceId =
                Entities.businessEntitiesById(DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdTypeAndDevice(vm.getId(),
                                VmDeviceGeneralType.INTERFACE,
                                VmDeviceType.BRIDGE.getName()));

        for (VmNic vmInterface : vm.getInterfaces()) {
            // get vm device for this disk from DB
            VmDevice vmDevice =
                    devicesByDeviceId.get(new VmDeviceId(vmInterface.getId(), vmInterface.getVmId()));

            if (vmDevice != null && vmDevice.getIsManaged() && vmDevice.getIsPlugged()) {

                Map struct = new HashMap();
                VmInterfaceType ifaceType = VmInterfaceType.rtl8139;

                if (vmInterface.getType() != null) {
                    ifaceType = VmInterfaceType.forValue(vmInterface.getType());
                }

                addNetworkInterfaceProperties(struct,
                        vmInterface,
                        vmDevice,
                        VmInfoBuilder.evaluateInterfaceType(ifaceType, vm.getHasAgent()),
                        vm.getVdsGroupCompatibilityVersion());
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
        buildVmDevicesFromDb(VmDeviceGeneralType.SOUND, true);
    }

    @Override
    protected void buildVmConsoleDevice() {
        buildVmDevicesFromDb(VmDeviceGeneralType.CONSOLE, false);
    }

    private void buildVmDevicesFromDb(VmDeviceGeneralType generalType, boolean addAddress) {
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getVmDeviceByVmIdAndType(vm.getId(),
                                generalType);

        for (VmDevice vmDevice : vmDevices) {
            Map struct = new HashMap();
            struct.put(VdsProperties.Type, vmDevice.getType().getValue());
            struct.put(VdsProperties.Device, vmDevice.getDevice());
            struct.put(VdsProperties.SpecParams, vmDevice.getSpecParams());
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
                (Map<String, String>) createInfo.get(VdsProperties.Custom) : new HashMap<String, String>();
        List<VmDevice> vmDevices =
                DbFacade.getInstance()
                        .getVmDeviceDao()
                        .getUnmanagedDevicesByVmId(vm.getId());
        if (vmDevices.size() > 0) {
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
            VmDeviceCommonUtils.updateVmDevicesBootOrder(vm,
                    managedDevices,
                    VmDeviceCommonUtils.isOldClusterVersion(vm.getVdsGroupCompatibilityVersion()));
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
        vmPayload.setType(VmDeviceType.FLOPPY);
        vmPayload.getFiles().put(SYSPREP_FILE_NAME, Base64.encodeBase64String(sysPrepContent.getBytes()));

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
                        null);
        Map<String, Object> struct = new HashMap<String, Object>();
        addFloppyDetails(vmDevice, struct);
        addDevice(struct, vmDevice, vm.getFloppyPath());
    }

    @Override
    protected void buildCloudInitVmPayload(Map<String, byte[]> cloudInitContent) {
        VmPayload vmPayload = new VmPayload();
        vmPayload.setType(VmDeviceType.CDROM);
        vmPayload.setVolumeId(CLOUD_INIT_VOL_ID);
        for (Map.Entry<String, byte[]> entry : cloudInitContent.entrySet()) {
            vmPayload.getFiles().put(entry.getKey(), Base64.encodeBase64String(entry.getValue()));
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
                        null);
        Map<String, Object> struct = new HashMap<String, Object>();
        addCdDetails(vmDevice, struct, vm);
        addDevice(struct, vmDevice, "");
    }

    private static void addBootOrder(VmDevice vmDevice, Map<String, Object> struct) {
        String s = String.valueOf(vmDevice.getBootOrder());
        if (!org.apache.commons.lang.StringUtils.isEmpty(s) && !s.equals("0")) {
            struct.put(VdsProperties.BootOrder, s);
        }
    }

    private static void addAddress(VmDevice vmDevice, Map<String, Object> struct) {
        Map<String, String> addressMap = XmlRpcStringUtils.string2Map(vmDevice.getAddress());
        if (addressMap.size() > 0) {
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

    public static void addProfileDataToNic(Map<String, Object> struct,
            VM vm,
            VmDevice vmDevice,
            VmNic nic) {
        VnicProfile vnicProfile = null;
        Network network = null;
        String networkName = "";
        List<VNIC_PROFILE_PROPERTIES> unsupportedFeatures = new ArrayList<>();
        if (nic.getVnicProfileId() != null) {
            vnicProfile = DbFacade.getInstance().getVnicProfileDao().get(nic.getVnicProfileId());
            if (vnicProfile != null) {
                network = DbFacade.getInstance().getNetworkDao().get(vnicProfile.getNetworkId());
                networkName = network.getName();
                log.debugFormat("VNIC {0} is using profile {1} on network {2}",
                        nic.getName(), vnicProfile, networkName);
                if (!addQosForDevice(struct, vnicProfile, vm.getVdsGroupCompatibilityVersion())) {
                    unsupportedFeatures.add(VNIC_PROFILE_PROPERTIES.NETWORK_QOS);
                }
            }
        }

        struct.put(VdsProperties.NETWORK, networkName);

        if (!addPortMirroringToVmInterface(struct, vnicProfile, vm.getVdsGroupCompatibilityVersion(), network)) {
            unsupportedFeatures.add(VNIC_PROFILE_PROPERTIES.PORT_MIRRORING);
        }

        if (!addCustomPropertiesForDevice(struct,
                vm,
                vmDevice,
                vm.getVdsGroupCompatibilityVersion(),
                getVnicCustomProperties(vnicProfile))) {
            unsupportedFeatures.add(VNIC_PROFILE_PROPERTIES.CUSTOM_PROPERTIES);
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
            Version vdsGroupCompatibilityVersion) {

        Guid qosId = vnicProfile.getNetworkQosId();
        if (!FeatureSupported.networkQoS(vdsGroupCompatibilityVersion)) {
            return qosId == null;
        }

        Map<String, Object> specParams = (Map<String, Object>) struct.get(VdsProperties.SpecParams);
        if (specParams == null) {
            specParams = new HashMap<>();
            struct.put(VdsProperties.SpecParams, specParams);
        }
        NetworkQoS networkQoS = (qosId == null) ? new NetworkQoS() : DbFacade.getInstance().getQosDao().get(qosId);
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

        return customProperties == null ? new HashMap<String, String>() : customProperties;
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
                vm.getVdsGroupCompatibilityVersion());

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
            // 3 is magic number for payload - we are using it as hdd
            struct.put(VdsProperties.Index, "3");
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

    private static HashMap<String, Object> getNewMonitorSpecParams(DisplayType displayType, int numOfMonitors, boolean singleQxlPci) {
        HashMap<String, Object> specParams = new HashMap<String, Object>();
        specParams.put("vram", String.valueOf(VmDeviceCommonUtils.LOW_VIDEO_MEM));
        specParams.put("heads", String.valueOf(numOfMonitors));
        specParams.put("vram", VmDeviceCommonUtils.singlePciVRamByHeads(numOfMonitors));
        if (displayType == DisplayType.qxl && singleQxlPci) {
            specParams.put("ram", VmDeviceCommonUtils.singlePciRamByHeads(numOfMonitors));
        }
        return specParams;
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
            Map<String, Object> specParams = new HashMap<String, Object>();
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
        Map<String, Object> struct = new HashMap<String, Object>();
        struct.put(VdsProperties.Type, vmDevice.getType().getValue());
        struct.put(VdsProperties.Device, vmDevice.getDevice());
        Map<String, Object> specParams = vmDevice.getSpecParams();
        // validate & set spec params for balloon device
        if (specParams == null) {
            specParams = new HashMap<String, Object>();
            vmDevice.setSpecParams(specParams);
        }
        specParams.put(VdsProperties.Model, VdsProperties.Virtio);
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
                specParams = new HashMap<String, Object>();
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
            boolean reserveFirstLun) {
        List<Disk> disks = new ArrayList<Disk>(vm.getDiskMap().values());
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
            int unit = getAvailableUnitForScsiDisk(vmDeviceUnitMap, reserveFirstLun);
            vmDeviceUnitMap.put(entry.getKey(), unit);
        }

        return vmDeviceUnitMap;
    }

    public static int getAvailableUnitForScsiDisk(Map<VmDevice, Integer> vmDeviceUnitMap, boolean reserveFirstLun) {
        int unit = reserveFirstLun ? 1 : 0;
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
}
