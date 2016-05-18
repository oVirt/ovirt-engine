package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.WindowsJavaTimezoneMapping;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class VmInfoBuilderBase {

    protected static final Logger log = LoggerFactory.getLogger(VmInfoBuilderBase.class);
    protected Map<String, Object> createInfo;
    protected VM vm;
    // IDE supports only 4 slots , slot 2 is preserved by VDSM to the CDROM
    protected int[] ideIndexSlots = new int[] { 0, 1, 3 };

    protected OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
    private Cluster cluster;

    private static class DiskImageByBootAndSnapshotComparator implements Comparator<Disk>, Serializable {
        private static final long serialVersionUID = 4732164571328497830L;
        private Guid vmId;

        DiskImageByBootAndSnapshotComparator(Guid vmId) {
            this.vmId = vmId;
        }

        @Override
        public int compare(Disk o1, Disk o2) {
            Boolean boot1 = o1.getDiskVmElementForVm(vmId).isBoot();
            Boolean boot2 = o2.getDiskVmElementForVm(vmId).isBoot();
            int bootResult = boot1.compareTo(boot2);
            if (bootResult == 0 && boot1) {
                return Boolean.compare(o2.isDiskSnapshot(), o1.isDiskSnapshot());
            }
            return bootResult;
        }
    }

    protected void buildVmProperties() {
        createInfo.put(VdsProperties.vm_guid, vm.getId().toString());
        createInfo.put(VdsProperties.vm_name, vm.getName());
        createInfo.put(VdsProperties.mem_size_mb, vm.getVmMemSizeMb());

        if (FeatureSupported.hotPlugMemory(vm.getCompatibilityVersion(), vm.getClusterArch())) {
            createInfo.put(VdsProperties.maxMemSize, VmCommonUtils.maxMemorySizeWithHotplugInMb(vm));
            createInfo.put(VdsProperties.maxMemSlots, Config.getValue(ConfigValues.MaxMemorySlots));
        }

        createInfo.put(VdsProperties.mem_guaranteed_size_mb, vm.getMinAllocatedMem());
        createInfo.put(VdsProperties.smartcardEnabled, Boolean.toString(vm.isSmartcardEnabled()));
        createInfo.put(VdsProperties.num_of_cpus, String.valueOf(vm.getNumOfCpus()));
        if (vm.getNumOfIoThreads() != 0) {
            createInfo.put(VdsProperties.numOfIoThreads, vm.getNumOfIoThreads());
        }

        if (Config.<Boolean> getValue(ConfigValues.SendSMPOnRunVm)) {
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
        final String compatibilityVersion = vm.getCompatibilityVersion().toString();
        addCpuPinning(compatibilityVersion);
        if(vm.getEmulatedMachine() != null) {
            createInfo.put(VdsProperties.emulatedMachine, vm.getEmulatedMachine());
        }

        createInfo.put(VdsProperties.kvmEnable, vm.getKvmEnable().toString()
                .toLowerCase());
        createInfo.put(VdsProperties.acpiEnable, vm.getAcpiEnable().toString()
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
        if (!StringUtils.isEmpty(vm.getHibernationVolHandle())) {
            createInfo.put(VdsProperties.hiberVolHandle,
                    vm.getHibernationVolHandle());
        }

        if (osRepository.isLinux(vm.getVmOsId())) {
            createInfo.put(VdsProperties.PitReinjection, "false");
        }

        if (vm.getGraphicsInfos().size() == 1 && vm.getGraphicsInfos().containsKey(GraphicsType.VNC)) {
            createInfo.put(VdsProperties.TabletEnable, "true");
        }
        createInfo.put(VdsProperties.transparent_huge_pages,
                vm.isTransparentHugePages() ? "true" : "false");

        if (osRepository.isHypervEnabled(vm.getVmOsId(), vm.getCompatibilityVersion())) {
            createInfo.put(VdsProperties.hypervEnable, "true");
        }
    }

    protected void addVmGraphicsOptions(Map<GraphicsType, GraphicsInfo> infos, Map<String, Object> params) {
        if (infos != null && infos.containsKey(GraphicsType.SPICE)) {
            params.put(VdsProperties.spiceFileTransferEnable,
                    Boolean.toString(vm.isSpiceFileTransferEnabled()));
            params.put(VdsProperties.spiceCopyPasteEnable,
                    Boolean.toString(vm.isSpiceCopyPasteEnabled()));

            if (Config.<Boolean>getValue(ConfigValues.SSLEnabled)) {
                params.put(VdsProperties.spiceSslCipherSuite,
                        Config.<String>getValue(ConfigValues.CipherSuite));
                params.put(VdsProperties.SpiceSecureChannels, Config.<String>getValue(
                        ConfigValues.SpiceSecureChannels, vm.getCompatibilityVersion().toString()));
            }
        }

        if (infos != null && infos.containsKey(GraphicsType.VNC)) {
            String keyboardLayout = vm.getDynamicData().getVncKeyboardLayout();
            if (keyboardLayout == null) {
                keyboardLayout = vm.getDefaultVncKeyboardLayout();
                if (keyboardLayout == null) {
                    keyboardLayout = Config.<String> getValue(ConfigValues.VncKeyboardLayout);
                }
            }

            params.put(VdsProperties.KeyboardMap, keyboardLayout);
        }
    }

    private Integer calcMaxVCpu() {
        Integer maxSockets = Config.<Integer>getValue(
                ConfigValues.MaxNumOfVmSockets,
                vm.getCompatibilityVersion().getValue());
        Integer maxVCpus = Config.<Integer>getValue(
                ConfigValues.MaxNumOfVmCpus,
                vm.getCompatibilityVersion().getValue());

        int threadsPerCore = vm.getThreadsPerCpu();
        int cpuPerSocket = vm.getCpuPerSocket();
        maxVCpus = cpuPerSocket * threadsPerCore *
                Math.min(maxSockets, maxVCpus / (cpuPerSocket * threadsPerCore));
        return maxVCpus;
    }

    private void addCpuPinning(final String compatibilityVersion) {
        final String cpuPinning = vm.getCpuPinning();
        if (StringUtils.isNotEmpty(cpuPinning)) {
            final Map<String, Object> pinDict = new HashMap<>();
            for (String pin : cpuPinning.split("_")) {
                final String[] split = pin.split("#");
                pinDict.put(split[0], split[1]);
            }
            createInfo.put(VdsProperties.cpuPinning, pinDict);
        }
    }

    protected void buildVmNetworkCluster() {
        // set Display network
        List<NetworkCluster> all = DbFacade.getInstance()
                .getNetworkClusterDao().getAllForCluster(vm.getClusterId());
        NetworkCluster networkCluster = null;
        for (NetworkCluster tempNetworkCluster : all) {
            if (tempNetworkCluster.isDisplay()) {
                networkCluster = tempNetworkCluster;
                break;
            }
        }
        if (networkCluster != null) {
            Network net = null;
            List<Network> allNetworks = DbFacade.getInstance().getNetworkDao()
                    .getAll();
            for (Network tempNetwork : allNetworks) {
                if (tempNetwork.getId().equals(networkCluster.getNetworkId())) {
                    net = tempNetwork;
                    break;
                }
            }
            if (net != null) {
                createInfo.put(VdsProperties.DISPLAY_NETWORK, net.getName());
            }
        }
    }

    protected void buildVmBootOptions() {
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

    protected void buildVmTimeZone() {
        // get vm timezone
        String timeZone = getTimeZoneForVm(vm);

        String javaZoneId = null;
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
        createInfo.put(VdsProperties.utc_diff, "" + offset);
    }

    private String getTimeZoneForVm(VM vm) {
        if (!StringUtils.isEmpty(vm.getTimeZone())) {
            return vm.getTimeZone();
        }

        // else fallback to engine config default for given OS type
        if (osRepository.isWindows(vm.getOs())) {
            return Config.<String> getValue(ConfigValues.DefaultWindowsTimeZone);
        } else {
            return "Etc/GMT";
        }
    }

    protected List<Disk> getSortedDisks() {
        // order first by drive numbers and then order by boot for the bootable
        // drive to be first (important for IDE to be index 0) !
        List<Disk> diskImages = new ArrayList<>(vm.getDiskMap()
                .values());
        Collections.sort(diskImages, new DiskByDiskAliasComparator());
        Collections.sort(diskImages,
                Collections.reverseOrder(new DiskImageByBootAndSnapshotComparator(vm.getId())));
        return diskImages;
    }

    protected void logUnsupportedInterfaceType() {
        log.error("Unsupported interface type, ISCSI interface type is not supported.");
    }

    protected static void reportUnsupportedVnicProfileFeatures(VM vm,
            VmNic nic,
            VnicProfile vnicProfile,
            List<VnicProfileProperties> unsupportedFeatures) {

        if (unsupportedFeatures.isEmpty()) {
            return;
        }

        AuditLogableBase event = new AuditLogableBase();
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

    protected static VmDevice getVmDeviceByDiskId(Guid diskId, Guid vmId) {
        // get vm device for this disk from DB
        return DbFacade.getInstance().getVmDeviceDao().get(new VmDeviceId(diskId, vmId));
    }

    public void buildVmSerialNumber() {
        new VmSerialNumberBuilder(vm, getCluster(), createInfo).buildVmSerialNumber();
    }

    protected abstract void buildVmVideoCards();

    protected abstract void buildVmGraphicsDevices();

    protected abstract void buildVmCD();

    protected abstract void buildVmFloppy();

    protected abstract void buildVmDrives();

    protected abstract void buildVmNetworkInterfaces();

    protected abstract void buildVmSoundDevices();

    protected abstract void buildVmConsoleDevice();

    protected abstract void buildUnmanagedDevices();

    protected abstract void buildVmBootSequence();

    protected abstract void buildSysprepVmPayload(String strSysPrepContent);

    protected abstract void buildCloudInitVmPayload(Map<String, byte[]> cloudInitContent);

    protected abstract void buildVmUsbDevices();

    protected abstract void buildVmMemoryBalloon();

    protected abstract void buildVmWatchdog();

    protected abstract void buildVmVirtioScsi();

    protected abstract void buildVmRngDevice();

    protected abstract void buildVmVirtioSerial();

    protected abstract void buildVmNumaProperties();

    protected abstract void buildVmHostDevices();

    protected static enum VnicProfileProperties {
        PORT_MIRRORING("Port Mirroring"),
        CUSTOM_PROPERTIES("Custom Properties"),
        NETWORK_QOS("Network QoS");

        private final String featureName;

        private VnicProfileProperties(String featureName) {
            this.featureName = featureName;
        }

        public String getFeatureName() {
            return featureName;
        }
    };

    protected Cluster getCluster() {
        if (cluster == null) {
            cluster = DbFacade.getInstance().getClusterDao().get(vm.getClusterId());
        }
        return cluster;
    }


    /**
     * Derives display type from vm configuration, used with legacy vdsm.
     * @return either "vnc" or "qxl" string or null if the vm is headless
     */
    protected String deriveDisplayTypeLegacy() {
        List<VmDevice> vmDevices =
            DbFacade.getInstance()
                    .getVmDeviceDao()
                    .getVmDeviceByVmIdAndType(vm.getId(),
                            VmDeviceGeneralType.GRAPHICS);

        if (vmDevices.isEmpty()) {
            return null;
        } else if (vmDevices.size() == 2) { // we have spice & vnc together, we prioritize SPICE
            return VdsProperties.QXL;
        }

        GraphicsType deviceType = GraphicsType.fromString(vmDevices.get(0).getDevice());
        return graphicsTypeToLegacyDisplayType(deviceType);
    }

    protected String graphicsTypeToLegacyDisplayType(GraphicsType graphicsType) {
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
