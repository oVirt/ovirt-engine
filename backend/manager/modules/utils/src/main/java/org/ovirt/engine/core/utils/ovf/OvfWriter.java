package org.ovirt.engine.core.utils.ovf;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmPayload;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
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
    protected VM _vm;
    protected VmBase vmBase;
    private Version version;
    private int diskCounter;
    /** map disk alias to backward compatible disk alias */
    private Map<String, String> diskAliasesMap;

    public OvfWriter(VmBase vmBase, List<DiskImage> images, Version version) {
        _document = new XmlDocument();
        _images = images;
        _writer = new XmlTextWriter();
        this.vmBase = vmBase;
        this.version = version;
        if (version.compareTo(Version.v3_1) < 0) {
            diskAliasesMap = new HashMap<String, String>();
        }
        writeHeader();
    }

    private void writeHeader() {
        _instanceId = 0;
        _writer.WriteStartDocument(false);

        _writer.SetPrefix(OVF_PREFIX, OVF_URI);
        _writer.SetPrefix(RASD_PREFIX, RASD_URI);
        _writer.SetPrefix(VSSD_PREFIX, VSSD_URI);
        _writer.SetPrefix(XSI_PREFIX, XSI_URI);

        _writer.WriteStartElement(OVF_URI, "Envelope");
        _writer.WriteNamespace(OVF_PREFIX, OVF_URI);
        _writer.WriteNamespace(RASD_PREFIX, RASD_URI);
        _writer.WriteNamespace(VSSD_PREFIX, VSSD_URI);
        _writer.WriteNamespace(XSI_PREFIX, XSI_URI);

        // Setting the OVF version according to ENGINE (in 2.2 , version was set to "0.9")
        _writer.WriteAttributeString(OVF_URI, "version", Config.<String> getValue(ConfigValues.VdcVersion));
    }

    protected long bytesToGigabyte(long bytes) {
        return bytes / 1024 / 1024 / 1024;
    }

    @Override
    public void buildReference() {
        _writer.WriteStartElement("References");
        for (DiskImage image : _images) {
            _writer.WriteStartElement("File");
            _writer.WriteAttributeString(OVF_URI, "href", OvfParser.CreateImageFile(image));
            _writer.WriteAttributeString(OVF_URI, "id", image.getImageId().toString());
            _writer.WriteAttributeString(OVF_URI, "size", String.valueOf(image.getSize()));
            _writer.WriteAttributeString(OVF_URI, "description", StringUtils.defaultString(image.getDescription()));
            _writer.WriteEndElement();

        }
        for (VmNetworkInterface iface : vmBase.getInterfaces()) {
            _writer.WriteStartElement("Nic");
            _writer.WriteAttributeString(OVF_URI, "id", iface.getId().toString());
            _writer.WriteEndElement();
        }
        _writer.WriteEndElement();
    }

    protected void writeVmInit() {
        if (vmBase.getVmInit() != null) {
            VmInit vmInit = vmBase.getVmInit();
            _writer.WriteStartElement("VmInit");
            if (vmInit.getHostname() != null) {
                _writer.WriteAttributeString(OVF_URI, "hostname", vmInit.getHostname());
            }
            if (vmInit.getDomain() != null) {
                _writer.WriteAttributeString(OVF_URI, "domain", vmInit.getDomain());
            }
            if (vmInit.getTimeZone() != null) {
                _writer.WriteAttributeString(OVF_URI, "timeZone", vmInit.getTimeZone());
            }
            if (vmInit.getAuthorizedKeys() != null) {
                _writer.WriteAttributeString(OVF_URI, "authorizedKeys", vmInit.getAuthorizedKeys());
            }
            if (vmInit.getRegenerateKeys() != null) {
                _writer.WriteAttributeString(OVF_URI, "regenerateKeys", vmInit.getRegenerateKeys().toString());
            }
            if (vmInit.getDnsSearch() != null) {
                _writer.WriteAttributeString(OVF_URI, "dnsSearch", vmInit.getDnsSearch());
            }
            if (vmInit.getDnsServers() != null) {
                _writer.WriteAttributeString(OVF_URI, "dnsServers", vmInit.getDnsServers());
            }
            if (vmInit.getNetworks() != null) {
                _writer.WriteAttributeString(OVF_URI, "networks", VmInitUtils.networkListToJson(vmInit.getNetworks()));
            }
            if (vmInit.getWinKey() != null) {
                _writer.WriteAttributeString(OVF_URI, "winKey", vmInit.getWinKey());
            }
            if (vmInit.getRootPassword() != null) {
                _writer.WriteAttributeString(OVF_URI, "rootPassword", vmInit.getRootPassword());
            }
            if (vmInit.getCustomScript() != null) {
                _writer.WriteAttributeString(OVF_URI, "customScript", vmInit.getCustomScript());
            }
            _writer.WriteEndElement();
        }
    }

    @Override
    public void buildNetwork() {
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString(XSI_URI, "type", OVF_PREFIX + ":NetworkSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw("List of networks");
        _writer.WriteEndElement();
        _writer.WriteStartElement("Network");
        _writer.WriteAttributeString(OVF_URI, "name", "Network 1");
        _writer.WriteEndElement();
        _writer.WriteEndElement();
    }

    @Override
    public void buildDisk() {
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString(XSI_URI, "type", OVF_PREFIX + ":DiskSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw("List of Virtual Disks");
        _writer.WriteEndElement();
        for (DiskImage image : _images) {
            _writer.WriteStartElement("Disk");
            _writer.WriteAttributeString(OVF_URI, "diskId", image.getImageId().toString());
            _writer.WriteAttributeString(OVF_URI, "size", String.valueOf(bytesToGigabyte(image.getSize())));
            _writer.WriteAttributeString(OVF_URI,
                    "actual_size",
                    String.valueOf(bytesToGigabyte(image.getActualSizeInBytes())));
            _writer.WriteAttributeString(OVF_URI, "vm_snapshot_id", (image.getVmSnapshotId() != null) ? image
                    .getVmSnapshotId().toString() : "");

            if (image.getParentId().equals(Guid.Empty)) {
                _writer.WriteAttributeString(OVF_URI, "parentRef", "");
            } else {
                int i = 0;
                while (_images.get(i).getImageId().equals(image.getParentId()))
                    i++;
                List<DiskImage> res = _images.subList(i, _images.size() - 1);

                if (res.size() > 0) {
                    _writer.WriteAttributeString(OVF_URI, "parentRef", OvfParser.CreateImageFile(res.get(0)));
                } else {
                    _writer.WriteAttributeString(OVF_URI, "parentRef", "");
                }
            }

            _writer.WriteAttributeString(OVF_URI, "fileRef", OvfParser.CreateImageFile(image));

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
            _writer.WriteAttributeString(OVF_URI, "format", format);
            _writer.WriteAttributeString(OVF_URI, "volume-format", image.getVolumeFormat().toString());
            _writer.WriteAttributeString(OVF_URI, "volume-type", image.getVolumeType().toString());
            _writer.WriteAttributeString(OVF_URI, "disk-interface", image.getDiskInterface().toString());
            _writer.WriteAttributeString(OVF_URI, "boot", String.valueOf(image.isBoot()));
            if (image.getDiskAlias() != null) {
                _writer.WriteAttributeString(OVF_URI, "disk-alias", image.getDiskAlias());
            }
            if (image.getDiskDescription() != null) {
                _writer.WriteAttributeString(OVF_URI, "disk-description", image.getDiskDescription());
            }
            _writer.WriteAttributeString(OVF_URI, "wipe-after-delete",
                    String.valueOf(image.isWipeAfterDelete()));
            _writer.WriteEndElement();
        }
        _writer.WriteEndElement();
    }

    @Override
    public void buildVirtualSystem() {
        // General Vm
        _writer.WriteStartElement("Content");
        _writer.WriteAttributeString(OVF_URI, "id", "out");
        _writer.WriteAttributeString(XSI_URI, "type", OVF_PREFIX + ":VirtualSystem_Type");

        // General Data
        writeGeneralData();

        // Application List
        writeAppList();

        // Content Items
        writeContentItems();

        _writer.WriteEndElement(); // End Content tag
    }

    protected void writeGeneralData() {
        if (vmBase.getDescription() != null) {
            _writer.WriteStartElement(OvfProperties.DESCRIPTION);
            _writer.WriteRaw(vmBase.getDescription());
            _writer.WriteEndElement();
        }

        if (vmBase.getComment() != null) {
            _writer.WriteStartElement(OvfProperties.COMMENT);
            _writer.WriteRaw(vmBase.getComment());
            _writer.WriteEndElement();
        }

        if (!vmInitEnabled() && vmBase.getVmInit() != null && vmBase.getVmInit().getDomain() != null) {
            _writer.WriteStartElement(OvfProperties.DOMAIN);
            _writer.WriteRaw(vmBase.getVmInit().getDomain());
            _writer.WriteEndElement();
        }

        _writer.WriteStartElement(OvfProperties.CREATION_DATE);
        _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(vmBase.getCreationDate()));
        _writer.WriteEndElement();

        _writer.WriteStartElement(OvfProperties.EXPORT_DATE);
        _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(new Date()));
        _writer.WriteEndElement();

        _writer.WriteStartElement(OvfProperties.DELETE_PROTECTED);
        _writer.WriteRaw(String.valueOf(vmBase.isDeleteProtected()));
        _writer.WriteEndElement();

        if (vmBase.getSsoMethod() != null) {
            _writer.WriteStartElement(OvfProperties.SSO_METHOD);
            _writer.WriteRaw(vmBase.getSsoMethod().toString());
            _writer.WriteEndElement();
        }

        _writer.WriteStartElement(OvfProperties.IS_SMARTCARD_ENABLED);
        _writer.WriteRaw(String.valueOf(vmBase.isSmartcardEnabled()));
        _writer.WriteEndElement();

        _writer.WriteStartElement(OvfProperties.TIMEZONE);
        _writer.WriteRaw(vmBase.getTimeZone());
        _writer.WriteEndElement();

        _writer.WriteStartElement(OvfProperties.DEFAULT_BOOT_SEQUENCE);
        _writer.WriteRaw(String.valueOf(vmBase.getDefaultBootSequence().getValue()));
        _writer.WriteEndElement();

        if (!StringUtils.isBlank(vmBase.getInitrdUrl())) {
            _writer.WriteStartElement(OvfProperties.INITRD_URL);
            _writer.WriteRaw(vmBase.getInitrdUrl());
            _writer.WriteEndElement();
        }
        if (!StringUtils.isBlank(vmBase.getKernelUrl())) {
            _writer.WriteStartElement(OvfProperties.KERNEL_URL);
            _writer.WriteRaw(vmBase.getKernelUrl());
            _writer.WriteEndElement();
        }
        if (!StringUtils.isBlank(vmBase.getKernelParams())) {
            _writer.WriteStartElement(OvfProperties.KERNEL_PARAMS);
            _writer.WriteRaw(vmBase.getKernelParams());
            _writer.WriteEndElement();
        }

        _writer.WriteStartElement(OvfProperties.GENERATION);
        _writer.WriteRaw(String.valueOf(vmBase.getDbGeneration()));
        _writer.WriteEndElement();

        _writer.WriteStartElement(OvfProperties.VM_TYPE);
        _writer.WriteRaw(String.valueOf(vmBase.getVmType().getValue()));
        _writer.WriteEndElement();

        if (vmBase.getTunnelMigration() != null) {
            _writer.WriteStartElement(OvfProperties.TUNNEL_MIGRATION);
            _writer.WriteRaw(String.valueOf(vmBase.getTunnelMigration()));
            _writer.WriteEndElement();
        }

        if (vmBase.getVncKeyboardLayout() != null) {
            _writer.WriteStartElement(OvfProperties.VNC_KEYBOARD_LAYOUT);
            _writer.WriteRaw(vmBase.getVncKeyboardLayout());
            _writer.WriteEndElement();
        }

        _writer.WriteStartElement(OvfProperties.MIN_ALLOCATED_MEMORY);
        _writer.WriteRaw(String.valueOf(vmBase.getMinAllocatedMem()));
        _writer.WriteEndElement();

        _writer.WriteStartElement(OvfProperties.IS_STATELESS);
        _writer.WriteRaw(String.valueOf(vmBase.isStateless()));
        _writer.WriteEndElement();

        _writer.WriteStartElement(OvfProperties.IS_RUN_AND_PAUSE);
        _writer.WriteRaw(String.valueOf(vmBase.isRunAndPause()));
        _writer.WriteEndElement();

        _writer.WriteStartElement(OvfProperties.AUTO_STARTUP);
        _writer.WriteRaw(String.valueOf(vmBase.isAutoStartup()));
        _writer.WriteEndElement();

        _writer.WriteStartElement(OvfProperties.PRIORITY);
        _writer.WriteRaw(String.valueOf(vmBase.getPriority()));
        _writer.WriteEndElement();

        if (vmBase.getCreatedByUserId() != null) {
            _writer.WriteStartElement(OvfProperties.CREATED_BY_USER_ID);
            _writer.WriteRaw(String.valueOf(vmBase.getCreatedByUserId()));
            _writer.WriteEndElement();
        }

        if (vmBase.getMigrationDowntime() != null) {
            _writer.WriteStartElement(OvfProperties.MIGRATION_DOWNTIME);
            _writer.WriteRaw(String.valueOf(vmBase.getMigrationDowntime()));
            _writer.WriteEndElement();
        }
        writeVmInit();

        if (vmBase.getMigrationSupport() != null) {
            _writer.WriteStartElement(OvfProperties.MIGRATION_SUPPORT);
            _writer.WriteRaw(String.valueOf(vmBase.getMigrationSupport().getValue()));
            _writer.WriteEndElement();
        }

        if (vmBase.getDedicatedVmForVds() != null) {
            _writer.WriteStartElement(OvfProperties.DEDICATED_VM_FOR_VDS);
            _writer.WriteRaw(String.valueOf(vmBase.getDedicatedVmForVds()));
            _writer.WriteEndElement();
        }

        if (vmBase.getSerialNumberPolicy() != null) {
            _writer.WriteStartElement(OvfProperties.SERIAL_NUMBER_POLICY);
            _writer.WriteRaw(String.valueOf(vmBase.getSerialNumberPolicy().getValue()));
            _writer.WriteEndElement();
        }

        if (vmBase.getCustomSerialNumber() != null) {
            _writer.WriteStartElement(OvfProperties.CUSTOM_SERIAL_NUMBER);
            _writer.WriteRaw(vmBase.getCustomSerialNumber());
            _writer.WriteEndElement();
        }

        _writer.WriteStartElement(OvfProperties.IS_BOOT_MENU_ENABLED);
        _writer.WriteRaw(String.valueOf(vmBase.isBootMenuEnabled()));
        _writer.WriteEndElement();

        _writer.WriteStartElement(OvfProperties.IS_SPICE_FILE_TRANSFER_ENABLED);
        _writer.WriteRaw(String.valueOf(vmBase.isSpiceFileTransferEnabled()));
        _writer.WriteEndElement();

        _writer.WriteStartElement(OvfProperties.IS_SPICE_COPY_PASTE_ENABLED);
        _writer.WriteRaw(String.valueOf(vmBase.isSpiceCopyPasteEnabled()));
        _writer.WriteEndElement();

        if (vmBase.getAutoConverge() != null) {
            _writer.WriteStartElement(OvfProperties.IS_AUTO_CONVERGE);
            _writer.WriteRaw(String.valueOf(vmBase.getAutoConverge()));
            _writer.WriteEndElement();
        }

        if (vmBase.getMigrateCompressed() != null) {
            _writer.WriteStartElement(OvfProperties.IS_MIGRATE_COMPRESSED);
            _writer.WriteRaw(String.valueOf(vmBase.getMigrateCompressed()));
            _writer.WriteEndElement();
        }

        _writer.WriteStartElement(OvfProperties.CUSTOM_EMULATED_MACHINE);
        _writer.WriteRaw(vmBase.getCustomEmulatedMachine());
        _writer.WriteEndElement();

        _writer.WriteStartElement(OvfProperties.CUSTOM_CPU_NAME);
        _writer.WriteRaw(vmBase.getCustomCpuName());
        _writer.WriteEndElement();
    }

    protected abstract void writeAppList();

    protected abstract void writeContentItems();

    protected void writeManagedDeviceInfo(VmBase vmBase, XmlTextWriter writer, Guid deviceId) {
        VmDevice vmDevice = vmBase.getManagedDeviceMap().get(deviceId);
        if (deviceId != null && vmDevice != null && vmDevice.getAddress() != null) {
            writeVmDeviceInfo(vmDevice);
        }
    }

    protected void writeOtherDevices(VmBase vmBase, XmlTextWriter write) {
        List<VmDevice> devices = vmBase.getUnmanagedDeviceList();

        Collection<VmDevice> managedDevices = vmBase.getManagedDeviceMap().values();
        for (VmDevice device : managedDevices) {
            if (VmDeviceCommonUtils.isSpecialDevice(device.getDevice(), device.getType())) {
                devices.add(device);
            }
        }

        for (VmDevice vmDevice : devices) {
            _writer.WriteStartElement("Item");
            _writer.WriteStartElement(RASD_URI, "ResourceType");
            _writer.WriteRaw(OvfHardware.OTHER);
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "InstanceId");
            _writer.WriteRaw(vmDevice.getId().getDeviceId().toString());
            _writer.WriteEndElement();
            writeVmDeviceInfo(vmDevice);
            _writer.WriteEndElement(); // item
        }
    }

    protected void writeMonitors(VmBase vmBase) {
        Collection<VmDevice> devices = vmBase.getManagedDeviceMap().values();
        int numOfMonitors = vmBase.getNumOfMonitors();
        int i = 0;
        for (VmDevice vmDevice : devices) {
            if (vmDevice.getType() == VmDeviceGeneralType.VIDEO) {
                _writer.WriteStartElement("Item");
                _writer.WriteStartElement(RASD_URI, "Caption");
                _writer.WriteRaw("Graphical Controller");
                _writer.WriteEndElement();
                _writer.WriteStartElement(RASD_URI, "InstanceId");
                _writer.WriteRaw(vmDevice.getId().getDeviceId().toString());
                _writer.WriteEndElement();
                _writer.WriteStartElement(RASD_URI, "ResourceType");
                _writer.WriteRaw(OvfHardware.Monitor);
                _writer.WriteEndElement();
                _writer.WriteStartElement(RASD_URI, "VirtualQuantity");
                // we should write number of monitors for each entry for backward compatibility
                _writer.WriteRaw(String.valueOf(numOfMonitors));
                _writer.WriteEndElement();
                _writer.WriteStartElement(RASD_URI, "SinglePciQxl");
                _writer.WriteRaw(String.valueOf(vmBase.getSingleQxlPci()));
                _writer.WriteEndElement();
                writeVmDeviceInfo(vmDevice);
                _writer.WriteEndElement(); // item
                if (i++ == numOfMonitors) {
                    break;
                }
            }
        }
    }

    protected void writeGraphics(VmBase vmBase) {
        Collection<VmDevice> devices = vmBase.getManagedDeviceMap().values();
        for (VmDevice vmDevice : devices) {
            if (vmDevice.getType() == VmDeviceGeneralType.GRAPHICS) {
                _writer.WriteStartElement("Item");
                _writer.WriteStartElement(RASD_URI, "Caption");
                _writer.WriteRaw("Graphical Framebuffer");
                _writer.WriteEndElement();
                _writer.WriteStartElement(RASD_URI, "InstanceId");
                _writer.WriteRaw(vmDevice.getId().getDeviceId().toString());
                _writer.WriteEndElement();
                _writer.WriteStartElement(RASD_URI, "ResourceType");
                _writer.WriteRaw(OvfHardware.Graphics);
                _writer.WriteEndElement();
                writeVmDeviceInfo(vmDevice);
                _writer.WriteEndElement(); // item
            }
        }
    }

    protected void writeCd(VmBase vmBase) {
        Collection<VmDevice> devices = vmBase.getManagedDeviceMap().values();
        for (VmDevice vmDevice : devices) {
            if (vmDevice.getDevice().equals(VmDeviceType.CDROM.getName())) {
                _writer.WriteStartElement("Item");
                _writer.WriteStartElement(RASD_URI, "Caption");
                _writer.WriteRaw("CDROM");
                _writer.WriteEndElement();
                _writer.WriteStartElement(RASD_URI, "InstanceId");
                _writer.WriteRaw(vmDevice.getId().getDeviceId().toString());
                _writer.WriteEndElement();
                _writer.WriteStartElement(RASD_URI, "ResourceType");
                _writer.WriteRaw(OvfHardware.CD);
                _writer.WriteEndElement();
                writeVmDeviceInfo(vmDevice);
                _writer.WriteEndElement(); // item
                break; // only one CD is currently supported
            }
        }
    }

    private void writeVmDeviceInfo(VmDevice vmDevice) {
        _writer.WriteStartElement(OvfProperties.VMD_TYPE);
        _writer.WriteRaw(String.valueOf(vmDevice.getType().getValue()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.VMD_DEVICE);
        _writer.WriteRaw(String.valueOf(vmDevice.getDevice()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.VMD_ADDRESS);
        _writer.WriteRaw(vmDevice.getAddress());
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.VMD_BOOT_ORDER);
        _writer.WriteRaw(String.valueOf(vmDevice.getBootOrder()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.VMD_IS_PLUGGED);
        _writer.WriteRaw(String.valueOf(vmDevice.getIsPlugged()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.VMD_IS_READONLY);
        _writer.WriteRaw(String.valueOf(vmDevice.getIsReadOnly()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.VMD_ALIAS);
        _writer.WriteRaw(String.valueOf(vmDevice.getAlias()));
        _writer.WriteEndElement();
        if (vmDevice.getSpecParams() != null && vmDevice.getSpecParams().size() != 0
                && !VmPayload.isPayload(vmDevice.getSpecParams())) {
            _writer.WriteStartElement(OvfProperties.VMD_SPEC_PARAMS);
            _writer.WriteMap(vmDevice.getSpecParams());
            _writer.WriteEndElement();
        }
        if (vmDevice.getCustomProperties() != null && !vmDevice.getCustomProperties().isEmpty()) {
            _writer.WriteStartElement(OvfProperties.VMD_CUSTOM_PROP);
            _writer.WriteRaw(DevicePropertiesUtils.getInstance().convertProperties(vmDevice.getCustomProperties()));
            _writer.WriteEndElement();
        }

        if (vmDevice.getSnapshotId() != null) {
            _writer.WriteStartElement(OvfProperties.VMD_SNAPSHOT_PROP);
            _writer.WriteRaw(String.valueOf(vmDevice.getSnapshotId()));
            _writer.WriteEndElement();
        }
    }

    @Override
    public String getStringRepresentation() {
        return _writer.getStringXML();
    }

    protected String getBackwardCompatibleUsbPolicy(UsbPolicy usbPolicy) {
        if (usbPolicy == null) {
            return version.compareTo(Version.v3_1) < 0 ? UsbPolicy.PRE_3_1_DISABLED : UsbPolicy.DISABLED.name();
        }

        if (version.compareTo(Version.v3_1) < 0) {
            switch (usbPolicy) {
            case ENABLED_LEGACY:
                return UsbPolicy.PRE_3_1_ENABLED;
            default:
                return UsbPolicy.PRE_3_1_DISABLED;
            }
        }
        else {
            return usbPolicy.toString();
        }
    }

    protected boolean vmInitEnabled() {
        return version.compareTo(Version.v3_4) < 0 ? false : true;
    }

    protected String getBackwardCompatibleDiskAlias(String diskAlias) {
        if (version.compareTo(Version.v3_1) >= 0) {
            return diskAlias;
        }

        String newDiskAlias = diskAliasesMap.get(diskAlias);
        if (newDiskAlias == null) {
            newDiskAlias = "Drive " + (++diskCounter);
            diskAliasesMap.put(diskAlias, newDiskAlias);
        }
        return newDiskAlias;
    }
}
