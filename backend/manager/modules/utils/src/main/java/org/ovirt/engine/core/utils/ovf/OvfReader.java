package org.ovirt.engine.core.utils.ovf;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.ConsoleDisconnectAction;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
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
import org.ovirt.engine.core.common.businessentities.VmResumeBehavior;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
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
    protected List<LunDisk> luns;
    protected List<VmNetworkInterface> interfaces;
    protected XmlDocument _document;
    protected XmlNamespaceManager _xmlNS;
    public static final String EmptyName = "[Empty Name]";
    private String version;
    private final VmBase vmBase;
    private String lastReadEntry = "";
    private boolean hasBalloonDevice;

    public OvfReader(
            XmlDocument document,
            List<DiskImage> images,
            List<LunDisk> luns,
            List<VmNetworkInterface> interfaces,
            VmBase vmBase,
            OsRepository osRepository) {
        _images = images;
        this.luns = luns;
        this.interfaces = interfaces;
        _document = document;
        this.vmBase = vmBase;
        this.osRepository = osRepository;

        _xmlNS = new XmlNamespaceManager();
        _xmlNS.addNamespace("ovf", OVF_URI);
        _xmlNS.addNamespace("rasd", RASD_URI);
        _xmlNS.addNamespace("vssd", VSSD_URI);
        _xmlNS.addNamespace("xsi", XSI_URI);

        XmlNode header = selectSingleNode(_document, "//ovf:Envelope", _xmlNS);
        if (header == null) {
            header = selectSingleNode(_document, "//Envelope", _xmlNS);
        }
        readHeader(header);
    }

    public String getName() {
        return StringUtils.isNotEmpty(vmBase.getName()) ? vmBase.getName() : EmptyName;
    }

    public String getVersion() {
        return version;
    }

    protected void setVersion(String version) {
        this.version = version;
    }

    /**
     * reads the OVF header
     */
    protected void readHeader(XmlNode header) {
    }

    @Override
    public void buildReference() {
        buildFileReference();
    }

    @Override
    public void buildNetwork() {
        // No implementation - networks aren't read from the OVF.
    }

    protected long convertGigabyteToBytes(long gb) {
        return gb * BYTES_IN_GB;
    }

    protected void readDisk(XmlNode node, DiskImage image) {
        image.setDiskVmElements(Collections.singletonList(new DiskVmElement(image.getId(), vmBase.getId())));
        DiskVmElement dve = image.getDiskVmElementForVm(vmBase.getId());

        // Default values:
        image.setActive(true);
        image.setImageStatus(ImageStatus.OK);

        if (node.attributes.get("ovf:vm_snapshot_id") != null) {
            image.setVmSnapshotId(new Guid(node.attributes.get("ovf:vm_snapshot_id").getValue()));
        }

        if (node.attributes.get("ovf:volume-format") != null) {
            if (!StringUtils.isEmpty(node.attributes.get("ovf:volume-format").getValue())) {
                image.setVolumeFormat(VolumeFormat.valueOf(node.attributes.get("ovf:volume-format")
                        .getValue()));
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
                image.setVolumeType(getDefaultVolumeType());
            }
        } else {
            image.setVolumeType(getDefaultVolumeType());
        }
        if (node.attributes.get("ovf:wipe-after-delete") != null) {
            if (!StringUtils.isEmpty(node.attributes.get("ovf:wipe-after-delete").getValue())) {
                image.setWipeAfterDelete(Boolean.parseBoolean(node.attributes.get("ovf:wipe-after-delete")
                        .getValue()));
            }
        }
        initGeneralDiskAttributes(node, image, dve);
    }

    protected void initGeneralDiskAttributes(XmlNode node, Disk disk, DiskVmElement dve) {
        if (node.attributes.get("ovf:disk-interface") != null) {
            if (!StringUtils.isEmpty(node.attributes.get("ovf:disk-interface").getValue())) {
                dve.setDiskInterface(DiskInterface.valueOf(node.attributes.get("ovf:disk-interface")
                        .getValue()));
            }
        } else {
            dve.setDiskInterface(DiskInterface.IDE);
        }
        if (node.attributes.get("ovf:boot") != null) {
            if (!StringUtils.isEmpty(node.attributes.get("ovf:boot").getValue())) {
                dve.setBoot(Boolean.parseBoolean(node.attributes.get("ovf:boot").getValue()));
            }
        }
        if (node.attributes.get("ovf:read-only") != null) {
            if (!StringUtils.isEmpty(node.attributes.get("ovf:read-only").getValue())) {
                dve.setReadOnly(Boolean.parseBoolean(node.attributes.get("ovf:read-only").getValue()));
            }
        }
        if (node.attributes.get("ovf:pass-discard") != null) {
            if (!StringUtils.isEmpty(node.attributes.get("ovf:pass-discard").getValue())) {
                dve.setPassDiscard(Boolean.parseBoolean(node.attributes.get("ovf:pass-discard").getValue()));
            }
        }
        if (node.attributes.get("ovf:disk-alias") != null) {
            if (!StringUtils.isEmpty(node.attributes.get("ovf:disk-alias").getValue())) {
                disk.setDiskAlias(String.valueOf(node.attributes.get("ovf:disk-alias")
                        .getValue()));
            }
        }
        if (node.attributes.get("ovf:disk-description") != null) {
            if (!StringUtils.isEmpty(node.attributes.get("ovf:disk-description").getValue())) {
                disk.setDiskDescription(String.valueOf(node.attributes.get("ovf:disk-description")
                        .getValue()));
            }
        }
        if (node.attributes.get("ovf:shareable") != null) {
            if (!StringUtils.isEmpty(node.attributes.get("ovf:shareable").getValue())) {
                disk.setShareable(Boolean.parseBoolean(node.attributes.get("ovf:shareable").getValue()));
            }
        }
        if (node.attributes.get("ovf:scsi_reservation") != null) {
            if (!StringUtils.isEmpty(node.attributes.get("ovf:scsi_reservation").getValue())) {
                dve.setUsingScsiReservation(Boolean.parseBoolean(node.attributes.get("ovf:scsi_reservation").getValue()));
            }
        }
        dve.setPlugged(Boolean.TRUE);
        if (node.attributes.get("ovf:plugged") != null) {
            if (!StringUtils.isEmpty(node.attributes.get("ovf:plugged").getValue())) {
                dve.setPlugged(Boolean.parseBoolean(node.attributes.get("ovf:plugged").getValue()));
            }
        }
    }

    protected void readLunDisk(XmlNode node, LunDisk lun) {
        // do nothing
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
        if (vmDevice.getType() == VmDeviceGeneralType.BALLOON) {
            hasBalloonDevice = true;
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
    protected abstract VmNetworkInterface getNetworkInterface(XmlNode node);

    /**
     * This method should return the string representation of 'default display type' property in the ovf file. this
     * representation is different for ovf file of VM and ovf file of template.
     *
     * @return the String representation of 'default display type' property in the ovf file
     */
    protected abstract String getDefaultDisplayTypeStringRepresentation();

    protected void readHardwareSection(XmlNode section) {
        boolean readVirtioSerial = false;

        int nicIdx = 0;
        for (XmlNode item : selectNodes(section, "Item")) {
            String resourceType = selectSingleNode(item, "rasd:ResourceType", _xmlNS).innerText;
            resourceType = adjustHardwareResourceType(resourceType);
            switch (resourceType) {
            case OvfHardware.CPU:
                readCpuItem(item);
                break;

            case OvfHardware.Memory:
                readMemoryItem(item);
                break;

            case OvfHardware.DiskImage:
                readDiskImageItem(item);
                break;

            case OvfHardware.Network:
                /**
                 *  If NIC items are found in the hardware section of the OVF, they are the real ones,
                 *  so clear the NIC references found in the <NetworkSection> of the OVF which are used
                 *  only to map networks, but should not be used to create NICs. Clear() is performed only
                 *  upon finding the first NIC item.
                 */
                if (nicIdx == 0) {
                    interfaces.clear();
                }
                readNetworkItem(item, ++nicIdx);
                break;

            case OvfHardware.USB:
                readUsbItem(item);
                break;

            case OvfHardware.Monitor:
                readMonitorItem(item);
                break;

            case OvfHardware.Graphics:
                readManagedVmDevice(item, readDeviceId(item)); // so far graphics doesn't contain anything special
                break;

            case OvfHardware.CD:
                readCdItem(item);
                break;

            case OvfHardware.OTHER:
                VmDevice vmDevice = readOtherHardwareItem(item);
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

    protected String adjustHardwareResourceType(String resourceType) {
        return resourceType;
    }

    protected void readDiskImageItem(XmlNode node, DiskImage image) {
        XmlNode templateNode = selectSingleNode(node, "rasd:Template", _xmlNS);
        if (templateNode != null && StringUtils.isNotEmpty(templateNode.innerText)) {
            image.setImageTemplateId(new Guid(templateNode.innerText));
        }

        XmlNode applicationsNode = selectSingleNode(node, "rasd:ApplicationList", _xmlNS);
        if (applicationsNode != null) {
            image.setAppList(applicationsNode.innerText);
        }

        XmlNodeList storageNodes = selectNodes(node, "rasd:StorageId", _xmlNS);
        if (storageNodes.iterator().hasNext()) {
            for (XmlNode storageIdNode : storageNodes) {
                if (storageIdNode != null && StringUtils.isNotEmpty(storageIdNode.innerText)) {
                    if (image.getStorageIds() == null) {
                        image.setStorageIds(new LinkedList<>());
                    }
                    image.getStorageIds().add(new Guid(storageIdNode.innerText));
                }
            }
        }

        XmlNode storagePoolNode = selectSingleNode(node, "rasd:StoragePoolId", _xmlNS);
        if (storagePoolNode != null && StringUtils.isNotEmpty(storagePoolNode.innerText)) {
            image.setStoragePoolId(new Guid(storagePoolNode.innerText));
        }

        XmlNode creationDateNode = selectSingleNode(node, "rasd:CreationDate", _xmlNS);
        Date creationDate = creationDateNode != null ? OvfParser.utcDateStringToLocalDate(creationDateNode.innerText)
                : null;
        if (creationDate != null) {
            image.setCreationDate(creationDate);
        }

        XmlNode lastModifiedNode = selectSingleNode(node, "rasd:LastModified", _xmlNS);
        Date lastModified = lastModifiedNode != null ? OvfParser.utcDateStringToLocalDate(lastModifiedNode.innerText)
                : null;
        if (lastModified != null) {
            image.setLastModified(lastModified);
        }

        XmlNode lastModifiedDateNode = selectSingleNode(node, "rasd:last_modified_date", _xmlNS);
        Date last_modified_date = lastModifiedDateNode != null ?
                OvfParser.utcDateStringToLocalDate(lastModifiedDateNode.innerText) : null;
        if (last_modified_date != null) {
            image.setLastModifiedDate(last_modified_date);
        }

        VmDevice readDevice = readManagedVmDevice(node, image.getId());
        image.setPlugged(readDevice.isPlugged());
    }

    protected void readMonitorItem(XmlNode node) {
        vmBase.setNumOfMonitors(
                Integer.parseInt(selectSingleNode(node, "rasd:VirtualQuantity", _xmlNS).innerText));

        readManagedVmDevice(node, readDeviceId(node));
    }

    protected void readCpuItem(XmlNode node) {
        XmlNode sockets = selectSingleNode(node, "rasd:num_of_sockets", _xmlNS);
        if (sockets == null || StringUtils.isEmpty(sockets.innerText)) {
            sockets = selectSingleNode(node, "rasd:VirtualQuantity", _xmlNS);
        }
        vmBase.setNumOfSockets(Integer.parseInt(sockets.innerText));

        XmlNode cpuPerSocket = selectSingleNode(node, "rasd:cpu_per_socket", _xmlNS);
        if (cpuPerSocket != null && StringUtils.isNotEmpty(cpuPerSocket.innerText)) {
            vmBase.setCpuPerSocket(Integer.parseInt(cpuPerSocket.innerText));
        }

        XmlNode threadsPerCpu = selectSingleNode(node, "rasd:threads_per_cpu", _xmlNS);
        if (threadsPerCpu != null && StringUtils.isNotEmpty(threadsPerCpu.innerText)) {
            vmBase.setThreadsPerCpu(Integer.parseInt(threadsPerCpu.innerText));
        }
    }

    private void readMemoryItem(XmlNode node) {
        vmBase.setMemSizeMb(
                Integer.parseInt(selectSingleNode(node, "rasd:VirtualQuantity", _xmlNS).innerText));
    }

    private void readCdItem(XmlNode node) {
        readManagedVmDevice(node, readDeviceId(node));
    }

    private void readNetworkItem(XmlNode node, int nicIdx) {
        VmNetworkInterface iface = getNetworkInterface(node);
        updateSingleNic(node, iface, nicIdx);
        readManagedVmDevice(node, iface.getId());
    }

    private void readUsbItem(XmlNode node) {
        XmlNode usbPolicy = selectSingleNode(node, "rasd:UsbPolicy", _xmlNS);
        vmBase.setUsbPolicy(usbPolicy != null ? UsbPolicy.forStringValue(usbPolicy.innerText) : UsbPolicy.ENABLED_NATIVE);
    }

    private VmDevice readOtherHardwareItem(XmlNode node) {
        boolean managed = false;
        if (selectSingleNode(node, VMD_TYPE, _xmlNS) != null
                && StringUtils.isNotEmpty(selectSingleNode(node, VMD_TYPE, _xmlNS).innerText)) {
            VmDeviceGeneralType type = VmDeviceGeneralType
                    .forValue(String.valueOf(selectSingleNode(node, VMD_TYPE, _xmlNS).innerText));
            String device = selectSingleNode(node, VMD_DEVICE, _xmlNS).innerText;
            // special devices are treated as managed devices but still have the OTHER OVF ResourceType
            managed = VmDeviceCommonUtils.isSpecialDevice(device, type, true);
        }

        return managed ? readManagedVmDevice(node, readDeviceId(node))
                : readUnmanagedVmDevice(node, Guid.newGuid());
    }

    private Guid readDeviceId(XmlNode node) {
        if (selectSingleNode(node, VMD_ID, _xmlNS) != null
                && StringUtils.isNotEmpty(selectSingleNode(node, VMD_TYPE, _xmlNS).innerText)) {
            return new Guid(String.valueOf(selectSingleNode(node, VMD_ID, _xmlNS).innerText));
        }
        return Guid.newGuid();
    }

    // must be called after readHardwareSection
    protected void readGeneralData(XmlNode content) {
        consumeReadProperty(content, DESCRIPTION, val -> vmBase.setDescription(val));
        consumeReadProperty(content, COMMENT, val -> vmBase.setComment(val));
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

        XmlNode node = selectSingleNode(content, CUSTOM_COMPATIBILITY_VERSION);
        if (node != null) {
            vmBase.setCustomCompatibilityVersion(new Version(node.innerText));
        }

        Version originVersion = new Version(getVersion()); // the originating ENGINE version
        node = selectSingleNode(content, CLUSTER_COMPATIBILITY_VERSION);
        if (node != null) {
            originVersion = new Version(node.innerText);
        }
        vmBase.setClusterCompatibilityVersionOrigin(originVersion);

        consumeReadProperty(content,
                getDefaultDisplayTypeStringRepresentation(),
                val -> vmBase.setDefaultDisplayType(DisplayType.forValue(Integer.parseInt(val))));

        // after reading the hardware section, if graphics device is still absent, add a default one
        addDefaultGraphicsDevice();

        fixDiskVmElements();

        // due to dependency on vmBase.getOsId() must be read AFTER readOsSection
        consumeReadProperty(content, TIMEZONE, val -> vmBase.setTimeZone(mapTimeZone(val)), () -> {
            if (osRepository.isWindows(vmBase.getOsId())) {
                vmBase.setTimeZone(Config.getValue(ConfigValues.DefaultWindowsTimeZone));
            } else {
                vmBase.setTimeZone(Config.getValue(ConfigValues.DefaultGeneralTimeZone));
            }
        });

        consumeReadProperty(content, ORIGIN, val -> vmBase.setOrigin(OriginType.forValue(Integer.parseInt(val))));
        consumeReadProperty(content, VM_TYPE, val -> vmBase.setVmType(VmType.forValue(Integer.parseInt(val))));
        consumeReadProperty(content, RESUME_BEHAVIOR, val -> vmBase.setResumeBehavior(VmResumeBehavior.valueOf(val)));
        consumeReadProperty(content,
                IS_SMARTCARD_ENABLED,
                val -> vmBase.setSmartcardEnabled(Boolean.parseBoolean(val)));
        consumeReadProperty(content, NUM_OF_IOTHREADS,
                val -> vmBase.setNumOfIoThreads(Integer.parseInt(val)),
                () -> vmBase.setNumOfIoThreads(0));
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
        readDedicatedHostsList(content);

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
        consumeReadProperty(content,
                CONSOLE_DISCONNECT_ACTION,
                val -> vmBase.setConsoleDisconnectAction(ConsoleDisconnectAction.fromString(val)));
        consumeReadProperty(content, IS_AUTO_CONVERGE, val -> vmBase.setAutoConverge(Boolean.parseBoolean(val)));
        consumeReadProperty(content,
                IS_MIGRATE_COMPRESSED,
                val -> vmBase.setMigrateCompressed(Boolean.parseBoolean(val)));
        consumeReadProperty(content,
                IS_MIGRATE_ENCRYPTED,
                val -> vmBase.setMigrateEncrypted(Boolean.parseBoolean(val)));
        consumeReadProperty(content,
                MIGRATION_POLICY_ID,
                val -> vmBase.setMigrationPolicyId(Guid.createGuidFromString(val)));
        consumeReadProperty(content, CUSTOM_EMULATED_MACHINE, val -> vmBase.setCustomEmulatedMachine(val));
        readBiosType(content);
        consumeReadProperty(content, CUSTOM_CPU_NAME, val -> vmBase.setCustomCpuName(val));
        consumeReadProperty(content, PREDEFINED_PROPERTIES, val -> vmBase.setPredefinedProperties(val));
        consumeReadProperty(content, USER_DEFINED_PROPERTIES, val -> vmBase.setUserDefinedProperties(val));
        consumeReadProperty(content, MAX_MEMORY_SIZE_MB, val -> vmBase.setMaxMemorySizeMb(Integer.parseInt(val)));

        vmBase.setCustomProperties(VmPropertiesUtils.getInstance().customProperties(
                vmBase.getPredefinedProperties(), vmBase.getUserDefinedProperties()));

        consumeReadProperty(content, VM_LEASE, val -> vmBase.setLeaseStorageDomainId(new Guid(val)));

        consumeReadProperty(content,
                MULTI_QUEUES_ENABLED,
                val -> vmBase.setMultiQueuesEnabled(Boolean.parseBoolean(val)));

        readVirtioScsiMultiQueues(content);

        consumeReadProperty(content,
                USE_HOST_CPU,
                val -> vmBase.setUseHostCpuFlags(Boolean.parseBoolean(val)));

        consumeReadProperty(content, BALLOON_ENABLED,
                val -> vmBase.setBalloonEnabled(Boolean.parseBoolean(val)),
                () -> vmBase.setBalloonEnabled(hasBalloonDevice));

        consumeReadProperty(content,
                CPU_PINNING_POLICY,
                val -> vmBase.setCpuPinningPolicy(CpuPinningPolicy.forValue(Integer.parseInt(val))));

        readVmInit(content);
    }

    private String mapTimeZone(String timezone) {
        // changes timezones mappings that:
        // - existed before and valid only to non-windows, to mappings that is valid to both non-windows and windows
        // - existed before but got removed because similar time zone exist, to mapping that exists
        switch (timezone) {
        case "America/Indianapolis":
            return "America/New_York";
        case "US Eastern Standard Time":
            return "Eastern Standard Time";
        case "Atlantic/Reykjavik":
            return "Etc/GMT";
        case "Iceland Standard Time":
            return "Greenwich Standard Time";
        }
        return timezone;
    }

    protected void consumeReadProperty(XmlNode content, String propertyKey, Consumer<String> then) {
        consumeReadProperty(content, propertyKey, then, null);
    }

    protected void consumeReadProperty(XmlNode content, String propertyKey, Consumer<String> then, Runnable orElse) {
        XmlNode node = selectSingleNode(content, propertyKey);
        acceptNode(then, orElse, node);
    }

    protected void consumeReadXmlAttribute(XmlNode content, String propertyKey, Consumer<String> then) {
        consumeReadXmlAttribute(content, propertyKey, then, null);
    }

    protected void consumeReadXmlAttribute(XmlNode content, String propertyKey, Consumer<String> then, Runnable orElse) {
        XmlAttribute node = content.attributes.get(propertyKey);
        acceptNode(then, orElse, node);
    }

    private void acceptNode(Consumer<String> then, Runnable orElse, XmlNode node) {
        if (node != null && StringUtils.isNotEmpty(node.innerText)) {
            then.accept(node.innerText);
            return;
        }
        if (orElse != null) {
            orElse.run();
        }
    }

    private void readDedicatedHostsList(XmlNode content) {
        vmBase.setDedicatedVmForVdsList(new LinkedList<>()); // initialize to empty list
        for (XmlNode hostNode : selectNodes(content, DEDICATED_VM_FOR_VDS)) {
            if (hostNode != null && StringUtils.isNotEmpty(hostNode.innerText)) {
                vmBase.getDedicatedVmForVdsList().add(Guid.createGuidFromString(hostNode.innerText));
            }
        }
    }

    private void readBiosType(XmlNode content) {
        XmlNode biosTypeNode = selectSingleNode(content, BIOS_TYPE);
        // For compatibility with oVirt 4.3, use values of BiosType constants that existed before
        // introduction of CLUSTER_DEFAULT:  0 == I440FX_SEA_BIOS and so on
        acceptNode(
                val -> {
                    vmBase.setBiosType(BiosType.forValue(Integer.parseInt(val) + 1));
                },
                () -> {
                    vmBase.setBiosType(BiosType.I440FX_SEA_BIOS);
                },
                biosTypeNode);
    }

    private String escapedNewLines(String value) {
        return value.replaceAll("\\\\n", "\n");
    }

    private void readVirtioScsiMultiQueues(XmlNode content) {

        XmlNode virtioScsiMultiQueuesNode = selectSingleNode(content, VIRTIO_SCSI_MULTI_QUEUES_ENABLED);
        if (virtioScsiMultiQueuesNode == null) {
            return;
        }
        boolean isVirtioMultiQueuesEnabled = Boolean.parseBoolean(virtioScsiMultiQueuesNode.innerText);
        if (isVirtioMultiQueuesEnabled) {
            XmlAttribute virtioScsiMultiQueuesNodeNumberAttribute =
                    virtioScsiMultiQueuesNode.attributes.get("ovf:queues");
            if (virtioScsiMultiQueuesNodeNumberAttribute == null) {
                vmBase.setVirtioScsiMultiQueues(-1);
            } else {
                vmBase.setVirtioScsiMultiQueues(
                        Integer.parseInt(virtioScsiMultiQueuesNodeNumberAttribute.getValue()));
            }
        } else {
            vmBase.setVirtioScsiMultiQueues(0);
        }
    }

    private void readVmInit(XmlNode content) {
        XmlNode node = selectSingleNode(content, "VmInit");
        if (node != null) {
            vmBase.setVmInit(new VmInit());
            consumeReadProperty(content, DOMAIN, val -> vmBase.getVmInit().setDomain(val));
            VmInit vmInit = vmBase.getVmInit();
            vmInit.setId(vmBase.getId());
            if (node.attributes.get("ovf:hostname") != null) {
                vmInit.setHostname(node.attributes.get("ovf:hostname").getValue());
            }
            if (node.attributes.get("ovf:domain") != null) {
                vmInit.setDomain(node.attributes.get("ovf:domain").getValue());
            }
            if (node.attributes.get("ovf:timeZone") != null) {
                vmInit.setTimeZone(mapTimeZone(node.attributes.get("ovf:timeZone").getValue()));
            }
            if (node.attributes.get("ovf:authorizedKeys") != null) {
                vmInit.setAuthorizedKeys(escapedNewLines(node.attributes.get("ovf:authorizedKeys").getValue()));
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
                vmInit.setCustomScript(escapedNewLines(node.attributes.get("ovf:customScript").getValue()));
            }
        }
    }

    protected void updateSingleNic(XmlNode node, VmNetworkInterface iface, int nicIdx) {
        String networkName = selectSingleNode(node, VMD_CONNECTION, _xmlNS).innerText;
        iface.setRemoteNetworkName(networkName);
        iface.setNetworkName(StringUtils.defaultIfEmpty(networkName, null));

        XmlNode vnicProfileNameNode = selectSingleNode(node, VMD_VNIC_PROFILE_NAME, _xmlNS);
        iface.setVnicProfileName(vnicProfileNameNode == null ? null
                : StringUtils.defaultIfEmpty(vnicProfileNameNode.innerText, null));

        XmlNode linkedNode = selectSingleNode(node, VMD_LINKED, _xmlNS);
        iface.setLinked(linkedNode == null ? true : Boolean.valueOf(linkedNode.innerText));

        XmlNode nameNode = selectSingleNode(node, VMD_NAME, _xmlNS);
        iface.setName(nameNode != null ? nameNode.innerText : String.format("nic%d", nicIdx));

        XmlNode resourceSubTypeNode = selectSingleNode(node, "rasd:ResourceSubType", _xmlNS);
        iface.setType(getVmInterfaceType(resourceSubTypeNode));

        XmlNode speed = selectSingleNode(node, "rasd:speed", _xmlNS);
        iface.setSpeed(speed != null ? Integer.parseInt(speed.innerText) : VmInterfaceType.forValue(iface.getType())
                .getSpeed());

    }

    protected abstract void buildFileReference();

    private int getVmInterfaceType(XmlNode resourceSubTypeNode) {
        String resourceSubType = resourceSubTypeNode != null ? resourceSubTypeNode.innerText : null;
        if (StringUtils.isNotEmpty(resourceSubType)) {
            try {
                return Integer.parseInt(resourceSubType);
            } catch(NumberFormatException ex) {
                for (VmInterfaceType vmInterfaceType : VmInterfaceType.values()) {
                    if (vmInterfaceType.getInternalName().equalsIgnoreCase(resourceSubType)) {
                        return vmInterfaceType.getValue();
                    }
                }
            }
        }
        return VmInterfaceType.pv.getValue();
    }

    private int getResourceType(XmlNode node, String resource) {
        if (selectSingleNode(node, resource, _xmlNS) != null
                && !StringUtils.isEmpty(selectSingleNode(node, resource, _xmlNS).innerText)) {
            return Integer.parseInt(selectSingleNode(node, resource, _xmlNS).innerText);
        }
        return -1;
    }

    private void setDeviceByResource(XmlNode node, VmDevice vmDevice) {
        String resourceType = selectSingleNode(node, VMD_RESOURCE_TYPE, _xmlNS).innerText;
        XmlNode resourceSubTypeNode = selectSingleNode(node, VMD_SUB_RESOURCE_TYPE, _xmlNS);
        if (resourceSubTypeNode == null) {
            // we need special handling for Monitor to define it as vnc or spice
            if (OvfHardware.Monitor.equals(adjustHardwareResourceType(resourceType))) {
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
                vmDevice.setDevice(VmDeviceType.getoVirtDevice(Integer.parseInt(resourceType)).getName());
            }
        } else if (OvfHardware.Network.equals(resourceType)) {
            // handle interfaces with different sub types : we have 0-5 as the VmInterfaceType enum
            VmInterfaceType nicType = VmInterfaceType.forValue(getVmInterfaceType(resourceSubTypeNode));
            if (nicType == VmInterfaceType.pciPassthrough) {
                vmDevice.setDevice(VmDeviceType.HOST_DEVICE.getName());
            } else {
                vmDevice.setDevice(VmDeviceType.BRIDGE.getName());
            }
        }
    }

    private void addDefaultGraphicsDevice() {
        VmDevice device = VmDeviceCommonUtils.findVmDeviceByGeneralType(
                vmBase.getManagedDeviceMap(), VmDeviceGeneralType.GRAPHICS);
        if (device != null) {
            return;
        }

        Version effectiveCompatibilityVersion = vmBase.getCustomCompatibilityVersion() != null ? vmBase.getCustomCompatibilityVersion() : vmBase.getClusterCompatibilityVersionOrigin();
        List<Pair<GraphicsType, DisplayType>> graphicsAndDisplays =
                osRepository.getGraphicsAndDisplays(vmBase.getOsId(), effectiveCompatibilityVersion);

        GraphicsType graphicsType;
        switch (vmBase.getDefaultDisplayType()) {
            case vga:
            case bochs:
                graphicsType = GraphicsType.VNC;
                break;
            default:
                graphicsType = GraphicsType.SPICE;
        }

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
        for (LunDisk lunDisk : luns) {
            lunDisk.getDiskVmElements().forEach(dve -> dve.setId(new VmDeviceId(lunDisk.getId(), vmBase.getId())));
            lunDisk.setDiskVmElements(lunDisk.getDiskVmElements());
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

    protected XmlNodeList selectNodes(XmlNode node, String pattern, XmlNamespaceManager ns) {
        this.lastReadEntry = pattern;
        return node.selectNodes(pattern, ns);
    }

    public String getLastReadEntry() {
        return lastReadEntry;
    }

    @Override
    public String getStringRepresentation() {
        return _document.getOuterXml();
    }

    protected VolumeType getDefaultVolumeType() {
        return VolumeType.Unassigned;
    }
}
