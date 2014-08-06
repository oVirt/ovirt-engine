package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.comparators.DiskImageByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.WindowsJavaTimezoneMapping;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.NumaUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public abstract class VmInfoBuilderBase {

    protected static final Log log = LogFactory.getLog(VmInfoBuilderBase.class);
    protected Map<String, Object> createInfo;
    protected VM vm;
    // IDE supports only 4 slots , slot 2 is preserved by VDSM to the CDROM
    protected int[] ideIndexSlots = new int[] { 0, 1, 3 };

    protected OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);
    private VDSGroup vdsGroup;

    private static class DiskImageByBootAndSnapshotComparator implements Comparator<Disk>, Serializable {
        private static final long serialVersionUID = 4732164571328497830L;

        @Override
        public int compare(Disk o1, Disk o2) {
            Boolean boot1 = o1.isBoot();
            Boolean boot2 = o2.isBoot();
            int bootResult = boot1.compareTo(boot2);
            if (bootResult == 0 && o1.isBoot()) {
                return Boolean.compare(o2.isDiskSnapshot(), o1.isDiskSnapshot());
            }
            return bootResult;
        }
    }

    protected void buildVmProperties() {
        createInfo.put(VdsProperties.vm_guid, vm.getId().toString());
        createInfo.put(VdsProperties.vm_name, vm.getName());
        createInfo.put(VdsProperties.mem_size_mb, vm.getVmMemSizeMb());
        createInfo.put(VdsProperties.mem_guaranteed_size_mb, vm.getMinAllocatedMem());
        createInfo.put(VdsProperties.smartcardEnabled, Boolean.toString(vm.isSmartcardEnabled()));
        createInfo.put(VdsProperties.num_of_cpus, String.valueOf(vm.getNumOfCpus()));
        if (Config.<Boolean> getValue(ConfigValues.SendSMPOnRunVm)) {
            createInfo.put(VdsProperties.cores_per_socket, (Integer.toString(vm.getCpuPerSocket())));
            if (FeatureSupported.supportedInConfig(
                    ConfigValues.HotPlugCpuSupported,
                    vm.getVdsGroupCompatibilityVersion(),
                    vm.getClusterArch())) {
                Integer maxSockets = Config.<Integer>getValue(
                        ConfigValues.MaxNumOfVmSockets,
                        vm.getVdsGroupCompatibilityVersion().getValue());
                Integer maxVCpus = Config.<Integer>getValue(
                        ConfigValues.MaxNumOfVmCpus,
                        vm.getVdsGroupCompatibilityVersion().getValue());
                maxVCpus = Math.min(maxVCpus, (vm.getCpuPerSocket() * maxSockets));
                createInfo.put(
                        VdsProperties.max_number_of_cpus,
                        maxVCpus.toString());
            }
        }
        final String compatibilityVersion = vm.getVdsGroupCompatibilityVersion().toString();
        addCpuPinning(compatibilityVersion);
        createInfo.put(VdsProperties.emulatedMachine, getVdsGroup().getEmulatedMachine());
        // send cipher suite and spice secure channels parameters only if ssl
        // enabled.
        if (Config.<Boolean> getValue(ConfigValues.SSLEnabled)) {
            createInfo.put(VdsProperties.spiceSslCipherSuite,
                    Config.<String> getValue(ConfigValues.CipherSuite));
            createInfo.put(VdsProperties.SpiceSecureChannels, Config.<String> getValue(
                    ConfigValues.SpiceSecureChannels, compatibilityVersion));
        }
        createInfo.put(VdsProperties.kvmEnable, vm.getKvmEnable().toString()
                .toLowerCase());
        createInfo.put(VdsProperties.acpiEnable, vm.getAcpiEnable().toString()
                .toLowerCase());
        createInfo.put(VdsProperties.BOOT_MENU_ENABLE, Boolean.toString(vm.isBootMenuEnabled()));

        createInfo.put(VdsProperties.Custom,
                VmPropertiesUtils.getInstance().getVMProperties(vm.getVdsGroupCompatibilityVersion(),
                        vm.getStaticData()));
        createInfo.put(VdsProperties.vm_type, "kvm"); // "qemu", "kvm"
        if (vm.isRunAndPause()) {
            createInfo.put(VdsProperties.launch_paused_param, "true");
        }
        if (vm.isUseHostCpuFlags()) {
            createInfo.put(VdsProperties.cpuType,
                    "hostPassthrough");
        } else if (vm.getVdsGroupCpuFlagsData() != null) {
            createInfo.put(VdsProperties.cpuType,
                    vm.getVdsGroupCpuFlagsData());
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

        String keyboardLayout = vm.getDynamicData().getVncKeyboardLayout();
        if (keyboardLayout == null) {
            keyboardLayout = vm.getDefaultVncKeyboardLayout();
            if (keyboardLayout == null) {
                keyboardLayout = Config.<String> getValue(ConfigValues.VncKeyboardLayout);
            }
        }

        createInfo.put(VdsProperties.KeyboardLayout, keyboardLayout);
        if (osRepository.isLinux(vm.getVmOsId())) {
            createInfo.put(VdsProperties.PitReinjection, "false");
        }

        if (vm.getDisplayType() == DisplayType.vnc) {
            createInfo.put(VdsProperties.TabletEnable, "true");
        }
        createInfo.put(VdsProperties.transparent_huge_pages,
                vm.isTransparentHugePages() ? "true" : "false");

        addNumaSetting(compatibilityVersion);

        if (vm.getDisplayType() == DisplayType.qxl) {
            createInfo.put(VdsProperties.spiceFileTransferEnable,
                Boolean.toString(vm.isSpiceFileTransferEnabled()));
            createInfo.put(VdsProperties.spiceCopyPasteEnable,
                Boolean.toString(vm.isSpiceCopyPasteEnabled()));
        }

        if (osRepository.isHypervEnabled(vm.getVmOsId(), vm.getVdsGroupCompatibilityVersion())) {
            createInfo.put(VdsProperties.hypervEnable, "true");
        }
    }

    private void addCpuPinning(final String compatibilityVersion) {
        final String cpuPinning = vm.getCpuPinning();
        if (StringUtils.isNotEmpty(cpuPinning)
                && Boolean.TRUE.equals(Config.<Boolean> getValue(ConfigValues.CpuPinningEnabled,
                        compatibilityVersion))) {
            final Map<String, Object> pinDict = new HashMap<String, Object>();
            for (String pin : cpuPinning.split("_")) {
                final String[] split = pin.split("#");
                pinDict.put(split[0], split[1]);
            }
            createInfo.put(VdsProperties.cpuPinning, pinDict);
        }
    }

    /**
     * Numa will use the same compatibilityVersion as cpu pinning since
     * numa may also add cpu pinning configuration and the two features
     * have almost the same libvirt version support
     *
     * @param compatibilityVersion
     */
    private void addNumaSetting(final String compatibilityVersion) {
        if (Boolean.TRUE.equals(Config.<Boolean> getValue(ConfigValues.CpuPinningEnabled,
                        compatibilityVersion))) {
            NumaTuneMode numaTune = vm.getNumaTuneMode() == null ? NumaTuneMode.PREFERRED : vm.getNumaTuneMode();
            List<VmNumaNode> vmNumaNodes = DbFacade.getInstance().getVmNumaNodeDAO().getAllVmNumaNodeByVmId(vm.getId());
            List<VdsNumaNode> totalVdsNumaNodes = DbFacade.getInstance().getVdsNumaNodeDAO()
                    .getAllVdsNumaNodeByVdsId(vm.getRunOnVds());
            List<Integer> totalVdsNumaNodesIndexes = NumaUtils.getNodeIndexList(totalVdsNumaNodes);
            Map<String, Object> createNumaTune = new HashMap<>(2);
            createNumaTune.put(VdsProperties.NUMA_TUNE_MODE, numaTune.getValue());
            boolean useAllVdsNodesMem = false;
            Set<Integer> vmNumaNodePinInfo = new HashSet<>();
            if (!vmNumaNodes.isEmpty()) {
                List<Map<String, Object>> createVmNumaNodes = new ArrayList<>();
                for (VmNumaNode node : vmNumaNodes) {
                    Map<String, Object> createVmNumaNode = new HashMap<>();
                    createVmNumaNode.put(VdsProperties.NUMA_NODE_CPU_LIST, NumaUtils.buildStringFromListForNuma(node.getCpuIds()));
                    createVmNumaNode.put(VdsProperties.VM_NUMA_NODE_MEM, String.valueOf(node.getMemTotal()));
                    createVmNumaNodes.add(createVmNumaNode);
                    if (node.getVdsNumaNodeList().isEmpty()) {
                        useAllVdsNodesMem = true;
                    }
                    else {
                        vmNumaNodePinInfo.addAll(NumaUtils.getPinnedNodeIndexList(node.getVdsNumaNodeList()));
                    }
                }
                createInfo.put(VdsProperties.VM_NUMA_NODES, createVmNumaNodes);
            }
            else {
                useAllVdsNodesMem = true;
            }
            if (useAllVdsNodesMem) {
                if (!totalVdsNumaNodesIndexes.isEmpty()) {
                    createNumaTune.put(VdsProperties.NUMA_TUNE_NODESET,
                            NumaUtils.buildStringFromListForNuma(totalVdsNumaNodesIndexes));
                }
            }
            else {
                if (!vmNumaNodePinInfo.isEmpty()) {
                    createNumaTune.put(VdsProperties.NUMA_TUNE_NODESET,
                            NumaUtils.buildStringFromListForNuma(vmNumaNodePinInfo));
                }
            }
            createInfo.put(VdsProperties.NUMA_TUNE, createNumaTune);
            if (StringUtils.isEmpty(vm.getCpuPinning())) {
                Map<String, Object> cpuPinDict = addCpuPinningForNumaSetting(vmNumaNodes, totalVdsNumaNodes);
                if (!cpuPinDict.isEmpty()) {
                    createInfo.put(VdsProperties.cpuPinning, cpuPinDict);
                }
            }
        }
    }

    private Map<String, Object> addCpuPinningForNumaSetting(List<VmNumaNode> vmNodes, List<VdsNumaNode> vdsNodes) {
        Map<Integer, List<Integer>> vdsNumaNodeCpus = new HashMap<>();
        Map<String, Object> cpuPinDict = new HashMap<>();
        for (VdsNumaNode node : vdsNodes) {
            vdsNumaNodeCpus.put(node.getIndex(), node.getCpuIds());
        }
        for (VmNumaNode node : vmNodes) {
            List<Integer> pinnedNodeIndexes = NumaUtils.getPinnedNodeIndexList(node.getVdsNumaNodeList());
            if (!pinnedNodeIndexes.isEmpty()) {
                Set <Integer> totalPinnedVdsCpus = new LinkedHashSet<>();
                for (Integer vCpu : node.getCpuIds()) {
                    for (Integer pinnedVdsNode : pinnedNodeIndexes) {
                        if (vdsNumaNodeCpus.containsKey(pinnedVdsNode)) {
                            totalPinnedVdsCpus.addAll(vdsNumaNodeCpus.get(pinnedVdsNode));
                        }
                    }
                    cpuPinDict.put(String.valueOf(vCpu), NumaUtils.buildStringFromListForNuma(totalPinnedVdsCpus));
                }
            }
        }
        return cpuPinDict;
    }

    protected void buildVmNetworkCluster() {
        // set Display network
        List<NetworkCluster> all = DbFacade.getInstance()
                .getNetworkClusterDao().getAllForCluster(vm.getVdsGroupId());
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
            javaZoneId = WindowsJavaTimezoneMapping.windowsToJava.get(timeZone);
        } else {
            javaZoneId = timeZone;
        }

        int offset = 0;
        if (javaZoneId != null) {
            offset = (TimeZone.getTimeZone(javaZoneId).getOffset(
                    new Date().getTime()) / 1000);
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
        List<Disk> diskImages = new ArrayList<Disk>(vm.getDiskMap()
                .values());
        Collections.sort(diskImages, new DiskImageByDiskAliasComparator());
        Collections.sort(diskImages,
                Collections.reverseOrder(new DiskImageByBootAndSnapshotComparator()));
        return diskImages;
    }

    protected void logUnsupportedInterfaceType() {
        log.error("Unsupported interface type, ISCSI interface type is not supported.");
    }

    protected static void reportUnsupportedVnicProfileFeatures(VM vm,
            VmNic nic,
            VnicProfile vnicProfile,
            List<VNIC_PROFILE_PROPERTIES> unsupportedFeatures) {

        if (unsupportedFeatures.isEmpty()) {
            return;
        }

        AuditLogableBase event = new AuditLogableBase();
        event.setVmId(vm.getId());
        event.setVdsGroupId(vm.getVdsGroupId());
        event.setCustomId(nic.getId().toString());
        event.setCompatibilityVersion(vm.getVdsGroupCompatibilityVersion().toString());
        event.addCustomValue("NicName", nic.getName());
        event.addCustomValue("VnicProfile", vnicProfile == null ? null : vnicProfile.getName());
        String[] unsupportedFeatureNames = new String[unsupportedFeatures.size()];
        for (int i = 0; i < unsupportedFeatures.size(); i++) {
            unsupportedFeatureNames[i] = unsupportedFeatures.get(i).getFeatureName();
        }

        event.addCustomValue("UnsupportedFeatures", StringUtils.join(unsupportedFeatureNames, ", "));
        AuditLogDirector.log(event, AuditLogType.VNIC_PROFILE_UNSUPPORTED_FEATURES);
    }

    protected static VmDevice getVmDeviceByDiskId(Guid diskId, Guid vmId) {
        // get vm device for this disk from DB
        return DbFacade.getInstance().getVmDeviceDao().get(new VmDeviceId(diskId, vmId));
    }

    public void buildVmSerialNumber() {
        new VmSerialNumberBuilder(vm, getVdsGroup(), createInfo).buildVmSerialNumber();
    }

    protected abstract void buildVmVideoCards();

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

    protected static enum VNIC_PROFILE_PROPERTIES {
        PORT_MIRRORING("Port Mirroring"),
        CUSTOM_PROPERTIES("Custom Properties"),
        NETWORK_QOS("Network QoS");

        private final String featureName;

        private VNIC_PROFILE_PROPERTIES(String featureName) {
            this.featureName = featureName;
        }

        public String getFeatureName() {
            return featureName;
        }
    };

    protected VDSGroup getVdsGroup() {
        if (vdsGroup == null) {
            vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(vm.getVdsGroupId());
        }
        return vdsGroup;
    }
}
