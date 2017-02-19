package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import static org.ovirt.engine.core.common.utils.VmDeviceCommonUtils.updateVmDevicesBootOrder;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.IoTuneUtils.ioTuneListFrom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.ChipsetType;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
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
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.StringMapUtils;
import org.ovirt.engine.core.utils.archstrategy.ArchStrategyFactory;
import org.ovirt.engine.core.utils.ovf.xml.XmlTextWriter;
import org.ovirt.engine.core.vdsbroker.architecture.GetControllerIndices;
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
    public static final String OVIRT_URI = "http://ovirt.org/vm/tune/1.0";
    // Namespace prefixes:
    public static final String OVIRT_PREFIX = "ovirt";

    /** Timeout for the boot menu, in milliseconds */
    public static final int BOOT_MENU_TIMEOUT = 10000;
    private static final int LIBVIRT_PORT_AUTOSELECT = -1;

    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmInfoBuildUtils vmInfoBuildUtils;
    @Inject
    private VnicProfileDao vnicProfileDao;
    @Inject
    private NetworkDao networkDao;

    private OsRepository osRepository;
    private String serialConsolePath;
    private boolean hypervEnabled;
    private XmlTextWriter writer;
    private Map<Guid, StorageQos> qosCache;

    private Map<String, Object> createInfo;
    private VM vm;

    public LibvirtVmXmlBuilder(Map<String, Object> createInfo, VM vm) {
        this.createInfo = createInfo;
        this.vm = vm;
    }

    @PostConstruct
    private void init() {
        osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
        hypervEnabled = osRepository.isHypervEnabled(vm.getVmOsId(), vm.getCompatibilityVersion());
        writer = new XmlTextWriter();
        qosCache = new HashMap<>();
    }

    public String build() {
        writeHeader();
        writeName();
        writeId();
        writeMemory();
        writeIoThreads();
        writeMaxMemory();
        writevCpu();
        writeMetadata();
        writeSystemInfo();
        writeClock();
        writeFeatures();
        writeCpu();
        writeNumaTune();
        writeDevices();
        // note that this must be called after writeDevices to get the serial console, if exists
        writeOs();
        return writer.getStringXML();
    }

    private void writeHeader() {
        writer.writeStartDocument(false);
        writer.setPrefix(OVIRT_PREFIX, OVIRT_URI);
        writer.writeStartElement("domain");
        writer.writeAttributeString("type", "kvm");
        writer.writeNamespace(OVIRT_PREFIX, OVIRT_URI);
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
    private void writeCpu() {
        writer.writeStartElement("cpu");

        String cpuType = createInfo.get(VdsProperties.cpuType).toString();
        switch(vm.getClusterArch().getFamily()) {
        case x86:
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
                writer.writeStartElement("model");
                writer.writeRaw(cpuType);
                // TODO: features
                writer.writeEndElement();
                break;
            }
            break;
        case ppc:
            writer.writeStartElement("model");
            writer.writeRaw(cpuType);
            writer.writeEndElement();
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

        if (StringUtils.isNotEmpty(vm.getCpuPinning())) {
            writer.writeStartElement("cputune");
            for (String pin : vm.getCpuPinning().split("_")) {
                writer.writeStartElement("vcpupin");
                final String[] split = pin.split("#");
                writer.writeAttributeString("vcpu", split[0]);
                writer.writeAttributeString("cpuset", split[1]);
                writer.writeEndElement();
            }
            writer.writeEndElement();
        } else {
            // TODO Map<String, Object> cpuPinDict = NumaSettingFactory.buildCpuPinningWithNumaSetting(vmNumaNodes, totalVdsNumaNodes);
        }

        if (createInfo.containsKey(VdsProperties.VM_NUMA_NODES)) {
            writer.writeStartElement("numa");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> createVmNumaNodes = (List<Map<String, Object>>) createInfo.get(VdsProperties.VM_NUMA_NODES);
            for (Map<String, Object> vmNumaNode : createVmNumaNodes) {
                writer.writeStartElement("cell");
                writer.writeAttributeString("cpus", vmNumaNode.get(VdsProperties.NUMA_NODE_CPU_LIST).toString());
                writer.writeAttributeString("memory", String.valueOf(Integer.parseInt((String) vmNumaNode.get(VdsProperties.VM_NUMA_NODE_MEM)) * 1024));
                writer.writeEndElement();
            }
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
        if (!createInfo.containsKey(VdsProperties.NUMA_TUNE)) {
            return;
        }

        // <numatune>
        //   <memory mode='strict' nodeset='0-1'/>
        //   <memnode cellid='0' mode='strict' nodeset='1'>
        // </numatune>
        @SuppressWarnings("unchecked")
        Map<String, Object> numaTuneSetting = (Map<String, Object>) createInfo.get(VdsProperties.NUMA_TUNE);
        String nodeSet = (String) numaTuneSetting.get(VdsProperties.NUMA_TUNE_NODESET);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> memNodes = (List<Map<String, String>>) numaTuneSetting.get(VdsProperties.NUMA_TUNE_MEMNODES);
        if (nodeSet != null || memNodes != null) {
            writer.writeStartElement("numatune");
            String mode = (String) createInfo.get(VdsProperties.NUMA_TUNE_MODE);

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

    private void writeOs() {
        writer.writeStartElement("os");

        writer.writeStartElement("type");
        writer.writeAttributeString("arch", vm.getClusterArch().toString());
        writer.writeAttributeString("machine", vm.getEmulatedMachine() != null ?
                vm.getEmulatedMachine()
                : vmInfoBuildUtils.getEmulatedMachineByClusterArch(vm.getClusterArch()));
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
        if (!acpiEnabled && !hypervEnabled) {
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

        writer.writeEndElement();
    }

    private void writeMetadata() {
        // <domain xmlns:ovirt="http://ovirt.org/vm/tune/1.0">
        // ...
        //   <metadata>
        //     <ovirt:qos xmlns:ovirt=>
        //   </metadata>
        // ...
        // </domain>
        writer.writeStartElement("metadata");
        writer.writeStartElement(OVIRT_URI, "qos");
        writer.writeEndElement();
        writer.writeEndElement();
    }

    private void writeDevices() {
        List<VmDevice> devices = vmDeviceDao.getVmDeviceByVmId(vm.getId());
        writer.writeStartElement("devices");

        writeInput(writer, vm);

        writeGuestAgentChannels(writer, vm);

        if (vm.getClusterArch() == ArchitectureType.ppc64 || vm.getClusterArch() == ArchitectureType.ppc64le) {
            writeEmulator(writer, vm);
        }

        Map<DiskInterface, Integer> controllerIndexMap =
                ArchStrategyFactory.getStrategy(vm.getClusterArch()).run(new GetControllerIndices()).returnValue();
        int virtioScsiIndex = controllerIndexMap.get(DiskInterface.VirtIO_SCSI);

        List<VmDevice> interfaceDevices = new ArrayList<>();
        List<VmDevice> diskDevices = new ArrayList<>();
        List<VmDevice> cdromDevices = new ArrayList<>();

        boolean spiceExists = false;
        for (VmDevice device : devices) {
            if (!device.isPlugged()) {
                continue;
            }

            switch (device.getType()) {
            case BALLOON:
                writeBalloon(writer, device, vm);
                break;
            case SMARTCARD:
                writeSmartcard(writer, device, vm);
                break;
            case WATCHDOG:
                writeWatchdog(writer, device, vm);
                break;
            case MEMORY:
                writeMemory(writer, device, vm);
                break;
            case VIDEO:
                writeVideo(writer, device, vm);
                break;
            case CONTROLLER:
                switch(device.getDevice()) {
                case "virtio-serial":
                    device.getSpecParams().put("index", 0);
                    device.getSpecParams().put("ports", 16);
                    device.getSpecParams().put("type", "virtio-serial");
                    writeController(writer, device, vm);
                    break;
                case "virtio-scsi":
                    device.getSpecParams().put("index", virtioScsiIndex++);
                    device.getSpecParams().put("type", VdsProperties.Scsi);
                    device.getSpecParams().put("model", "virtio-scsi");
                    writeController(writer, device, vm);
                    break;
                }
                break;
            case GRAPHICS:
                writeGraphics(writer, device, vm);
                spiceExists = spiceExists || device.getDevice().equals("spice");
                break;
            case SOUND:
                writeSound(writer, device, vm);
                break;
            case RNG:
                writeRng(writer, device, vm);
                break;
            case CONSOLE:
                writeConsole(writer, device, vm);
                if (device.getSpecParams() != null && "serial".equals(device.getSpecParams().get("consoleType"))) {
                    serialConsolePath = getSerialConsolePath(device, vm);
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
                default:
                }
                break;
            case INTERFACE:
                interfaceDevices.add(device);
                break;
            case REDIR:
                writeRedir(writer, device);
                break;
            case CHANNEL:
                break;
            case HOSTDEV:
                break;
            case UNKNOWN:
                break;
            default:
                break;
            }
        }

        writeSerialConsole(writer, serialConsolePath);

        writeLease(writer, vm);

        if (spiceExists) {
            writeSpiceVmcChannel(writer);
        }

        updateBootOrder(diskDevices, cdromDevices, interfaceDevices);

        writeInterfaces(writer, interfaceDevices, vm);
        writeDisks(writer, diskDevices, vm);
        writeCdRom(writer, cdromDevices, vm);
        // TODO floppy

        writer.writeEndElement();
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

    private void writeLease(XmlTextWriter writer, VM vm) {
        if (vm.getLeaseStorageDomainId() == null) {
            return;
        }

        writer.writeStartElement("lease");
        writer.writeElement("key", vm.getId().toString());
        writer.writeElement("lockspace", vm.getLeaseStorageDomainId().toString());

        writer.writeStartElement("target");
        writer.writeAttributeString("offset", String.format("LEASE-OFFSET:%s:%s",
                vm.getId(),
                vm.getLeaseStorageDomainId()));
        writer.writeAttributeString("path", String.format("LEASE-PATH:%s:%s",
                vm.getId(),
                vm.getLeaseStorageDomainId()));
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeInterfaces(XmlTextWriter writer, List<VmDevice> devices, VM vm) {
        Map<VmDeviceId, VmNetworkInterface> devIdToNic = vm.getInterfaces().stream()
                .collect(Collectors.toMap(nic -> new VmDeviceId(nic.getId(), nic.getVmId()), nic -> nic));
        devices.forEach(dev -> writeInterface(writer, dev, vm, devIdToNic.get(dev.getId())));
    }

    private void writeDisks(XmlTextWriter writer, List<VmDevice> devices, VM vm) {
        Map<VmDeviceId, VmDevice> deviceIdToDevice = devices.stream()
                .collect(Collectors.toMap(VmDevice::getId, dev -> dev));
        for (Disk disk : vmInfoBuildUtils.getSortedDisks(vm)) {
            VmDevice device = deviceIdToDevice.get(new VmDeviceId(disk.getId(), vm.getId()));
            if (device.isManaged()) {
                writeManagedDisk(writer, device, vm, disk);
            }
            // TODO: else
        }
    }

    private void writeConsole(XmlTextWriter writer, VmDevice device, VM vm) {
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

        String path = getSerialConsolePath(device, vm);
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

        writer.writeEndElement();
    }

    public void writeEmulator(XmlTextWriter writer, VM vm) {
        writer.writeStartElement("emulator");
        writer.writeAttributeString("text", String.format("/usr/bin/qemu-system-%s", vm.getClusterArch()));
        writer.writeEndElement();
    }

    private void writeSpiceVmcChannel(XmlTextWriter writer) {
        writer.writeStartElement("channel");
        writer.writeAttributeString("type", "spicevmc");

        writer.writeStartElement("target");
        writer.writeAttributeString("type", "virtio");
        writer.writeAttributeString("name", "com.redhat.spice.0");
        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeGuestAgentChannels(XmlTextWriter writer, VM vm) {
        // <channel type='unix'>
        //   <target type='virtio' name='org.linux-kvm.port.0'/>
        //   <source mode='bind' path='/tmp/socket'/>
        // </channel>
        writer.writeStartElement("channel");
        writer.writeAttributeString("type", "unix");

        writer.writeStartElement("target");
        writer.writeAttributeString("type", "virtio");
        writer.writeAttributeString("name", "com.redhat.rhevm.vdsm");
        writer.writeEndElement();

        writer.writeStartElement("source");
        writer.writeAttributeString("mode", "bind");
        writer.writeAttributeString("path", String.format("/var/lib/libvirt/qemu/channels/%s.com.redhat.rhevm.vdsm", vm.getId()));
        writer.writeEndElement();

        writer.writeEndElement();

        writer.writeStartElement("channel");

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

    private void writeSerialConsole(XmlTextWriter writer, String path) {
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

    private String getSerialConsolePath(VmDevice device, VM vm) {
        Object enableSocketFromSpecParams = device.getSpecParams().get("enableSocket");
        return enableSocketFromSpecParams != null && Boolean.parseBoolean(enableSocketFromSpecParams.toString()) ?
                String.format("/var/run/ovirt-vmconsole-console/%s.sock", vm.getId())
                : "";
    }

    private void writeRedir(XmlTextWriter writer, VmDevice device) {
        // <redirdev bus='usb' type='spicevmc'>
        //   <address type='usb' bus='0' port='1'/>
        // </redirdev>
        writer.writeStartElement("redirdev");
        writer.writeAttributeString("type", "spicevmc");
        writer.writeAttributeString("bus", "usb");
        writeDeviceAliasAndAddress(writer, device);
        writer.writeEndElement();
    }

    private void writeRng(XmlTextWriter writer, VmDevice device, VM vm) {
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

        writer.writeEndElement();
    }

    private void writeSound(XmlTextWriter writer, VmDevice device, VM vm) {
        writer.writeStartElement("sound");
        writer.writeAttributeString("model", device.getDevice());
        writeDeviceAliasAndAddress(writer, device);
        writer.writeEndElement();
    }

    private void writeGraphics(XmlTextWriter writer, VmDevice device, VM vm) {
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
                for (String channel : channels.split(",")) {
                    writer.writeStartElement("channel");
                    writer.writeAttributeString("name", channel);
                    writer.writeAttributeString("mode", "secure");
                    writer.writeEndElement();
                }
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
                writer.writeAttributeString("network", String.format("DISPLAY-NETWORK:%s", displayNetwork.getName()));
            } else {
                writer.writeAttributeString("type", "address");
                writer.writeAttributeString("address", displayIp);
            }

            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void writeController(XmlTextWriter writer, VmDevice device, VM vm) {
        writer.writeStartElement("controller");
        writer.writeAttributeString("type", device.getSpecParams().get("type").toString());
        if (device.getSpecParams().containsKey(VdsProperties.Model)) {
            writer.writeAttributeString("model", device.getSpecParams().get(VdsProperties.Model).toString());
        }
        if (device.getSpecParams().containsKey(VdsProperties.Index)) {
            writer.writeAttributeString("index", device.getSpecParams().get(VdsProperties.Index).toString());
        }
        if (device.getSpecParams().containsKey("ports")) {
            writer.writeAttributeString("ports", device.getSpecParams().get("ports").toString());
        }
        // TODO: master??
        writeDeviceAliasAndAddress(writer, device);
        writer.writeEndElement();
    }

    /**
     * TODO:
     * add qemu_drive_cache configurable like in VDSM?
     */
    private void writeManagedDisk(XmlTextWriter writer, VmDevice device, VM vm, Disk disk) {
        // <disk type='file' device='disk' snapshot='no'>
        //   <driver name='qemu' type='qcow2' cache='none'/>
        //   <source file='/path/to/image'/>
        //   <target dev='hda' bus='ide'/>
        //   <serial>54-a672-23e5b495a9ea</serial>
        // </disk>
        writer.writeStartElement("disk");

        DiskVmElement dve = disk.getDiskVmElementForVm(vm.getId());

        writeGeneralDiskAttributes(writer, device, disk, dve);
        writeDiskTarget(writer, dve);
        writeDiskSource(writer, disk);
        writeDiskDriver(writer, device, disk, dve, vm);
        writeDeviceAliasAndAddress(writer, device);
        writeBootOrder(writer, device.getBootOrder());
        // TODO: index of the first bootable disk

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
            writeIoTune(writer, (DiskImage) disk);
        }

        writer.writeEndElement();
    }

    private void writeIoTune(XmlTextWriter writer, DiskImage diskImage) {
        if (!qosCache.containsKey(diskImage.getDiskProfileId())) {
            qosCache.put(diskImage.getDiskProfileId(), vmInfoBuildUtils.loadStorageQos(diskImage));
        }
        StorageQos storageQos = qosCache.get(diskImage.getDiskProfileId());
        if (storageQos == null) {
            return;
        }
        writer.writeStartElement("iotune");
        ioTuneListFrom(storageQos).forEach(pair -> writer.writeAttributeString(pair.getFirst(), pair.getSecond().toString()));
        writer.writeEndElement();
    }

    private void writeDiskDriver(XmlTextWriter writer, VmDevice device, Disk disk, DiskVmElement dve, VM vm) {
        writer.writeStartElement("driver");
        switch (disk.getDiskStorageType()) {
        case IMAGE:
            DiskImage diskImage = (DiskImage) disk;
            writer.writeAttributeString("name", "qemu");
            writer.writeAttributeString("io", "threads");
            writer.writeAttributeString("type", diskImage.getVolumeFormat() == VolumeFormat.COW ? "qcow2" : "raw");
            if (FeatureSupported.passDiscardSupported(vm.getCompatibilityVersion()) && dve.isPassDiscard()) {
                writer.writeAttributeString("discard", "unmap");
            }
            if (device.getSpecParams().containsKey("pinToIoThread")) {
                writer.writeAttributeString("iothread", device.getSpecParams().get("pinToIoThread").toString());
            }
            writer.writeAttributeString("error_policy", disk.getPropagateErrors() == PropagateErrors.On ? "enospace" : "stop");
            break;

        case LUN:
            writer.writeAttributeString("name", "qemu");
            writer.writeAttributeString("io", "native");
            writer.writeAttributeString("type", "raw");
            if (FeatureSupported.passDiscardSupported(vm.getCompatibilityVersion()) && dve.isPassDiscard()) {
                writer.writeAttributeString("discard", "unmap");
            }
            if (device.getSpecParams().containsKey("pinToIoThread")) {
                writer.writeAttributeString("iothread", device.getSpecParams().get("pinToIoThread").toString());
            }
            writer.writeAttributeString("error_policy", disk.getPropagateErrors() == PropagateErrors.On ? "enospace" : "stop");
            break;

        case CINDER:
            // TODO
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
                // TODO: if custom property is set...
            default:
                writer.writeAttributeString("cache", "none");
            }
        }

        writer.writeEndElement();
    }

    private void writeDiskSource(XmlTextWriter writer, Disk disk) {
        writer.writeStartElement("source");
        switch (disk.getDiskStorageType()) {
        case IMAGE:
            DiskImage diskImage = (DiskImage) disk;
            writer.writeAttributeString(
                    "file",
                    String.format("/rhev/data-center/%s/%s/images/%s/%s",
                            diskImage.getStoragePoolId(),
                            diskImage.getStorageIds().get(0),
                            diskImage.getId(),
                            diskImage.getImageId()));
            break;

        case LUN:
            LunDisk lunDisk = (LunDisk) disk;
            writer.writeAttributeString(
                    "file",
                    String.format("/dev/mapper/%s",
                            lunDisk.getLun().getLUNId()));
            break;

        case CINDER:
            // TODO (auth)
            break;
        }
        writer.writeEndElement();
    }

    private void writeDiskTarget(XmlTextWriter writer, DiskVmElement dve) {
        writer.writeStartElement("target");
        switch (dve.getDiskInterface()) {
        case IDE:
            writer.writeAttributeString("dev", "hda"); // TODO: device name
            writer.writeAttributeString("bus", "ide");
            break;
        case VirtIO:
            writer.writeAttributeString("dev", "sda"); // TODO: device name
            writer.writeAttributeString("bus", "virtio");

            // TODO: index
            break;
        case VirtIO_SCSI:
            writer.writeAttributeString("dev", "sda"); // TODO: device name
            writer.writeAttributeString("bus", "scsi");

            // TODO address
            break;
        case SPAPR_VSCSI:
            // TODO address, name
            break;
        default:
            log.error("Unsupported interface type, ISCSI interface type is not supported.");
        }
        writer.writeEndElement();
    }

    private void writeGeneralDiskAttributes(XmlTextWriter writer, VmDevice device, Disk disk, DiskVmElement dve) {
        writer.writeAttributeString("snapshot", "no");

        switch (disk.getDiskStorageType()) {
        case IMAGE:
            writer.writeAttributeString("type", "file"); // TODO type of storage domain
            break;
        case LUN:
            writer.writeAttributeString("type", "block");
            break;
        case CINDER:
            // TODO
            break;
        }

        switch (dve.getDiskInterface()) {
        case IDE:
            writer.writeAttributeString("device", device.getDevice());
            break;
        case VirtIO:
            writer.writeAttributeString("device", disk.getDiskStorageType() == DiskStorageType.LUN ?
                    VmDeviceType.LUN.getName()
                    : device.getDevice());
            break;
        case VirtIO_SCSI:
            if (disk.getDiskStorageType() == DiskStorageType.LUN && disk.isScsiPassthrough()) {
                writer.writeAttributeString("device", VmDeviceType.LUN.getName());
                writer.writeAttributeString("sgio", disk.getSgio().toString().toLowerCase());
            } else {
                writer.writeAttributeString("device", device.getDevice());
            }
            // TODO
            break;
        case SPAPR_VSCSI:
            break;
        }
    }

    private void writeCdRom(XmlTextWriter writer, List<VmDevice> devices, VM vm) {
        if (devices.isEmpty()) {
            return;
        }
        // <disk type='file' device='cdrom'>
        //   <driver name='qemu' type='raw'/>
        //   <source startupPolicy='optional'/>
        //   <backingStore/>
        //   <target dev='hdc' bus='ide'/>
        //   <readonly/>
        //   <alias name='ide0-1-0'/>
        //   <address type='drive' controller='0' bus='1' target='0' unit='0'/>
        // </disk>
        VmDevice device = devices.get(0); // assume only 1 CD
        writer.writeStartElement("disk");
        writer.writeAttributeString("type", "file");
        writer.writeAttributeString("device", "cdrom");
        writer.writeAttributeString("snapshot", "no");

        writer.writeStartElement("source");
        writer.writeAttributeString("file", ""); // TODO: path
        writer.writeAttributeString("startupPolicy", "optional");
        writer.writeEndElement();

        String cdInterface = osRepository.getCdInterface(
                vm.getOs(),
                vm.getCompatibilityVersion(),
                ChipsetType.fromMachineType(vm.getEmulatedMachine()));

        writer.writeStartElement("target");
        writer.writeAttributeString("dev", "hdc"); // TODO
        writer.writeAttributeString("bus", cdInterface);
        writer.writeEndElement();

        writer.writeElement("readonly");

        writeDeviceAliasAndAddress(writer, device);

        writer.writeEndElement();
    }

    private void writeInterface(XmlTextWriter writer, VmDevice device, VM vm, VmNetworkInterface nic) {
        writer.writeStartElement("interface");

        Map<String, String> properties = VmPropertiesUtils.getInstance().getVMProperties(
                vm.getCompatibilityVersion(),
                vm.getStaticData());
        // TODO: driver
        switch (device.getDevice()) {
        case "bridge":
            writer.writeAttributeString("type", "bridge");
            writer.writeStartElement("model");
            VmInterfaceType ifaceType = nic.getType() != null ? VmInterfaceType.forValue(nic.getType()) : VmInterfaceType.rtl8139;
            writer.writeAttributeString("type", ifaceType == VmInterfaceType.pv ? "virtio" : ifaceType.getInternalName());
            writer.writeEndElement();

            writer.writeStartElement("link");
            writer.writeAttributeString("state", nic.isLinked() ? "up" : "down");
            writer.writeEndElement();
            // TODO: OVS
            writer.writeStartElement("source");
            writer.writeAttributeString("bridge", nic.getNetworkName());
            writer.writeEndElement();

            break;

        case "hostdev":
            writer.writeAttributeString("type", "hostdev");
            writer.writeAttributeString("managed", "no");
            writer.writeStartElement("driver");
            writer.writeAttributeString("name", "vfio");
            writer.writeEndElement();
            VnicProfile vnicProfile = vnicProfileDao.get(nic.getVnicProfileId());
            Network network = networkDao.get(vnicProfile.getNetworkId());
            if (NetworkUtils.isVlan(network)) {
                writer.writeStartElement("vlan");
                writer.writeStartElement("tag");
                writer.writeAttributeString("id", network.getVlanId().toString());
                writer.writeEndElement();
                writer.writeEndElement();
            }
            String vfDeviceName = vm.getPassthroughVnicToVfMap().get(nic.getId());
//            writer.writeStartElement("$SOURCE:" + vfDeviceName+"$");
//            writer.writeEndElement();
            break;
        }

        writeDeviceAliasAndAddress(writer, device);

        writeBootOrder(writer, device.getBootOrder());

        writer.writeStartElement("mac");
        writer.writeAttributeString("address", nic.getMacAddress());
        writer.writeEndElement();

        NetworkFilter networkFilter = vmInfoBuildUtils.fetchVnicProfileNetworkFilter(nic);
        if (networkFilter != null) {
            writer.writeStartElement("filterref");
            writer.writeAttributeString("filter", networkFilter.getName());
            writer.writeEndElement();
        }
        if (properties.containsKey("sndbuf")) {
            writer.writeStartElement("tune");
            writer.writeStartElement("sndbuf");
            writer.writeRaw(properties.get("sndbuf"));
            writer.writeEndElement();
            writer.writeEndElement();
        }
        if (device.getSpecParams().containsKey("inbound") || device.getSpecParams().containsKey("outbound")) {
            writer.writeStartElement("bandwidth");
            Map<String, Object> map = new HashMap<>();
            vmInfoBuildUtils.addProfileDataToNic(map, vm, device, nic);
            @SuppressWarnings("unchecked")
            Map<String, String> inboundMap = (Map<String, String>) map.get("inbound");
            if (inboundMap != null && !inboundMap.isEmpty()) {
                writer.writeStartElement("inbound");
                writer.writeAttributeString("average", inboundMap.get("average"));
                writer.writeAttributeString("burst", inboundMap.get("burst"));
                writer.writeAttributeString("peak", inboundMap.get("peak"));
                writer.writeEndElement();
            }
            @SuppressWarnings("unchecked")
            Map<String, String> outboundMap = (Map<String, String>) map.get("outbound");
            if (outboundMap != null && !outboundMap.isEmpty()) {
                writer.writeStartElement("outbound");
                writer.writeAttributeString("average", outboundMap.get("average"));
                writer.writeAttributeString("burst", outboundMap.get("burst"));
                writer.writeAttributeString("peak", outboundMap.get("peak"));
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void writeBalloon(XmlTextWriter writer, VmDevice device, VM vm) {
        // <memballoon model='virtio'>
        //   <address type='pci' domain='0x0000' bus='0x00' slot='0x04' function='0x0'/>
        // </memballoon>
        writer.writeStartElement("membaloon");
        writer.writeAttributeString("model", device.getSpecParams().get(VdsProperties.Model).toString());
        writeDeviceAliasAndAddress(writer, device);
        writer.writeEndElement();
    }

    private void writeSmartcard(XmlTextWriter writer, VmDevice device, VM vm) {
        // <smartcard mode='passthrough' type='spicevmc'>
        //   <address/>
        // </smartcard>
        writer.writeStartElement("smartcard");
        // TODO
//        String mode = device.getSpecParams().get(VdsProperties.Model).toString();
//        writer.writeAttributeString("model", );
        writeDeviceAliasAndAddress(writer, device);
        writer.writeEndElement();
    }

    private void writeWatchdog(XmlTextWriter writer, VmDevice device, VM vm) {
        // <watchdog model='i6300esb' action='reset'>
        //   <address type='pci' domain='0x0000' bus='0x00' slot='0x05' function='0x0'/>
        // </watchdog>
        writer.writeStartElement("watchdog");
        Object model = device.getSpecParams().get(VdsProperties.Model);
        writer.writeAttributeString("model", model != null ? model.toString() : "i6300esb");
        Object action = device.getSpecParams().get(VdsProperties.action);
        writer.writeAttributeString("action", action != null ? action.toString() : "none");
        writeDeviceAliasAndAddress(writer, device);
        writer.writeEndElement();
    }

    private void writeMemory(XmlTextWriter writer, VmDevice device, VM vm) {
        // <memory model='dimm'>
        //   <target>
        //     <size unit='KiB'>524287</size>
        //     <node>1</node>
        //   </target>
        // </memory>
        writer.writeStartElement("memory");
        writer.writeAttributeString("model", "dimm");

        writer.writeStartElement("target");

        writer.writeStartElement("size");
        writer.writeAttributeString("unit", "KiB");
        writer.writeRaw(String.valueOf(vm.getMemSizeMb() * 1000));
        writer.writeEndElement();

        writer.writeElement("node", "1"); // TODO

        writer.writeEndElement();

        writer.writeEndElement();
    }

    private void writeVideo(XmlTextWriter writer, VmDevice device, VM vm) {
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

        writeDeviceAliasAndAddress(writer, device);

        writer.writeEndElement();
    }

    private void writeBootOrder(XmlTextWriter writer, int order) {
        if (order > 0) {
            writer.writeStartElement("boot");
            writer.writeAttributeString("order", String.valueOf(order));
            writer.writeEndElement();
        }
    }

    private void writeDeviceAliasAndAddress(XmlTextWriter writer, VmDevice device) {
        Map<String, String> addressMap = StringMapUtils.string2Map(device.getAddress());
        if (!addressMap.isEmpty()) {
            writer.writeStartElement("address");
            addressMap.entrySet().forEach(x -> writer.writeAttributeString(x.getKey(), x.getValue()));
            writer.writeEndElement();
        }

        String alias = device.getAlias();
        if (StringUtils.isNotEmpty(alias)) {
            writer.writeStartElement("alias");
            writer.writeAttributeString("name", alias);
            writer.writeEndElement();
        }
    }

    private void writeInput(XmlTextWriter writer, VM vm) {
        writer.writeStartElement("input");

        boolean tabletEnable = vm.getGraphicsInfos().size() == 1 && vm.getGraphicsInfos().containsKey(GraphicsType.VNC);
        if (tabletEnable) {
            writer.writeAttributeString("type", "tablet");
            writer.writeAttributeString("bus", "usb");
        }
        else if (vm.getClusterArch().getFamily() == ArchitectureType.x86) {
            writer.writeAttributeString("type", "mouse");
            writer.writeAttributeString("bus", "ps2");
        }
        else {
            writer.writeAttributeString("type", "mouse");
            writer.writeAttributeString("bus", "usb");
        }

        writer.writeEndElement();
    }

//    private int getBootableDiskIndex(Disk disk) {
//        int index = ArchStrategyFactory.getStrategy(vm.getClusterArch())
//                .run(new GetBootableDiskIndex(numOfReservedScsiIndexes))
//                .returnValue();
//        log.info("Bootable disk '{}' set to index '{}'", disk.getId(), index);
//        return index;
//    }

}
