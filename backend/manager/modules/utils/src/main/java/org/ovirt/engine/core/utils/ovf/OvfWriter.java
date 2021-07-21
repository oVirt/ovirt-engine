package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Match;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.VmInitUtils;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlTextWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OvfWriter implements IOvfBuilder {
    private int _instanceId;
    protected List<DiskImage> _images;
    protected List<LunDisk> lunDisks;
    protected XmlTextWriter _writer;
    protected XmlDocument _document;
    protected VmBase vmBase;
    protected Version version;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public abstract String getOvfUri();

    public OvfWriter(VmBase vmBase, List<DiskImage> images, List<LunDisk> lunDisks, Version version) {
        _document = new XmlDocument();
        _images = images;

        // We use a specific parameter for lunDisks to avoid any additional changes in other writer classes.
        this.lunDisks = lunDisks;
        _writer = new XmlTextWriter();
        this.vmBase = vmBase;
        this.version = version;
        writeHeader();
    }

    protected Version getVersion() {
        return version;
    }

    protected void writeHeader() {
        _instanceId = 0;
        _writer.writeStartDocument(false);

        _writer.setPrefix(OVF_PREFIX, getOvfUri());
        _writer.setPrefix(RASD_PREFIX, RASD_URI);
        _writer.setPrefix(VSSD_PREFIX, VSSD_URI);
        _writer.setPrefix(XSI_PREFIX, XSI_URI);

        _writer.writeStartElement(getOvfUri(), "Envelope");
        _writer.writeNamespace(OVF_PREFIX, getOvfUri());
        _writer.writeNamespace(RASD_PREFIX, RASD_URI);
        _writer.writeNamespace(VSSD_PREFIX, VSSD_URI);
        _writer.writeNamespace(XSI_PREFIX, XSI_URI);
    }

    protected long bytesToGigabyte(long bytes) {
        return (long) Math.ceil(bytes / Math.pow(1024, 3));
    }

    @Override
    public void buildReference() {
        _writer.writeStartElement("References");
        writeReferenceData();
        _writer.writeEndElement();
    }

    protected void writeReferenceData() {
        _images.forEach(image -> {
            _writer.writeStartElement("File");
            writeFile(image);
            _writer.writeEndElement();
        });
        lunDisks.forEach(lun -> {
            _writer.writeStartElement("File");
            writeFileForLunDisk(lun);
            _writer.writeEndElement();
        });
    }

    protected abstract void writeFile(DiskImage image);

    protected void writeFileForLunDisk(LunDisk lun) {
        // do nothing
    }

    private String escapeNewLines(String value){
        return value.replaceAll("\n", "&#10;");
    }

    protected void writeVmInit() {
        if (vmBase.getVmInit() != null) {
            VmInit vmInit = vmBase.getVmInit();
            _writer.writeStartElement("VmInit");
            if (vmInit.getHostname() != null) {
                _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "hostname", vmInit.getHostname());
            }
            if (vmInit.getDomain() != null) {
                _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "domain", vmInit.getDomain());
            }
            if (vmInit.getTimeZone() != null) {
                _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "timeZone", mapTimeZone(vmInit.getTimeZone()));
            }
            if (vmInit.getAuthorizedKeys() != null) {
                _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "authorizedKeys", escapeNewLines(vmInit.getAuthorizedKeys()));
            }
            if (vmInit.getRegenerateKeys() != null) {
                _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "regenerateKeys", vmInit.getRegenerateKeys().toString());
            }
            if (vmInit.getDnsSearch() != null) {
                _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "dnsSearch", vmInit.getDnsSearch());
            }
            if (vmInit.getDnsServers() != null) {
                _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "dnsServers", vmInit.getDnsServers());
            }
            if (vmInit.getNetworks() != null) {
                _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "networks", VmInitUtils.networkListToJson(vmInit.getNetworks()));
            }
            if (vmInit.getWinKey() != null) {
                _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "winKey", vmInit.getWinKey());
            }
            if (vmInit.getRootPassword() != null) {
                _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "rootPassword", vmInit.getRootPassword());
            }
            if (vmInit.getCustomScript() != null) {
                _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "customScript", escapeNewLines(vmInit.getCustomScript()));
            }
            _writer.writeEndElement();
        }
    }

    @Override
    public void buildNetwork() {
        _writer.writeStartElement("NetworkSection");
        _writer.writeElement("Info", "List of networks");
        vmBase.getInterfaces().stream().map(VmNetworkInterface::getNetworkName).filter(Objects::nonNull).distinct()
        .forEach(network -> {
            _writer.writeStartElement("Network");
            _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "name", network);
            _writer.writeEndElement();
        });
        _writer.writeEndElement();
    }

    @Override
    public void buildDisk() {
        startDiskSection();
        _writer.writeElement("Info", "List of Virtual Disks");
        _images.forEach(image -> {
            _writer.writeStartElement("Disk");
            writeDisk(image);
            _writer.writeEndElement();
        });
        lunDisks.forEach(lun -> {
            _writer.writeStartElement("Disk");
            writeLunDisk(lun);
            _writer.writeEndElement();
        });
        _writer.writeEndElement();
    }

    protected abstract void writeDisk(DiskImage image);

    protected void writeLunDisk(LunDisk lun) {
        // do nothing
    }

    @Override
    public void buildVirtualSystem() {
        startVirtualSystem();

        writeGeneralData();
        writeAppList();
        writeOS();
        writeHardware();

        _writer.writeEndElement();
    }

    protected void writeGeneralData() {
        _writer.writeElement(NAME, vmBase.getName());
        if (vmBase.getDescription() != null) {
            _writer.writeElement(DESCRIPTION, vmBase.getDescription());
        }

        if (vmBase.getComment() != null) {
            _writer.writeElement(COMMENT, vmBase.getComment());
        }

        _writer.writeElement(CREATION_DATE, OvfParser.localDateToUtcDateString(vmBase.getCreationDate()));
        _writer.writeElement(EXPORT_DATE, OvfParser.localDateToUtcDateString(new Date()));
        _writer.writeElement(DELETE_PROTECTED, String.valueOf(vmBase.isDeleteProtected()));

        if (vmBase.getSsoMethod() != null) {
            _writer.writeElement(SSO_METHOD, vmBase.getSsoMethod().toString());
        }

        _writer.writeElement(IS_SMARTCARD_ENABLED, String.valueOf(vmBase.isSmartcardEnabled()));

        _writer.writeElement(NUM_OF_IOTHREADS, String.valueOf(vmBase.getNumOfIoThreads()));

        _writer.writeElement(TIMEZONE, mapTimeZone(vmBase.getTimeZone()));
        _writer.writeElement(DEFAULT_BOOT_SEQUENCE, String.valueOf(vmBase.getDefaultBootSequence().getValue()));

        if (!StringUtils.isBlank(vmBase.getInitrdUrl())) {
            _writer.writeElement(INITRD_URL, vmBase.getInitrdUrl());
        }
        if (!StringUtils.isBlank(vmBase.getKernelUrl())) {
            _writer.writeElement(KERNEL_URL, vmBase.getKernelUrl());
        }
        if (!StringUtils.isBlank(vmBase.getKernelParams())) {
            _writer.writeElement(KERNEL_PARAMS, vmBase.getKernelParams());
        }

        _writer.writeElement(GENERATION, String.valueOf(vmBase.getDbGeneration()));

        if (vmBase.getCustomCompatibilityVersion() != null) {
            _writer.writeElement(CUSTOM_COMPATIBILITY_VERSION, String.valueOf(vmBase.getCustomCompatibilityVersion()));
        }

        _writer.writeElement(CLUSTER_COMPATIBILITY_VERSION, String.valueOf(version));// cluster version the VM/Snapshot
                                                                                     // originates from
        _writer.writeElement(VM_TYPE, String.valueOf(vmBase.getVmType().getValue()));
        if (vmBase.getResumeBehavior() != null) {
            _writer.writeElement(RESUME_BEHAVIOR, String.valueOf(vmBase.getResumeBehavior()));
        }

        if (vmBase.getTunnelMigration() != null) {
            _writer.writeElement(TUNNEL_MIGRATION, String.valueOf(vmBase.getTunnelMigration()));
        }

        if (vmBase.getVncKeyboardLayout() != null) {
            _writer.writeElement(VNC_KEYBOARD_LAYOUT, vmBase.getVncKeyboardLayout());
        }

        _writer.writeElement(MIN_ALLOCATED_MEMORY, String.valueOf(vmBase.getMinAllocatedMem()));
        _writer.writeElement(IS_STATELESS, String.valueOf(vmBase.isStateless()));
        _writer.writeElement(IS_RUN_AND_PAUSE, String.valueOf(vmBase.isRunAndPause()));
        _writer.writeElement(AUTO_STARTUP, String.valueOf(vmBase.isAutoStartup()));
        _writer.writeElement(PRIORITY, String.valueOf(vmBase.getPriority()));

        if (vmBase.getCreatedByUserId() != null) {
            _writer.writeElement(CREATED_BY_USER_ID, String.valueOf(vmBase.getCreatedByUserId()));
        }

        if (vmBase.getMigrationDowntime() != null) {
            _writer.writeElement(MIGRATION_DOWNTIME, String.valueOf(vmBase.getMigrationDowntime()));
        }
        writeVmInit();

        if (vmBase.getMigrationSupport() != null) {
            _writer.writeElement(MIGRATION_SUPPORT, String.valueOf(vmBase.getMigrationSupport().getValue()));
        }

        // TODO dedicated to multiple hosts - are we breaking any standard here?
        vmBase.getDedicatedVmForVdsList().forEach(d -> _writer.writeElement(DEDICATED_VM_FOR_VDS, String.valueOf(d)));

        if (vmBase.getSerialNumberPolicy() != null) {
            _writer.writeElement(SERIAL_NUMBER_POLICY, String.valueOf(vmBase.getSerialNumberPolicy().getValue()));
        }

        if (vmBase.getCustomSerialNumber() != null) {
            _writer.writeElement(CUSTOM_SERIAL_NUMBER, vmBase.getCustomSerialNumber());
        }

        _writer.writeElement(IS_BOOT_MENU_ENABLED, String.valueOf(vmBase.isBootMenuEnabled()));
        _writer.writeElement(IS_SPICE_FILE_TRANSFER_ENABLED, String.valueOf(vmBase.isSpiceFileTransferEnabled()));
        _writer.writeElement(IS_SPICE_COPY_PASTE_ENABLED, String.valueOf(vmBase.isSpiceCopyPasteEnabled()));
        _writer.writeElement(ALLOW_CONSOLE_RECONNECT, String.valueOf(vmBase.isAllowConsoleReconnect()));
        _writer.writeElement(CONSOLE_DISCONNECT_ACTION, String.valueOf(vmBase.getConsoleDisconnectAction()));

        if (vmBase.getAutoConverge() != null) {
            _writer.writeElement(IS_AUTO_CONVERGE, String.valueOf(vmBase.getAutoConverge()));
        }

        if (vmBase.getMigrateCompressed() != null) {
            _writer.writeElement(IS_MIGRATE_COMPRESSED, String.valueOf(vmBase.getMigrateCompressed()));
        }

        if (vmBase.getMigrateEncrypted() != null) {
            _writer.writeElement(IS_MIGRATE_ENCRYPTED, String.valueOf(vmBase.getMigrateEncrypted()));
        }

        if (vmBase.getMigrationPolicyId() != null) {
            _writer.writeElement(MIGRATION_POLICY_ID, String.valueOf(vmBase.getMigrationPolicyId()));
        }

        writeCustomEmulatedMachine();
        writeBiosType();
        writeCustomCpuName();

        _writer.writeElement(PREDEFINED_PROPERTIES, vmBase.getPredefinedProperties());
        _writer.writeElement(USER_DEFINED_PROPERTIES, vmBase.getUserDefinedProperties());
        _writer.writeElement(MAX_MEMORY_SIZE_MB, String.valueOf(vmBase.getMaxMemorySizeMb()));

        if (vmBase.getLeaseStorageDomainId() != null) {
            _writer.writeElement(VM_LEASE, vmBase.getLeaseStorageDomainId().toString());
        }

        _writer.writeElement(MULTI_QUEUES_ENABLED, String.valueOf(vmBase.isMultiQueuesEnabled()));
        writeVirtioMultiQueues();

        _writer.writeElement(USE_HOST_CPU, String.valueOf(vmBase.isUseHostCpuFlags()));
        _writer.writeElement(BALLOON_ENABLED, String.valueOf(vmBase.isBalloonEnabled()));
        _writer.writeElement(CPU_PINNING_POLICY, String.valueOf(vmBase.getCpuPinningPolicy().getValue()));
    }

    protected void writeVirtioMultiQueues() {
        int numOfQueues = vmBase.getVirtioScsiMultiQueues();
        _writer.writeStartElement(VIRTIO_SCSI_MULTI_QUEUES_ENABLED);
        if (numOfQueues > 0) {
            _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "queues", String.valueOf(numOfQueues));
        }
        String virtioMultiQueueEnabled = String.valueOf(numOfQueues != 0);
        _writer.writeRaw(virtioMultiQueueEnabled);
        _writer.writeEndElement();
    }

    protected void writeCustomEmulatedMachine() {
        _writer.writeElement(CUSTOM_EMULATED_MACHINE, vmBase.getCustomEmulatedMachine());
    }

    protected void writeBiosType() {
        BiosType biosType = vmBase.getBiosType();
        if (biosType != null) {
            // For compatibility with oVirt 4.3, use values of BiosType constants that existed before
            // introduction of CLUSTER_DEFAULT:  0 == I440FX_SEA_BIOS and so on
            _writer.writeStartElement(BIOS_TYPE);
            _writer.writeRaw(String.valueOf(biosType.getValue() - 1));
            _writer.writeEndElement();
        }
    }

    protected abstract String getInstaceIdTag();

    protected void writeCustomCpuName() {
        _writer.writeElement(CUSTOM_CPU_NAME, vmBase.getCustomCpuName());

    }

    protected void writeAppList() {
        if (!_images.isEmpty()) {
            if (StringUtils.isBlank(_images.get(0).getAppList())) {
                return;
            }

            String[] apps = _images.get(0).getAppList().split("[,]", -1);
            for (String app : apps) {
                String product = app;
                String version = "";
                Match match = Regex.match(app, "(.*) ([0-9.]+)", RegexOptions.Singleline | RegexOptions.IgnoreCase);

                if (match.groups().size() > 1) {
                    product = match.groups().get(1).getValue();
                }
                if (match.groups().size() > 2) {
                    version = match.groups().get(2).getValue();
                }

                _writer.writeStartElement("ProductSection");
                _writer.writeAttributeString(OVF_PREFIX, getOvfUri(), "class", product);
                _writer.writeElement("Info", app);
                _writer.writeElement("Product", product);
                _writer.writeElement("Version", version);
                _writer.writeEndElement();
            }
        }
    }

    protected void writeHardware() {
        startHardwareSection();
        writeInfo();
        writeSystem();
        writeCpu();
        writeMemory();
        writeDrive();
        writeNetwork();
        writeUsb();
        writeMonitors();
        writeGraphics();
        writeCd();
        writeTpm();
        writeOtherDevices();
        _writer.writeEndElement();
    }

    private void writeManagedDeviceInfo(VmBase vmBase, Guid deviceId) {
        VmDevice vmDevice = vmBase.getManagedDeviceMap().get(deviceId);
        if (deviceId != null && vmDevice != null && vmDevice.getAddress() != null) {
            writeVmDeviceInfo(vmDevice);
        }
    }

    private String mapTimeZone(String timezone) {
        // map non-windows timezone that was added in 4.4.8 and didn't existed before to known timezone
        // to support importing OVA in older versions from exported version >= 4.4.8
        if (timezone != null) {
            switch (timezone) {
                case "Atlantic/South_Georgia":
                    return "Etc/GMT";
            }
        }
        return timezone;
    }

    private void writeOtherDevices() {
        List<VmDevice> devices = new ArrayList<>(vmBase.getUnmanagedDeviceList());

        Collection<VmDevice> managedDevices = vmBase.getManagedDeviceMap().values();
        for (VmDevice device : managedDevices) {
            if (isSpecialDevice(device)) {
                devices.add(device);
            }
        }

        for (VmDevice vmDevice : devices) {
            writeVmDevice(vmDevice);
        }
    }

    protected boolean isSpecialDevice(VmDevice vmDevice) {
        return VmDeviceCommonUtils.isSpecialDevice(vmDevice.getDevice(), vmDevice.getType(), false);
    }

    protected void writeVmDevice(VmDevice vmDevice) {
        _writer.writeStartElement("Item");
        _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.OTHER);
        _writer.writeElement(RASD_URI, getInstaceIdTag(), vmDevice.getId().getDeviceId().toString());
        writeVmDeviceInfo(vmDevice);
        _writer.writeEndElement(); // item
    }

    private void writeMonitors() {
        Collection<VmDevice> devices = vmBase.getManagedDeviceMap().values();
        int numOfMonitors = vmBase.getNumOfMonitors();
        int i = 0;
        for (VmDevice vmDevice : devices) {
            if (vmDevice.getType() == VmDeviceGeneralType.VIDEO) {
                _writer.writeStartElement("Item");
                _writer.writeElement(RASD_URI, "Caption", "Graphical Controller");
                _writer.writeElement(RASD_URI, getInstaceIdTag(), vmDevice.getId().getDeviceId().toString());
                _writer.writeElement(RASD_URI, "ResourceType", adjustHardwareResourceType(OvfHardware.Monitor));
                // we should write number of monitors for each entry for backward compatibility
                _writer.writeElement(RASD_URI, "VirtualQuantity", String.valueOf(numOfMonitors));
                _writer.writeElement(RASD_URI, "SinglePciQxl", String.valueOf(VmDeviceCommonUtils.isSingleQxlPci(vmBase)));
                writeVmDeviceInfo(vmDevice);
                _writer.writeEndElement(); // item
                if (i++ == numOfMonitors) {
                    break;
                }
            }
        }
    }

    private void writeGraphics() {
        Collection<VmDevice> devices = vmBase.getManagedDeviceMap().values();
        for (VmDevice vmDevice : devices) {
            if (vmDevice.getType() == VmDeviceGeneralType.GRAPHICS) {
                _writer.writeStartElement("Item");
                _writer.writeElement(RASD_URI, "Caption", "Graphical Framebuffer");
                _writer.writeElement(RASD_URI, getInstaceIdTag(), vmDevice.getId().getDeviceId().toString());
                _writer.writeElement(RASD_URI, "ResourceType", adjustHardwareResourceType(OvfHardware.Graphics));
                writeVmDeviceInfo(vmDevice);
                _writer.writeEndElement(); // item
            }
        }
    }

    protected String adjustHardwareResourceType(String resourceType) {
        return resourceType;
    }

    private void writeCd() {
        Collection<VmDevice> devices = vmBase.getManagedDeviceMap().values();
        for (VmDevice vmDevice : devices) {
            if (vmDevice.getDevice().equals(VmDeviceType.CDROM.getName())) {
                _writer.writeStartElement("Item");
                _writer.writeElement(RASD_URI, "Caption", "CDROM");
                _writer.writeElement(RASD_URI, getInstaceIdTag(), vmDevice.getId().getDeviceId().toString());
                _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.CD);
                writeVmDeviceInfo(vmDevice);
                _writer.writeEndElement(); // item
                break; // only one CD is currently supported
            }
        }
    }

    private void writeTpm() {
        Collection<VmDevice> devices = vmBase.getManagedDeviceMap().values();
        for (VmDevice vmDevice : devices) {
            if (vmDevice.getDevice().equals(VmDeviceType.TPM.getName())) {
                _writer.writeStartElement("Item");
                _writer.writeElement(RASD_URI, "Caption", "TPM");
                _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.OTHER);
                _writer.writeElement(RASD_URI, getInstaceIdTag(), vmDevice.getId().getDeviceId().toString());
                writeTpmHostResource();
                writeVmDeviceInfo(vmDevice);
                _writer.writeEndElement(); // item
                break; // only one TPM device is currently supported
            }
        }
    }

    protected void writeTpmHostResource() {
    }

    protected void writeVmDeviceInfo(VmDevice vmDevice) {
        _writer.writeElement(VMD_TYPE, String.valueOf(vmDevice.getType().getValue()));
        _writer.writeElement(VMD_DEVICE, String.valueOf(vmDevice.getDevice()));
        _writer.writeElement(VMD_ADDRESS, vmDevice.getAddress());
        _writer.writeElement(VMD_BOOT_ORDER, String.valueOf(vmDevice.getBootOrder()));
        _writer.writeElement(VMD_IS_PLUGGED, String.valueOf(vmDevice.isPlugged()));
        _writer.writeElement(VMD_IS_READONLY, String.valueOf(vmDevice.getReadOnly()));
        _writer.writeElement(VMD_ALIAS, String.valueOf(vmDevice.getAlias()));
        if (vmDevice.getSpecParams() != null && vmDevice.getSpecParams().size() != 0
                && !VmPayload.isPayload(vmDevice.getSpecParams())) {
            _writer.writeStartElement(VMD_SPEC_PARAMS);
            _writer.writeMap(vmDevice.getSpecParams());
            _writer.writeEndElement();
        }
        if (vmDevice.getCustomProperties() != null && !vmDevice.getCustomProperties().isEmpty()) {
            _writer.writeElement(VMD_CUSTOM_PROP,
                    DevicePropertiesUtils.getInstance().convertProperties(vmDevice.getCustomProperties()));
        }

        if (vmDevice.getSnapshotId() != null) {
            _writer.writeElement(VMD_SNAPSHOT_PROP, String.valueOf(vmDevice.getSnapshotId()));
        }
    }

    @Override
    public String getStringRepresentation() {
        return _writer.getStringXML();
    }

    protected abstract void writeOS();
    protected abstract void startHardwareSection();
    protected abstract void startDiskSection();
    protected abstract void startVirtualSystem();

    protected void writeInfo() {
        _writer.writeElement("Info",
                String.format("%1$s CPU, %2$s Memory", vmBase.getNumOfCpus(), vmBase.getMemSizeMb()));
    }

    protected void writeSystem() {
        _writer.writeStartElement("System");
        _writer.writeElement(VSSD_URI, "VirtualSystemType", String.format("%1$s %2$s",
                Config.getValue(ConfigValues.OvfVirtualSystemType),
                Config.getValue(ConfigValues.VdcVersion)));
        _writer.writeEndElement();
    }

    private void writeCpu() {
        _writer.writeStartElement("Item");
        _writer.writeElement(RASD_URI, "Caption", String.format("%1$s virtual cpu", vmBase.getNumOfCpus()));
        _writer.writeElement(RASD_URI, "Description", "Number of virtual CPU");
        _writer.writeElement(RASD_URI, getInstaceIdTag(), String.valueOf(++_instanceId));
        _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.CPU);
        _writer.writeElement(RASD_URI, "num_of_sockets", String.valueOf(vmBase.getNumOfSockets()));
        _writer.writeElement(RASD_URI, "cpu_per_socket", String.valueOf(vmBase.getCpuPerSocket()));
        _writer.writeElement(RASD_URI, "threads_per_cpu", String.valueOf(vmBase.getThreadsPerCpu()));
        _writer.writeElement(RASD_URI, "max_num_of_vcpus", String.valueOf(maxNumOfVcpus())); // TODO: replace with Limit
        _writer.writeElement(RASD_URI, "VirtualQuantity", String.valueOf(vmBase.getNumOfCpus()));
        _writer.writeEndElement();
    }

    protected abstract Integer maxNumOfVcpus();

    private void writeMemory() {
        _writer.writeStartElement("Item");
        _writer.writeElement(RASD_URI, "Caption", String.format("%1$s MB of memory", vmBase.getMemSizeMb()));
        _writer.writeElement(RASD_URI, "Description", "Memory Size");
        _writer.writeElement(RASD_URI, getInstaceIdTag(), String.valueOf(++_instanceId));
        _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.Memory);
        _writer.writeElement(RASD_URI, "AllocationUnits", "MegaBytes");
        _writer.writeElement(RASD_URI, "VirtualQuantity", String.valueOf(vmBase.getMemSizeMb()));
        _writer.writeEndElement(); // item
    }

    private void writeDrive() {
        for (DiskImage image : _images) {
            _writer.writeStartElement("Item");
            _writer.writeElement(RASD_URI, "Caption", image.getDiskAlias());
            _writer.writeElement(RASD_URI, getInstaceIdTag(), image.getImageId().toString());
            _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.DiskImage);
            _writer.writeElement(RASD_URI, "HostResource", getDriveHostResource(image));
            _writer.writeElement(RASD_URI, "Parent", image.getParentId().toString());
            _writer.writeElement(RASD_URI, "Template", image.getImageTemplateId().toString());
            _writer.writeElement(RASD_URI, "ApplicationList", image.getAppList());
            if (image.getStorageIds() != null && image.getStorageIds().size() > 0) {
                image.getStorageIds().forEach(guid ->
                        _writer.writeElement(RASD_URI, "StorageId", guid.toString()));
            }
            if (image.getStoragePoolId() != null) {
                _writer.writeElement(RASD_URI, "StoragePoolId", image.getStoragePoolId().toString());
            }
            _writer.writeElement(RASD_URI, "CreationDate", OvfParser.localDateToUtcDateString(image.getCreationDate()));
            _writer.writeElement(RASD_URI, "LastModified", OvfParser.localDateToUtcDateString(image.getLastModified()));
            _writer.writeElement(RASD_URI,
                    "last_modified_date",
                    OvfParser.localDateToUtcDateString(image.getLastModifiedDate()));
            writeManagedDeviceInfo(vmBase, image.getId());
            _writer.writeEndElement(); // item
        }
    }

    protected abstract String getDriveHostResource(DiskImage image);

    private void writeUsb() {
        _writer.writeStartElement("Item");
        _writer.writeElement(RASD_URI, "Caption", "USB Controller");
        _writer.writeElement(RASD_URI, getInstaceIdTag(), String.valueOf(++_instanceId));
        _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.USB);
        _writer.writeElement(RASD_URI,
                "UsbPolicy",
                vmBase.getUsbPolicy() != null ? vmBase.getUsbPolicy().toString() : UsbPolicy.DISABLED.name());
        _writer.writeEndElement(); // item
    }

    private void writeNetwork() {
        for (VmNetworkInterface iface : vmBase.getInterfaces()) {
            _writer.writeStartElement("Item");
            _writer.writeStartElement(RASD_URI, "Caption");
            String networkName = iface.getNetworkName() != null ? iface.getNetworkName() : "[No Network]";
            _writer.writeRaw("Ethernet adapter on " + networkName);
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, getInstaceIdTag());
            _writer.writeRaw(iface.getId().toString());
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "ResourceType");
            _writer.writeRaw(OvfHardware.Network);
            _writer.writeEndElement();

            _writer.writeStartElement(RASD_URI, "OtherResourceType");
            if (StringUtils.isNotEmpty(iface.getVnicProfileName())) {
                _writer.writeRaw(iface.getVnicProfileName());
            }
            _writer.writeEndElement();

            _writer.writeStartElement(RASD_URI, "ResourceSubType");
            if (iface.getType() != null) {
                _writer.writeRaw(iface.getType().toString());
            }
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "Connection");
            if (iface.getNetworkName() != null) {
                _writer.writeRaw(iface.getNetworkName());
            }
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "Linked");
            _writer.writeRaw(String.valueOf(iface.isLinked()));
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "Name");
            _writer.writeRaw(iface.getName());
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "ElementName");
            _writer.writeRaw(iface.getName());
            _writer.writeEndElement();

            writeMacAddress(iface);

            _writer.writeStartElement(RASD_URI, "speed");
            // version prior to 2.3 may not have speed so we get it by type
            if (iface.getSpeed() != null) {
                _writer.writeRaw(iface.getSpeed().toString());
            } else {
                _writer.writeRaw(String.valueOf(VmInterfaceType.forValue(
                        iface.getType()).getSpeed()));
            }
            _writer.writeEndElement();
            writeManagedDeviceInfo(vmBase, iface.getId());
            _writer.writeEndElement(); // item
        }
    }

    protected void writeMacAddress(VmNetworkInterface iface) {
        _writer.writeElement(RASD_URI, "MACAddress", iface.getMacAddress());
    }

    protected String getVolumeImageFormat(VolumeFormat format) {
        switch (format) {
        case RAW:
            return "http://www.vmware.com/specifications/vmdk.html#sparse";
        case COW:
            return "http://www.gnome.org/~markmc/qcow-image-format.html";
        case Unassigned:
        default:
            return "";
        }
    }

    protected int convertBytesToGigabyte(long bytes) {
        return (int) Math.ceil((double) bytes / BYTES_IN_GB);
    }
}
