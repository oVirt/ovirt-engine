package org.ovirt.engine.core.utils.ovf;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.VmInitUtils;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlTextWriter;

public abstract class OvfWriter implements IOvfBuilder {
    protected int _instanceId;
    protected List<DiskImage> _images;
    protected XmlTextWriter _writer;
    protected XmlDocument _document;
    protected VmBase vmBase;
    private Version version;

    private OsRepository osRepository;

    public OvfWriter(VmBase vmBase, List<DiskImage> images, Version version, OsRepository osRepository) {
        _document = new XmlDocument();
        _images = images;
        _writer = new XmlTextWriter();
        this.vmBase = vmBase;
        this.version = version;
        this.osRepository = osRepository;
        writeHeader();
    }

    protected Version getVersion() {
        return version;
    }

    private void writeHeader() {
        _instanceId = 0;
        _writer.writeStartDocument(false);

        _writer.setPrefix(OVF_PREFIX, OVF_URI);
        _writer.setPrefix(RASD_PREFIX, RASD_URI);
        _writer.setPrefix(VSSD_PREFIX, VSSD_URI);
        _writer.setPrefix(XSI_PREFIX, XSI_URI);

        _writer.writeStartElement(OVF_URI, "Envelope");
        _writer.writeNamespace(OVF_PREFIX, OVF_URI);
        _writer.writeNamespace(RASD_PREFIX, RASD_URI);
        _writer.writeNamespace(VSSD_PREFIX, VSSD_URI);
        _writer.writeNamespace(XSI_PREFIX, XSI_URI);

        // Setting the OVF version according to ENGINE (in 2.2 , version was set to "0.9")
        _writer.writeAttributeString(OVF_URI, "version", Config.getValue(ConfigValues.VdcVersion));
    }

    protected long bytesToGigabyte(long bytes) {
        return bytes / 1024 / 1024 / 1024;
    }

    @Override
    public void buildReference() {
        _writer.writeStartElement("References");
        for (DiskImage image : _images) {
            _writer.writeStartElement("File");
            _writer.writeAttributeString(OVF_URI, "href", OvfParser.createImageFile(image));
            _writer.writeAttributeString(OVF_URI, "id", image.getImageId().toString());
            _writer.writeAttributeString(OVF_URI, "size", String.valueOf(image.getActualSizeInBytes()));
            _writer.writeAttributeString(OVF_URI, "description", StringUtils.defaultString(image.getDescription()));
            _writer.writeAttributeString(OVF_URI, "disk_storage_type", image.getDiskStorageType().name());
            _writer.writeAttributeString(OVF_URI, "cinder_volume_type", StringUtils.defaultString(image.getCinderVolumeType()));
            _writer.writeEndElement();
        }
        _writer.writeEndElement();
    }

    protected void writeVmInit() {
        if (vmBase.getVmInit() != null) {
            VmInit vmInit = vmBase.getVmInit();
            _writer.writeStartElement("VmInit");
            if (vmInit.getHostname() != null) {
                _writer.writeAttributeString(OVF_URI, "hostname", vmInit.getHostname());
            }
            if (vmInit.getDomain() != null) {
                _writer.writeAttributeString(OVF_URI, "domain", vmInit.getDomain());
            }
            if (vmInit.getTimeZone() != null) {
                _writer.writeAttributeString(OVF_URI, "timeZone", vmInit.getTimeZone());
            }
            if (vmInit.getAuthorizedKeys() != null) {
                _writer.writeAttributeString(OVF_URI, "authorizedKeys", vmInit.getAuthorizedKeys());
            }
            if (vmInit.getRegenerateKeys() != null) {
                _writer.writeAttributeString(OVF_URI, "regenerateKeys", vmInit.getRegenerateKeys().toString());
            }
            if (vmInit.getDnsSearch() != null) {
                _writer.writeAttributeString(OVF_URI, "dnsSearch", vmInit.getDnsSearch());
            }
            if (vmInit.getDnsServers() != null) {
                _writer.writeAttributeString(OVF_URI, "dnsServers", vmInit.getDnsServers());
            }
            if (vmInit.getNetworks() != null) {
                _writer.writeAttributeString(OVF_URI, "networks", VmInitUtils.networkListToJson(vmInit.getNetworks()));
            }
            if (vmInit.getWinKey() != null) {
                _writer.writeAttributeString(OVF_URI, "winKey", vmInit.getWinKey());
            }
            if (vmInit.getRootPassword() != null) {
                _writer.writeAttributeString(OVF_URI, "rootPassword", vmInit.getRootPassword());
            }
            if (vmInit.getCustomScript() != null) {
                _writer.writeAttributeString(OVF_URI, "customScript", vmInit.getCustomScript());
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
            _writer.writeAttributeString(OVF_URI, "name", network);
            _writer.writeEndElement();
        });
        _writer.writeEndElement();
    }

    @Override
    public void buildDisk() {
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":DiskSection_Type");
        _writer.writeStartElement("Info");
        _writer.writeRaw("List of Virtual Disks");
        _writer.writeEndElement();
        for (DiskImage image : _images) {
            DiskVmElement dve = image.getDiskVmElementForVm(vmBase.getId());
            _writer.writeStartElement("Disk");
            _writer.writeAttributeString(OVF_URI, "diskId", image.getImageId().toString());
            _writer.writeAttributeString(OVF_URI, "size", String.valueOf(bytesToGigabyte(image.getSize())));
            _writer.writeAttributeString(OVF_URI,
                    "actual_size",
                    String.valueOf(bytesToGigabyte(image.getActualSizeInBytes())));
            _writer.writeAttributeString(OVF_URI, "vm_snapshot_id", (image.getVmSnapshotId() != null) ? image
                    .getVmSnapshotId().toString() : "");

            if (image.getParentId().equals(Guid.Empty)) {
                _writer.writeAttributeString(OVF_URI, "parentRef", "");
            } else {
                int i = 0;
                while (_images.get(i).getImageId().equals(image.getParentId())) {
                    i++;
                }
                List<DiskImage> res = _images.subList(i, _images.size() - 1);

                if (res.size() > 0) {
                    _writer.writeAttributeString(OVF_URI, "parentRef", OvfParser.createImageFile(res.get(0)));
                } else {
                    _writer.writeAttributeString(OVF_URI, "parentRef", "");
                }
            }

            _writer.writeAttributeString(OVF_URI, "fileRef", OvfParser.createImageFile(image));

            String format = "";
            switch (image.getVolumeFormat()) {
            case RAW:
                format = "http://www.vmware.com/specifications/vmdk.html#sparse";
                break;

            case COW:
                format = "http://www.gnome.org/~markmc/qcow-image-format.html";
                break;

            case Unassigned:
                break;

            default:
                break;
            }
            _writer.writeAttributeString(OVF_URI, "format", format);
            _writer.writeAttributeString(OVF_URI, "volume-format", image.getVolumeFormat().toString());
            _writer.writeAttributeString(OVF_URI, "volume-type", image.getVolumeType().toString());
            _writer.writeAttributeString(OVF_URI, "disk-interface", dve.getDiskInterface().toString());
            _writer.writeAttributeString(OVF_URI, "boot", String.valueOf(dve.isBoot()));
            if (FeatureSupported.passDiscardSupported(version)) {
                _writer.writeAttributeString(OVF_URI, "pass-discard", String.valueOf(dve.isPassDiscard()));
            }
            if (image.getDiskAlias() != null) {
                _writer.writeAttributeString(OVF_URI, "disk-alias", image.getDiskAlias());
            }
            if (image.getDiskDescription() != null) {
                _writer.writeAttributeString(OVF_URI, "disk-description", image.getDiskDescription());
            }
            _writer.writeAttributeString(OVF_URI,
                    "wipe-after-delete",
                    String.valueOf(image.isWipeAfterDelete()));
            _writer.writeEndElement();
        }
        _writer.writeEndElement();
    }

    @Override
    public void buildVirtualSystem() {
        // General Vm
        _writer.writeStartElement("Content");
        _writer.writeAttributeString(OVF_URI, "id", "out");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":VirtualSystem_Type");

        // General Data
        writeGeneralData();

        // Application List
        writeAppList();

        // Content Items
        writeContentItems();

        _writer.writeEndElement(); // End Content tag
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

        if (vmBase.getNumOfIoThreads() != 0) {
            _writer.writeElement(NUM_OF_IOTHREADS, String.valueOf(vmBase.getNumOfIoThreads()));
        }

        _writer.writeElement(TIMEZONE, vmBase.getTimeZone());
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
        if (vmBase.getDedicatedVmForVdsList().size() > 0) {
            for (Guid hostId : vmBase.getDedicatedVmForVdsList()) {
                _writer.writeElement(DEDICATED_VM_FOR_VDS, String.valueOf(hostId));
            }
        }

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

        if (vmBase.getAutoConverge() != null) {
            _writer.writeElement(IS_AUTO_CONVERGE, String.valueOf(vmBase.getAutoConverge()));
        }

        if (vmBase.getMigrateCompressed() != null) {
            _writer.writeElement(IS_MIGRATE_COMPRESSED, String.valueOf(vmBase.getMigrateCompressed()));
        }

        if (vmBase.getMigrationPolicyId() != null) {
            _writer.writeElement(MIGRATION_POLICY_ID, String.valueOf(vmBase.getMigrationPolicyId()));
        }

        writeCustomEmulatedMachine();
        writeCustomCpuName();

        _writer.writeElement(PREDEFINED_PROPERTIES, vmBase.getPredefinedProperties());
        _writer.writeElement(USER_DEFINED_PROPERTIES, vmBase.getUserDefinedProperties());
        _writer.writeElement(MAX_MEMORY_SIZE_MB, String.valueOf(vmBase.getMaxMemorySizeMb()));

        if (vmBase.getLeaseStorageDomainId() != null) {
            _writer.writeElement(VM_LEASE, vmBase.getLeaseStorageDomainId().toString());
        }
    }

    protected void writeCustomEmulatedMachine() {
        _writer.writeElement(CUSTOM_EMULATED_MACHINE, vmBase.getCustomEmulatedMachine());
    }

    protected void writeCustomCpuName() {
        _writer.writeElement(CUSTOM_CPU_NAME, vmBase.getCustomCpuName());

    }

    protected abstract void writeAppList();

    protected void writeContentItems() {
        writeOS();

        startHardware();
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
        writeOtherDevices();
        endHardware();
    }

    protected void writeManagedDeviceInfo(VmBase vmBase, Guid deviceId) {
        VmDevice vmDevice = vmBase.getManagedDeviceMap().get(deviceId);
        if (deviceId != null && vmDevice != null && vmDevice.getAddress() != null) {
            writeVmDeviceInfo(vmDevice);
        }
    }

    protected void writeOtherDevices() {
        List<VmDevice> devices = vmBase.getUnmanagedDeviceList();

        Collection<VmDevice> managedDevices = vmBase.getManagedDeviceMap().values();
        for (VmDevice device : managedDevices) {
            if (VmDeviceCommonUtils.isSpecialDevice(device.getDevice(), device.getType())) {
                devices.add(device);
            }
        }

        for (VmDevice vmDevice : devices) {
            _writer.writeStartElement("Item");
            _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.OTHER);
            _writer.writeElement(RASD_URI, "InstanceId", vmDevice.getId().getDeviceId().toString());
            writeVmDeviceInfo(vmDevice);
            _writer.writeEndElement(); // item
        }
    }

    protected void writeMonitors() {
        Collection<VmDevice> devices = vmBase.getManagedDeviceMap().values();
        int numOfMonitors = vmBase.getNumOfMonitors();
        int i = 0;
        for (VmDevice vmDevice : devices) {
            if (vmDevice.getType() == VmDeviceGeneralType.VIDEO) {
                _writer.writeStartElement("Item");
                _writer.writeElement(RASD_URI, "Caption", "Graphical Controller");
                _writer.writeElement(RASD_URI, "InstanceId", vmDevice.getId().getDeviceId().toString());
                _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.Monitor);
                // we should write number of monitors for each entry for backward compatibility
                _writer.writeElement(RASD_URI, "VirtualQuantity", String.valueOf(numOfMonitors));
                _writer.writeElement(RASD_URI, "SinglePciQxl", String.valueOf(vmBase.getSingleQxlPci()));
                writeVmDeviceInfo(vmDevice);
                _writer.writeEndElement(); // item
                if (i++ == numOfMonitors) {
                    break;
                }
            }
        }
    }

    protected void writeGraphics() {
        Collection<VmDevice> devices = vmBase.getManagedDeviceMap().values();
        for (VmDevice vmDevice : devices) {
            if (vmDevice.getType() == VmDeviceGeneralType.GRAPHICS) {
                _writer.writeStartElement("Item");
                _writer.writeElement(RASD_URI, "Caption", "Graphical Framebuffer");
                _writer.writeElement(RASD_URI, "InstanceId", vmDevice.getId().getDeviceId().toString());
                _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.Graphics);
                writeVmDeviceInfo(vmDevice);
                _writer.writeEndElement(); // item
            }
        }
    }

    protected void writeCd() {
        Collection<VmDevice> devices = vmBase.getManagedDeviceMap().values();
        for (VmDevice vmDevice : devices) {
            if (vmDevice.getDevice().equals(VmDeviceType.CDROM.getName())) {
                _writer.writeStartElement("Item");
                _writer.writeElement(RASD_URI, "Caption", "CDROM");
                _writer.writeElement(RASD_URI, "InstanceId", vmDevice.getId().getDeviceId().toString());
                _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.CD);
                writeVmDeviceInfo(vmDevice);
                _writer.writeEndElement(); // item
                break; // only one CD is currently supported
            }
        }
    }

    private void writeVmDeviceInfo(VmDevice vmDevice) {
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

    protected void writeOS() {
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(OVF_URI, "id", vmBase.getId().toString());
        _writer.writeAttributeString(OVF_URI, "required", "false");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":OperatingSystemSection_Type");
        _writer.writeElement("Info", "Guest Operating System");
        _writer.writeElement("Description", osRepository.getUniqueOsNames().get(vmBase.getOsId()));
        _writer.writeEndElement();
    }

    protected void startHardware() {
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":VirtualHardwareSection_Type");
    }

    protected void endHardware() {
        _writer.writeEndElement();
    }

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

    protected void writeCpu() {
        _writer.writeStartElement("Item");
        _writer.writeElement(RASD_URI, "Caption", String.format("%1$s virtual cpu", vmBase.getNumOfCpus()));
        _writer.writeElement(RASD_URI, "Description", "Number of virtual CPU");
        _writer.writeElement(RASD_URI, "InstanceId", String.valueOf(++_instanceId));
        _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.CPU);
        _writer.writeElement(RASD_URI, "num_of_sockets", String.valueOf(vmBase.getNumOfSockets()));
        _writer.writeElement(RASD_URI, "cpu_per_socket", String.valueOf(vmBase.getCpuPerSocket()));
        _writer.writeElement(RASD_URI, "threads_per_cpu", String.valueOf(vmBase.getThreadsPerCpu()));
        _writer.writeElement(RASD_URI, "max_num_of_vcpus", String.valueOf(maxNumOfVcpus()));
        _writer.writeEndElement(); // item
    }

    protected abstract Integer maxNumOfVcpus();

    protected void writeMemory() {
        _writer.writeStartElement("Item");
        _writer.writeElement(RASD_URI, "Caption", String.format("%1$s MB of memory", vmBase.getMemSizeMb()));
        _writer.writeElement(RASD_URI, "Description", "Memory Size");
        _writer.writeElement(RASD_URI, "InstanceId", String.valueOf(++_instanceId));
        _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.Memory);
        _writer.writeElement(RASD_URI, "AllocationUnits", "MegaBytes");
        _writer.writeElement(RASD_URI, "VirtualQuantity", String.valueOf(vmBase.getMemSizeMb()));
        _writer.writeEndElement(); // item
    }

    protected void writeDrive() {
        for (DiskImage image : _images) {
            _writer.writeStartElement("Item");
            _writer.writeElement(RASD_URI, "Caption", image.getDiskAlias());
            _writer.writeElement(RASD_URI, "InstanceId", image.getImageId().toString());
            _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.DiskImage);
            _writer.writeElement(RASD_URI, "HostResource", OvfParser.createImageFile(image));
            _writer.writeElement(RASD_URI, "Parent", image.getParentId().toString());
            _writer.writeElement(RASD_URI, "Template", image.getImageTemplateId().toString());
            _writer.writeElement(RASD_URI, "ApplicationList", image.getAppList());
            if (image.getStorageIds() != null && image.getStorageIds().size() > 0) {
                _writer.writeElement(RASD_URI, "StorageId", image.getStorageIds().get(0).toString());
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

    protected void writeUsb() {
        _writer.writeStartElement("Item");
        _writer.writeElement(RASD_URI, "Caption", "USB Controller");
        _writer.writeElement(RASD_URI, "InstanceId", String.valueOf(++_instanceId));
        _writer.writeElement(RASD_URI, "ResourceType", OvfHardware.USB);
        _writer.writeElement(RASD_URI,
                "UsbPolicy",
                vmBase.getUsbPolicy() != null ? vmBase.getUsbPolicy().toString() : UsbPolicy.DISABLED.name());
        _writer.writeEndElement(); // item
    }

    protected void writeNetwork() {
        for (VmNetworkInterface iface : vmBase.getInterfaces()) {
            _writer.writeStartElement("Item");
            _writer.writeStartElement(RASD_URI, "Caption");
            String networkName = iface.getNetworkName() != null ? iface.getNetworkName() : "[No Network]";
            _writer.writeRaw("Ethernet adapter on " + networkName);
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "InstanceId");
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

    protected abstract void writeMacAddress(VmNetworkInterface iface);
}
