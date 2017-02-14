package org.ovirt.engine.core.utils.ovf;

import java.util.Collection;
import java.util.Date;
import java.util.List;

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
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
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

    private OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);

    public OvfWriter(VmBase vmBase, List<DiskImage> images, Version version) {
        _document = new XmlDocument();
        _images = images;
        _writer = new XmlTextWriter();
        this.vmBase = vmBase;
        this.version = version;
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
        _writer.writeAttributeString(OVF_URI, "version", Config.<String>getValue(ConfigValues.VdcVersion));
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
            _writer.writeAttributeString(OVF_URI, "size", String.valueOf(image.getSize()));
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
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":NetworkSection_Type");
        _writer.writeStartElement("Info");
        _writer.writeRaw("List of networks");
        _writer.writeEndElement();
        _writer.writeStartElement("Network");
        _writer.writeAttributeString(OVF_URI, "name", "Network 1");
        _writer.writeEndElement();
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
            _writer.writeAttributeString(OVF_URI, "qcow-compat", image.getQcowCompat().toString());
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
            _writer.writeAttributeString(OVF_URI, "wipe-after-delete",
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
        if (vmBase.getDescription() != null) {
            _writer.writeElement(OvfProperties.DESCRIPTION, vmBase.getDescription());
        }

        if (vmBase.getComment() != null) {
            _writer.writeElement(OvfProperties.COMMENT, vmBase.getComment());
        }

        _writer.writeElement(OvfProperties.CREATION_DATE, OvfParser.localDateToUtcDateString(vmBase.getCreationDate()));
        _writer.writeElement(OvfProperties.EXPORT_DATE, OvfParser.localDateToUtcDateString(new Date()));
        _writer.writeElement(OvfProperties.DELETE_PROTECTED, String.valueOf(vmBase.isDeleteProtected()));

        if (vmBase.getSsoMethod() != null) {
            _writer.writeElement(OvfProperties.SSO_METHOD, vmBase.getSsoMethod().toString());
        }

        _writer.writeElement(OvfProperties.IS_SMARTCARD_ENABLED, String.valueOf(vmBase.isSmartcardEnabled()));

        if (vmBase.getNumOfIoThreads() != 0) {
            _writer.writeElement(OvfProperties.NUM_OF_IOTHREADS, String.valueOf(vmBase.getNumOfIoThreads()));
        }

        _writer.writeElement(OvfProperties.TIMEZONE, vmBase.getTimeZone());
        _writer.writeElement(OvfProperties.DEFAULT_BOOT_SEQUENCE, String.valueOf(vmBase.getDefaultBootSequence().getValue()));

        if (!StringUtils.isBlank(vmBase.getInitrdUrl())) {
            _writer.writeElement(OvfProperties.INITRD_URL, vmBase.getInitrdUrl());
        }
        if (!StringUtils.isBlank(vmBase.getKernelUrl())) {
            _writer.writeElement(OvfProperties.KERNEL_URL, vmBase.getKernelUrl());
        }
        if (!StringUtils.isBlank(vmBase.getKernelParams())) {
            _writer.writeElement(OvfProperties.KERNEL_PARAMS, vmBase.getKernelParams());
        }

        _writer.writeElement(OvfProperties.GENERATION, String.valueOf(vmBase.getDbGeneration()));

        if (vmBase.getCustomCompatibilityVersion() != null) {
            _writer.writeElement(OvfProperties.CUSTOM_COMPATIBILITY_VERSION, String.valueOf(vmBase.getCustomCompatibilityVersion()));
        }

        _writer.writeElement(OvfProperties.CLUSTER_COMPATIBILITY_VERSION, String.valueOf(version));// cluster version the VM/Snapshot originates from
        _writer.writeElement(OvfProperties.VM_TYPE, String.valueOf(vmBase.getVmType().getValue()));

        if (vmBase.getTunnelMigration() != null) {
            _writer.writeElement(OvfProperties.TUNNEL_MIGRATION, String.valueOf(vmBase.getTunnelMigration()));
        }

        if (vmBase.getVncKeyboardLayout() != null) {
            _writer.writeElement(OvfProperties.VNC_KEYBOARD_LAYOUT, vmBase.getVncKeyboardLayout());
        }

        _writer.writeElement(OvfProperties.MIN_ALLOCATED_MEMORY, String.valueOf(vmBase.getMinAllocatedMem()));
        _writer.writeElement(OvfProperties.IS_STATELESS, String.valueOf(vmBase.isStateless()));
        _writer.writeElement(OvfProperties.IS_RUN_AND_PAUSE, String.valueOf(vmBase.isRunAndPause()));
        _writer.writeElement(OvfProperties.AUTO_STARTUP, String.valueOf(vmBase.isAutoStartup()));
        _writer.writeElement(OvfProperties.PRIORITY, String.valueOf(vmBase.getPriority()));

        if (vmBase.getCreatedByUserId() != null) {
            _writer.writeElement(OvfProperties.CREATED_BY_USER_ID, String.valueOf(vmBase.getCreatedByUserId()));
        }

        if (vmBase.getMigrationDowntime() != null) {
            _writer.writeElement(OvfProperties.MIGRATION_DOWNTIME, String.valueOf(vmBase.getMigrationDowntime()));
        }
        writeVmInit();

        if (vmBase.getMigrationSupport() != null) {
            _writer.writeElement(OvfProperties.MIGRATION_SUPPORT, String.valueOf(vmBase.getMigrationSupport().getValue()));
        }

        // TODO dedicated to multiple hosts - are we breaking any standard here?
        if (vmBase.getDedicatedVmForVdsList().size() > 0) {
            for (Guid hostId : vmBase.getDedicatedVmForVdsList()) {
                _writer.writeElement(OvfProperties.DEDICATED_VM_FOR_VDS, String.valueOf(hostId));
            }
        }

        if (vmBase.getSerialNumberPolicy() != null) {
            _writer.writeElement(OvfProperties.SERIAL_NUMBER_POLICY, String.valueOf(vmBase.getSerialNumberPolicy().getValue()));
        }

        if (vmBase.getCustomSerialNumber() != null) {
            _writer.writeElement(OvfProperties.CUSTOM_SERIAL_NUMBER, vmBase.getCustomSerialNumber());
        }

        _writer.writeElement(OvfProperties.IS_BOOT_MENU_ENABLED, String.valueOf(vmBase.isBootMenuEnabled()));
        _writer.writeElement(OvfProperties.IS_SPICE_FILE_TRANSFER_ENABLED, String.valueOf(vmBase.isSpiceFileTransferEnabled()));
        _writer.writeElement(OvfProperties.IS_SPICE_COPY_PASTE_ENABLED, String.valueOf(vmBase.isSpiceCopyPasteEnabled()));
        _writer.writeElement(OvfProperties.ALLOW_CONSOLE_RECONNECT, String.valueOf(vmBase.isAllowConsoleReconnect()));

        if (vmBase.getAutoConverge() != null) {
            _writer.writeElement(OvfProperties.IS_AUTO_CONVERGE, String.valueOf(vmBase.getAutoConverge()));
        }

        if (vmBase.getMigrateCompressed() != null) {
            _writer.writeElement(OvfProperties.IS_MIGRATE_COMPRESSED, String.valueOf(vmBase.getMigrateCompressed()));
        }

        if (vmBase.getMigrationPolicyId() != null) {
            _writer.writeElement(OvfProperties.MIGRATION_POLICY_ID, String.valueOf(vmBase.getMigrationPolicyId()));
        }

        writeCustomEmulatedMachine();
        writeCustomCpuName();

        _writer.writeElement(OvfProperties.PREDEFINED_PROPERTIES, vmBase.getPredefinedProperties());
        _writer.writeElement(OvfProperties.USER_DEFINED_PROPERTIES, vmBase.getUserDefinedProperties());
        _writer.writeElement(OvfProperties.MAX_MEMORY_SIZE_MB, String.valueOf(vmBase.getMaxMemorySizeMb()));

        if (vmBase.getLeaseStorageDomainId() != null) {
            _writer.writeElement(OvfProperties.VM_LEASE, vmBase.getLeaseStorageDomainId().toString());
        }
    }

    protected void writeCustomEmulatedMachine() {
        _writer.writeElement(OvfProperties.CUSTOM_EMULATED_MACHINE, vmBase.getCustomEmulatedMachine());
    }

    protected void writeCustomCpuName() {
        _writer.writeElement(OvfProperties.CUSTOM_CPU_NAME, vmBase.getCustomCpuName());

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

    protected void writeManagedDeviceInfo(VmBase vmBase, XmlTextWriter writer, Guid deviceId) {
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
            _writer.writeStartElement(RASD_URI, "ResourceType");
            _writer.writeRaw(OvfHardware.OTHER);
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "InstanceId");
            _writer.writeRaw(vmDevice.getId().getDeviceId().toString());
            _writer.writeEndElement();
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
                _writer.writeStartElement(RASD_URI, "Caption");
                _writer.writeRaw("Graphical Controller");
                _writer.writeEndElement();
                _writer.writeStartElement(RASD_URI, "InstanceId");
                _writer.writeRaw(vmDevice.getId().getDeviceId().toString());
                _writer.writeEndElement();
                _writer.writeStartElement(RASD_URI, "ResourceType");
                _writer.writeRaw(OvfHardware.Monitor);
                _writer.writeEndElement();
                _writer.writeStartElement(RASD_URI, "VirtualQuantity");
                // we should write number of monitors for each entry for backward compatibility
                _writer.writeRaw(String.valueOf(numOfMonitors));
                _writer.writeEndElement();
                _writer.writeStartElement(RASD_URI, "SinglePciQxl");
                _writer.writeRaw(String.valueOf(vmBase.getSingleQxlPci()));
                _writer.writeEndElement();
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
                _writer.writeStartElement(RASD_URI, "Caption");
                _writer.writeRaw("Graphical Framebuffer");
                _writer.writeEndElement();
                _writer.writeStartElement(RASD_URI, "InstanceId");
                _writer.writeRaw(vmDevice.getId().getDeviceId().toString());
                _writer.writeEndElement();
                _writer.writeStartElement(RASD_URI, "ResourceType");
                _writer.writeRaw(OvfHardware.Graphics);
                _writer.writeEndElement();
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
                _writer.writeStartElement(RASD_URI, "Caption");
                _writer.writeRaw("CDROM");
                _writer.writeEndElement();
                _writer.writeStartElement(RASD_URI, "InstanceId");
                _writer.writeRaw(vmDevice.getId().getDeviceId().toString());
                _writer.writeEndElement();
                _writer.writeStartElement(RASD_URI, "ResourceType");
                _writer.writeRaw(OvfHardware.CD);
                _writer.writeEndElement();
                writeVmDeviceInfo(vmDevice);
                _writer.writeEndElement(); // item
                break; // only one CD is currently supported
            }
        }
    }

    private void writeVmDeviceInfo(VmDevice vmDevice) {
        _writer.writeElement(OvfProperties.VMD_TYPE, String.valueOf(vmDevice.getType().getValue()));
        _writer.writeElement(OvfProperties.VMD_DEVICE, String.valueOf(vmDevice.getDevice()));
        _writer.writeElement(OvfProperties.VMD_ADDRESS, vmDevice.getAddress());
        _writer.writeElement(OvfProperties.VMD_BOOT_ORDER, String.valueOf(vmDevice.getBootOrder()));
        _writer.writeElement(OvfProperties.VMD_IS_PLUGGED, String.valueOf(vmDevice.isPlugged()));
        _writer.writeElement(OvfProperties.VMD_IS_READONLY, String.valueOf(vmDevice.getReadOnly()));
        _writer.writeElement(OvfProperties.VMD_ALIAS, String.valueOf(vmDevice.getAlias()));
        if (vmDevice.getSpecParams() != null && vmDevice.getSpecParams().size() != 0
                && !VmPayload.isPayload(vmDevice.getSpecParams())) {
            _writer.writeStartElement(OvfProperties.VMD_SPEC_PARAMS);
            _writer.writeMap(vmDevice.getSpecParams());
            _writer.writeEndElement();
        }
        if (vmDevice.getCustomProperties() != null && !vmDevice.getCustomProperties().isEmpty()) {
            _writer.writeElement(OvfProperties.VMD_CUSTOM_PROP,
                    DevicePropertiesUtils.getInstance().convertProperties(vmDevice.getCustomProperties()));
        }

        if (vmDevice.getSnapshotId() != null) {
            _writer.writeElement(OvfProperties.VMD_SNAPSHOT_PROP, String.valueOf(vmDevice.getSnapshotId()));
        }
    }

    @Override
    public String getStringRepresentation() {
        return _writer.getStringXML();
    }

    protected String getBackwardCompatibleUsbPolicy(UsbPolicy usbPolicy) {
        if (usbPolicy == null) {
            return UsbPolicy.DISABLED.name();
        }
        return usbPolicy.toString();
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
        _writer.writeStartElement("Info");
        _writer.writeRaw(String.format("%1$s CPU, %2$s Memory", vmBase.getNumOfCpus(), vmBase.getMemSizeMb()));
        _writer.writeEndElement();
    }

    protected void writeSystem() {
        _writer.writeStartElement("System");
        _writer.writeStartElement(VSSD_URI, "VirtualSystemType");
        _writer.writeRaw(String.format("%1$s %2$s", Config.<String>getValue(ConfigValues.OvfVirtualSystemType),
                Config.<String>getValue(ConfigValues.VdcVersion)));
        _writer.writeEndElement();
        _writer.writeEndElement();
    }

    protected void writeCpu() {
        _writer.writeStartElement("Item");
        _writer.writeStartElement(RASD_URI, "Caption");
        _writer.writeRaw(String.format("%1$s virtual cpu", vmBase.getNumOfCpus()));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "Description");
        _writer.writeRaw("Number of virtual CPU");
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "InstanceId");
        _writer.writeRaw(String.valueOf(++_instanceId));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "ResourceType");
        _writer.writeRaw(OvfHardware.CPU);
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "num_of_sockets");
        _writer.writeRaw(String.valueOf(vmBase.getNumOfSockets()));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "cpu_per_socket");
        _writer.writeRaw(String.valueOf(vmBase.getCpuPerSocket()));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "threads_per_cpu");
        _writer.writeRaw(String.valueOf(vmBase.getThreadsPerCpu()));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "max_num_of_vcpus");
        _writer.writeRaw(String.valueOf(maxNumOfVcpus()));
        _writer.writeEndElement();
        _writer.writeEndElement(); // item
    }

    protected abstract Integer maxNumOfVcpus();

    protected void writeMemory() {
        _writer.writeStartElement("Item");
        _writer.writeStartElement(RASD_URI, "Caption");
        _writer.writeRaw(String.format("%1$s MB of memory", vmBase.getMemSizeMb()));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "Description");
        _writer.writeRaw("Memory Size");
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "InstanceId");
        _writer.writeRaw(String.valueOf(++_instanceId));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "ResourceType");
        _writer.writeRaw(OvfHardware.Memory);
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "AllocationUnits");
        _writer.writeRaw("MegaBytes");
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "VirtualQuantity");
        _writer.writeRaw(String.valueOf(vmBase.getMemSizeMb()));
        _writer.writeEndElement();
        _writer.writeEndElement(); // item
    }

    protected void writeDrive() {
        for (DiskImage image : _images) {
            _writer.writeStartElement("Item");
            _writer.writeStartElement(RASD_URI, "Caption");
            _writer.writeRaw(image.getDiskAlias());
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "InstanceId");
            _writer.writeRaw(image.getImageId().toString());
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "ResourceType");
            _writer.writeRaw(OvfHardware.DiskImage);
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "HostResource");
            _writer.writeRaw(OvfParser.createImageFile(image));
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "Parent");
            _writer.writeRaw(image.getParentId().toString());
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "Template");
            _writer.writeRaw(image.getImageTemplateId().toString());
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "ApplicationList");
            _writer.writeRaw(image.getAppList());
            _writer.writeEndElement();
            if (image.getStorageIds() != null && image.getStorageIds().size() > 0) {
                _writer.writeStartElement(RASD_URI, "StorageId");
                _writer.writeRaw(image.getStorageIds().get(0).toString());
                _writer.writeEndElement();
            }
            if (image.getStoragePoolId() != null) {
                _writer.writeStartElement(RASD_URI, "StoragePoolId");
                _writer.writeRaw(image.getStoragePoolId().toString());
                _writer.writeEndElement();
            }
            _writer.writeStartElement(RASD_URI, "CreationDate");
            _writer.writeRaw(OvfParser.localDateToUtcDateString(image.getCreationDate()));
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "LastModified");
            _writer.writeRaw(OvfParser.localDateToUtcDateString(image.getLastModified()));
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "last_modified_date");
            _writer.writeRaw(OvfParser.localDateToUtcDateString(image.getLastModifiedDate()));
            _writer.writeEndElement();
            writeManagedDeviceInfo(vmBase, _writer, image.getId());
            _writer.writeEndElement(); // item
        }
    }

    protected void writeUsb() {
        _writer.writeStartElement("Item");
        _writer.writeStartElement(RASD_URI, "Caption");
        _writer.writeRaw("USB Controller");
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "InstanceId");
        _writer.writeRaw(String.valueOf(++_instanceId));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "ResourceType");
        _writer.writeRaw(OvfHardware.USB);
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "UsbPolicy");
        _writer.writeRaw(getBackwardCompatibleUsbPolicy(vmBase.getUsbPolicy()));
        _writer.writeEndElement();
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
            writeManagedDeviceInfo(vmBase, _writer, iface.getId());
            _writer.writeEndElement(); // item
        }
    }

    protected abstract void writeMacAddress(VmNetworkInterface iface);
}
