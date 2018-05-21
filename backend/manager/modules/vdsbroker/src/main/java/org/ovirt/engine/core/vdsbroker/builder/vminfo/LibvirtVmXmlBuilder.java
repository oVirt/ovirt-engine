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
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.HugePage;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmHostDevice;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
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
import org.ovirt.engine.core.common.utils.HugePageUtils;
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
    // Namespace prefixes:
    public static final String OVIRT_TUNE_PREFIX = "ovirt-tune";
    public static final String OVIRT_VM_PREFIX = "ovirt-vm";

    /** Timeout for the boot menu, in milliseconds */
    public static final int BOOT_MENU_TIMEOUT = 30000;
    private static final int LIBVIRT_PORT_AUTOSELECT = -1;
    private static final Set<String> SPICE_CHANNEL_NAMES = new HashSet<>(Arrays.asList(
            "main", "display", "inputs", "cursor", "playback", "record", "smartcard", "usbredir"));

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

    private VM vm;
    private int vdsCpuThreads;
    private MemoizingSupplier<Map<String, HostDevice>> hostDevicesSupplier;
    private MemoizingSupplier<VdsStatistics> hostStatisticsSupplier;
    private MemoizingSupplier<List<VdsNumaNode>> hostNumaNodesSupplier;
    private MemoizingSupplier<List<VmNumaNode>> vmNumaNodesSupplier;

    private Map<String, Map<String, Object>> vnicMetadata;
    private Map<String, Map<String, String>> diskMetadata;
    private Pair<String, VmPayload> payloadMetadata;

    private List<Pair<Guid, Guid>> volumeLeases = Collections.emptyList();

    /** Hot-set fields */
    private VmNic nic;
    private VmDevice device;

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
        } else {
            hostDevicesSupplier = new MemoizingSupplier<>(() -> Collections.emptyMap());
            hostStatisticsSupplier = new MemoizingSupplier<>(() -> null);
            hostNumaNodesSupplier = new MemoizingSupplier<>(() -> Collections.emptyList());
        }
        vmNumaNodesSupplier = new MemoizingSupplier<>(() -> vmInfoBuildUtils.getVmNumaNodes(vm));
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
        writeDevices();
        writePowerManagement();
        // note that this must be called after writeDevices to get the serial console, if exists
        writeOs();
        writeMemoryBacking();
        writeMetadata();
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

    private void writeHeader() {
        writer.setPrefix(OVIRT_TUNE_PREFIX, OVIRT_TUNE_URI);
        writer.setPrefix(OVIRT_VM_PREFIX, OVIRT_VM_URI);
        writer.writeStartDocument(false);
        writer.writeStartElement("domain");
        writer.writeAttributeString("type", "kvm");
        writer.writeNamespace(OVIRT_TUNE_PREFIX, OVIRT_TUNE_URI);
        writer.writeNamespace(OVIRT_VM_PREFIX, OVIRT_VM_URI);
    }

    private void writeName() {
        writer.writeElement("name", vm.getName());
    }

    private void writeId() {
        writer.writeElement("uuid", vm.getId().toString());
    }

    private void writeMemory() {
        int memSizeKB = vm.getMemSizeMb() * 1024;
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
        if (!FeatureSupported.hotPlugMemory(vm.getCompatibilityVersion(), vm.getClusterArch())
                // the next check is because QEMU fails if memory and maxMemory are the same
                || vm.getVmMemSizeMb() == vm.getMaxMemorySizeMb()) {
                return;
        }

        writer.writeStartElement("maxMemory");
        writer.writeAttributeString("slots", Config.getValue(ConfigValues.MaxMemorySlots).toString());
        writer.writeRaw(String.valueOf(vm.getMaxMemorySizeMb() * 1024));
        writer.writeEndElement();
    }

    private void writevCpu() {
        writer.writeStartElement("vcpu");
        writer.writeAttributeString("current", String.valueOf(vm.getNumOfCpus()));
        writer.writeRaw(FeatureSupported.supportedInConfig(ConfigValues.HotPlugCpuSupported, vm.getCompatibilityVersion(), vm.getClusterArch()) ?
                VmCpuCountHelper.calcMaxVCpu(vm, vm.getClusterCompatibilityVersion()).toString()
                : String.valueOf(vm.getNumOfCpus()));
        writer.writeEndElement();
    }

    @SuppressWarnings("incomplete-switch")
    private void writeCpu(boolean addVmNumaNodes) {
        writer.writeStartElement("cpu");

        String cpuType = vm.getCpuName();
        if (vm.isUseHostCpuFlags()){
            cpuType = "hostPassthrough";
        }

        switch(vm.getClusterArch().getFamily()) {
        case x86:
        case s390x:
            writer.writeAttributeString("match", "exact");

            // is this a list of strings??..
            switch(cpuType) {
            case "hostPassthrough":
                writer.writeAttributeString("mode", "host-passthrough");
                break;
            case "hostModel":
                writer.writeAttributeString("mode", "host-model");
                break;
            default:
                String[] typeAndFlags = cpuType.split(",");
                writer.writeElement("model", typeAndFlags[0]);
                writeCpuFlags(typeAndFlags);
                break;
            }
            break;
        case ppc:
            writer.writeAttributeString("mode", "host-model");
            // needs to be lowercase for libvirt
            String[] typeAndFlags = cpuType.split(",");
            writer.writeElement("model", typeAndFlags[0].toLowerCase());
            writeCpuFlags(typeAndFlags);
        }

        if ((boolean) Config.getValue(ConfigValues.SendSMPOnRunVm)) {
            writer.writeStartElement("topology");
            writer.writeAttributeString("cores", Integer.toString(vm.getCpuPerSocket()));
            writer.writeAttributeString("threads", Integer.toString(vm.getThreadsPerCpu()));
            int vcpus = FeatureSupported.supportedInConfig(ConfigValues.HotPlugCpuSupported, vm.getCompatibilityVersion(), vm.getClusterArch()) ?
                    VmCpuCountHelper.calcMaxVCpu(vm, vm.getClusterCompatibilityVersion())
                    : vm.getNumOfCpus();
            writer.writeAttributeString("sockets", String.valueOf(vcpus / vm.getCpuPerSocket() / vm.getThreadsPerCpu()));
            writer.writeEndElement();
        }

        if (addVmNumaNodes) {
            writer.writeStartElement("numa");
            NumaSettingFactory.buildVmNumaNodeSetting(vmNumaNodesSupplier.get()).forEach(vmNumaNode -> {
                writer.writeStartElement("cell");
                writer.writeAttributeString("id", vmNumaNode.get(VdsProperties.NUMA_NODE_INDEX).toString());
                writer.writeAttributeString("cpus", vmNumaNode.get(VdsProperties.NUMA_NODE_CPU_LIST).toString());
                writer.writeAttributeString("memory", String.valueOf(Integer.parseInt((String) vmNumaNode.get(VdsProperties.VM_NUMA_NODE_MEM)) * 1024));
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
        Map<String, Object> cpuPinning = vmInfoBuildUtils.parseCpuPinning(vm.getCpuPinning());
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
        if (vm.getClusterArch().getFamily() != ArchitectureType.x86) {
            return;
        }
        /**
         <sysinfo type="smbios">
          <system>
            <entry name="manufacturer">Fedora</entry>
            <entry name="product">Virt-Manager</entry>
            <entry name="version">0.8.2-3.fc14</entry>
            <entry name="serial">32dfcb37-5af1-552b-357c-be8c3aa38310</entry>
            <entry name="uuid">c7a5fdbd-edaf-9455-926a-d65c16db1809</entry>
          </system>
         </sysinfo>
         */
        writer.writeStartElement("sysinfo");
        writer.writeAttributeString("type", "smbios");

        writer.writeStartElement("system");

        writer.writeStartElement("entry");
        writer.writeAttributeString("name", "manufacturer");
        writer.writeRaw("oVirt");
        writer.writeEndElement();

        writer.writeStartElement("entry");
        writer.writeAttributeString("name", "product");
        writer.writeRaw("OS-NAME:");
        writer.writeEndElement();

        writer.writeStartElement("entry");
        writer.writeAttributeString("name", "version");
        writer.writeRaw("OS-VERSION:");
        writer.writeEndElement();

        writer.writeStartElement("entry");
        writer.writeAttributeString("name", "serial");
        writer.writeRaw("HOST-SERIAL:");
        writer.writeEndElement();

        writer.writeStartElement("entry");
        writer.writeAttributeString("name", "uuid");
        writer.writeRaw(vm.getId().toString());
        writer.writeEndElement();

        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeNumaTune() {
        NumaTuneMode numaTune = vm.getNumaTuneMode();
        if (numaTune == null) {
            return;
        }

        Map<String, Object> numaTuneSetting = NumaSettingFactory.buildVmNumatuneSetting(
                numaTune,
                vmNumaNodesSupplier.get());
        if (numaTuneSetting.isEmpty()) {
            return;
        }

        // <numatune>
        //   <memory mode='strict' nodeset='0-1'/>
        //   <memnode cellid='0' mode='strict' nodeset='1'>
        // </numatune>
        String nodeSet = (String) numaTuneSetting.get(VdsProperties.NUMA_TUNE_NODESET);
        String mode = (String) numaTuneSetting.get(VdsProperties.NUMA_TUNE_MODE);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> memNodes = (List<Map<String, String>>) numaTuneSetting.get(VdsProperties.NUMA_TUNE_MEMNODES);
        if (nodeSet != null || memNodes != null) {
            writer.writeStartElement("numatune");

            if (nodeSet != null) {
                writer.writeStartElement("memory");
                writer.writeAttributeString("mode", mode);
                writer.writeAttributeString("modeset", nodeSet);
                writer.writeEndElement();
            }

            if (memNodes != null) {
                for (Map<String, String> memnode : memNodes) {
                    writer.writeStartElement("memnode");
                    writer.writeAttributeString("mode", mode);
                    writer.writeAttributeString("cellid", (String) memnode.get(VdsProperties.NUMA_TUNE_VM_NODE_INDEX));
                    writer.writeAttributeString("nodeset", (String) memnode.get(VdsProperties.NUMA_TUNE_NODESET));
                    writer.writeEndElement();
                }
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

    private void writeClock() {
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

        writer.writeEndElement();
    }

    private void writeFeatures() {
        if (vm.getClusterArch().getFamily() != ArchitectureType.x86) {
            return;
        }

        // Currently only
        // <features>
        //   <acpi/>
        // <features/>
        // for hyperv:
        // <features>
        //   <acpi/>
        //   <hyperv>
        //     <relaxed state='on'/>
        //   </hyperv>
        // <features/>
        boolean acpiEnabled = vm.getAcpiEnable();
        boolean kaslr = vmInfoBuildUtils.isKASLRDumpEnabled(vm.getVmOsId());
        if (!acpiEnabled && !hypervEnabled && !kaslr) {
            return;
        }

        writer.writeStartElement("features");

        if (acpiEnabled) {
            writer.writeStartElement("acpi");
            writer.writeEndElement();
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

            writer.writeEndElement();
        }

        if (kaslr) {
            writer.writeElement("vmcoreinfo");
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
        int hugepageSize = hugepageSizeOpt.get();
        List<Integer> hugepageSizes = hostStatisticsSupplier.get().getHugePages().stream()
                .map(HugePage::getSizeKB)
                .collect(Collectors.toList());
        if (!hugepageSizes.contains(hugepageSize)) {
            hugepageSize = vmInfoBuildUtils.getDefaultHugepageSize(vm);
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
        vnicMetadata.forEach((mac, data) -> {
            writer.writeStartElement(OVIRT_VM_URI, "device");
            writer.writeAttributeString("mac_address", mac);
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
        writeVmCustomMetadata();
        writeNetworkInterfaceMetadata();
        writeDiskMetadata();
        writeRunAndPauseMetadata();
        writePayloadMetadata();
        writeResumeBehaviorMetadata();
        writer.writeEndElement();
    }

    private void writeResumeBehaviorMetadata() {
        if (FeatureSupported.isResumeBehaviorSupported(vm.getCompatibilityVersion())) {
            writer.writeElement("resumeBehavior", String.valueOf(vm.getResumeBehavior()).toLowerCase());
        }
    }

    private void writeRunAndPauseMetadata() {
        writer.writeElement("launchPaused", String.valueOf(vm.isRunAndPause()));
    }

    private void writeVmCustomMetadata() {
        writer.writeStartElement(OVIRT_VM_URI, "custom");
        vmCustomProperties.forEach((key, value) -> writer.writeElement(OVIRT_VM_URI, key, value));
        writer.writeEndElement();
    }

    private void writeMinGuaranteedMemoryMetadata() {
        writer.writeStartElement("minGuaranteedMemoryMb");
        writer.writeAttributeString("type", "int");
        writer.writeRaw(String.valueOf(vm.getMinAllocatedMem()));
        writer.writeEndElement();
    }

    private void writeClusterVersionMetadata() {
        Version version = vm.getCompatibilityVersion();
        writer.writeStartElement("clusterVersion");
        writer.writeRaw(String.valueOf(version.getMajor()) + "." + String.valueOf(version.getMinor()));
        writer.writeEndElement();
    }

    private void writePowerEvents() {
        if (volatileRun) {
            writer.writeElement("on_reboot", "destroy");
        }
    }

    private void writeDevices() {
        List<VmDevice> devices = vmInfoBuildUtils.getVmDevices(vm.getId());
        // replacement of some devices in run-once mode should eventually be done by the run-command
        devices = overrideDevicesForRunOnce(devices);
        devices = processPayload(devices);
        devices.stream().filter(d -> d.getSpecParams() == null).forEach(d -> d.setSpecParams(Collections.emptyMap()));
        ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new CreateAdditionalControllersForDomainXml(devices));

        writer.writeStartElement("devices");

        if (vm.getClusterArch() != ArchitectureType.s390x &&
            !(vm.getClusterArch().getFamily() == ArchitectureType.ppc && vm.getVmType() == VmType.HighPerformance)) {
            // no mouse or tablet for s390x and for HP VMS with ppc architecture type
            writeInput();
        }

        writeGuestAgentChannels();

        if (vm.getClusterArch() == ArchitectureType.ppc64 || vm.getClusterArch() == ArchitectureType.ppc64le) {
            writeEmulator();
        }

        Map<DiskInterface, Integer> controllerIndexMap =
                ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new GetControllerIndices()).returnValue();
        int virtioScsiIndex = controllerIndexMap.get(DiskInterface.VirtIO_SCSI);

        List<VmDevice> interfaceDevices = new ArrayList<>();
        List<VmDevice> diskDevices = new ArrayList<>();
        List<VmDevice> cdromDevices = new ArrayList<>();
        VmDevice floppyDevice = null;

        boolean spiceExists = false;
        boolean balloonExists = false;
        boolean forceRefreshDevices = false;
        for (VmDevice device : devices) {
            if (!device.isPlugged()) {
                continue;
            }

            switch (device.getType()) {
            case BALLOON:
                balloonExists = true;
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
                writeVideo(device);
                break;
            case CONTROLLER:
                switch(device.getDevice()) {
                case "virtio-serial":
                    device.getSpecParams().put("index", 0);
                    device.getSpecParams().put("ports", 16);
                    break;
                case "virtio-scsi":
                    device.setDevice(VdsProperties.Scsi);
                    device.getSpecParams().put("index", virtioScsiIndex++);
                    device.getSpecParams().put("model", "virtio-scsi");
                    break;
                }
                writeController(device);
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
                writeHostDevice(device, hostDevice);
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

        writeSerialConsole(serialConsolePath);

        if (spiceExists) {
            writeSpiceVmcChannel();
        }

        updateBootOrder(diskDevices, cdromDevices, interfaceDevices);

        writeInterfaces(interfaceDevices);
        writeCdRom(cdromDevices);
        writeFloppy(floppyDevice);
        // we must write the disk after writing cd-rom and floppy to know reserved indices
        writeDisks(diskDevices);
        writeLeases();

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
        devices.stream()
                .sorted(Comparator.comparing(dev -> devIdToNic.get(dev.getId()).getMacAddress()))
                .forEach(dev -> writeInterface(dev, devIdToNic.get(dev.getId())));
    }

    private void writeDisks(List<VmDevice> devices) {
        Map<VmDeviceId, VmDevice> deviceIdToDevice = devices.stream()
                .collect(Collectors.toMap(VmDevice::getId, dev -> dev));
        Map<Integer, Map<VmDevice, Integer>> vmDeviceSpaprVscsiUnitMap = vmInfoBuildUtils.getVmDeviceUnitMapForSpaprScsiDisks(vm);
        Map<Integer, Map<VmDevice, Integer>> vmDeviceVirtioScsiUnitMap = vmInfoBuildUtils.getVmDeviceUnitMapForVirtioScsiDisks(vm);
        int hdIndex = -1;
        int sdIndex = -1;
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

    private void writeHostDevice(VmDevice device, HostDevice hostDevice) {
        switch (hostDevice.getCapability()) {
        case "pci":
            writePciHostDevice(new VmHostDevice(device), hostDevice);
            break;
        case "usb":
        case "usb_device":
            writeUsbHostDevice(new VmHostDevice(device), hostDevice);
            break;
        case "scsi":
            writeScsiHostDevice(new VmHostDevice(device), hostDevice);
            break;
        default:
            log.warn("Skipping host device: {}", device.getDevice());
        }
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
        writeAddress(device);
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
        writer.writeAttributeString("model", "virtio");

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
        writer.writeAttributeString("passwd", "*****");
        writer.writeAttributeString("passwdValidTo", "1970-01-01T00:00:01");

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

    private void writeController(VmDevice device) {
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
        if (ioThreadId != null) {
            writer.writeStartElement("driver");
            writer.writeAttributeString("iothread", ioThreadId.toString());
            writer.writeEndElement();
        }

        // USB controllers must not be set with user-aliases (bz #1552127)
        if (!VmDeviceType.USB.getName().equals(device.getDevice())) {
            writeAlias(device);
        }

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
        writeDiskSource(device, disk, dev);
        writeDiskDriver(device, disk, dve, pinTo);
        writeAlias(device);
        writeAddress(device);
        writeBootOrder(device.getBootOrder());

        if (disk.getDiskStorageType() != DiskStorageType.LUN) {
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
        if (FeatureSupported.passDiscardSupported(vm.getCompatibilityVersion()) && dve.isPassDiscard()) {
            writer.writeAttributeString("discard", "unmap");
        }
        if (pinTo > 0) {
            writer.writeAttributeString("iothread", String.valueOf(pinTo));
        }

        boolean nativeIO = false;
        switch (disk.getDiskStorageType()) {
        case IMAGE:
            DiskImage diskImage = (DiskImage) disk;
            String diskType = this.vmInfoBuildUtils.getDiskType(this.vm, diskImage, device);
            nativeIO = !"file".equals(diskType);
            writer.writeAttributeString("io", nativeIO ? "native" : "threads");
            writer.writeAttributeString("type", diskImage.getVolumeFormat() == VolumeFormat.COW ? "qcow2" : "raw");
            writer.writeAttributeString("error_policy", disk.getPropagateErrors() == PropagateErrors.On ? "enospace" : "stop");
            break;

        case LUN:
            nativeIO = true;
            writer.writeAttributeString("io", "native");
            writer.writeAttributeString("type", "raw");
            writer.writeAttributeString("error_policy", disk.getPropagateErrors() == PropagateErrors.On ? "enospace" : "stop");
            break;

        case CINDER:
            // case RBD
            writer.writeAttributeString("io", "threads");
            writer.writeAttributeString("type", "raw");
            writer.writeAttributeString("error_policy", "stop");
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

    private void writeDiskSource(VmDevice device, Disk disk, String dev) {
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
        }
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
        writer.writeAttributeString("snapshot", "no");

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
        //   <source file="/var/run/vdsm/payload/8b5fa6b8-9c57-4d7c-80cb-64537eea560f.6e38a5ccb3c6b2b674086e9d07126a03.img" startupPolicy="optional" />
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

        // add a device that points to vm.getCdPath()
        String cdPath = vm.getCdPath();
        VmDevice nonPayload = devices.stream()
                .filter(d -> !VmPayload.isPayload(d.getSpecParams()))
                .findAny().orElse(null);
        if (nonPayload != null || (vm.isRunOnce() && !StringUtils.isEmpty(cdPath))) {
            cdRomIndex = VmDeviceCommonUtils.getCdDeviceIndex(cdInterface);
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
            writer.writeAttributeString("startupPolicy", "optional");
            writer.writeEndElement();

            writer.writeStartElement("target");
            writer.writeAttributeString("dev", dev);
            writer.writeAttributeString("bus", cdInterface);
            writer.writeEndElement();

            writer.writeElement("readonly");

            if (nonPayload != null) {
                writeAlias(nonPayload);
                if ("scsi".equals(cdInterface)) {
                    writeAddress(vmInfoBuildUtils.createAddressForScsiDisk(0, cdRomIndex));
                } else {
                    writeAddress(nonPayload);
                }
                writeBootOrder(nonPayload.getBootOrder());
            }

            writer.writeEndElement();
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

            String queues = vnicProfile != null ? vnicProfile.getCustomProperties().remove("queues") : null;
            String driverName = getDriverNameForNetwork(!networkless ? network.getName() : "");
            if (queues != null || driverName != null) {
                writer.writeStartElement("driver");
                if (queues != null) {
                    writer.writeAttributeString("queues", queues);
                    if (driverName == null) {
                        driverName = "vhost";
                    }
                }
                writer.writeAttributeString("name", driverName);
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
            break;
        }

        writeAlias(device);
        writeAddress(device);

        writeBootOrder(device.getBootOrder());

        writer.writeStartElement("mac");
        writer.writeAttributeString("address", nic.getMacAddress());
        writer.writeEndElement();

        if (!networkless) {
            writer.writeStartElement("mtu");
            writer.writeAttributeString("size", String.valueOf(NetworkUtils.getVmMtuActualValue(network)));
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
        vmInfoBuildUtils.addProfileDataToNic(profileData, vm, device, nic);

        List<String> portMirroring = (List<String>) profileData.get(VdsProperties.PORT_MIRRORING);
        if (portMirroring != null && !portMirroring.isEmpty()) {
            // store port mirroring in the metadata
            vnicMetadata.computeIfAbsent(nic.getMacAddress(), mac -> new HashMap<>());
            vnicMetadata.get(nic.getMacAddress()).put("portMirroring", portMirroring);
        }

        Map<String, String> runtimeCustomProperties = vm.getRuntimeDeviceCustomProperties().get(device.getId());
        if (runtimeCustomProperties != null && !runtimeCustomProperties.isEmpty()) {
            // store runtime custom properties in the metadata
            vnicMetadata.computeIfAbsent(nic.getMacAddress(), mac -> new HashMap<>());
            vnicMetadata.get(nic.getMacAddress()).put("runtimeCustomProperties", runtimeCustomProperties);
        }

        if (vnicProfile != null && vnicProfile.getCustomProperties() != null) {
            vnicMetadata.computeIfAbsent(nic.getMacAddress(), mac -> new HashMap<>());
            vnicMetadata.get(nic.getMacAddress()).putAll(vnicProfile.getCustomProperties());
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

    private void writeVideo(VmDevice device) {
        writer.writeStartElement("video");

        writer.writeStartElement("model");
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
        writer.writeEndElement();
        writeAlias(device);
        writeAddress(device);
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
            writer.writeAttributeString("name", String.format("ua-%s", device.getId().getDeviceId()));
            writer.writeEndElement();
        }
    }

    private void writeAddress(Map<String, String> addressMap) {
        if (!addressMap.isEmpty()) {
            writer.writeStartElement("address");
            addressMap.forEach(writer::writeAttributeString);
            writer.writeEndElement();
        }
    }

    private void writeInput() {
        writer.writeStartElement("input");

        boolean tabletEnable =
                vm.getVmType() != VmType.HighPerformance // avoid adding Tablet device for HP VMs since no USB devices are set
                && vm.getGraphicsInfos().size() == 1
                && vm.getGraphicsInfos().containsKey(GraphicsType.VNC);
        if (tabletEnable) {
            writer.writeAttributeString("type", "tablet");
            writer.writeAttributeString("bus", "usb");
        } else if (vm.getClusterArch().getFamily() == ArchitectureType.x86) {
            writer.writeAttributeString("type", "mouse");
            writer.writeAttributeString("bus", "ps2");
        } else {
            writer.writeAttributeString("type", "mouse");
            writer.writeAttributeString("bus", "usb");
        }

        writer.writeEndElement();
    }
}
