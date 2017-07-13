package org.ovirt.engine.core.utils.ovf;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.GraphicsDevice;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.SsoMethod;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.VmInitUtils;
import org.ovirt.engine.core.utils.customprop.DevicePropertiesUtils;
import org.ovirt.engine.core.utils.ovf.xml.XmlAttribute;
import org.ovirt.engine.core.utils.ovf.xml.XmlAttributeCollection;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNamespaceManager;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class OvfReader implements IOvfBuilder {
    private static final Logger log = LoggerFactory.getLogger(OvfReader.class);

    protected OsRepository osRepository;
    protected List<DiskImage> _images;
    protected List<VmNetworkInterface> interfaces;
    protected XmlDocument _document;
    protected XmlNamespaceManager _xmlNS;
    private static final int BYTES_IN_GB = 1024 * 1024 * 1024;
    public static final String EmptyName = "[Empty Name]";
    protected String name = EmptyName;
    private String version;
    private final VmBase vmBase;
    private String lastReadEntry = "";
    private Map<String, XmlAttributeCollection> fileIdToFileAttributes;

    public OvfReader(
            XmlDocument document,
            List<DiskImage> images,
            List<VmNetworkInterface> interfaces,
            VmBase vmBase,
            OsRepository osRepository) {
        _images = images;
        this.interfaces = interfaces;
        _document = document;
        this.vmBase = vmBase;
        this.osRepository = osRepository;

        fileIdToFileAttributes = new HashMap<>();
        _xmlNS = new XmlNamespaceManager();
        _xmlNS.addNamespace("ovf", OVF_URI);
        _xmlNS.addNamespace("rasd", RASD_URI);
        _xmlNS.addNamespace("vssd", VSSD_URI);
        _xmlNS.addNamespace("xsi", XSI_URI);
        readHeader();

    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    /**
     * reads the OVF header
     */
    private void readHeader() {
        XmlNode node = selectSingleNode(_document, "//ovf:Envelope", _xmlNS);
        if (node == null) {
            node = selectSingleNode(_document, "//Envelope", _xmlNS);
        }
        version = node != null ? node.attributes.get("ovf:version").getValue() : "";
    }

    @Override
    public void buildReference() {
        buildFileReference();
        buildNicReference();
    }

    @Override
    public void buildNetwork() {
        // No implementation - networks aren't read from the OVF.
    }

    protected long convertGigabyteToBytes(long gb) {
        return gb * BYTES_IN_GB;
    }

    @Override
    public void buildDisk() {
        XmlNode diskSection = selectSingleNode(_document, "//*/DiskSection");
        XmlNodeList list =
                diskSection != null ? diskSection.selectNodes("Disk") : selectNodes(_document, "//*/Section/Disk");
        for (XmlNode node : list) {
            String diskId = node.attributes.get("ovf:diskId").getValue();
            // oVirt used the diskId as the file id
            XmlAttributeCollection fileAttributes = fileIdToFileAttributes.get(diskId);
            // Accroding to the OVF specification the fileRef should match the file id instead
            if (fileAttributes == null) {
                fileAttributes = fileIdToFileAttributes.get(node.attributes.get("ovf:fileRef").getValue());
            }
            if (fileAttributes == null) {
                // TODO: the OVF specification defines empty disks as ones that do not have File
                // elements in the References section. We don't support it yet.
                continue;
            }

            // If the disk storage type is Cinder then override the disk image with Cinder object,
            // otherwise use the disk image.
            DiskImage image = new DiskImage();

            XmlAttribute diskStorageType = fileAttributes.get("ovf:disk_storage_type");
            if (diskStorageType == null) {
                diskStorageType = node.attributes.get("ovf:disk_storage_type");
            }
            // If the OVF is old and does not contain any storage type reference then we assume we can only have disk
            // image.
            if (diskStorageType != null) {
                if (DiskStorageType.CINDER.name().equals(diskStorageType.getValue())) {
                    image = new CinderDisk();
                    XmlAttribute cinderVolumeType = fileAttributes.get("ovf:cinder_volume_type");
                    if (cinderVolumeType == null) {
                        cinderVolumeType = node.attributes.get("ovf:cinder_volume_type");
                    }
                    if (cinderVolumeType != null) {
                        image.setCinderVolumeType(cinderVolumeType.getValue());
                    }
                }
            }
            try {
                image.setImageId(new Guid(diskId));
            } catch (IllegalArgumentException ex) {
                log.warn("could not retrieve volume id of {} from ovf, generating new guid", diskId);
                image.setImageId(Guid.newGuid());
            }
            try {
                image.setId(OvfParser.getImageGroupIdFromImageFile(fileAttributes.get("ovf:href").getValue()));
            } catch (IllegalArgumentException ex) {
                log.warn("could not retrieve disk id of {} from ovf, generating new guid", diskId);
                image.setId(Guid.newGuid());
            }
            // Default values:
            image.setActive(true);
            image.setImageStatus(ImageStatus.OK);
            XmlAttribute description = fileAttributes.get("ovf:description");
            if (description == null) {
                description = node.attributes.get("ovf:description");
            }
            image.setDescription(description != null ? description.getValue() : diskId);

            image.setDiskVmElements(Collections.singletonList(new DiskVmElement(image.getId(), vmBase.getId())));

            DiskVmElement dve = image.getDiskVmElementForVm(vmBase.getId());

            if (node.attributes.get("ovf:vm_snapshot_id") != null) {
                image.setVmSnapshotId(new Guid(node.attributes.get("ovf:vm_snapshot_id").getValue()));
            }

            XmlAttribute virtualSize = node.attributes.get("ovf:size");
            if (virtualSize == null) {
                virtualSize = node.attributes.get("ovf:capacity");
                // TODO take ovf:capacityAllocationUnits into account
            }
            if (!StringUtils.isEmpty(virtualSize.getValue())) {
                image.setSize(convertGigabyteToBytes(Long.parseLong(virtualSize.getValue())));
            }

            XmlAttribute actualSize = node.attributes.get("ovf:actual_size");
            if (actualSize == null) {
                actualSize = fileAttributes.get("ovf:size");
                // TODO populatedSize in case of compression
                image.setActualSizeInBytes(Long.parseLong(actualSize.getValue()));
            } else {
                if (!StringUtils.isEmpty(actualSize.getValue())) {
                    image.setActualSizeInBytes(convertGigabyteToBytes(Long.parseLong(actualSize.getValue())));
                }
            }

            if (node.attributes.get("ovf:volume-format") != null) {
                if (!StringUtils.isEmpty(node.attributes.get("ovf:volume-format").getValue())) {
                    image.setVolumeFormat(VolumeFormat.valueOf(node.attributes.get("ovf:volume-format").getValue()));
                } else {
                    image.setVolumeFormat(VolumeFormat.Unassigned);
                }
            } else {
                image.setVolumeFormat(VolumeFormat.Unassigned);
            }

            if (node.attributes.get("ovf:volume-type") != null) {
                if (!StringUtils.isEmpty(node.attributes.get("ovf:volume-type").getValue())) {
                    image.setVolumeType(VolumeType.valueOf(node.attributes.get("ovf:volume-type").getValue()));
                } else {
                    image.setVolumeType(VolumeType.Unassigned);
                }
            } else {
                image.setVolumeType(VolumeType.Unassigned);
            }
            if (node.attributes.get("ovf:disk-interface") != null) {
                if (!StringUtils.isEmpty(node.attributes.get("ovf:disk-interface").getValue())) {
                    dve.setDiskInterface(DiskInterface.valueOf(node.attributes.get("ovf:disk-interface").getValue()));
                }
            } else {
                dve.setDiskInterface(DiskInterface.IDE);
            }
            if (node.attributes.get("ovf:boot") != null) {
                if (!StringUtils.isEmpty(node.attributes.get("ovf:boot").getValue())) {
                    dve.setBoot(Boolean.parseBoolean(node.attributes.get("ovf:boot").getValue()));
                }
            }
            if (node.attributes.get("ovf:pass-discard") != null) {
                if (!StringUtils.isEmpty(node.attributes.get("ovf:pass-discard").getValue())) {
                    dve.setPassDiscard(Boolean.parseBoolean(node.attributes.get("ovf:pass-discard").getValue()));
                }
            }
            if (node.attributes.get("ovf:wipe-after-delete") != null) {
                if (!StringUtils.isEmpty(node.attributes.get("ovf:wipe-after-delete").getValue())) {
                    image.setWipeAfterDelete(Boolean.parseBoolean(node.attributes.get("ovf:wipe-after-delete")
                            .getValue()));
                }
            }
            if (node.attributes.get("ovf:disk-alias") != null) {
                if (!StringUtils.isEmpty(node.attributes.get("ovf:disk-alias").getValue())) {
                    image.setDiskAlias(String.valueOf(node.attributes.get("ovf:disk-alias")
                            .getValue()));
                }
            }
            if (node.attributes.get("ovf:disk-description") != null) {
                if (!StringUtils.isEmpty(node.attributes.get("ovf:disk-description").getValue())) {
                    image.setDiskDescription(String.valueOf(node.attributes.get("ovf:disk-description")
                            .getValue()));
                }
            }

            _images.add(image);
        }
    }

    @Override
    public void buildVirtualSystem() {
        readGeneralData();
    }

    protected VmDevice readManagedVmDevice(XmlNode node, Guid deviceId) {
        return addManagedVmDevice(readVmDevice(node, deviceId));
    }

    private VmDevice addManagedVmDevice(VmDevice vmDevice) {
        vmDevice.setManaged(true);
        vmBase.getManagedDeviceMap().put(vmDevice.getDeviceId(), vmDevice);
        return vmDevice;
    }

    protected VmDevice readUnmanagedVmDevice(XmlNode node, Guid deviceId) {
        VmDevice vmDevice = readVmDevice(node, deviceId);
        vmBase.getUnmanagedDeviceList().add(vmDevice);
        return vmDevice;
    }

    /**
     * Reads vm device attributes from OVF and stores it in the collection
     */
    private VmDevice readVmDevice(XmlNode node, Guid deviceId) {
        VmDevice vmDevice = new VmDevice();
        vmDevice.setId(new VmDeviceId(deviceId, vmBase.getId()));
        if (selectSingleNode(node, VMD_ADDRESS, _xmlNS) != null
                && !StringUtils.isEmpty(selectSingleNode(node, VMD_ADDRESS, _xmlNS).innerText)) {
            vmDevice.setAddress(String.valueOf(selectSingleNode(node, VMD_ADDRESS, _xmlNS).innerText));
        } else {
            vmDevice.setAddress("");
        }
        if (selectSingleNode(node, VMD_ALIAS, _xmlNS) != null
                && !StringUtils.isEmpty(selectSingleNode(node, VMD_ALIAS, _xmlNS).innerText)) {
            vmDevice.setAlias(String.valueOf(selectSingleNode(node, VMD_ALIAS, _xmlNS).innerText));
        } else {
            vmDevice.setAlias("");
        }
        XmlNode specParamsNode = selectSingleNode(node, VMD_SPEC_PARAMS, _xmlNS);
        if (specParamsNode != null
                && !StringUtils.isEmpty(specParamsNode.innerText)) {
            vmDevice.setSpecParams(getMapNode(specParamsNode));
        } else {
            // Empty map
            vmDevice.setSpecParams(Collections.emptyMap());
        }
        if (selectSingleNode(node, VMD_TYPE, _xmlNS) != null
                && !StringUtils.isEmpty(selectSingleNode(node, VMD_TYPE, _xmlNS).innerText)) {
            vmDevice.setType(
                    VmDeviceGeneralType.forValue(String.valueOf(selectSingleNode(node, VMD_TYPE, _xmlNS).innerText)));
        } else {
            int resourceType = getResourceType(node, VMD_RESOURCE_TYPE);
            vmDevice.setType(VmDeviceGeneralType.forValue(VmDeviceType.getoVirtDevice(resourceType)));
        }
        if (selectSingleNode(node, VMD_DEVICE, _xmlNS) != null
                && !StringUtils.isEmpty(selectSingleNode(node, VMD_DEVICE, _xmlNS).innerText)) {
            vmDevice.setDevice(String.valueOf(selectSingleNode(node, VMD_DEVICE, _xmlNS).innerText));
        } else {
            setDeviceByResource(node, vmDevice);
        }
        if (selectSingleNode(node, VMD_IS_PLUGGED, _xmlNS) != null
                && !StringUtils.isEmpty(selectSingleNode(node, VMD_IS_PLUGGED, _xmlNS).innerText)) {
            vmDevice.setPlugged(Boolean.valueOf(selectSingleNode(node, VMD_IS_PLUGGED, _xmlNS).innerText));
        } else {
            vmDevice.setPlugged(Boolean.TRUE);
        }
        if (selectSingleNode(node, VMD_IS_READONLY, _xmlNS) != null
                && !StringUtils.isEmpty(selectSingleNode(node, VMD_IS_READONLY, _xmlNS).innerText)) {
            vmDevice.setReadOnly(Boolean.valueOf(selectSingleNode(node, VMD_IS_READONLY, _xmlNS).innerText));
        } else {
            vmDevice.setReadOnly(Boolean.FALSE);
        }
        if (selectSingleNode(node, VMD_CUSTOM_PROP, _xmlNS) != null
                && StringUtils.isNotEmpty(selectSingleNode(node, VMD_CUSTOM_PROP, _xmlNS).innerText)) {
            vmDevice.setCustomProperties(DevicePropertiesUtils.getInstance().convertProperties(
                    String.valueOf(selectSingleNode(node, VMD_CUSTOM_PROP, _xmlNS).innerText)));
        } else {
            vmDevice.setCustomProperties(null);
        }

        if (selectSingleNode(node, VMD_SNAPSHOT_PROP, _xmlNS) != null
                && StringUtils.isNotEmpty(selectSingleNode(node, VMD_SNAPSHOT_PROP, _xmlNS).innerText)) {
            vmDevice.setSnapshotId(new Guid(String.valueOf(selectSingleNode(node, VMD_CUSTOM_PROP, _xmlNS).innerText)));
        }

        return vmDevice;
    }

    /**
     * gets the VM interface
     *
     * @param node
     *            the xml node
     * @return VmNetworkInterface
     */
    public VmNetworkInterface getNetworkInterface(XmlNode node) {
        // prior to 3.0 the instanceId is int , in 3.1 and on this is Guid
        String str = selectSingleNode(node, "rasd:InstanceId", _xmlNS).innerText;
        if (!StringUtils.isNumeric(str)) { // 3.1 and above OVF format
            final Guid guid = new Guid(str);
            VmNetworkInterface iface = interfaces.stream().filter(i -> i.getId().equals(guid)).findFirst().orElse(null);
            if (iface == null) {
                iface = new VmNetworkInterface();
                iface.setId(guid);
            }
            return iface;
        } else { // 3.0 and below OVF format
            return new VmNetworkInterface();
        }
    }

    /**
     * This method should return the string representation of 'default display type' property in the ovf file. this
     * representation is different for ovf file of VM and ovf file of template.
     *
     * @return the String representation of 'default display type' property in the ovf file
     */
    protected abstract String getDefaultDisplayTypeStringRepresentation();

    protected abstract void readOsSection(XmlNode section);

    protected void readHardwareSection(XmlNode section) {
        boolean readVirtioSerial = false;

        for (XmlNode node : selectNodes(section, "Item")) {
            switch (selectSingleNode(node, "rasd:ResourceType", _xmlNS).innerText) {
            case OvfHardware.CPU:
                readCpuItem(node);
                break;

            case OvfHardware.Memory:
                readMemoryItem(node);
                break;

            case OvfHardware.DiskImage:
                readDiskImageItem(node);
                break;

            case OvfHardware.Network:
                readNetworkItem(node);
                break;

            case OvfHardware.USB:
                readUsbItem(node);
                break;

            case OvfHardware.Monitor:
                readMonitorItem(node);
                break;

            case OvfHardware.Graphics:
                readManagedVmDevice(node, Guid.newGuid()); // so far graphics doesn't contain anything special
                break;

            case OvfHardware.CD:
                readCdItem(node);
                break;

            case OvfHardware.OTHER:
                VmDevice vmDevice = readOtherHardwareItem(node);
                readVirtioSerial = readVirtioSerial ||
                        VmDeviceType.VIRTIOSERIAL.getName().equals(vmDevice.getDevice());
                break;
            }
        }

        if (!readVirtioSerial) {
            addManagedVmDevice(VmDeviceCommonUtils.createVirtioSerialDeviceForVm(vmBase.getId()));
        }
    }

    protected abstract void readDiskImageItem(XmlNode node);

    protected void readMonitorItem(XmlNode node) {
        vmBase.setNumOfMonitors(
                Integer.parseInt(selectSingleNode(node, "rasd:VirtualQuantity", _xmlNS).innerText));
        if (selectSingleNode(node, "rasd:SinglePciQxl", _xmlNS) != null) {
            vmBase.setSingleQxlPci(Boolean.parseBoolean(selectSingleNode(node, "rasd:SinglePciQxl", _xmlNS).innerText));
        }

        readManagedVmDevice(node, Guid.newGuid());
    }

    protected Integer parseNodeInteger(XmlNode sourceNode, String string, Integer defaultValue) {
        XmlNode subNode = selectSingleNode(sourceNode, string, _xmlNS);

        if (subNode != null && subNode.innerText != null) {
            return Integer.parseInt(subNode.innerText);
        }

        return defaultValue;
    }

    private void readCpuItem(XmlNode node) {
        vmBase.setNumOfSockets(
                Integer.parseInt(selectSingleNode(node, "rasd:num_of_sockets", _xmlNS).innerText));
        vmBase.setCpuPerSocket(
                Integer.parseInt(selectSingleNode(node, "rasd:cpu_per_socket", _xmlNS).innerText));
        vmBase.setThreadsPerCpu(parseNodeInteger(node, "rasd:threads_per_cpu", 1));
    }

    private void readMemoryItem(XmlNode node) {
        vmBase.setMemSizeMb(
                Integer.parseInt(selectSingleNode(node, "rasd:VirtualQuantity", _xmlNS).innerText));
    }

    private void readCdItem(XmlNode node) {
        readManagedVmDevice(node, Guid.newGuid());
    }

    private void readNetworkItem(XmlNode node) {
        VmNetworkInterface iface = getNetworkInterface(node);
        updateSingleNic(node, iface);
        vmBase.getInterfaces().add(iface);
        readManagedVmDevice(node, iface.getId());
    }

    private void readUsbItem(XmlNode node) {
        vmBase.setUsbPolicy(
                UsbPolicy.forStringValue(selectSingleNode(node, "rasd:UsbPolicy", _xmlNS).innerText));
    }

    private VmDevice readOtherHardwareItem(XmlNode node) {
        boolean managed = false;
        if (selectSingleNode(node, VMD_TYPE, _xmlNS) != null
                && StringUtils.isNotEmpty(selectSingleNode(node, VMD_TYPE, _xmlNS).innerText)) {
            VmDeviceGeneralType type = VmDeviceGeneralType
                    .forValue(String.valueOf(selectSingleNode(node, VMD_TYPE, _xmlNS).innerText));
            String device = selectSingleNode(node, VMD_DEVICE, _xmlNS).innerText;
            // special devices are treated as managed devices but still have the OTHER OVF ResourceType
            managed = VmDeviceCommonUtils.isSpecialDevice(device, type);
        }

        return managed ? readManagedVmDevice(node, Guid.newGuid())
                : readUnmanagedVmDevice(node, Guid.newGuid());
    }

    protected void readGeneralData() {
        XmlNode content = selectSingleNode(_document, "//*/Content");
        XmlNode node;
        vmBase.setVmInit(new VmInit());

        // set ovf version to the ovf object
        vmBase.setOvfVersion(getVersion());

        consumeReadProperty(content, DESCRIPTION, val -> vmBase.setDescription(val));
        consumeReadProperty(content, COMMENT, val -> vmBase.setComment(val));
        consumeReadProperty(content, DOMAIN, val -> vmBase.getVmInit().setDomain(val));
        consumeReadProperty(content,
                CREATION_DATE,
                val -> vmBase.setCreationDate(OvfParser.utcDateStringToLocalDate(val)));
        consumeReadProperty(content,
                EXPORT_DATE,
                val -> vmBase.setExportDate(OvfParser.utcDateStringToLocalDate(val)));
        consumeReadProperty(content,
                DEFAULT_BOOT_SEQUENCE,
                val -> vmBase.setDefaultBootSequence(BootSequence.forValue(Integer.parseInt(val))));
        consumeReadProperty(content, INITRD_URL, val -> vmBase.setInitrdUrl(val));
        consumeReadProperty(content, KERNEL_URL, val -> vmBase.setKernelUrl(val));
        consumeReadProperty(content, KERNEL_PARAMS, val -> vmBase.setKernelParams(val));
        consumeReadProperty(content,
                GENERATION,
                val -> vmBase.setDbGeneration(Long.parseLong(val)),
                () -> vmBase.setDbGeneration(1L));

        node = selectSingleNode(content, CUSTOM_COMPATIBILITY_VERSION);
        if (node != null) {
            vmBase.setCustomCompatibilityVersion(new Version(node.innerText));
        }

        Version originVersion = new Version(getVersion()); // the originating ENGINE version
        node = selectSingleNode(content, CLUSTER_COMPATIBILITY_VERSION);
        if (node != null) {
            originVersion = new Version(node.innerText);
        }
        vmBase.setClusterCompatibilityVersionOrigin(originVersion);

        // Note: the fetching of 'default display type' should happen before reading
        // the hardware section
        consumeReadProperty(content,
                getDefaultDisplayTypeStringRepresentation(),
                val -> vmBase.setDefaultDisplayType(DisplayType.forValue(Integer.parseInt(val))));

        XmlNodeList list = selectNodes(content, "Section");

        if (list != null) {
            // The Os need to be read before the hardware
            node = getNode(list, "xsi:type", "ovf:OperatingSystemSection_Type");
            if (node != null) {
                readOsSection(node);
                if (!osRepository.isLinux(vmBase.getOsId())
                        || vmBase.getDefaultDisplayType() != DisplayType.qxl) {
                    vmBase.setSingleQxlPci(false);
                }
            }

            node = getNode(list, "xsi:type", "ovf:VirtualHardwareSection_Type");
            if (node != null) {
                readHardwareSection(node);
            }

            node = getNode(list, "xsi:type", "ovf:SnapshotsSection_Type");
            if (node != null) {
                readSnapshotsSection(node);
            }
        }

        // after reading the hardware section, if graphics device is still absent, add a default one
        addDefaultGraphicsDevice();

        fixDiskVmElements();

        // due to dependency on vmBase.getOsId() must be read AFTER readOsSection
        consumeReadProperty(content, TIMEZONE, val -> vmBase.setTimeZone(val), () -> {
            if (osRepository.isWindows(vmBase.getOsId())) {
                vmBase.setTimeZone(Config.getValue(ConfigValues.DefaultWindowsTimeZone));
            } else {
                vmBase.setTimeZone(Config.getValue(ConfigValues.DefaultGeneralTimeZone));
            }
        });

        consumeReadProperty(content, ORIGIN, val -> vmBase.setOrigin(OriginType.forValue(Integer.parseInt(val))));
        consumeReadProperty(content, VM_TYPE, val -> vmBase.setVmType(VmType.forValue(Integer.parseInt(val))));
        consumeReadProperty(content,
                IS_SMARTCARD_ENABLED,
                val -> vmBase.setSmartcardEnabled(Boolean.parseBoolean(val)));
        consumeReadProperty(content, NUM_OF_IOTHREADS, val -> vmBase.setNumOfIoThreads(Integer.parseInt(val)));
        consumeReadProperty(content, DELETE_PROTECTED, val -> vmBase.setDeleteProtected(Boolean.parseBoolean(val)));
        consumeReadProperty(content, SSO_METHOD, val -> vmBase.setSsoMethod(SsoMethod.fromString(val)));
        consumeReadProperty(content, TUNNEL_MIGRATION, val -> vmBase.setTunnelMigration(Boolean.parseBoolean(val)));
        consumeReadProperty(content, VNC_KEYBOARD_LAYOUT, val -> vmBase.setVncKeyboardLayout(val));
        consumeReadProperty(content, MIN_ALLOCATED_MEMORY, val -> vmBase.setMinAllocatedMem(Integer.parseInt(val)));
        consumeReadProperty(content, IS_STATELESS, val -> vmBase.setStateless(Boolean.parseBoolean(val)));
        consumeReadProperty(content, IS_RUN_AND_PAUSE, val -> vmBase.setRunAndPause(Boolean.parseBoolean(val)));
        consumeReadProperty(content,
                CREATED_BY_USER_ID,
                val -> vmBase.setCreatedByUserId(Guid.createGuidFromString(val)));
        consumeReadProperty(content, MIGRATION_DOWNTIME, val -> vmBase.setMigrationDowntime(Integer.parseInt(val)));
        consumeReadProperty(content,
                MIGRATION_SUPPORT,
                val -> vmBase.setMigrationSupport(MigrationSupport.forValue(Integer.parseInt(val))));

        // TODO dedicated to multiple hosts
        readDedicatedHostsList();

        consumeReadProperty(content,
                SERIAL_NUMBER_POLICY,
                val -> vmBase.setSerialNumberPolicy(SerialNumberPolicy.forValue(Integer.parseInt(val))));
        consumeReadProperty(content, CUSTOM_SERIAL_NUMBER, val -> vmBase.setCustomSerialNumber(val));
        consumeReadProperty(content, AUTO_STARTUP, val -> vmBase.setAutoStartup(Boolean.parseBoolean(val)));
        consumeReadProperty(content, PRIORITY, val -> vmBase.setPriority(Integer.parseInt(val)));
        consumeReadProperty(content, IS_BOOT_MENU_ENABLED, val -> vmBase.setBootMenuEnabled(Boolean.parseBoolean(val)));
        consumeReadProperty(content,
                IS_SPICE_FILE_TRANSFER_ENABLED,
                val -> vmBase.setSpiceFileTransferEnabled(Boolean.parseBoolean(val)));
        consumeReadProperty(content,
                IS_SPICE_COPY_PASTE_ENABLED,
                val -> vmBase.setSpiceCopyPasteEnabled(Boolean.parseBoolean(val)));
        consumeReadProperty(content,
                ALLOW_CONSOLE_RECONNECT,
                val -> vmBase.setAllowConsoleReconnect(Boolean.parseBoolean(val)));
        consumeReadProperty(content, IS_AUTO_CONVERGE, val -> vmBase.setAutoConverge(Boolean.parseBoolean(val)));
        consumeReadProperty(content,
                IS_MIGRATE_COMPRESSED,
                val -> vmBase.setMigrateCompressed(Boolean.parseBoolean(val)));
        consumeReadProperty(content,
                MIGRATION_POLICY_ID,
                val -> vmBase.setMigrationPolicyId(Guid.createGuidFromString(val)));
        consumeReadProperty(content, CUSTOM_EMULATED_MACHINE, val -> vmBase.setCustomEmulatedMachine(val));
        consumeReadProperty(content, CUSTOM_CPU_NAME, val -> vmBase.setCustomCpuName(val));
        consumeReadProperty(content, PREDEFINED_PROPERTIES, val -> vmBase.setPredefinedProperties(val));
        consumeReadProperty(content, USER_DEFINED_PROPERTIES, val -> vmBase.setUserDefinedProperties(val));
        consumeReadProperty(content, MAX_MEMORY_SIZE_MB, val -> vmBase.setMaxMemorySizeMb(Integer.parseInt(val)));

        vmBase.setCustomProperties(VmPropertiesUtils.getInstance().customProperties(
                vmBase.getPredefinedProperties(), vmBase.getUserDefinedProperties()));

        consumeReadProperty(content, VM_LEASE, val -> vmBase.setLeaseStorageDomainId(new Guid(val)));

        readGeneralData(content);

        readVmInit(content);
    }

    protected void consumeReadProperty(XmlNode content, String propertyKey, Consumer<String> then) {
        consumeReadProperty(content, propertyKey, then, null);
    }

    protected void consumeReadProperty(XmlNode content, String propertyKey, Consumer<String> then, Runnable orElse) {
        XmlNode node = selectSingleNode(content, propertyKey);
        if (node != null && StringUtils.isNotEmpty(node.innerText)) {
            then.accept(node.innerText);
            return;
        }
        if (orElse != null) {
            orElse.run();
        }
    }

    private void readDedicatedHostsList() {
        vmBase.setDedicatedVmForVdsList(new LinkedList<>()); // initialize to empty list
        // search all dedicated hosts with xPath
        XmlNodeList hostsList = selectNodes(_document, "//*/Content/" + DEDICATED_VM_FOR_VDS);
        for (XmlNode hostNode : hostsList) {
            if (hostNode != null && StringUtils.isNotEmpty(hostNode.innerText)) {
                vmBase.getDedicatedVmForVdsList().add(Guid.createGuidFromString(hostNode.innerText));
            }
        }
    }

    private void readVmInit(XmlNode content) {
        XmlNode node = selectSingleNode(content, "VmInit");
        VmInit vmInit = vmBase.getVmInit();
        vmInit.setId(vmBase.getId());
        if (node != null) {
            if (node.attributes.get("ovf:hostname") != null) {
                vmInit.setHostname(node.attributes.get("ovf:hostname").getValue());
            }
            if (node.attributes.get("ovf:domain") != null) {
                vmInit.setDomain(node.attributes.get("ovf:domain").getValue());
            }
            if (node.attributes.get("ovf:timeZone") != null) {
                vmInit.setTimeZone(node.attributes.get("ovf:timeZone").getValue());
            }
            if (node.attributes.get("ovf:authorizedKeys") != null) {
                vmInit.setAuthorizedKeys(node.attributes.get("ovf:authorizedKeys").getValue());
            }
            if (node.attributes.get("ovf:regenerateKeys") != null) {
                vmInit.setRegenerateKeys(Boolean.parseBoolean(node.attributes.get("ovf:regenerateKeys").getValue()));
            }
            if (node.attributes.get("ovf:dnsServers") != null) {
                vmInit.setDnsServers(node.attributes.get("ovf:dnsServers").getValue());
            }
            if (node.attributes.get("ovf:dnsSearch") != null) {
                vmInit.setDnsSearch(node.attributes.get("ovf:dnsSearch").getValue());
            }
            if (node.attributes.get("ovf:networks") != null) {
                vmInit.setNetworks(VmInitUtils.jsonNetworksToList(node.attributes.get("ovf:networks").getValue()));
            }
            if (node.attributes.get("ovf:winKey") != null) {
                vmInit.setWinKey(node.attributes.get("ovf:winKey").getValue());
            }
            if (node.attributes.get("ovf:rootPassword") != null) {
                vmInit.setRootPassword(node.attributes.get("ovf:rootPassword").getValue());
            }
            if (node.attributes.get("ovf:customScript") != null) {
                vmInit.setCustomScript(node.attributes.get("ovf:customScript").getValue());
            }
        }
    }

    private XmlNode getNode(XmlNodeList nodeList, String attributeName, String attributeValue) {

        for (XmlNode section : nodeList) {
            String value = section.attributes.get(attributeName).getValue();

            if (value.equals(attributeValue)) {
                return section;
            }
        }

        return null;
    }

    protected void readSnapshotsSection(@SuppressWarnings("unused") XmlNode section) {
        // The snapshot section only has meaning for VMs, and is overridden in OvfVmReader.
    }

    protected abstract void readGeneralData(XmlNode content);

    protected void buildNicReference() {
        XmlNodeList list = selectNodes(_document, "//*/Nic", _xmlNS);
        for (XmlNode node : list) {
            VmNetworkInterface iface = new VmNetworkInterface();
            iface.setId(new Guid(node.attributes.get("ovf:id").getValue()));
            interfaces.add(iface);
        }
        if (!list.iterator().hasNext()) {
            String pattern = "//*/Item[" +
                    VMD_RESOURCE_TYPE +
                    "=" +
                    OvfHardware.Network +
                    "]";
            list = selectNodes(_document, pattern, _xmlNS);
            for (XmlNode node : list) {
                VmNetworkInterface iface = new VmNetworkInterface();
                iface.setId(Guid.newGuid());
                updateSingleNic(node, iface);
                interfaces.add(iface);
            }
        }
    }

    protected void updateSingleNic(XmlNode node, VmNetworkInterface iface) {
        String networkName = selectSingleNode(node, VMD_CONNECTION, _xmlNS).innerText;
        iface.setNetworkName(StringUtils.defaultIfEmpty(networkName, null));

        XmlNode vnicProfileNameNode = selectSingleNode(node, VMD_VNIC_PROFILE_NAME, _xmlNS);
        iface.setVnicProfileName(vnicProfileNameNode == null ? null
                : StringUtils.defaultIfEmpty(vnicProfileNameNode.innerText, null));

        XmlNode linkedNode = selectSingleNode(node, VMD_LINKED, _xmlNS);
        iface.setLinked(linkedNode == null ? true : Boolean.valueOf(linkedNode.innerText));

        iface.setName(selectSingleNode(node, VMD_NAME, _xmlNS).innerText);

        String resourceSubType = selectSingleNode(node, "rasd:ResourceSubType", _xmlNS).innerText;
        if (StringUtils.isNotEmpty(resourceSubType)) {
            iface.setType(Integer.parseInt(resourceSubType));
        }

        XmlNode speed = selectSingleNode(node, "rasd:speed", _xmlNS);
        iface.setSpeed((speed != null) ? Integer.parseInt(speed.innerText) : VmInterfaceType.forValue(iface.getType())
                .getSpeed());

    }

    private void buildFileReference() {
        for (XmlNode node : selectNodes(_document, "//*/File", _xmlNS)) {
            fileIdToFileAttributes.put(node.attributes.get("ovf:id").getValue(), node.attributes);
        }
    }

    private int getResourceType(XmlNode node, String resource) {
        if (selectSingleNode(node, resource, _xmlNS) != null
                && !StringUtils.isEmpty(selectSingleNode(node, resource, _xmlNS).innerText)) {
            return Integer.parseInt(selectSingleNode(node, resource, _xmlNS).innerText);
        }
        return -1;
    }

    private void setDeviceByResource(XmlNode node, VmDevice vmDevice) {
        int resourceType = getResourceType(node, VMD_RESOURCE_TYPE);
        int resourceSubType = getResourceType(node, VMD_SUB_RESOURCE_TYPE);
        if (resourceSubType == -1) {
            // we need special handling for Monitor to define it as vnc or spice
            if (Integer.parseInt(OvfHardware.Monitor) == resourceType) {
                // get number of monitors from VirtualQuantity in OVF
                if (selectSingleNode(node, VMD_VIRTUAL_QUANTITY, _xmlNS) != null
                        && !StringUtils.isEmpty(selectSingleNode(node,
                                VMD_VIRTUAL_QUANTITY,
                                _xmlNS).innerText)) {
                    int virtualQuantity =
                            Integer.parseInt(
                                    selectSingleNode(node, VMD_VIRTUAL_QUANTITY, _xmlNS).innerText);
                    if (virtualQuantity > 1) {
                        vmDevice.setDevice(VmDeviceType.QXL.getName());
                    } else {
                        // get first supported display device
                        List<Pair<GraphicsType, DisplayType>> supportedGraphicsAndDisplays =
                                osRepository.getGraphicsAndDisplays(vmBase.getOsId(), new Version(getVersion()));
                        if (!supportedGraphicsAndDisplays.isEmpty()) {
                            DisplayType firstDisplayType = supportedGraphicsAndDisplays.get(0).getSecond();
                            vmDevice.setDevice(firstDisplayType.getDefaultVmDeviceType().getName());
                        } else {
                            vmDevice.setDevice(VmDeviceType.QXL.getName());
                        }
                    }
                } else { // default to spice if quantity not found
                    vmDevice.setDevice(VmDeviceType.QXL.getName());
                }
            } else {
                vmDevice.setDevice(VmDeviceType.getoVirtDevice(resourceType).getName());
            }
        } else if (Integer.parseInt(OvfHardware.Network) == resourceType) {
            // handle interfaces with different sub types : we have 0-5 as the VmInterfaceType enum
            VmInterfaceType nicType = VmInterfaceType.forValue(resourceSubType);
            if (nicType != null) {
                if (nicType == VmInterfaceType.pciPassthrough) {
                    vmDevice.setDevice(VmDeviceType.HOST_DEVICE.getName());
                } else {
                    vmDevice.setDevice(VmDeviceType.BRIDGE.getName());
                }
            } else {
                vmDevice.setDevice(VmDeviceType.getoVirtDevice(resourceType).getName());
            }
        }
    }

    private void addDefaultGraphicsDevice() {
        VmDevice device = VmDeviceCommonUtils.findVmDeviceByGeneralType(
                vmBase.getManagedDeviceMap(), VmDeviceGeneralType.GRAPHICS);
        if (device != null) {
            return;
        }

        List<Pair<GraphicsType, DisplayType>> graphicsAndDisplays =
                osRepository.getGraphicsAndDisplays(vmBase.getOsId(), new Version(getVersion()));
        GraphicsType graphicsType =
                vmBase.getDefaultDisplayType() == DisplayType.cirrus ? GraphicsType.VNC : GraphicsType.SPICE;
        GraphicsType supportedGraphicsType = null;
        for (Pair<GraphicsType, DisplayType> pair : graphicsAndDisplays) {
            if (pair.getSecond() == vmBase.getDefaultDisplayType()) {
                if (pair.getFirst() == graphicsType) {
                    supportedGraphicsType = graphicsType;
                    break;
                }
                if (supportedGraphicsType == null) {
                    supportedGraphicsType = pair.getFirst();
                }
            }
        }
        if (supportedGraphicsType != null) {
            device = new GraphicsDevice(supportedGraphicsType.getCorrespondingDeviceType());
            device.setId(new VmDeviceId(Guid.newGuid(), vmBase.getId()));
            addManagedVmDevice(device);
        } else {
            log.warn("Cannot find any graphics type for display type {} supported by OS {} in compatibility version {}",
                    vmBase.getDefaultDisplayType().name(),
                    osRepository.getOsName(vmBase.getOsId()),
                    getVersion());
        }
    }

    private void fixDiskVmElements() {
        // In the time of disk creation the VM ID is an empty Guid, this is changed to the real ID only after the
        // reading of the OS properties which comes after the disks creation so the disk VM elements are set to
        // the wrong VM ID this part sets them to the correct VM ID
        for (DiskImage disk : _images) {
            disk.getDiskVmElements().forEach(dve -> dve.setId(new VmDeviceId(disk.getId(), vmBase.getId())));
            disk.setDiskVmElements(disk.getDiskVmElements());
        }
    }

    private static Map<String, Object> getMapNode(XmlNode node) {
        Map<String, Object> returnValue = new HashMap<>();

        NodeList list = node.getChildNodes();
        for (int index = 0; index < list.getLength(); ++index) {
            Node currNode = list.item(index);
            short nodeType = currNode.getNodeType();
            if (nodeType == Node.ELEMENT_NODE) {
                NodeList childNodes = currNode.getChildNodes();
                // If the element node has only one child, then it contains the value
                if (childNodes.getLength() == 1) {
                    Node valueNode = childNodes.item(0);
                    if (valueNode.getNodeType() == Node.TEXT_NODE) {
                        returnValue.put(currNode.getNodeName(), valueNode.getNodeValue());
                    }
                } else if (childNodes.getLength() > 1) {
                    // In this case, we have a nested map, so we parse it
                    returnValue.put(currNode.getNodeName(), getMapNode(new XmlNode(currNode)));
                }
            }
        }

        return returnValue;
    }

    protected XmlNode selectSingleNode(XmlDocument doc, String pattern) {
        return selectSingleNode(doc, pattern, null);
    }

    protected XmlNode selectSingleNode(XmlDocument doc, String pattern, XmlNamespaceManager ns) {
        this.lastReadEntry = pattern;
        if (ns == null) {
            return doc.selectSingleNode(pattern);
        }
        return doc.selectSingleNode(pattern, ns);
    }

    protected XmlNodeList selectNodes(XmlDocument doc, String pattern) {
        return selectNodes(doc, pattern, null);
    }

    protected XmlNodeList selectNodes(XmlDocument doc, String pattern, XmlNamespaceManager ns) {
        this.lastReadEntry = pattern;
        if (ns == null) {
            return doc.selectNodes(pattern);
        }
        return doc.selectNodes(pattern, ns);
    }

    protected XmlNode selectSingleNode(XmlNode node, String pattern) {
        return selectSingleNode(node, pattern, null);
    }

    protected XmlNode selectSingleNode(XmlNode node, String pattern, XmlNamespaceManager ns) {
        this.lastReadEntry = pattern;
        if (ns == null) {
            return node.selectSingleNode(pattern);
        }
        return node.selectSingleNode(pattern, ns);
    }

    protected XmlNodeList selectNodes(XmlNode node, String pattern) {
        this.lastReadEntry = pattern;
        return node.selectNodes(pattern);
    }

    public String getLastReadEntry() {
        return lastReadEntry;
    }

    @Override
    public String getStringRepresentation() {
        return _document.getOuterXml();
    }
}
