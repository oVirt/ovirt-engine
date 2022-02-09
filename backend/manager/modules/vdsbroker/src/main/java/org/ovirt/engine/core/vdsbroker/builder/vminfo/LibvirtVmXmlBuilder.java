package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import static org.ovirt.engine.core.common.utils.VmDeviceCommonUtils.updateVmDevicesBootOrder;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.IoTuneUtils.ioTuneMapFrom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.HugePage;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VgpuPlacement;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmHostDevice;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeDriver;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.HugePageUtils;
import org.ovirt.engine.core.common.utils.MDevTypesUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.MemoizingSupplier;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.StringMapUtils;
import org.ovirt.engine.core.utils.archstrategy.ArchStrategyFactory;
import org.ovirt.engine.core.utils.ovf.xml.XmlTextWriter;
import org.ovirt.engine.core.vdsbroker.architecture.CreateAdditionalControllersForDomainXml;
import org.ovirt.engine.core.vdsbroker.architecture.GetControllerIndices;
import org.ovirt.engine.core.vdsbroker.vdsbroker.DeviceInfoReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.NumaSettingFactory;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class generates a Libvirt's Domain XML from the internal representation of
 * a given Virtual Machine within ovirt-engine.
 * Note that any non-trivial logic that can be extracted into a utility method that could be
 * used when generating another representation of the VM should reside in {@link VmInfoBuildUtils}.
 * Also note that there should not be any call to VDSM from this class. If the generated XML
 * needs to contain information that the engine does not know about then either this information
 * should be added to the hosts/VMs monitoring or to GetCapabilities, or to represent the data
 * using place-holders that are replaced by VDSM (see {@link #writeLease(XmlTextWriter, VM)}).
 */
public class LibvirtVmXmlBuilder {

    private static final Logger log = LoggerFactory.getLogger(LibvirtVmXmlBuilder.class);
    // Namespace URIs:
    public static final String OVIRT_TUNE_URI = "http://ovirt.org/vm/tune/1.0";
    public static final String OVIRT_VM_URI = "http://ovirt.org/vm/1.0";
    public static final String QEMU_URI = "http://libvirt.org/schemas/domain/qemu/1.0";

    // Namespace prefixes:
    public static final String OVIRT_TUNE_PREFIX = "ovirt-tune";
    public static final String OVIRT_VM_PREFIX = "ovirt-vm";
    public static final String QEMU_PREFIX = "qemu";

    /** Timeout for the boot menu, in milliseconds */
    public static final int BOOT_MENU_TIMEOUT = 30000;
    private static final int LIBVIRT_PORT_AUTOSELECT = -1;
    private static final Set<String> SPICE_CHANNEL_NAMES = new HashSet<>(Arrays.asList(
            "main", "display", "inputs", "cursor", "playback", "record", "smartcard", "usbredir"));
    private static final String SCSI_HD = "scsi_hd";
    private static final String SCSI_BLOCK = "scsi_block";
    public static final String SCSI_VIRTIO_BLK_PCI = "virtio_blk_pci";
    public static final List<String> SCSI_HOST_DEV_DRIVERS = Arrays.asList(SCSI_HD, SCSI_BLOCK, SCSI_VIRTIO_BLK_PCI);

    private VmInfoBuildUtils vmInfoBuildUtils;

    private String serialConsolePath;
    private boolean hypervEnabled;
    private XmlTextWriter writer;
    private Map<Guid, StorageQos> qosCache;
    private String emulatedMachine;
    private String cdInterface;
    private int payloadIndex;
    private int cdRomIndex;
    private VmDevice payload;
    private boolean volatileRun;
    private Map<Guid, String> passthroughVnicToVfMap;
    private Map<String, String> vmCustomProperties;
    private boolean legacyVirtio;

    private VM vm;
    private int vdsCpuThreads;
    private MemoizingSupplier<Map<String, HostDevice>> hostDevicesSupplier;
    private MemoizingSupplier<List<VmDevice>> vmDevicesSupplier;
    private MemoizingSupplier<VdsStatistics> hostStatisticsSupplier;
    private MemoizingSupplier<List<VdsNumaNode>> hostNumaNodesSupplier;
    private MemoizingSupplier<List<VmNumaNode>> vmNumaNodesSupplier;
    private MemoizingSupplier<VgpuPlacement> hostVgpuPlacementSupplier;
    private MemoizingSupplier<String> tscFrequencySupplier;
    private MemoizingSupplier<String> cpuFlagsSupplier;
    private MemoizingSupplier<String> cpuModelSupplier;
    private MemoizingSupplier<Boolean> incrementalBackupSupplier;
    private MemoizingSupplier<Boolean> kernelFipsModeSupplier;

    private Map<String, Map<String, Object>> vnicMetadata;
    private Map<String, Map<String, String>> diskMetadata;
    private Map<String, Map<String, String>> mdevMetadata;
    private Pair<String, VmPayload> payloadMetadata;

    private List<Pair<Guid, Guid>> volumeLeases = Collections.emptyList();

    /** Hot-set fields */
    private VmNic nic;
    private Disk disk;
    private VmDevice device;
    private boolean mdevDisplayOn;
    private int sdIndex;

    /**
     * This constructor is meant for building a complete XML for runnning
     * a VM on the specified host.
     */
    public LibvirtVmXmlBuilder(
            VM vm,
            Guid hostId,
            VmDevice payload,
            int vdsCpuThreads,
            boolean volatileRun,
            Map<Guid, String> passthroughVnicToVfMap,
            VmInfoBuildUtils vmInfoBuildUtils) {
        this.payload = payload;
        this.vdsCpuThreads = vdsCpuThreads;
        this.volatileRun = volatileRun;
        this.passthroughVnicToVfMap = passthroughVnicToVfMap;
        init(vm, vmInfoBuildUtils, hostId);
    }

    /**
     * This constructor is meant for building a partial XML for hot-(un)plugging
     * a network interface.
     */
    public LibvirtVmXmlBuilder(
            VM vm,
            Guid hostId,
            VmNic nic,
            VmDevice device,
            VmInfoBuildUtils vmInfoBuildUtils,
            Map<Guid, String> passthroughVnicToVfMap) {
        this.passthroughVnicToVfMap = passthroughVnicToVfMap;
        this.nic = nic;
        this.device = device;
        init(vm, vmInfoBuildUtils, hostId);
    }

    /**
     * This constructor is meant for building a partial XML for hot-plugging disk.
     */
    public LibvirtVmXmlBuilder(
            VM vm,
            Guid hostId,
            Disk disk,
            VmDevice device,
            VmInfoBuildUtils vmInfoBuildUtils) {
        this.disk = disk;
        this.device = device;
        init(vm, vmInfoBuildUtils, hostId);
    }

    /**
     * This constructor is meant for building an host-agnostic XML for
     * hosted-engine VM. Note that the given VM must not be defined
     * with custom properties (that will result in an NPE at {@link #writeMemoryBacking()}
     */
    public LibvirtVmXmlBuilder(
            VM vm,
            VmInfoBuildUtils vmInfoBuildUtils) {
        init(vm, vmInfoBuildUtils, null);
    }

    private void init(VM vm, VmInfoBuildUtils vmInfoBuildUtils, Guid hostId) {
        this.vm = vm;
        this.vmInfoBuildUtils = vmInfoBuildUtils;
        payloadIndex = -1;
        cdRomIndex = -1;
        vnicMetadata = new HashMap<>();
        diskMetadata = new HashMap<>();
        mdevMetadata = new HashMap<>();
        hypervEnabled = vmInfoBuildUtils.isHypervEnabled(vm.getVmOsId(), vm.getCompatibilityVersion());
        emulatedMachine = vm.getEmulatedMachine() != null ?
                vm.getEmulatedMachine()
                : vmInfoBuildUtils.getEmulatedMachineByClusterArch(vm.getClusterArch());
        cdInterface = vmInfoBuildUtils.getCdInterface(
                vm.getOs(),
                vm.getCompatibilityVersion(),
                ChipsetType.fromMachineType(emulatedMachine));
        writer = new XmlTextWriter();
        qosCache = new HashMap<>();
        vmCustomProperties = VmPropertiesUtils.getInstance().getVMProperties(
                vm.getCompatibilityVersion(),
                vm.getStaticData());

        if (hostId != null) {
            hostDevicesSupplier = new MemoizingSupplier<>(() -> vmInfoBuildUtils.getHostDevices(hostId));
            hostStatisticsSupplier = new MemoizingSupplier<>(() -> vmInfoBuildUtils.getVdsStatistics(hostId));
            hostNumaNodesSupplier = new MemoizingSupplier<>(() -> vmInfoBuildUtils.getVdsNumaNodes(hostId));
            hostVgpuPlacementSupplier = new MemoizingSupplier<>(() -> vmInfoBuildUtils.vgpuPlacement(hostId));
            VdsDynamic vds = vmInfoBuildUtils.getVdsDynamic(hostId);
            tscFrequencySupplier = new MemoizingSupplier<>(() -> vds.getTscFrequency());
            cpuFlagsSupplier = new MemoizingSupplier<>(() -> vds.getCpuFlags());
            cpuModelSupplier = new MemoizingSupplier<>(() -> vds.getCpuModel());
            incrementalBackupSupplier = new MemoizingSupplier<>(() -> vds.isBackupEnabled());
            kernelFipsModeSupplier = new MemoizingSupplier<>(() -> vmInfoBuildUtils.isKernelFipsMode(vds));
        } else {
            hostDevicesSupplier = new MemoizingSupplier<>(() -> Collections.emptyMap());
            hostStatisticsSupplier = new MemoizingSupplier<>(() -> null);
            hostNumaNodesSupplier = new MemoizingSupplier<>(() -> Collections.emptyList());
            hostVgpuPlacementSupplier = new MemoizingSupplier<>(() -> null);
            tscFrequencySupplier = new MemoizingSupplier<>(() -> null);
            cpuFlagsSupplier = new MemoizingSupplier<>(() -> "");
            cpuModelSupplier = new MemoizingSupplier<>(() -> "");
            incrementalBackupSupplier = new MemoizingSupplier<>(() -> false);
            kernelFipsModeSupplier = new MemoizingSupplier<>(() -> false);
        }
        vmDevicesSupplier = new MemoizingSupplier<>(() -> vmInfoBuildUtils.getVmDevices(vm.getId()));
        vmNumaNodesSupplier = new MemoizingSupplier<>(() -> vmInfoBuildUtils.getVmNumaNodes(vm));
        mdevDisplayOn = MDevTypesUtils.isMdevDisplayOn(vm);
        legacyVirtio = vmInfoBuildUtils.isLegacyVirtio(vm.getVmOsId(), ChipsetType.fromMachineType(emulatedMachine));
    }

    public String buildCreateVm() {
        writeHeader();
        writeName();
        writeId();
        writeMemory();
        writeIoThreads();
        writeMaxMemory();
        writevCpu();
        writeSystemInfo();
        writeClock();
        writePowerEvents();
        writeFeatures();
        boolean numaEnabled = vmInfoBuildUtils.isNumaEnabled(hostNumaNodesSupplier, vmNumaNodesSupplier, vm);
        if (numaEnabled) {
            writeNumaTune();
        }
        writeCpu(numaEnabled || (vm.isHostedEngine() && !vmNumaNodesSupplier.get().isEmpty()));
        writeCpuTune(numaEnabled);
        writeQemuCapabilities();
        writeDevices();
        writePowerManagement();
        // note that this must be called after writeDevices to get the serial console, if exists
        writeOs();
        writeMemoryBacking();
        writeMetadata(); // must remain the last call!
        return writer.getStringXML();
    }

    public String buildHotplugNic() {
        writer.writeStartDocument(false);
        writer.writeStartElement("hotplug");

        writer.writeStartElement("devices");
        writeInterface(device, nic);
        writer.writeEndElement();

        writer.writeStartElement("metadata");
        writer.setPrefix(OVIRT_VM_PREFIX, OVIRT_VM_URI);
        writer.writeNamespace(OVIRT_VM_PREFIX, OVIRT_VM_URI);
        writer.writeStartElement(OVIRT_VM_URI, "vm");
        writeNetworkInterfaceMetadata();
        writer.writeEndElement();
        writer.writeEndElement();

        return writer.getStringXML();
    }

    public String buildHotplugDisk() {
        writer.writeStartDocument(false);
        writer.writeStartElement("hotplug");

        writer.writeStartElement("devices");
        DiskVmElement dve = disk.getDiskVmElementForVm(vm.getId());
        DiskInterface iface = dve.getDiskInterface();
        // The device name serves just as an hint to libvirt
        String dev = vmInfoBuildUtils.makeDiskName(iface.getName(), 0);
        int pinToIoThread = iface == DiskInterface.VirtIO ? vmInfoBuildUtils.nextIoThreadToPinTo(vm) : 0;
        writeDisk(device, disk, dve, dev, pinToIoThread);
        writer.writeEndElement();

        writer.writeStartElement("metadata");
        writer.setPrefix(OVIRT_VM_PREFIX, OVIRT_VM_URI);
        writer.writeNamespace(OVIRT_VM_PREFIX, OVIRT_VM_URI);
        writer.writeStartElement(OVIRT_VM_URI, "vm");
        writeDiskMetadata();
        writer.writeEndElement();
        writer.writeEndElement();

        return writer.getStringXML();
    }

    private void writeHeader() {
        writer.setPrefix(OVIRT_TUNE_PREFIX, OVIRT_TUNE_URI);
        writer.setPrefix(OVIRT_VM_PREFIX, OVIRT_VM_URI);
        if (incrementalBackupSupplier.get()) {
            writer.setPrefix(QEMU_PREFIX, QEMU_URI);
        }
        writer.writeStartDocument(false);
        writer.writeStartElement("domain");
        writer.writeAttributeString("type", "kvm");
        writer.writeNamespace(OVIRT_TUNE_PREFIX, OVIRT_TUNE_URI);
        writer.writeNamespace(OVIRT_VM_PREFIX, OVIRT_VM_URI);
        if (incrementalBackupSupplier.get()) {
            writer.writeNamespace(QEMU_PREFIX, QEMU_URI);
        }
    }

    private void writeName() {
        writer.writeElement("name", vm.getName());
    }

    private void writeId() {
        writer.writeElement("uuid", vm.getId().toString());
    }

    private void writeMemory() {
        long memSizeKB = (long) vm.getMemSizeMb() * 1024;
        writer.writeElement("memory", String.valueOf(memSizeKB));
        writer.writeElement("currentMemory", String.valueOf(memSizeKB));
    }

    private void writeIoThreads() {
        if (vm.getNumOfIoThreads() == 0) {
            return;
        }

        writer.writeElement("iothreads", String.valueOf(vm.getNumOfIoThreads()));
    }

    private void writeMaxMemory() {
        Long nvdimmSize = vmInfoBuildUtils.getNvdimmTotalSize(vm, hostDevicesSupplier);

        if (!FeatureSupported.hotPlugMemory(vm.getCompatibilityVersion(), vm.getClusterArch())
                // the next check is because QEMU fails if memory and maxMemory are the same
                || (vm.getVmMemSizeMb() == vm.getMaxMemorySizeMb()) && nvdimmSize == 0) {
                return;
        }

        long maxMemory = (long) vm.getMaxMemorySizeMb() * 1024 + nvdimmSize / 1024;
        writer.writeStartElement("maxMemory");
        writer.writeAttributeString("slots", Config.getValue(ConfigValues.MaxMemorySlots).toString());
        writer.writeRaw(String.valueOf(maxMemory));
        writer.writeEndElement();
    }

    private void writevCpu() {
        writer.writeStartElement("vcpu");
        writer.writeAttributeString("current", String.valueOf(VmCpuCountHelper.getDynamicNumOfCpu(vm)));
        writer.writeRaw(String.valueOf(VmCpuCountHelper.isAutoPinning(vm) ?
                VmCpuCountHelper.getDynamicNumOfCpu(vm) : VmInfoBuildUtils.maxNumberOfVcpus(vm)));
        writer.writeEndElement();
    }

    @SuppressWarnings("incomplete-switch")
    void writeCpu(boolean addVmNumaNodes) {
        writer.writeStartElement("cpu");

        String cpuType = vm.getCpuName();
        if (vm.isUseHostCpuFlags()){
            cpuType = "hostPassthrough";
        }
        if (vm.getUseTscFrequency() && tscFrequencySupplier.get() != null) {
            cpuType += ",+invtsc";
        }

        // Work around for https://bugzilla.redhat.com/1689362
        // If it is a nested VM on AMD EPYC, monitor feature must be
        // disabled manually, until libvirt is fixed.
        if (cpuModelSupplier.get() != null && cpuModelSupplier.get().contains("AMD EPYC") &&
                cpuFlagsSupplier.get() != null && !cpuFlagsSupplier.get().contains("monitor")) {
            cpuType += ",-monitor";
        }

        String cpuFlagsProperty = vmCustomProperties.get("extra_cpu_flags");
        if (StringUtils.isNotEmpty(cpuFlagsProperty)) {
            cpuType += "," + cpuFlagsProperty;
        }

        String[] typeAndFlags = cpuType.split(",");

        switch(vm.getClusterArch().getFamily()) {
        case x86:
        case s390x:
            writer.writeAttributeString("match", "exact");

            // is this a list of strings??..
            switch(typeAndFlags[0]) {
            case "hostPassthrough":
                writer.writeAttributeString("mode", "host-passthrough");
                writeCpuFlags(typeAndFlags);
                break;
            case "hostModel":
                writer.writeAttributeString("mode", "host-model");
                writeCpuFlags(typeAndFlags);
                break;
            default:
                writer.writeElement("model", typeAndFlags[0]);
                writeCpuFlags(typeAndFlags);
                break;
            }
            break;
        case ppc:
            writer.writeAttributeString("mode", "host-model");
            // needs to be lowercase for libvirt
            writer.writeElement("model", typeAndFlags[0].toLowerCase());
            writeCpuFlags(typeAndFlags);
        }

        if ((boolean) Config.getValue(ConfigValues.SendSMPOnRunVm)) {
            int sockets;
            int cores;
            int threads;
            if (VmCpuCountHelper.isDynamicCpuTopologySet(vm)) {
                sockets = vm.getCurrentSockets();
                cores = vm.getCurrentCoresPerSocket();
                threads = vm.getCurrentThreadsPerCore();
            } else {
                int vcpus = VmInfoBuildUtils.maxNumberOfVcpus(vm);
                sockets = vcpus / vm.getCpuPerSocket() / vm.getThreadsPerCpu();
                cores = vm.getCpuPerSocket();
                threads = vm.getThreadsPerCpu();
            }
            writer.writeStartElement("topology");
            writer.writeAttributeString("cores", Integer.toString(cores));
            writer.writeAttributeString("threads", Integer.toString(threads));
            writer.writeAttributeString("sockets", String.valueOf(sockets));
            writer.writeEndElement();
        }

        if (addVmNumaNodes) {
            writer.writeStartElement("numa");
            NumaSettingFactory.buildVmNumaNodeSetting(vmNumaNodesSupplier.get()).forEach(vmNumaNode -> {
                writer.writeStartElement("cell");
                writer.writeAttributeString("id", vmNumaNode.get(VdsProperties.NUMA_NODE_INDEX).toString());
                writer.writeAttributeString("cpus", vmNumaNode.get(VdsProperties.NUMA_NODE_CPU_LIST).toString());
                writer.writeAttributeString("memory", String.valueOf(Long.parseLong((String) vmNumaNode.get(VdsProperties.VM_NUMA_NODE_MEM)) * 1024));
                if (HugePageUtils.isHugepagesShared(vm.getStaticData())) {
                    writer.writeAttributeString("memAccess", "shared");
                }
                writer.writeEndElement();
            });
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void writeCpuFlags(String[] typeAndFlags) {
        Stream.of(typeAndFlags).skip(1).filter(StringUtils::isNotEmpty).forEach(flag -> {
            writer.writeStartElement("feature");
            switch(flag.charAt(0)) {
            case '+':
                writer.writeAttributeString("name", flag.substring(1));
                writer.writeAttributeString("policy", "require");
                break;
            case '-':
                writer.writeAttributeString("name", flag.substring(1));
                writer.writeAttributeString("policy", "disable");
                break;
            default:
                writer.writeAttributeString("name", flag);
            }
            writer.writeEndElement();
        });
    }

    private void writeCpuTune(boolean numaEnabled) {
        writer.writeStartElement("cputune");
        Map<String, Object> cpuPinning = vmInfoBuildUtils.parseCpuPinning(VmCpuCountHelper.isAutoPinning(vm) ?
                vm.getCurrentCpuPinning() : vm.getCpuPinning());
        if (cpuPinning.isEmpty() && numaEnabled) {
            cpuPinning = NumaSettingFactory.buildCpuPinningWithNumaSetting(
                    vmNumaNodesSupplier.get(),
                    hostNumaNodesSupplier.get());
        }
        cpuPinning.forEach((vcpu, cpuset) -> {
            writer.writeStartElement("vcpupin");
            writer.writeAttributeString("vcpu", vcpu);
            writer.writeAttributeString("cpuset", (String) cpuset);
            writer.writeEndElement();
        });

        if (vm.getCpuShares() > 0) {
            writer.writeElement("shares", String.valueOf(vm.getCpuShares()));
        }

        // iothreadpin + emulatorpin
        String ioEmulatorCpus = vmInfoBuildUtils.getIoThreadsAndEmulatorPinningCpus(vm, cpuPinning, hostNumaNodesSupplier, vdsCpuThreads);
        if (ioEmulatorCpus != null) {
            for (int i = 0; i < vm.getNumOfIoThreads(); i++) {
                writer.writeStartElement("iothreadpin");
                writer.writeAttributeString("iothread", String.valueOf(i+1));
                writer.writeAttributeString("cpuset", ioEmulatorCpus);
                writer.writeEndElement();
            }

            writer.writeStartElement("emulatorpin");
            writer.writeAttributeString("cpuset", ioEmulatorCpus);
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void writeSystemInfo() {
        /*
         RHEL-8.2.0  AV will now report:

         System Manufacturer: oVirt or Red Hat    RHV needs 'Red Hat' for Windows Update, decided by OriginType
         System Product Name: OS name
         System Version: OS version
         System Family: oVirt or RHV
         System SKU Number:                       can be set in vdc_options (SkuToAVLevel)
         Baseboard Manufacturer: Red Hat          hardcoded but added only if SkuToAVLevel options set
         Baseboard Product Name: RHEL-AV          hardcoded but added only if SkuToAVLevel options set
        */

        if (vm.getClusterArch().getFamily() != ArchitectureType.x86) {
            return;
        }

        final String product = Config.getValue(ConfigValues.OriginType);
        final String manufacturer = OriginType.valueOf(product) == OriginType.OVIRT ? "oVirt" : "Red Hat";
        final String productName = OriginType.valueOf(product) == OriginType.OVIRT ? "oVirt" : "RHV";
        boolean skuToAVLevelExists = StringUtils.isNotEmpty(
                Config.getValue(ConfigValues.SkuToAVLevel, vm.getCompatibilityVersion().toString()));
        boolean version4_4orHigher = vm.getClusterCompatibilityVersion().greaterOrEquals(Version.v4_4);

        writer.writeStartElement("sysinfo");
        writer.writeAttributeString("type", "smbios");

        writer.writeStartElement("system");
        writeEntryElement("manufacturer", manufacturer);
        writeEntryElement("product", "OS-NAME:");
        writeEntryElement("version", "OS-VERSION:");
        if (version4_4orHigher) {
            writeEntryElement("family", productName);
            if (skuToAVLevelExists) {
                writeEntryElement("sku", Config.getValue(ConfigValues.SkuToAVLevel, vm.getCompatibilityVersion().toString()));
            }
        }
        writeEntryElement("serial", vmInfoBuildUtils.getVmSerialNumber(vm, "HOST-SERIAL:"));
        writeEntryElement("uuid", vm.getId().toString());
        writer.writeEndElement(); // system

        if (version4_4orHigher && skuToAVLevelExists) {
            writer.writeStartElement("baseBoard");
            writeEntryElement("manufacturer", "Red Hat");
            writeEntryElement("product", "RHEL-AV");
            writer.writeEndElement(); // baseBoard
        }
        writer.writeEndElement(); // sysinfo
    }

    private void writeEntryElement(String attributeValue, String rawString) {
        writer.writeStartElement("entry");
        writer.writeAttributeString("name", attributeValue);
        writer.writeRaw(rawString);
        writer.writeEndElement();
    }

    private Map<String, Object> getNumaTuneSetting() {
        Map<String, Object> numaTuneSetting = NumaSettingFactory.buildVmNumatuneSetting(
                vmNumaNodesSupplier.get());
        if (numaTuneSetting.isEmpty()) {
            return null;
        }

        return numaTuneSetting;
    }

    private void writeNumaTune() {
        Map<String, Object> numaTuneSetting = getNumaTuneSetting();
        if (numaTuneSetting == null) {
            return;
        }

        // <numatune>
        //   <memnode cellid='0' mode='strict' nodeset='1'>
        // </numatune>
        @SuppressWarnings("unchecked")
        List<Map<String, String>> memNodes = (List<Map<String, String>>) numaTuneSetting.get(VdsProperties.NUMA_TUNE_MEMNODES);
        if (memNodes != null) {
            writer.writeStartElement("numatune");

            for (Map<String, String> memnode : memNodes) {
                writer.writeStartElement("memnode");
                writer.writeAttributeString("mode", memnode.get(VdsProperties.NUMA_TUNE_MODE));
                writer.writeAttributeString("cellid", memnode.get(VdsProperties.NUMA_TUNE_VM_NODE_INDEX));
                writer.writeAttributeString("nodeset", memnode.get(VdsProperties.NUMA_TUNE_NODESET));
                writer.writeEndElement();
            }

            writer.writeEndElement();
        }
    }

    private void writePowerManagement() {
        if (vm.getClusterArch().getFamily() != ArchitectureType.x86) {
            // ACPI PM features relevant only on x86
            return;
        }

        writer.writeStartElement("pm");
        writer.writeStartElement("suspend-to-disk");
        writer.writeAttributeString("enabled", "no");
        writer.writeEndElement();
        writer.writeStartElement("suspend-to-mem");
        writer.writeAttributeString("enabled", "no");
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeOs() {
        writer.writeStartElement("os");

        writer.writeStartElement("type");
        writer.writeAttributeString("arch", vm.getClusterArch().toString());
        writer.writeAttributeString("machine", emulatedMachine);
        writer.writeRaw("hvm");
        writer.writeEndElement();

        // No need to the boot section that VDSM defines

        if (!StringUtils.isEmpty(vm.getInitrdUrl())) {
            writer.writeElement("initrd", vm.getInitrdUrl());
        }

        if (!StringUtils.isEmpty(vm.getKernelUrl())) {
            writer.writeElement("kernel", vm.getKernelUrl());
            if (!StringUtils.isEmpty(vm.getKernelParams())) {
                writer.writeElement("cmdline", vm.getKernelParams());
            }
        }

        if (vm.getClusterArch().getFamily() == ArchitectureType.x86) {
            writer.writeStartElement("smbios");
            writer.writeAttributeString("mode", "sysinfo");
            writer.writeEndElement();
        }

        if (vm.getBiosType().isOvmf()) {
            writer.writeStartElement("loader");
            writer.writeAttributeString("readonly", "yes");
            boolean secureBoot = vm.getBiosType() == BiosType.Q35_SECURE_BOOT;
            writer.writeAttributeString("secure", secureBoot ? "yes" : "no");
            writer.writeAttributeString("type", "pflash");
            writer.writeRaw("/usr/share/OVMF/OVMF_CODE.secboot.fd");
            writer.writeEndElement();
            writer.writeStartElement("nvram");
            String nvramTemplate = vmCustomProperties.get("nvram_template");
            if (nvramTemplate == null) {
                nvramTemplate = String.format("/usr/share/OVMF/%s",
                        secureBoot ? "OVMF_VARS.secboot.fd" : "OVMF_VARS.fd");
            }
            writer.writeAttributeString("template", nvramTemplate);
            writer.writeRaw(String.format("/var/lib/libvirt/qemu/nvram/%s.fd", vm.getId()));
            writer.writeEndElement();
        }

        if (vm.isBootMenuEnabled()) {
            writer.writeStartElement("bootmenu");
            writer.writeAttributeString("enable", "yes");
            writer.writeAttributeString("timeout", String.valueOf(BOOT_MENU_TIMEOUT));
            writer.writeEndElement();
        }

        if (serialConsolePath != null && vm.getClusterArch().getFamily() == ArchitectureType.x86) {
            writer.writeStartElement("bios");
            writer.writeAttributeString("useserial", "yes");
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    void writeClock() {
        // <clock offset="variable" adjustment="-3600">
        //   <timer name="rtc" tickpolicy="catchup">
        // </clock>
        // for hyperv:
        // <clock offset="variable" adjustment="-3600">
        //   <timer name="hypervclock" present="yes">
        //   <timer name="rtc" tickpolicy="catchup">
        // </clock>
        writer.writeStartElement("clock");
        writer.writeAttributeString("offset", "variable");
        writer.writeAttributeString("adjustment", String.valueOf(vmInfoBuildUtils.getVmTimeZone(vm)));

        if (hypervEnabled) {
            writer.writeStartElement("timer");
            writer.writeAttributeString("name", "hypervclock");
            writer.writeAttributeString("present", "yes");
            writer.writeEndElement();
        }

        writer.writeStartElement("timer");
        writer.writeAttributeString("name", "rtc");
        writer.writeAttributeString("tickpolicy", "catchup");
        writer.writeEndElement();

        writer.writeStartElement("timer");
        writer.writeAttributeString("name", "pit");
        writer.writeAttributeString("tickpolicy", "delay");
        writer.writeEndElement();

        if (vm.getClusterArch().getFamily() == ArchitectureType.x86) {
            writer.writeStartElement("timer");
            writer.writeAttributeString("name", "hpet");
            writer.writeAttributeString("present", "no");
            writer.writeEndElement();
        }
        if (vm.getUseTscFrequency() && tscFrequencySupplier.get() != null) {
            writer.writeStartElement("timer");
            writer.writeAttributeString("name", "tsc");
            writer.writeAttributeString("frequency", tscFrequencySupplier.get());
            writer.writeEndElement();
        }
        // Intentionally no 'break;', as code for s390x is shared with x86

        writer.writeEndElement();
    }

    private void writeFeatures() {
        if (vm.getClusterArch().getFamily() != ArchitectureType.x86) {
            return;
        }

        boolean acpiEnabled = vm.getAcpiEnable();
        boolean kaslrEnabled = vmInfoBuildUtils.isKASLRDumpEnabled(vm.getVmOsId());
        boolean secureBootEnabled = vm.getBiosType() == BiosType.Q35_SECURE_BOOT;
        if (!acpiEnabled && !hypervEnabled && !kaslrEnabled && !secureBootEnabled) {
            return;
        }

        writer.writeStartElement("features");

        if (acpiEnabled) {
            writer.writeElement("acpi");
        }

        if (hypervEnabled) {
            writer.writeStartElement("hyperv");

            writer.writeStartElement("relaxed");
            writer.writeAttributeString("state", "on");
            writer.writeEndElement();

            writer.writeStartElement("vapic");
            writer.writeAttributeString("state", "on");
            writer.writeEndElement();

            writer.writeStartElement("spinlocks");
            writer.writeAttributeString("state", "on");
            writer.writeAttributeString("retries", "8191");
            writer.writeEndElement();

            if (FeatureSupported.hyperVSynicStimerSupported(vm.getCompatibilityVersion())) {
                writer.writeStartElement("synic");
                writer.writeAttributeString("state", "on");
                writer.writeEndElement();

                writer.writeStartElement("stimer");
                writer.writeAttributeString("state", "on");
                if (vm.getCompatibilityVersion().greater(Version.v4_6)) {
                    writer.writeStartElement("direct");
                    writer.writeAttributeString("state", "on");
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }

            if (vm.getCompatibilityVersion().greaterOrEquals(Version.v4_4)) {
                writer.writeStartElement("reset");
                writer.writeAttributeString("state", "on");
                writer.writeEndElement();

                writer.writeStartElement("vpindex");
                writer.writeAttributeString("state", "on");
                writer.writeEndElement();

                writer.writeStartElement("runtime");
                writer.writeAttributeString("state", "on");
                writer.writeEndElement();

                writer.writeStartElement("frequencies");
                writer.writeAttributeString("state", "on");
                writer.writeEndElement();

                writer.writeStartElement("reenlightenment");
                writer.writeAttributeString("state", "on");
                writer.writeEndElement();

                writer.writeStartElement("tlbflush");
                writer.writeAttributeString("state", "on");
                writer.writeEndElement();
            }

            if (vm.getCompatibilityVersion().greater(Version.v4_6)) {
                writer.writeStartElement("ipi");
                writer.writeAttributeString("state", "on");
                writer.writeEndElement();
            }

            writer.writeEndElement();
        }

        if (kaslrEnabled) {
            writer.writeElement("vmcoreinfo");
        }

        if (secureBootEnabled) {
            writer.writeStartElement("smm");
            writer.writeAttributeString("state", "on");
            writer.writeEndElement();
        }

        if (VmInfoBuildUtils.isVmWithHighNumberOfX86Vcpus(vm)) {
            writer.writeStartElement("ioapic");
            writer.writeAttributeString("driver", "qemu");
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void writeMemoryBacking() {
        Optional<Integer> hugepageSizeOpt = HugePageUtils.getHugePageSize(vm.getStaticData());
        if (!hugepageSizeOpt.isPresent()) {
            return;
        }

        writer.writeStartElement("memoryBacking");
        writer.writeStartElement("hugepages");
        writer.writeStartElement("page");
        int hugepageSize = vmInfoBuildUtils.getDefaultHugepageSize(vm);
        if (hostStatisticsSupplier.get() != null) {
            int hugepageSizeFromOpt = hugepageSizeOpt.get();
            List<Integer> hugepageSizes = hostStatisticsSupplier.get().getHugePages().stream()
                    .map(HugePage::getSizeKB)
                    .collect(Collectors.toList());
            if (hugepageSizes.contains(hugepageSizeFromOpt)) {
                hugepageSize = hugepageSizeFromOpt;
            }
        }
        writer.writeAttributeString("size", String.valueOf(hugepageSize));
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeMetadata() {
        // <domain>
        // ...
        //   <metadata>
        //     <ovirt-tune:qos/>
        //     <ovirt-vm:vm/>
        //   </metadata>
        // ...
        // </domain>
        writer.writeStartElement("metadata");
        writeQosMetadata();
        writeVmMetadata();
        writer.writeEndElement();
    }

    private void writeNetworkInterfaceMetadata() {
        vnicMetadata.forEach((alias, data) -> {
            writer.writeStartElement(OVIRT_VM_URI, "device");
            writer.writeAttributeString("alias", alias);
            var mac = (String) data.remove("mac");
            if (mac != null) {
                writer.writeAttributeString("mac_address", mac);
            }
            List<String> portMirroring = (List<String>) data.remove("portMirroring");
            if (portMirroring != null) {
                writer.writeStartElement(OVIRT_VM_URI, "portMirroring");
                portMirroring.forEach(network -> writer.writeElement(OVIRT_VM_URI, "network", network));
                writer.writeEndElement();
            }
            writer.writeStartElement(OVIRT_VM_URI, "custom");
            Map<String, String> runtimeCustomProperties = (Map<String, String>) data.remove("runtimeCustomProperties");
            if (runtimeCustomProperties != null) {
                runtimeCustomProperties.forEach((key, value) -> writer.writeElement(OVIRT_VM_URI, key, value));
            }
            // write the other custom properties
            data.forEach((key, value) -> writer.writeElement(OVIRT_VM_URI, key, value.toString()));
            writer.writeEndElement();
            writer.writeEndElement();
        });
    }

    private void writeDiskMetadata() {
        diskMetadata.forEach((dev, data) -> {
            writer.writeStartElement(OVIRT_VM_URI, "device");
            writer.writeAttributeString("devtype", "disk");
            writer.writeAttributeString("name", dev);
            data.forEach((key, value) -> writer.writeElement(OVIRT_VM_URI, key, value));
            writer.writeEndElement();
        });
    }

    private void writePayloadMetadata() {
        if (payloadMetadata == null) {
            return;
        }
        writer.writeStartElement(OVIRT_VM_URI, "device");
        writer.writeAttributeString("devtype", "disk");
        writer.writeAttributeString("name", payloadMetadata.getFirst());
        writer.writeStartElement(OVIRT_VM_URI, "payload");
        String volumeId = payloadMetadata.getSecond().getVolumeId();
        if (volumeId != null) {
            writer.writeElement(OVIRT_VM_URI, "volId", volumeId);
        }
        Map<String, String> files = payloadMetadata.getSecond().getFiles();
        if (files != null) {
            files.forEach((path, data) -> {
                writer.writeStartElement(OVIRT_VM_URI, "file");
                writer.writeAttributeString("path", path);
                writer.writeRaw(data);
                writer.writeEndElement();
            });
        }
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeQosMetadata() {
        writer.writeStartElement(OVIRT_TUNE_URI, "qos");
        writer.writeEndElement();
    }

    private void writeVmMetadata() {
        writer.writeStartElement(OVIRT_VM_URI, "vm");
        writeMinGuaranteedMemoryMetadata();
        writeClusterVersionMetadata();
        writeMDevMetadata();
        writeVmCustomMetadata();
        writeNetworkInterfaceMetadata();
        writeDiskMetadata();
        writeRunAndPauseMetadata();
        writePayloadMetadata();
        writeResumeBehaviorMetadata();
        writeBalloonMetadata();
        writer.writeEndElement();
    }

    private void writeMDevMetadata() {
        mdevMetadata.forEach((address, data) -> {
            writer.writeStartElement(OVIRT_VM_URI, "device");
            writer.writeAttributeString("devtype", "hostdev");
            writer.writeAttributeString("uuid", address);
            data.forEach((key, value) -> writer.writeElement(OVIRT_VM_URI, key, value.toString()));
            writer.writeEndElement();
        });
    }

    private void writeResumeBehaviorMetadata() {
        writer.writeElement(OVIRT_VM_URI, "resumeBehavior", String.valueOf(vm.getResumeBehavior()).toLowerCase());
    }

    private void writeRunAndPauseMetadata() {
        writer.writeElement(OVIRT_VM_URI, "launchPaused", String.valueOf(vm.isRunAndPause()));
    }

    private void writeVmCustomMetadata() {
        writer.writeStartElement(OVIRT_VM_URI, "custom");
        vmCustomProperties.forEach((key, value) -> writer.writeElement(OVIRT_VM_URI, key, value));
        writer.writeEndElement();
    }

    private void writeMinGuaranteedMemoryMetadata() {
        writer.writeStartElement(OVIRT_VM_URI, "minGuaranteedMemoryMb");
        writer.writeAttributeString("type", "int");
        writer.writeRaw(String.valueOf(vm.getMinAllocatedMem()));
        writer.writeEndElement();
    }

    private void writeClusterVersionMetadata() {
        writer.writeStartElement(OVIRT_VM_URI, "clusterVersion");
        Version version = vm.getCompatibilityVersion();
        writer.writeRaw(String.valueOf(version.getMajor()) + "." + String.valueOf(version.getMinor()));
        writer.writeEndElement();
    }

    private void writeBalloonMetadata() {
        writer.writeElement(OVIRT_VM_URI, "ballooningEnabled", String.valueOf(vm.isBalloonEnabled()));
    }

    private void writePowerEvents() {
        if (volatileRun) {
            writer.writeElement("on_reboot", "destroy");
        }
    }

    void writeDevices() {
        List<Pair<VmDevice, HostDevice>> hostDevDisks = new ArrayList<>();
        List<VmDevice> devices = vmDevicesSupplier.get();
        // replacement of some devices in run-once mode should eventually be done by the run-command
        devices = overrideDevicesForRunOnce(devices);
        devices = processPayload(devices);
        devices.stream().filter(d -> d.getSpecParams() == null).forEach(d -> d.setSpecParams(Collections.emptyMap()));
        ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new CreateAdditionalControllersForDomainXml(devices));
        Map<Integer, Map<VmDevice, Integer>> vmDeviceVirtioScsiUnitMap = vmInfoBuildUtils.getVmDeviceUnitMapForVirtioScsiDisks(vm);

        writer.writeStartElement("devices");

        switch(vm.getClusterArch().getFamily()) {
        // No mouse or tablet for s390x and for headless HP VMS with ppc architecture type.
        case x86:
            writeInput();
            break;
        case ppc:
            if (vmInfoBuildUtils.hasUsbController(vm)) {
                writeInput();
                break;
            }
        }

        writeGuestAgentChannels();

        if (vm.getClusterArch() == ArchitectureType.ppc64 || vm.getClusterArch() == ArchitectureType.ppc64le) {
            writeEmulator();
        }

        writeIommu();

        Map<DiskInterface, Integer> controllerIndexMap =
                ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new GetControllerIndices()).returnValue();
        int virtioScsiIndex = controllerIndexMap.get(DiskInterface.VirtIO_SCSI);

        List<VmDevice> interfaceDevices = new ArrayList<>();
        List<VmDevice> diskDevices = new ArrayList<>();
        List<VmDevice> cdromDevices = new ArrayList<>();
        VmDevice floppyDevice = null;

        boolean spiceExists = false;
        boolean balloonExists = false;
        boolean videoExists = false;
        boolean forceRefreshDevices = false;
        boolean pciERootExists = false;
        int pciEPorts = 0;
        for (VmDevice device : devices) {
            if (!device.isPlugged()) {
                continue;
            }

            switch (device.getType()) {
            case BALLOON:
                balloonExists = true;
                if (legacyVirtio) {
                    device.getSpecParams().put("model", "virtio-transitional");
                }
                writeBalloon(device);
                break;
            case SMARTCARD:
                writeSmartcard(device);
                break;
            case WATCHDOG:
                writeWatchdog(device);
                break;
            case MEMORY:
                // memory devices are only used for hot-plug
                break;
            case VIDEO:
                videoExists = true;
                writeVideo(device);
                break;
            case CONTROLLER:
                switch(device.getDevice()) {
                case "usb":
                    if ("qemu-xhci".equals(device.getSpecParams().get("model"))) {
                        device.getSpecParams().put("ports", 8);
                    }
                    break;
                case "virtio-serial":
                    device.getSpecParams().put("index", 0);
                    device.getSpecParams().put("ports", 16);
                    if (legacyVirtio) {
                        device.getSpecParams().put("model", "virtio-transitional");
                    }
                    break;
                case "virtio-scsi":
                    device.setDevice(VdsProperties.Scsi);
                    device.getSpecParams().put("index", virtioScsiIndex++);
                    if (legacyVirtio) {
                        device.getSpecParams().put("model", "virtio-transitional");
                    } else {
                        device.getSpecParams().put("model", "virtio-scsi");
                    }
                    break;
                case "pci":
                    Object model = device.getSpecParams().get("model");
                    if ("pcie-root".equals(model)) {
                        pciERootExists = true;
                    } else if ("pcie-root-port".equals(model)) {
                        pciEPorts++;
                    }
                    break;
                }
                writeController(device, vmDeviceVirtioScsiUnitMap);
                break;
            case GRAPHICS:
                writeGraphics(device);
                spiceExists = spiceExists || device.getDevice().equals("spice");
                break;
            case SOUND:
                writeSound(device);
                break;
            case RNG:
                writeRng(device);
                break;
            case TPM:
                writeTpm(device);
                break;
            case CONSOLE:
                writeConsole(device);
                if ("serial".equals(device.getSpecParams().get("consoleType"))) {
                    serialConsolePath = getSerialConsolePath(device);
                }
                break;
            case DISK:
                switch(VmDeviceType.getByName(device.getDevice())) {
                case CDROM:
                    cdromDevices.add(device);
                    break;
                case DISK:
                    diskDevices.add(device);
                    break;
                case FLOPPY:
                    if (floppyDevice == null || !VmPayload.isPayload(floppyDevice.getSpecParams())) {
                        floppyDevice = device;
                    }
                    break;
                default:
                }
                break;
            case INTERFACE:
                interfaceDevices.add(device);
                break;
            case REDIR:
                writeRedir(device);
                break;
            case REDIRDEV:
                break;
            case CHANNEL:
                break;
            case HOSTDEV:
                HostDevice hostDevice = hostDevicesSupplier.get().get(device.getDevice());
                if (hostDevice == null) {
                    if (!"mdev".equals(device.getDevice())) {
                        log.info("skipping VM host device {} for VM {}, no corresponding host device was found",
                                device.getDevice(), device.getVmId());
                    }
                    forceRefreshDevices = true;
                    break;
                }
                writeHostDevice(device, hostDevice, hostDevDisks);
                break;
            case UNKNOWN:
                break;
            default:
                break;
            }
        }

        if (forceRefreshDevices) {
            vmInfoBuildUtils.refreshVmDevices(vm.getId());
        }

        if (!balloonExists) {
            writeDefaultBalloon();
        }

        if (!videoExists) {
            writeDefaultVideo();
        }

        writeSerialConsole(serialConsolePath);

        if (spiceExists) {
            writeSpiceVmcChannel();
        }

        if (vm.getClusterArch().getFamily() == ArchitectureType.x86
                && vm.getBiosType().getChipsetType() == ChipsetType.Q35) {
            writePciEControllers(pciERootExists, pciEPorts);
        }

        updateBootOrder(diskDevices, cdromDevices, interfaceDevices);

        writeInterfaces(interfaceDevices);
        writeCdRom(cdromDevices);
        writeFloppy(floppyDevice);
        // we must write the disk after writing cd-rom and floppy to know reserved indices
        writeDisks(diskDevices, vmDeviceVirtioScsiUnitMap);
        writeHostdevDisks(hostDevDisks);
        writeLeases();

        writeVGpu();

        writer.writeEndElement();
    }

    private List<VmDevice> processPayload(List<VmDevice> devices) {
        if (payload != null) {
            devices = devices.stream()
                    .filter(dev -> !VmPayload.isPayload(dev.getSpecParams()))
                    .collect(Collectors.toList());
            devices.add(payload);
        }
        return devices;
    }

    private List<VmDevice> overrideDevicesForRunOnce(List<VmDevice> devices) {
        if (!vm.isRunOnce()) {
            return devices;
        }

        // video device handling
        DisplayType displayType = vm.getDefaultDisplayType();
        if (displayType != null) {
            // remove existing video device
            devices = devices.stream()
                    .filter(dev -> dev.getType() != VmDeviceGeneralType.VIDEO)
                    .collect(Collectors.toList());

            // add new video device
            if (displayType != DisplayType.none) {
                devices.add(vmInfoBuildUtils.createVideoDeviceByDisplayType(displayType, vm.getId()));
            }
        }

        // graphics device handling
        if (displayType == DisplayType.none || (vm.getGraphicsInfos() != null && !vm.getGraphicsInfos().isEmpty())) {
            // remove existing graphics devices
            devices = devices.stream()
                    .filter(dev -> dev.getType() != VmDeviceGeneralType.GRAPHICS)
                    .collect(Collectors.toList());

            if (displayType != DisplayType.none) {
                // add new graphics devices
                Map<GraphicsType, GraphicsInfo> infos = vm.getGraphicsInfos();
                Map<String, Object> specParamsFromVm = new HashMap<>();
                vmInfoBuildUtils.addVmGraphicsOptions(infos, specParamsFromVm, vm);

                devices.addAll(vmInfoBuildUtils.createGraphicsDevices(infos, specParamsFromVm, vm.getId()));
            }
        }

        // the user may specify floppy path while there is no device in the database
        if (!StringUtils.isEmpty(vm.getFloppyPath()) &&
                devices.stream().noneMatch(dev -> dev.getDevice().equals(VmDeviceType.FLOPPY.getName()))) {
            devices.add(vmInfoBuildUtils.createFloppyDevice(vm));
        }
        // the user may want a secondary disk for windows guest tools
        if (vm.getWgtCdPath() != null && !StringUtils.isEmpty(vm.getWgtCdPath())) {
            devices.add(vmInfoBuildUtils.createCdRomDevice(vm));
        }

        return devices;
    }

    @SafeVarargs
    private final void updateBootOrder(List<VmDevice> ... bootableDevices) {
        List<VmDevice> managedAndPluggedBootableDevices = Arrays.stream(bootableDevices)
                .flatMap(Collection::stream)
                .filter(VmDevice::isManaged)
                .collect(Collectors.toList());
        updateVmDevicesBootOrder(
                vm.getBootSequence(),
                managedAndPluggedBootableDevices,
                vm.getInterfaces(),
                VmDeviceCommonUtils.extractDiskVmElements(vm));
    }

    void writeVGpu() {
        for (String mdevType : MDevTypesUtils.getMDevTypes(vm)) {
            writer.writeStartElement("hostdev");
            writer.writeAttributeString("mode", "subsystem");
            writer.writeAttributeString("type", "mdev");
            writer.writeAttributeString("model", "vfio-pci");
            if (mdevDisplayOn) {
                // Nvidia vGPU VNC console is only supported on RHEL >= 7.6
                // See https://bugzilla.redhat.com/show_bug.cgi?id=1633623 for details and discussion
                writer.writeAttributeString("display", "on");
                if (FeatureSupported.isVgpuFramebufferSupported(vm.getCompatibilityVersion())) {
                    writer.writeAttributeString("ramfb", "on");
                }
            }

            writer.writeStartElement("source");
            String address = Guid.newGuid().toString();
            writer.writeStartElement("address");
            writer.writeAttributeString("uuid", address);
            writer.writeEndElement();
            writer.writeEndElement();

            writer.writeEndElement();

            String mdevTypeMeta = mdevType;
            if (FeatureSupported.isVgpuPlacementSupported(vm.getCompatibilityVersion())) {
                VgpuPlacement vgpuPlacement = hostVgpuPlacementSupplier.get();
                String vgpuPlacementString;
                if (vgpuPlacement == VgpuPlacement.CONSOLIDATED) {
                    vgpuPlacementString = "compact";
                } else if (vgpuPlacement == VgpuPlacement.SEPARATED) {
                    vgpuPlacementString = "separate";
                } else {
                    log.warn("Unrecognized vGPU placement type (using `{}' instead): {}",
                            VgpuPlacement.CONSOLIDATED,
                            vgpuPlacement);
                    vgpuPlacementString = "compact";
                }
                mdevTypeMeta = mdevTypeMeta + "|" + vgpuPlacementString;
            }
            // removing from custom properties since it will be processed separately
            vmCustomProperties.remove("mdev_type");
            mdevMetadata.put(address, Collections.singletonMap("mdevType", mdevTypeMeta));
        }
    }

    private void writeLeases() {
        // Write volume leases
        for (Pair<Guid, Guid> pair : volumeLeases) {
            String leaseId = pair.getFirst().toString();
            String leaseSdId = pair.getSecond().toString();

            // The templates LEASE-PATH and LEASE-OFFSET are replaced with real values in vdsm
            // For example:
            //   LEASE-PATH:<VOLUME_ID>:<DOMAIN_ID> -> /rhev/data-center/mnt/nfs/<DOMAIN_ID>/images/<VOLUME_ID>/abc.lease
            //   LEASE-OFFSET:<VOLUME_ID>:<DOMAIN_ID> -> 0
            writeLease(
                    leaseId,
                    leaseSdId,
                    String.format("LEASE-PATH:%s:%s", leaseId, leaseSdId),
                    String.format("LEASE-OFFSET:%s:%s", leaseId, leaseSdId)
            );
        }

        // Write VM lease
        if (vm.getLeaseStorageDomainId() != null) {
            writeLease(vm.getId().toString(),
                    vm.getLeaseStorageDomainId().toString(),
                    vm.getLeaseInfo().get(VdsProperties.VmLeasePath),
                    vm.getLeaseInfo().get(VdsProperties.VmLeaseOffset));
        }
    }

    private void writeLease(String key, String lockspace, String path, String offset) {
        writer.writeStartElement("lease");
        writer.writeElement("key", key);
        writer.writeElement("lockspace", lockspace);

        writer.writeStartElement("target");
        writer.writeAttributeString("offset", offset);
        writer.writeAttributeString("path", path);
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeInterfaces(List<VmDevice> devices) {
        Map<VmDeviceId, VmNetworkInterface> devIdToNic = vm.getInterfaces().stream()
                .collect(Collectors.toMap(nic -> new VmDeviceId(nic.getId(), nic.getVmId()), nic -> nic));
        Map<Guid, VnicProfile> vnicProfilesById = Entities.businessEntitiesById(vmInfoBuildUtils.getAllVnicProfiles());

        devices.stream()
                .sorted(Comparator.comparing(dev -> devIdToNic.get(dev.getId()).getMacAddress()))
                .forEach(dev -> {
                            var nic = devIdToNic.get(dev.getId());
                            writeInterface(dev, nic);

                            var vnicProfile = vnicProfilesById.get(nic.getVnicProfileId());
                            if (vnicProfile != null && vnicProfile.getFailoverVnicProfileId() != null) {
                                writeFailoverInterface(vnicProfile.getFailoverVnicProfileId(), nic.getMacAddress());
                            }
                        }
                );

    }

    private void writeFailoverInterface(Guid failoverId, String macAddress) {
        var failoverDevice = VmDeviceCommonUtils.createFailoverVmDevice(failoverId, vm.getId());
        var failoverNic = VmDeviceCommonUtils.createFailoverVmNic(failoverId, vm.getId(), macAddress);
        writeInterface(failoverDevice, failoverNic);
    }

    private void writeDisks(List<VmDevice> devices, Map<Integer, Map<VmDevice, Integer>> vmDeviceVirtioScsiUnitMap) {
        Map<VmDeviceId, VmDevice> deviceIdToDevice = devices.stream()
                .collect(Collectors.toMap(VmDevice::getId, dev -> dev));
        Map<Integer, Map<VmDevice, Integer>> vmDeviceSpaprVscsiUnitMap = vmInfoBuildUtils.getVmDeviceUnitMapForSpaprScsiDisks(vm);
        int hdIndex = -1;
        sdIndex = -1;
        int vdIndex = -1;
        int pinnedDriveIndex = 0;

        Map<Disk, VmDevice> vmDisksToDevices = vm.getDiskMap().values().stream()
                        .map(d -> new Pair<>(d, deviceIdToDevice.get(new VmDeviceId(d.getId(), vm.getId()))))
                        .filter(p -> p.getSecond() != null && p.getSecond().isManaged())
                        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        for (Entry<Disk, VmDevice> diskAndDevice : vmInfoBuildUtils.getSortedDisks(vmDisksToDevices, vm.getId())) {
            Disk disk = diskAndDevice.getKey();
            VmDevice device = diskAndDevice.getValue();
            DiskVmElement dve = disk.getDiskVmElementForVm(vm.getId());
            DiskInterface diskInterface = dve.getDiskInterface();
            int index = 0;
            int pinTo = 0;
            switch(diskInterface) {
            case IDE:
                index = hdIndex = skipCdIndices(++hdIndex, diskInterface);
                break;
            case VirtIO:
                pinTo = vmInfoBuildUtils.pinToIoThreads(vm, pinnedDriveIndex++);
                index = vdIndex = skipCdIndices(++vdIndex, diskInterface);
                break;
            case SPAPR_VSCSI:
            case VirtIO_SCSI:
                vmInfoBuildUtils.calculateAddressForScsiDisk(vm, disk, device, vmDeviceSpaprVscsiUnitMap, vmDeviceVirtioScsiUnitMap);
            case SATA:
                index = sdIndex = skipCdIndices(++sdIndex, diskInterface);
                break;
            }

            String dev = vmInfoBuildUtils.makeDiskName(dve.getDiskInterface().getName(), index);
            writeDisk(device, disk, dve, dev, pinTo);
        }
    }

    private int skipCdIndices(int index, DiskInterface diskInterface) {
        if (Objects.equals(
                vmInfoBuildUtils.diskInterfaceToDevName(diskInterface.getName()),
                vmInfoBuildUtils.diskInterfaceToDevName(cdInterface))) {
            while (index == payloadIndex || index == cdRomIndex) {
                ++index;
            }
        }
        return index;
    }

    private void writeConsole(VmDevice device) {
        // <console type='pty'>
        //   <target type='serial' port='0'/>
        // </console>
        // or:
        // <console type='pty'>
        //   <target type='virtio' port='0'/>
        // </console>
        // or:
        // <console type='unix'>
        //   <source mode='bind' path='/path/to/${vmid}.sock'>
        //   <target type='virtio' port='0'/>
        // </console>
        writer.writeStartElement("console");

        String path = getSerialConsolePath(device);
        if (!path.isEmpty()) {
            writer.writeAttributeString("type", "unix");
            writer.writeStartElement("source");
            writer.writeAttributeString("path", path);
            writer.writeAttributeString("mode", "bind");
            writer.writeEndElement();
        } else {
            writer.writeAttributeString("type", "pty");
        }

        writer.writeStartElement("target");
        Object consoleTypeFromSpecParams = device.getSpecParams().get("consoleType");
        String consoleType = consoleTypeFromSpecParams != null ? consoleTypeFromSpecParams.toString() : "virtio";
        writer.writeAttributeString("type", consoleType);
        writer.writeAttributeString("port", "0");
        writer.writeEndElement();

        writeAlias(device);
        writer.writeEndElement();
    }

    public void writeEmulator() {
        writer.writeStartElement("emulator");
        writer.writeAttributeString("text", String.format("/usr/bin/qemu-system-%s", vm.getClusterArch()));
        writer.writeEndElement();
    }

    private void writeIommu() {
        if (VmInfoBuildUtils.isVmWithHighNumberOfX86Vcpus(vm)) {
            writer.writeStartElement("iommu");
            writer.writeAttributeString("model", "intel");
            writer.writeStartElement("driver");
            writer.writeAttributeString("intremap", "on");
            writer.writeAttributeString("eim", "on");
            if (vmInfoBuildUtils.needsIommuCachingMode(vm, hostDevicesSupplier, vmDevicesSupplier)) {
                writer.writeAttributeString("caching_mode", "on");
            }
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    private void writeSpiceVmcChannel() {
        writer.writeStartElement("channel");
        writer.writeAttributeString("type", "spicevmc");

        writer.writeStartElement("target");
        writer.writeAttributeString("type", "virtio");
        writer.writeAttributeString("name", "com.redhat.spice.0");
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeGuestAgentChannels() {
        // <channel type='unix'>
        //   <target type='virtio' name='org.linux-kvm.port.0'/>
        //   <source mode='bind' path='/tmp/socket'/>
        // </channel>
        if (vmInfoBuildUtils.isOvirtGuestAgent(vm.getVmOsId())) {
            writer.writeStartElement("channel");
            writer.writeAttributeString("type", "unix");

            writer.writeStartElement("target");
            writer.writeAttributeString("type", "virtio");
            writer.writeAttributeString("name", "ovirt-guest-agent.0");
            writer.writeEndElement();

            writer.writeStartElement("source");
            writer.writeAttributeString("mode", "bind");
            writer.writeAttributeString("path", String.format("/var/lib/libvirt/qemu/channels/%s.ovirt-guest-agent.0", vm.getId()));
            writer.writeEndElement();

            writer.writeEndElement();
        }

        writer.writeStartElement("channel");
        writer.writeAttributeString("type", "unix");

        writer.writeStartElement("target");
        writer.writeAttributeString("type", "virtio");
        writer.writeAttributeString("name", "org.qemu.guest_agent.0");
        writer.writeEndElement();

        writer.writeStartElement("source");
        writer.writeAttributeString("mode", "bind");
        writer.writeAttributeString("path", String.format("/var/lib/libvirt/qemu/channels/%s.org.qemu.guest_agent.0", vm.getId()));
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeSerialConsole(String path) {
        if (serialConsolePath == null) {
            return;
        }
        // <serial type='pty'>
        //    <target port='0'>
        // </serial>
        // or:
        // <serial type='unix'>
        //    <source mode='bind'
        //      path='/var/run/ovirt-vmconsole-console/${VMID}.sock'/>
        //    <target port='0'/>
        // </serial>
        writer.writeStartElement("serial");

        if (!path.isEmpty()) {
            writer.writeAttributeString("type", "unix");
            writer.writeStartElement("source");
            writer.writeAttributeString("path", path);
            writer.writeAttributeString("mode", "bind");
            writer.writeEndElement();
        } else {
            writer.writeAttributeString("type", "pty");
        }

        writer.writeStartElement("target");
        writer.writeAttributeString("port", "0");
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private String getSerialConsolePath(VmDevice device) {
        Object enableSocketFromSpecParams = device.getSpecParams().get("enableSocket");
        return enableSocketFromSpecParams != null && Boolean.parseBoolean(enableSocketFromSpecParams.toString()) ?
                String.format("/var/run/ovirt-vmconsole-console/%s.sock", vm.getId())
                : "";
    }

    private void writeHostDevice(VmDevice device, HostDevice hostDevice, List<Pair<VmDevice, HostDevice>> hostDevDisks) {
        switch (hostDevice.getCapability()) {
        case "pci":
            writePciHostDevice(new VmHostDevice(device), hostDevice);
            break;
        case "usb":
        case "usb_device":
            writeUsbHostDevice(new VmHostDevice(device), hostDevice);
            break;
        case "scsi":
            if (SCSI_HOST_DEV_DRIVERS.contains(vmCustomProperties.get("scsi_hostdev"))) {
                hostDevDisks.add(new Pair<>(device, hostDevice));
            } else {
                writeScsiHostDevice(new VmHostDevice(device), hostDevice);
            }
            break;
        case "nvdimm":
            writeNvdimmHostDevice(new VmHostDevice(device), hostDevice);
            break;
        default:
            log.warn("Skipping host device: {}", device.getDevice());
        }
    }

    private void writeHostdevDisks(List<Pair<VmDevice, HostDevice>> hostDevDisks) {
        hostDevDisks.sort(Comparator
                .comparing((Pair<VmDevice, HostDevice> p) -> Integer.parseInt(p.getSecond().getAddress().get("host")))
                .thenComparing(p -> Integer.parseInt(p.getSecond().getAddress().get("bus")))
                .thenComparing(p-> Integer.parseInt(p.getSecond().getAddress().get("target")))
                .thenComparing(p-> Integer.parseInt(p.getSecond().getAddress().get("lun")))
        );

        for (Pair<VmDevice, HostDevice> pair : hostDevDisks) {
            sdIndex = skipCdIndices(++sdIndex, DiskInterface.VirtIO_SCSI);
            String diskName = vmInfoBuildUtils.makeDiskName("scsi", sdIndex);

            writeScsiHostDevAsDisk(new VmHostDevice(pair.getFirst()), pair.getSecond(), diskName);
        }
    }

    private void writeScsiHostDevAsDisk(VmHostDevice device, HostDevice hostDevice, String diskName) {
        String scsiHostdevProperty = vmCustomProperties.get("scsi_hostdev");

        writer.writeStartElement("disk");
        writer.writeAttributeString("type", "block");
        // Adding snapshot='no' attribute to prevent libvirt from adding this disk from the domxml. This will prevent
        // libvirt from selecting this disk as a target to snapshot. Snapshots of passthrough disks are not allowed.
        writer.writeAttributeString("snapshot", "no");

        if (SCSI_VIRTIO_BLK_PCI.equals(scsiHostdevProperty) && legacyVirtio) {
            writer.writeAttributeString("model", "virtio-transitional");
        }
        if (SCSI_BLOCK.equals(scsiHostdevProperty)) {
            writer.writeAttributeString("device", "lun");
            writer.writeAttributeString("rawio", "yes");
        } else {
            writer.writeAttributeString("device", "disk");
        }

        writer.writeStartElement("driver");
        writer.writeAttributeString("name", "qemu");
        writer.writeAttributeString("type", "raw");
        writer.writeEndElement(); // driver

        writer.writeStartElement("source");
        writer.writeAttributeString("dev", hostDevice.getBlockPath());
        writer.writeStartElement("seclabel");
        writer.writeAttributeString("model", "dac");
        writer.writeAttributeString("type", "none");
        writer.writeAttributeString("relabel", "yes");
        writer.writeEndElement(); // seclabel
        writer.writeEndElement(); // source

        if (SCSI_HD.equals(scsiHostdevProperty)) {
            writer.writeStartElement("blockio");
            writer.writeAttributeString("logical_block_size", "512");
            writer.writeAttributeString("physical_block_size", "4096");
            writer.writeEndElement(); // blockio
        }

        writer.writeStartElement("target");

        if (SCSI_VIRTIO_BLK_PCI.equals(scsiHostdevProperty)) {
            writer.writeAttributeString("dev", diskName.replaceFirst("s", "v"));
            writer.writeAttributeString("bus", "virtio");
        } else {
            writer.writeAttributeString("dev", diskName);
            writer.writeAttributeString("bus", "scsi");
        }
        writer.writeEndElement(); // target

        writeAlias(device);
        if (SCSI_VIRTIO_BLK_PCI.equals(scsiHostdevProperty)) {
            var addressMap = StringMapUtils.string2Map(device.getAddress());
            if ("pci".equals(addressMap.get("type"))) {
                writeAddress(addressMap);
            }
        } else {
            writeAddress(buildDriveAddress(hostDevice.getAddress()));
        }

        writer.writeEndElement(); // disk
    }

    private Map<String, String> buildDriveAddress(Map<String, String> address) {
        Map<String, String> diskAddress = new HashMap<>();
        diskAddress.put("bus", address.get("bus"));
        diskAddress.put("target", address.get("target"));
        diskAddress.put("controller", address.get("host"));
        diskAddress.put("unit", address.get("lun"));
        diskAddress.put("type", "drive");
        return diskAddress;
    }

    private void writeScsiHostDevice(VmHostDevice device, HostDevice hostDevice) {
        // Create domxml for a host device.
        //
        // <hostdev managed="no" mode="subsystem" rawio="yes" type="scsi">
        // <source>
        // <adapter name="scsi_host4"/>
        // <address bus="0" target="0" unit="0"/>
        // </source>
        // </hostdev>
        writer.writeStartElement("hostdev");
        writer.writeAttributeString("managed", "no");
        writer.writeAttributeString("mode", "subsystem");
        writer.writeAttributeString("rawio", "yes");
        writer.writeAttributeString("type", "scsi");

        writer.writeStartElement("source");
        writer.writeStartElement("adapter");
        writer.writeAttributeString("name", String.format("scsi_host%s", hostDevice.getAddress().get("host")));
        writer.writeEndElement();
        writer.writeStartElement("address");
        writer.writeAttributeString("bus", hostDevice.getAddress().get("bus"));
        writer.writeAttributeString("target", hostDevice.getAddress().get("target"));
        writer.writeAttributeString("unit", hostDevice.getAddress().get("lun"));
        writer.writeEndElement();
        writer.writeEndElement();

        writeAlias(device);
        // TODO: boot
        writer.writeEndElement();
    }

    private void writeUsbHostDevice(VmHostDevice device, HostDevice hostDevice) {
        // Create domxml for a host device.
        //
        // <hostdev managed="no" mode="subsystem" type="usb">
        //     <source>
        //         <address bus="1" device="2"/>
        //     </source>
        // </hostdev>
        writer.writeStartElement("hostdev");
        writer.writeAttributeString("managed", "no");
        writer.writeAttributeString("mode", "subsystem");
        writer.writeAttributeString("type", "usb");

        writer.writeStartElement("source");
        writer.writeStartElement("address");
        writer.writeAttributeString("bus", hostDevice.getAddress().get("bus"));
        writer.writeAttributeString("device", hostDevice.getAddress().get("device"));
        writer.writeEndElement();
        writer.writeEndElement();

        writeAlias(device);
        writeAddress(device);
        // TODO: boot
        writer.writeEndElement();
    }

    private void writePciHostDevice(VmHostDevice device, HostDevice hostDevice) {
        // <hostdev mode='subsystem' type='pci' managed='no'>
        // <source>
        // <address domain='0x0000' bus='0x06' slot='0x02'
        // function='0x0'/>
        // </source>
        // <boot order='1'/>
        // </hostdev>
        if (device.isIommuPlaceholder()) {
            return;
        }

        writer.writeStartElement("hostdev");
        writer.writeAttributeString("managed", "no");
        writer.writeAttributeString("mode", "subsystem");
        writer.writeAttributeString("type", "pci");

        writer.writeStartElement("source");
        writer.writeStartElement("address");
        writer.writeAttributeString("domain", hostDevice.getAddress().get("domain"));
        writer.writeAttributeString("bus", hostDevice.getAddress().get("bus"));
        writer.writeAttributeString("slot", hostDevice.getAddress().get("slot"));
        writer.writeAttributeString("function", hostDevice.getAddress().get("function"));
        writer.writeEndElement();
        writer.writeEndElement();

        writeAlias(device);
        writeAddress(device);
        // TODO: boot
        writer.writeEndElement();
    }

    private void writeNvdimmHostDevice(VmHostDevice device, HostDevice hostDevice) {
        Map<String, Object> specParams = hostDevice.getSpecParams();
        String mode = (String)specParams.get(VdsProperties.MODE);
        String numaNode = (String)specParams.get(VdsProperties.NUMA_NODE);
        String targetNode = vmInfoBuildUtils.getMatchingNumaNode(getNumaTuneSetting(), vmNumaNodesSupplier, numaNode);
        if (targetNode == null) {
            log.error("No NUMA node, cannot add NVDIMM devices");
            return;
        }
        Long alignSize = (Long)specParams.get(VdsProperties.ALIGN_SIZE);
        if (alignSize == null) {
            // If we didn't specify alignsize, libvirt would select one based on memory page size.
            // Better to set it ourselves, to the memory block size.
            // See also VmInfoBuildUtils::getNvdimmAlignedSize().
            alignSize = new Long(vm.getClusterArch().getHotplugMemorySizeFactorMb() * 1024 * 1024);
        }
        Long size = vmInfoBuildUtils.getNvdimmAlignedSize(vm, hostDevice);
        if (size == null) {
            log.error("Invalid alignment, cannot add NVDIMM device");
            return;
        }

        // <memory model='nvdimm' access='shared'>
        //   <source>
        //     <path>/dev/pmem0</path>
        //     <alignsize unit='KiB'>2048</alignsize>
        //     <pmem/>
        //   </source>
        //   <target>
        //     <size unit='KiB'>16646144</size>
        //     <node>0</node>
        //     <label>
        //       <size unit='KiB'>128</size>
        //     </label>
        //   </target>
        //   <alias name="ua-1234"/>
        // </memory>
        writer.writeStartElement("memory");
        writer.writeAttributeString("model", "nvdimm");
        writer.writeAttributeString("access", "shared");

        writer.writeStartElement("source");
        writer.writeElement("path", (String)specParams.get(VdsProperties.DEVICE_PATH));
        writer.writeStartElement("alignsize");
        writer.writeAttributeString("unit", "KiB");
        writer.writeRaw(String.valueOf(alignSize / 1024));
        writer.writeEndElement();  // alignsize
        writer.writeElement("pmem");
        writer.writeEndElement();  // source

        writer.writeStartElement("target");
        writer.writeStartElement("size");
        writer.writeAttributeString("unit", "KiB");
        writer.writeRaw(String.valueOf(size / 1024));
        writer.writeEndElement();  // size
        writer.writeElement("node", targetNode);
        writer.writeStartElement("label");
        writer.writeStartElement("size");
        writer.writeAttributeString("unit", "KiB");
        writer.writeRaw(String.valueOf(VmInfoBuildUtils.NVDIMM_LABEL_SIZE / 1024));
        writer.writeEndElement();  // size
        writer.writeEndElement();  // label
        writer.writeEndElement();  // target

        writeAlias(device);
        writer.writeEndElement();  // memory
    }

    private void writeRedir(VmDevice device) {
        // <redirdev bus='usb' type='spicevmc'>
        //   <address type='usb' bus='0' port='1'/>
        // </redirdev>
        writer.writeStartElement("redirdev");
        writer.writeAttributeString("type", "spicevmc");
        writer.writeAttributeString("bus", "usb");
        writeAlias(device);
        writeAddress(device);
        writer.writeEndElement();
    }

    private void writeRng(VmDevice device) {
        // <rng model='virtio'>
        //   <rate period="2000" bytes="1234"/>
        //   <backend model='random'>/dev/random</backend>
        // </rng>
        writer.writeStartElement("rng");
        if (legacyVirtio) {
            writer.writeAttributeString("model", "virtio-transitional");
        } else {
            writer.writeAttributeString("model", "virtio");
        }

        Map<String, Object> specParams = device.getSpecParams();
        if (specParams.containsKey("bytes")) {
            writer.writeStartElement("rate");
            writer.writeAttributeString("bytes", specParams.get("bytes").toString());
            if (specParams.containsKey("period")) {
                writer.writeAttributeString("period", specParams.get("period").toString());
            }
            writer.writeEndElement();
        }

        writer.writeStartElement("backend");
        writer.writeAttributeString("model", "random");
        switch(specParams.get("source").toString()) {
        case "random":
            writer.writeRaw("/dev/random");
            break;
        case "urandom":
            writer.writeRaw("/dev/urandom");
            break;
        case "hwrng":
            writer.writeRaw("/dev/hwrng");
            break;
        }
        writer.writeEndElement();

        writeAlias(device);
        writer.writeEndElement();
    }

    private void writeTpm(VmDevice device) {
        writer.writeStartElement("tpm");
        if (vm.getClusterArch().getFamily() == ArchitectureType.x86) {
            writer.writeAttributeString("model", "tpm-crb");
        }
        writeAlias(device);
        writer.writeStartElement("backend");
        writer.writeAttributeString("type", "emulator");
        writer.writeAttributeString("version", "2.0");
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeSound(VmDevice device) {
        writer.writeStartElement("sound");
        writer.writeAttributeString("model", device.getDevice());
        writeAlias(device);
        writeAddress(device);
        writer.writeEndElement();
    }

    private void writeGraphics(VmDevice device) {
        GraphicsType graphicsType = GraphicsType.fromString(device.getDevice());
        if (graphicsType == null) {
            log.error("Unsupported graphics type: {}", device.getDevice());
            return;
        }

        // <graphics type='spice' port='5900' tlsPort='5901' autoport='yes'
        //           listen='0' keymap='en-us'
        //           passwdValidTo='1970-01-01T00:00:01'>
        //   <listen type='address' address='0'/>
        //   <clipboard copypaste='no'/>
        // </graphics>
        // or:
        // <graphics type='vnc' port='5900' autoport='yes' listen='0'
        //           keymap='en-us' passwdValidTo='1970-01-01T00:00:01'>
        //   <listen type='address' address='0'/>
        // </graphics>
        writer.writeStartElement("graphics");
        writer.writeAttributeString("type", device.getDevice());
        writer.writeAttributeString("port", String.valueOf(LIBVIRT_PORT_AUTOSELECT));
        writer.writeAttributeString("autoport", "yes");
        // TODO: defaultMode
        if (graphicsType == GraphicsType.SPICE  // SPICE always needs password
                || !kernelFipsModeSupplier.get()) {
            writer.writeAttributeString("passwd", "*****");
            writer.writeAttributeString("passwdValidTo", "1970-01-01T00:00:01");
        }

        Network displayNetwork = vmInfoBuildUtils.getDisplayNetwork(vm);
        if (displayNetwork == null) {
            writer.writeAttributeString("listen", "0");
        }

        switch (graphicsType) {
        case SPICE:
            writer.writeAttributeString("tlsPort", String.valueOf(LIBVIRT_PORT_AUTOSELECT));

            if (!vm.isSpiceFileTransferEnabled()) {
                writer.writeStartElement("filetransfer");
                writer.writeAttributeString("enable", "no");
                writer.writeEndElement();
            }

            if (!vm.isSpiceCopyPasteEnabled()) {
                writer.writeStartElement("clipboard");
                writer.writeAttributeString("copypaste", "no");
                writer.writeEndElement();
            }

            if ((boolean) Config.getValue(ConfigValues.SSLEnabled)) {
                String channels = Config.getValue(ConfigValues.SpiceSecureChannels, vm.getCompatibilityVersion().toString());
                adjustSpiceSecureChannels(channels.split(",")).forEach(channel -> {
                    writer.writeStartElement("channel");
                    writer.writeAttributeString("name", channel);
                    writer.writeAttributeString("mode", "secure");
                    writer.writeEndElement();
                });
            }

            break;

        case VNC:
            writer.writeAttributeString("keymap",
                    vm.getDynamicData().getVncKeyboardLayout() != null ?
                            vm.getDynamicData().getVncKeyboardLayout()
                            : vm.getDefaultVncKeyboardLayout() != null ?
                                    vm.getDefaultVncKeyboardLayout()
                                    : Config.getValue(ConfigValues.VncKeyboardLayout));

            break;
        }

        if (displayNetwork != null) {
            writer.writeStartElement("listen");
            String displayIp = (String) device.getSpecParams().get("displayIp");
            if (displayIp == null) {
                writer.writeAttributeString("type", "network");
                writer.writeAttributeString("network", String.format("vdsm-%s", displayNetwork.getVdsmName()));
            } else {
                writer.writeAttributeString("type", "address");
                writer.writeAttributeString("address", displayIp);
            }

            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    protected static Stream<String> adjustSpiceSecureChannels(String[] spiceSecureChannels) {
        return Arrays.stream(spiceSecureChannels)
                .map(channel -> {
                    if (SPICE_CHANNEL_NAMES.contains(channel)) {
                        return channel;
                    }
                    String legacyChannel;
                    if (channel.startsWith("s") && SPICE_CHANNEL_NAMES.contains(legacyChannel = channel.substring(1))) {
                        return legacyChannel;
                    }
                    log.warn("Unsupported spice channel name {}, ignoring", channel);
                    return null;
                })
                .filter(Objects::nonNull);
    }

    private void writePciEControllers(boolean rootExists, int ports) {
        if (!rootExists) {
            writer.writeStartElement("controller");
            writer.writeAttributeString("type", "pci");
            writer.writeAttributeString("model", "pcie-root");
            writer.writeEndElement();
        }

        int numOfPorts = Config.<Integer> getValue(ConfigValues.NumOfPciExpressPorts);
        for (int i = ports; i < numOfPorts; i++) {
            writer.writeStartElement("controller");
            writer.writeAttributeString("type", "pci");
            writer.writeAttributeString("model", "pcie-root-port");
            writer.writeEndElement();
        }
    }

    private void writeController(VmDevice device, Map<Integer, Map<VmDevice, Integer>> vmDeviceVirtioScsiUnitMap) {
        writer.writeStartElement("controller");
        writer.writeAttributeString("type", device.getDevice());

        Object model = device.getSpecParams().get(VdsProperties.Model);
        if (model != null) {
            writer.writeAttributeString("model", model.toString());
        }
        Object index = device.getSpecParams().get(VdsProperties.Index);
        if (index != null) {
            writer.writeAttributeString("index", index.toString());
        }
        Object ports = device.getSpecParams().get("ports");
        if (ports != null) {
            writer.writeAttributeString("ports", ports.toString());
        }
        Object ioThreadId = device.getSpecParams().get(VdsProperties.ioThreadId);

        // Add multiple queues support to the virtio-scsi controller
        int queues = 0;

        if ("virtio-scsi".equals(model)) {
            if (vm.getVirtioScsiMultiQueues() == -1) {
                int numOfDisks = 0;
                if (index != null && vmDeviceVirtioScsiUnitMap.containsKey(index)) {
                    numOfDisks = vmDeviceVirtioScsiUnitMap.get(index).size();
                }
                queues = vmInfoBuildUtils.getNumOfScsiQueues(numOfDisks, VmCpuCountHelper.getDynamicNumOfCpu(vm));
            } else if (vm.getVirtioScsiMultiQueues() > 0) {
                queues = vm.getVirtioScsiMultiQueues();
            }
        }

        if (ioThreadId != null || queues > 0) {
            writer.writeStartElement("driver");
            if (queues > 0) {
                writer.writeAttributeString("queues", String.valueOf(queues));
            }
            if (ioThreadId != null) {
                writer.writeAttributeString("iothread", ioThreadId.toString());
            }
            writer.writeEndElement();
        }

        writeAlias(device);

        writeAddress(device);
        writer.writeEndElement();
    }

    /**
     * TODO:
     * add qemu_drive_cache configurable like in VDSM?
     */
    private void writeDisk(
            VmDevice device,
            Disk disk,
            DiskVmElement dve,
            String dev,
            int pinTo) {
        // <disk type='file' device='disk' snapshot='no'>
        //   <driver name='qemu' type='qcow2' cache='none'/>
        //   <source file='/path/to/image'/>
        //   <target dev='hda' bus='ide'/>
        //   <serial>54-a672-23e5b495a9ea</serial>
        // </disk>
        writer.writeStartElement("disk");

        writeGeneralDiskAttributes(device, disk, dve);
        writeDiskTarget(dve, dev);
        writeDiskSource(device, disk, dev, dve);
        writeDiskDriver(device, disk, dve, pinTo);
        writeAlias(device);
        writeAddress(device);
        writeBootOrder(device.getBootOrder());

        if (disk.getDiskStorageType() != DiskStorageType.LUN || !disk.isScsiPassthrough()) {
            writer.writeElement("serial", disk.getId().toString());
        }

        if (device.getReadOnly()) {
            writer.writeElement("readonly");
        }

        if (device.getSnapshotId() == null && disk.isShareable()) {
            writer.writeElement("shareable");
        }

        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            writeIoTune((DiskImage) disk);
        }

        if (disk.getDiskStorageType() == DiskStorageType.CINDER /** && case RBD */) {
            writeNetworkDiskAuth((CinderDisk) disk);
        }

        writer.writeEndElement();
    }

    private void writeQemuCapabilities() {
        // TODO: using the qemu namespace XML override is needed in order
        // to use experimental incremental backup feature, should be removed
        // when the feature will be fully supported by libvirt -
        // <qemu:capabilities>
        //   <qemu:add capability='blockdev'/>
        //   <qemu:add capability='incremental-backup'/>
        // </qemu:capabilities>

        if (incrementalBackupSupplier.get()) {
            writer.writeStartElement(QEMU_URI, "capabilities");

            writer.writeStartElement(QEMU_URI, "add");
            writer.writeAttributeString("capability", "blockdev");
            writer.writeEndElement();

            writer.writeStartElement(QEMU_URI, "add");
            writer.writeAttributeString("capability", "incremental-backup");
            writer.writeEndElement();

            writer.writeEndElement();
        }
    }

    private void writeNetworkDiskAuth(CinderDisk cinderDisk) {
        Map<String, Object> connectionInfoData = cinderDisk.getCinderConnectionInfo().getData();
        boolean authEnabled = (boolean) connectionInfoData.get(VdsProperties.CinderAuthEnabled);
        if (authEnabled) {
            writer.writeStartElement("auth");
            writer.writeAttributeString("username", (String) connectionInfoData.get(VdsProperties.CinderAuthUsername));

            writer.writeStartElement("secret");
            writer.writeAttributeString("type", (String) connectionInfoData.get(VdsProperties.CinderSecretType));
            writer.writeAttributeString("uuid", (String) connectionInfoData.get(VdsProperties.CinderSecretUuid));
            writer.writeEndElement();

            writer.writeEndElement();
        }
    }

    private void writeIoTune(DiskImage diskImage) {
        if (!qosCache.containsKey(diskImage.getDiskProfileId())) {
            qosCache.put(diskImage.getDiskProfileId(), vmInfoBuildUtils.loadStorageQos(diskImage));
        }
        StorageQos storageQos = qosCache.get(diskImage.getDiskProfileId());
        if (storageQos == null) {
            return;
        }
        writer.writeStartElement("iotune");
        ioTuneMapFrom(storageQos).forEach((key, val) -> writer.writeElement(key, val.toString()));
        writer.writeEndElement();
    }

    private void writeDiskDriver(VmDevice device, Disk disk, DiskVmElement dve, int pinTo) {
        writer.writeStartElement("driver");
        writer.writeAttributeString("name", "qemu");
        if (dve.isPassDiscard()) {
            writer.writeAttributeString("discard", "unmap");
        }
        if (pinTo > 0) {
            writer.writeAttributeString("iothread", String.valueOf(pinTo));
        }
        /**
        When the disk propagate error is set to On, we need to ensure that the proper
        error policy is set.
        For IMAGE, we need to use enospace since image may be thin provisioned and requires
        handling of enospace in vdsm. Using report will break the thin provisioning.
        For LUN and Cinder, enospace is incorrect since the system cannot handle this value. This
        must be handled by the guest. For these reasons, report is the right value.
        Here is link to the thread in Ovirt Development -
        https://lists.ovirt.org/archives/list/devel@ovirt.org/thread/YY56B5LCNO6ROSUPDWWHGKGUQVOLHCAR/
        **/
        boolean nativeIO = false;
        switch (disk.getDiskStorageType()) {
        case IMAGE:
            DiskImage diskImage = (DiskImage) disk;
            nativeIO =  vmInfoBuildUtils.shouldUseNativeIO(vm, diskImage, device);
            writer.writeAttributeString("io", nativeIO ? "native" : "threads");
            writer.writeAttributeString("type", diskImage.getVolumeFormat() == VolumeFormat.COW ? "qcow2" : "raw");
            writer.writeAttributeString("error_policy", disk.getPropagateErrors() == PropagateErrors.On ? "enospace" : "stop");
            break;

        case LUN:
            nativeIO = true;
            writer.writeAttributeString("io", "native");
            writer.writeAttributeString("type", "raw");
            writer.writeAttributeString("error_policy", disk.getPropagateErrors() == PropagateErrors.On ? "report" : "stop");
            break;

        case CINDER:
            // case RBD
            writer.writeAttributeString("io", "threads");
            writer.writeAttributeString("type", "raw");
            writer.writeAttributeString("error_policy", disk.getPropagateErrors() == PropagateErrors.On ? "report" : "stop");
            break;
        }

        if (device.getSnapshotId() != null) { // transient disk
            /**
            Force the cache to be writethrough, which is qemu's default.
            This is done to ensure that we don't ever use cache=none for
            transient disks, since we create them in /var/run/vdsm which
            may end up on tmpfs and don't support O_DIRECT, and qemu uses
            O_DIRECT when cache=none and hence hotplug might fail with
            error that one can take eternity to debug the reason behind it!
             */
            writer.writeAttributeString("cache", "writethrough");
        } else {
            switch (dve.getDiskInterface()) {
            case VirtIO:
            case VirtIO_SCSI:
                String viodiskcache = vmCustomProperties.get("viodiskcache");
                if (viodiskcache != null && !nativeIO) {
                    writer.writeAttributeString("cache", viodiskcache);
                    break;
                }
            default:
                writer.writeAttributeString("cache", "none");
            }
        }

        writer.writeEndElement();
    }

    private void writeDiskSource(VmDevice device, Disk disk, String dev, DiskVmElement dve) {
        writer.writeStartElement("source");
        switch (disk.getDiskStorageType()) {
        case IMAGE:
            DiskImage diskImage = (DiskImage) disk;

            // Change parameters for the HE disk
            if (vm.isHostedEngine()) {
                // Hosted engine disk images have to have empty storage pool ID,
                // so they can be mounted even if storage pool is not connected.
                diskImage.setStoragePoolId(Guid.Empty);
                diskImage.setPropagateErrors(PropagateErrors.Off);

                // The disk requires a lease
                addVolumeLease(diskImage.getImageId(), diskImage.getStorageIds().get(0));
            }

            String diskType = this.vmInfoBuildUtils.getDiskType(this.vm, diskImage, device);

            switch (diskType) {
            case "block":
                writer.writeAttributeString(
                        "dev", vmInfoBuildUtils.getPathToImage(diskImage));
                break;
            case "network":
                String[] volInfo = vmInfoBuildUtils.getGlusterVolInfo(disk);
                // Sometimes gluster methods return garbage instead of
                // correct volume info string and, as we can't parse it,
                // we will have a null volInfo. In that case we will just
                //drop to the 'file' case as a fallback.
                if (volInfo != null) {
                    writer.writeAttributeString("protocol", "gluster");
                    writer.writeAttributeString(
                            "name",
                            String.format("%s/%s/images/%s/%s",
                                    volInfo[1],
                                    diskImage.getStorageIds().get(0),
                                    diskImage.getId(),
                                    diskImage.getImageId()));
                    writer.writeStartElement("host");
                    writer.writeAttributeString("name", volInfo[0]);
                    writer.writeAttributeString("port", "0");
                    writer.writeEndElement();
                    break;
                }
            case "file":
                writer.writeAttributeString(
                        "file", vmInfoBuildUtils.getPathToImage(diskImage));
                break;
            }
            diskMetadata.put(dev, createDiskParams(diskImage));

            break;

        case LUN:
            LunDisk lunDisk = (LunDisk) disk;
            writer.writeAttributeString(
                    "dev",
                    String.format("/dev/mapper/%s",
                            lunDisk.getLun().getLUNId()));
            diskMetadata.put(dev, Collections.singletonMap("GUID", lunDisk.getLun().getLUNId()));

            if (FeatureSupported.isScsiReservationSupported(vm.getCompatibilityVersion()) &&
                        dve.isUsingScsiReservation()) {
                writer.writeStartElement("reservations");
                writer.writeAttributeString("managed", "yes");
                writer.writeEndElement();
            }

            break;

        case CINDER:
            // case RBD
            CinderDisk cinderDisk = (CinderDisk) disk;
            Map<String, Object> connectionInfoData = cinderDisk.getCinderConnectionInfo().getData();
            writer.writeAttributeString("protocol", cinderDisk.getCinderConnectionInfo().getDriverVolumeType());
            writer.writeAttributeString("name", connectionInfoData.get("name").toString());
            List<String> hostAddresses = (List<String>) connectionInfoData.get("hosts");
            List<String> hostPorts = (List<String>) connectionInfoData.get("ports");
            // Looping over hosts addresses to create 'hosts' element
            // (Cinder should ensure that the addresses and ports lists are synced in order).
            for (int i = 0; i < hostAddresses.size(); i++) {
                writer.writeStartElement("host");
                writer.writeAttributeString("name", hostAddresses.get(i));
                writer.writeAttributeString("port", hostPorts.get(i));
                //  If no transport is specified, "tcp" is assumed.
                writer.writeEndElement();
            }
            break;

        case MANAGED_BLOCK_STORAGE:
            ManagedBlockStorageDisk managedBlockStorageDisk = (ManagedBlockStorageDisk) disk;
            Map<String, String> metadata = new HashMap<>();
            String path = (String) managedBlockStorageDisk.getDevice().get(DeviceInfoReturn.PATH);

            if (managedBlockStorageDisk.getCinderVolumeDriver() == CinderVolumeDriver.RBD) {
                // For rbd we need to pass the entire path since we rely on more than a single
                // variable e.g: /dev/rbd/<pool-name>/<vol-name>
                metadata = Collections.singletonMap("RBD", path);
            } else if (managedBlockStorageDisk.getCinderVolumeDriver() == CinderVolumeDriver.BLOCK) {
                Map<String, Object> attachment =
                        (Map<String, Object>) managedBlockStorageDisk.getDevice().get(DeviceInfoReturn.ATTACHMENT);
                metadata = Map.of(
                        "GUID", (String)attachment.get(DeviceInfoReturn.SCSI_WWN),
                        "managed", "true"
                );
            }

            writer.writeAttributeString("dev", path);
            diskMetadata.put(dev, metadata);

            break;
        }

        if (disk.getDiskStorageType() != DiskStorageType.CINDER /** && ! RBD */) {
            writeSeclabel();
        }

        writer.writeEndElement();
    }

    private void writeSeclabel() {
        // We need to make sure that libvirt DAC (file system permission driver)
        // is disabled for disks:
        //   model='dac' -- dac is the file system permissions driver
        //   type='none' -- type is currently used for SELinux/AppArmor drivers
        //   relabel='no' -- disable the change of permissions
        // See https://libvirt.org/formatdomain.html#seclabel for more details.
        writer.writeStartElement("seclabel");
        writer.writeAttributeString("model", "dac");
        writer.writeAttributeString("type", "none");
        writer.writeAttributeString("relabel", "no");
        writer.writeEndElement();
    }

    private void addVolumeLease(Guid leaseId, Guid leaseSdId) {
        // Volume leases are currently used only for the hosted engine VM,
        // so the list is created only when needed.
        if (volumeLeases.isEmpty()) {
            volumeLeases = new ArrayList<>(1);
        }

        volumeLeases.add(new Pair<>(leaseId, leaseSdId));
    }

    private Map<String, String> createDiskParams(DiskImage diskImage) {
        Map<String, String> diskParams = vmInfoBuildUtils.createDiskUuidsMap(diskImage);

        if (!diskImage.getActive()) {
            diskParams.put(VdsProperties.Shareable, VdsProperties.Transient);
        } else if (diskImage.isShareable()) {
            diskParams.put(VdsProperties.Shareable, VdsProperties.Shareable);
        } else if (vm.isHostedEngine()) {
            diskParams.put(VdsProperties.Shareable, VdsProperties.Exclusive);
        }

        return diskParams;
    }

    private void writeDiskTarget(DiskVmElement dve, String dev) {
        writer.writeStartElement("target");
        writer.writeAttributeString("dev", dev);
        writer.writeAttributeString("bus", dve.getDiskInterface().getName());
        writer.writeEndElement();
    }

    private void writeGeneralDiskAttributes(VmDevice device, Disk disk, DiskVmElement dve) {
        // Adding snapshot='no' attribute to prevent from libvirt adding this disk from the domxml. This will prevent
        // libvirt from selecting this disk as a target to snapshot. When we do execute snapshot, we specify the disks
        // we wish to take a snapshot for explicitly.
        writer.writeAttributeString("snapshot", "no");

        if (dve.getDiskInterface() == DiskInterface.VirtIO && legacyVirtio) {
            writer.writeAttributeString("model", "virtio-transitional");
        }

        switch (disk.getDiskStorageType()) {
        case IMAGE:
            writer.writeAttributeString("type", this.vmInfoBuildUtils.getDiskType(this.vm, (DiskImage) disk, device));
            break;
        case LUN:
            writer.writeAttributeString("type", "block");
            break;
        case CINDER:
            // case RBD
            writer.writeAttributeString("type", "network");
            break;
        case MANAGED_BLOCK_STORAGE:
            writer.writeAttributeString("type", "block");
            break;
        }

        switch (dve.getDiskInterface()) {
        case VirtIO_SCSI:
            if (disk.getDiskStorageType() == DiskStorageType.LUN && disk.isScsiPassthrough()) {
                writer.writeAttributeString("device", VmDeviceType.LUN.getName());
                writer.writeAttributeString("sgio", disk.getSgio().toString().toLowerCase());
                break;
            }
        default:
            writer.writeAttributeString("device", device.getDevice());
        }
    }

    private void writeFloppy(VmDevice device) {
        if (device == null) {
            return;
        }
        // <disk device="floppy" snapshot="no" type="file">
        //   <source file="/var/run/vdsm/payload/8b5fa6b8-9c57-4d7c-80cb-64537eea560f.6e38a5ccb3c6b2b674086e9d07126a03.img" startupPolicy="optional">
        //     <seclabel model='dac' relabel='no' type='none'/>
        //   </source>
        //   <target bus="fdc" dev="fda" />
        //   <readonly />
        // </disk>
        writer.writeStartElement("disk");
        writer.writeAttributeString("type", "file");
        writer.writeAttributeString("device", "floppy");
        writer.writeAttributeString("snapshot", "no");

        final boolean payload = VmPayload.isPayload(device.getSpecParams());
        writer.writeStartElement("source");
        writer.writeAttributeString("file", payload ? "PAYLOAD:" : vm.getFloppyPath());
        writer.writeAttributeString("startupPolicy", "optional");
        writeSeclabel();
        writer.writeEndElement();

        writer.writeStartElement("target");
        String name = vmInfoBuildUtils.makeDiskName(VdsProperties.Fdc, 0);  // IDE slot 2 is reserved by VDSM to CDROM
        writer.writeAttributeString("dev", name);
        writer.writeAttributeString("bus", VdsProperties.Fdc);
        writer.writeEndElement();

        writer.writeElement("readonly");

        writeAlias(device);
        writeAddress(device);

        if (payload) {
            payloadMetadata = new Pair<>(name, new VmPayload(device));
        }

        writer.writeEndElement();
    }

    private void writeCdRom(List<VmDevice> devices) {
        // <disk type='file' device='cdrom' snapshot='no'>
        //   <driver name='qemu' type='raw' error_policy='report' />
        //   <source file='<path>' startupPolicy='optional'/>
        //     <seclabel model='dac' relabel='no' type='none'/>
        //   </source>
        //   <target dev='hdc' bus='ide'/>
        //   <readonly/>
        //   <address type='drive' controller='0' bus='1' target='0' unit='0'/>
        // </disk>
        devices.stream().filter(d -> VmPayload.isPayload(d.getSpecParams())).forEach(device -> {
            writer.writeStartElement("disk");
            writer.writeAttributeString("type", "file");
            writer.writeAttributeString("device", "cdrom");
            writer.writeAttributeString("snapshot", "no");

            writer.writeStartElement("driver");
            writer.writeAttributeString("name", "qemu");
            writer.writeAttributeString("type", "raw");
            writer.writeAttributeString("error_policy", "report");
            writer.writeEndElement();

            writer.writeStartElement("source");
            writer.writeAttributeString("file", "PAYLOAD:");
            writer.writeAttributeString("startupPolicy", "optional");
            writeSeclabel();
            writer.writeEndElement();

            payloadIndex = VmDeviceCommonUtils.getCdPayloadDeviceIndex(cdInterface);

            writer.writeStartElement("target");
            String name = vmInfoBuildUtils.makeDiskName(cdInterface, payloadIndex);
            writer.writeAttributeString("dev", name);
            writer.writeAttributeString("bus", cdInterface);
            writer.writeEndElement();

            writer.writeElement("readonly");

            writeAlias(device);
            if ("scsi".equals(cdInterface)) {
                int index = VmDeviceCommonUtils.getCdPayloadDeviceIndex(cdInterface);
                writeAddress(vmInfoBuildUtils.createAddressForScsiDisk(0, index));
            }

            payloadMetadata = new Pair<>(name, new VmPayload(device));
            writer.writeEndElement();
        });

        // add devices that points to vm.getCdPath() and vm.getWgtCdPath
        Iterator<String> cdPaths = List.of(
                vm.getCdPath() != null ? vm.getCdPath() : "", vm.getWgtCdPath() != null ? vm.getWgtCdPath() : "")
                .iterator();
        int cdRomCounter = 0;
        for (VmDevice device : devices) {
            if (VmPayload.isPayload(device.getSpecParams())) {
                continue;
            }
            if (!cdPaths.hasNext()) {
                break;
            }
            String cdPath = cdPaths.next();
            cdRomIndex = VmDeviceCommonUtils.getCdDeviceIndex(cdInterface) + cdRomCounter;
            String dev = vmInfoBuildUtils.makeDiskName(cdInterface, cdRomIndex);

            boolean isoOnBlockDomain = vmInfoBuildUtils.isBlockDomainPath(cdPath);
            if (isoOnBlockDomain) {
                diskMetadata.put(dev, vmInfoBuildUtils.createDiskUuidsMap(vm, cdPath));
            }

            writer.writeStartElement("disk");
            writer.writeAttributeString("type", isoOnBlockDomain ? "block" : "file");
            writer.writeAttributeString("device", "cdrom");
            writer.writeAttributeString("snapshot", "no");

            writer.writeStartElement("driver");
            writer.writeAttributeString("name", "qemu");
            writer.writeAttributeString("type", "raw");
            writer.writeAttributeString("error_policy", "report");
            writer.writeEndElement();

            writer.writeStartElement("source");
            writer.writeAttributeString(isoOnBlockDomain ? "dev" : "file", cdPath);
            if (!isoOnBlockDomain) {
                writer.writeAttributeString("startupPolicy", "optional");
            }
            writeSeclabel();
            writer.writeEndElement();

            writer.writeStartElement("target");
            writer.writeAttributeString("dev", dev);
            writer.writeAttributeString("bus", cdInterface);
            writer.writeEndElement();

            writer.writeElement("readonly");

            writeAlias(device);
            if ("scsi".equals(cdInterface)) {
                writeAddress(vmInfoBuildUtils.createAddressForScsiDisk(0, cdRomIndex));
            } else {
                writeAddress(device);
            }
            writeBootOrder(device.getBootOrder());

            writer.writeEndElement();
            cdRomCounter++;
        }
    }

    private void writeInterface(VmDevice device, VmNic nic) {
        //  <interface type="bridge">
        //    <mac address="aa:bb:dd:dd:aa:bb"/>
        //    <model type="virtio"/>
        //    <source bridge="engine"/>
        //    [<driver name="vhost/qemu" queues="int"/>]
        //    [<filterref filter='filter name'>
        //      [<parameter name='parameter name' value='parameter value'>]
        //     </filterref>]
        //    [<tune><sndbuf>0</sndbuf></tune>]
        //     [<link state='up|down'/>]
        //     [<bandwidth>
        //       [<inbound average="int" [burst="int"]  [peak="int"]/>]
        //       [<outbound average="int" [burst="int"]  [peak="int"]/>]
        //      </bandwidth>]
        //  </interface>
        //
        //  -- or -- a slightly different SR-IOV network interface
        //  <interface type='hostdev' managed='no'>
        //    <driver name='vfio'/>
        //    <source>
        //     <address type='pci' domain='0x0000' bus='0x00' slot='0x07'
        //     function='0x0'/>
        //    </source>
        //    <mac address='52:54:00:6d:90:02'/>
        //    <vlan>
        //     <tag id=100/>
        //    </vlan>
        //    <address type='pci' domain='0x0000' bus='0x00' slot='0x07'
        //    function='0x0'/>
        //    <boot order='1'/>
        //  </interface>
        writer.writeStartElement("interface");

        VnicProfile vnicProfile = vmInfoBuildUtils.getVnicProfile(nic.getVnicProfileId());
        Network network = vnicProfile != null ? vmInfoBuildUtils.getNetwork(vnicProfile.getNetworkId()) : null;
        boolean networkless = network == null;
        boolean hasFailover = vnicProfile != null && vnicProfile.getFailoverVnicProfileId() != null;

        String alias = generateUserAliasForDevice(device);
        vnicMetadata.computeIfAbsent(alias, a -> new HashMap<>());
        vnicMetadata.get(alias).put("mac", nic.getMacAddress());

        switch (device.getDevice()) {
        case "bridge":
            writer.writeAttributeString("type", "bridge");
            writer.writeStartElement("model");
            VmInterfaceType ifaceType = nic.getType() != null ?
                    VmInterfaceType.forValue(nic.getType())
                    : VmInterfaceType.rtl8139;
            String evaluatedIfaceType = vmInfoBuildUtils.evaluateInterfaceType(ifaceType, vm.getHasAgent());
            if ("pv".equals(evaluatedIfaceType)) {
                evaluatedIfaceType = "virtio";
                if (legacyVirtio) {
                    evaluatedIfaceType = "virtio-transitional";
                }
            }
            writer.writeAttributeString("type", evaluatedIfaceType);
            writer.writeEndElement();

            writer.writeStartElement("link");
            writer.writeAttributeString("state", !networkless && nic.isLinked() ? "up" : "down");
            writer.writeEndElement();
            // The source element is different when using legacy or OVS bridge. We
            // expect VDSM to replace the source element if it is a non legacy bridge
            writer.writeStartElement("source");
            writer.writeAttributeString("bridge", !networkless ? network.getVdsmName() : ";vdsmdummy;");
            writer.writeEndElement();

            if (!networkless && network.isPortIsolation()) {
                writer.writeStartElement("port");
                writer.writeAttributeString("isolated", "yes");
                writer.writeEndElement();
            }

            String queues = null;
            if (vnicProfile != null) {
                queues = vnicProfile.getCustomProperties().remove("queues");
            }

            if (queues == null && vm.isMultiQueuesEnabled() && vmInfoBuildUtils.isInterfaceQueuable(device, nic)) {
                queues = String.valueOf(vmInfoBuildUtils.getOptimalNumOfQueuesPerVnic(VmCpuCountHelper.getDynamicNumOfCpu(vm)));
            }

            String driverName = getDriverNameForNetwork(!networkless ? network.getName() : "");
            boolean nonDefaultQueues = queues != null && Integer.parseInt(queues) != 1;
            if (nonDefaultQueues || driverName != null) {
                writer.writeStartElement("driver");
                if (nonDefaultQueues) {
                    writer.writeAttributeString("queues", queues);
                    if (driverName == null) {
                        driverName = "vhost";
                    }
                }
                writer.writeAttributeString("name", driverName);
                writer.writeEndElement();
            }

            if (vnicProfile != null && device.getCustomProperties().remove("failover") != null) {
                writer.writeStartElement("teaming");
                writer.writeAttributeString("type", "persistent");
                writer.writeEndElement();
            }

            break;

        case "hostdev":
            writer.writeAttributeString("type", "hostdev");
            writer.writeAttributeString("managed", "no");
            writer.writeStartElement("driver");
            writer.writeAttributeString("name", "vfio");
            writer.writeEndElement();
            if (!networkless && NetworkUtils.isVlan(network)) {
                writer.writeStartElement("vlan");
                writer.writeStartElement("tag");
                writer.writeAttributeString("id", network.getVlanId().toString());
                writer.writeEndElement();
                writer.writeEndElement();
            }
            writer.writeStartElement("source");
            writer.writeStartElement("address");
            String vfDeviceName = passthroughVnicToVfMap.get(nic.getId());
            Map<String, String> sourceAddress = hostDevicesSupplier.get().get(vfDeviceName).getAddress();
            sourceAddress.put("type", "pci");
            sourceAddress.forEach(writer::writeAttributeString);
            writer.writeEndElement();
            writer.writeEndElement();
            if (hasFailover) {
                writer.writeStartElement("teaming");
                writer.writeAttributeString("type", "transient");
                writer.writeAttributeString("persistent",
                        String.format("ua-%s", vnicProfile.getFailoverVnicProfileId()));
                writer.writeEndElement();
            }
            break;
        }

        writeAlias(device);
        writeAddress(device);

        writeBootOrder(device.getBootOrder());

        writer.writeStartElement("mac");
        writer.writeAttributeString("address", nic.getMacAddress());
        writer.writeEndElement();

        if (networkless || !vnicProfile.isPassthrough()) {
            writer.writeStartElement("mtu");
            writer.writeAttributeString("size",
                    networkless ?
                            String.valueOf(NetworkUtils.getHostDefaultMtu()) :
                            String.valueOf(NetworkUtils.getVmMtuActualValue(network)));
            writer.writeEndElement();
        }

        NetworkFilter networkFilter = vmInfoBuildUtils.fetchVnicProfileNetworkFilter(nic);
        if (networkFilter != null) {
            writer.writeStartElement("filterref");
            writer.writeAttributeString("filter", networkFilter.getName());
            vmInfoBuildUtils.getAllNetworkFiltersForVmNic(nic.getId()).forEach(parameter -> {
                writer.writeStartElement("parameter");
                writer.writeAttributeString("name", parameter.getName());
                writer.writeAttributeString("value", parameter.getValue());
                writer.writeEndElement();
            });
            writer.writeEndElement();
        }
        String sndbuf = vmCustomProperties.get("sndbuf");
        if (sndbuf != null) {
            writer.writeStartElement("tune");
            writer.writeElement("sndbuf", sndbuf);
            writer.writeEndElement();
        }

        Map<String, Object> profileData = new HashMap<>();
        vmInfoBuildUtils.addProfileDataToNic(profileData, vm, device, nic, vnicProfile);

        List<String> portMirroring = (List<String>) profileData.get(VdsProperties.PORT_MIRRORING);
        if (portMirroring != null && !portMirroring.isEmpty()) {
            // store port mirroring in the metadata
            vnicMetadata.get(alias).put("portMirroring", portMirroring);
        }

        Map<String, String> runtimeCustomProperties = vm.getRuntimeDeviceCustomProperties().get(device.getId());
        if (runtimeCustomProperties != null && !runtimeCustomProperties.isEmpty()) {
            // store runtime custom properties in the metadata
            vnicMetadata.get(alias).put("runtimeCustomProperties", runtimeCustomProperties);
        }

        if (vnicProfile != null && vnicProfile.getCustomProperties() != null) {
            vnicMetadata.get(alias).putAll(vnicProfile.getCustomProperties());
        }

        writer.writeStartElement("bandwidth");
        @SuppressWarnings("unchecked")
        Map<String, Object> specParams = (Map<String, Object>) profileData.get("specParams");
        if (specParams != null && (specParams.containsKey("inbound") || specParams.containsKey("outbound"))) {
            @SuppressWarnings("unchecked")
            Map<String, String> inboundMap = (Map<String, String>) specParams.get("inbound");
            if (inboundMap != null && !inboundMap.isEmpty()) {
                writer.writeStartElement("inbound");
                writer.writeAttributeString("average", String.valueOf(inboundMap.get("average")));
                writer.writeAttributeString("burst", String.valueOf(inboundMap.get("burst")));
                writer.writeAttributeString("peak", String.valueOf(inboundMap.get("peak")));
                writer.writeEndElement();
            }
            @SuppressWarnings("unchecked")
            Map<String, String> outboundMap = (Map<String, String>) specParams.get("outbound");
            if (outboundMap != null && !outboundMap.isEmpty()) {
                writer.writeStartElement("outbound");
                writer.writeAttributeString("average", String.valueOf(outboundMap.get("average")));
                writer.writeAttributeString("burst", String.valueOf(outboundMap.get("burst")));
                writer.writeAttributeString("peak", String.valueOf(outboundMap.get("peak")));
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private String getDriverNameForNetwork(String network) {
        String vhostProp = vmCustomProperties.get("vhost");
        if (vhostProp == null) {
            return null;
        }

        for (String vhost : vhostProp.split(",")) {
            String[] bridgeAndstatus = vhost.split(":");
            if (network.equals(bridgeAndstatus[0])) {
                if ("true".equalsIgnoreCase(bridgeAndstatus[1])) {
                    return "vhost";
                }
                if ("false".equalsIgnoreCase(bridgeAndstatus[1])) {
                    return "qemu";
                }
                log.warn("invalid vhost setting for network {}: {}", network, bridgeAndstatus[1]);
                break;
            }
        }

        return null;
    }

    private void writeBalloon(VmDevice device) {
        // <memballoon model='virtio'>
        //   <stats period='5' />
        //   <address type='pci' domain='0x0000' bus='0x00' slot='0x04' function='0x0'/>
        // </memballoon>
        writer.writeStartElement("memballoon");
        writer.writeAttributeString("model", device.getSpecParams().get(VdsProperties.Model).toString());
        writer.writeStartElement("stats");
        writer.writeAttributeString("period", "5");
        writer.writeEndElement();
        writeAlias(device);
        writeAddress(device);
        writer.writeEndElement();
    }

    private void writeDefaultBalloon() {
        // <memballoon model='none' />
        writer.writeStartElement("memballoon");
        writer.writeAttributeString("model", "none");
        writer.writeEndElement();
    }

    private void writeSmartcard(VmDevice device) {
        // <smartcard mode='passthrough' type='spicevmc'>
        //   <address/>
        // </smartcard>
        writer.writeStartElement("smartcard");
        writer.writeAttributeString("mode", device.getSpecParams().get("mode").toString());
        writer.writeAttributeString("type", device.getSpecParams().get("type").toString());
        writeAlias(device);
        writeAddress(device);
        writer.writeEndElement();
    }

    private void writeWatchdog(VmDevice device) {
        // <watchdog model='i6300esb' action='reset'>
        //   <address type='pci' domain='0x0000' bus='0x00' slot='0x05' function='0x0'/>
        // </watchdog>
        writer.writeStartElement("watchdog");
        Object model = device.getSpecParams().get(VdsProperties.Model);
        writer.writeAttributeString("model", model != null ? model.toString() : "i6300esb");
        Object action = device.getSpecParams().get(VdsProperties.action);
        writer.writeAttributeString("action", action != null ? action.toString() : "none");
        writeAlias(device);
        writeAddress(device);
        writer.writeEndElement();
    }

    void writeVideo(VmDevice device) {
        writer.writeStartElement("video");

        writer.writeStartElement("model");
        if (mdevDisplayOn) {
            writer.writeAttributeString("type", "none");
        } else {
            writer.writeAttributeString("type", device.getDevice());
            Object vram = device.getSpecParams().get(VdsProperties.VIDEO_VRAM);
            writer.writeAttributeString("vram", vram != null ? vram.toString() : "32768");
            Object heads = device.getSpecParams().get(VdsProperties.VIDEO_HEADS);
            writer.writeAttributeString("heads", heads != null ? heads.toString() : "1");
            if (device.getSpecParams().containsKey(VdsProperties.VIDEO_RAM)) {
                writer.writeAttributeString("ram", device.getSpecParams().get(VdsProperties.VIDEO_RAM).toString());
            }
            if (device.getSpecParams().containsKey(VdsProperties.VIDEO_VGAMEM)) {
                writer.writeAttributeString("vgamem", device.getSpecParams().get(VdsProperties.VIDEO_VGAMEM).toString());
            }
        }

        writer.writeEndElement();
        writeAlias(device);
        writeAddress(device);
        writer.writeEndElement();
    }

    private void writeDefaultVideo() {
        writer.writeStartElement("video");
        writer.writeStartElement("model");
        writer.writeAttributeString("type", "none");
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeBootOrder(int order) {
        if (order > 0) {
            writer.writeStartElement("boot");
            writer.writeAttributeString("order", String.valueOf(order));
            writer.writeEndElement();
        }
    }

    private void writeAddress(VmDevice device) {
        writeAddress(StringMapUtils.string2Map(device.getAddress()));
    }

    private void writeAlias(VmDevice device) {
        if (device.isManaged()) {
            writer.writeStartElement("alias");
            writer.writeAttributeString("name", generateUserAliasForDevice(device));
            writer.writeEndElement();
        }
    }

    public static String generateUserAliasForDevice(VmDevice device) {
        return String.format("ua-%s", device.getId().getDeviceId());
    }

    private void writeAddress(Map<String, String> addressMap) {
        if (!addressMap.isEmpty()) {
            writer.writeStartElement("address");
            addressMap.forEach(writer::writeAttributeString);
            if (!addressMap.containsKey("type")) {
                // Default type
                writer.writeAttributeString("type", "drive");
            }
            writer.writeEndElement();
        }
    }

    private void writeInput() {
        writer.writeStartElement("input");

        if (vmInfoBuildUtils.isTabletEnabled(vm)) {
            writer.writeAttributeString("type", "tablet");
            writer.writeAttributeString("bus", "usb");
        } else {
            writer.writeAttributeString("type", "mouse");
            writer.writeAttributeString("bus", vm.getClusterArch().getFamily() == ArchitectureType.x86 ? "ps2" :"usb");
        }

        writer.writeEndElement();
    }
}
