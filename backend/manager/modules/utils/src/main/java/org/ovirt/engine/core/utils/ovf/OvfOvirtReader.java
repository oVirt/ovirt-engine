package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;

public abstract class OvfOvirtReader extends OvfReader {

    private VmBase vmBase;
    private static final String COLON = ":";

    public OvfOvirtReader(XmlDocument document,
            List<DiskImage> images,
            List<LunDisk> luns,
            List<VmNetworkInterface> interfaces,
            VmBase vmBase,
            OsRepository osRepository) {
        super(document, images, luns, interfaces, vmBase, osRepository);
        this.vmBase = vmBase;
    }

    @Override
    protected void readHeader(XmlNode header) {
        super.readHeader(header);
        setVersion(header != null ? header.attributes.get("ovf:version").getValue() : "");
    }

    @Override
    public void buildVirtualSystem() {
        XmlNode virtualSystem = selectSingleNode(_document, "//*/Content");
        consumeReadProperty(virtualSystem, NAME, val -> vmBase.setName(val));

        // set ovf version to the ovf object
        vmBase.setOvfVersion(getVersion());

        XmlNodeList list = selectNodes(virtualSystem, "Section");
        if (list != null) {
            // The Os need to be read before the hardware
            XmlNode node = getNode(list, "xsi:type", "ovf:OperatingSystemSection_Type");
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

        readGeneralData(virtualSystem);
    }

    @Override
    protected void readLunDisk(XmlNode node, LunDisk lun) {
        lun.setDiskVmElements(Collections.singletonList(new DiskVmElement(lun.getId(), vmBase.getId())));
        LUNs luns = new LUNs();
        consumeReadXmlAttribute(node,
                OVF_PREFIX + COLON + LUN_DISCARD_ZEROES_DATA,
                val -> luns.setDiscardZeroesData(Boolean.parseBoolean(val)));
        consumeReadXmlAttribute(node,
                OVF_PREFIX + COLON + LUN_DISCARD_MAX_SIZE,
                val -> luns.setDiscardMaxSize(Long.parseLong(val)));
        consumeReadXmlAttribute(node, OVF_PREFIX + COLON + LUN_DEVICE_SIZE, val -> luns.setDeviceSize(Integer.parseInt(val)));
        consumeReadXmlAttribute(node, OVF_PREFIX + COLON + LUN_PRODUCT_ID, val -> luns.setProductId(val));
        consumeReadXmlAttribute(node, OVF_PREFIX + COLON + LUN_VENDOR_ID, val -> luns.setVendorId(val));
        consumeReadXmlAttribute(node, OVF_PREFIX + COLON + LUN_MAPPING, val -> luns.setLunMapping(Integer.parseInt(val)));
        consumeReadXmlAttribute(node, OVF_PREFIX + COLON + LUN_SERIAL, val -> luns.setSerial(val));
        consumeReadXmlAttribute(node, OVF_PREFIX + COLON + LUN_VOLUME_GROUP_ID, val -> luns.setVolumeGroupId(val));
        consumeReadXmlAttribute(node, OVF_PREFIX + COLON + LUN_ID, val -> luns.setLUNId(val));
        consumeReadXmlAttribute(node, OVF_PREFIX + COLON + LUN_PHYSICAL_VOLUME_ID, val -> luns.setPhysicalVolumeId(val));
        ArrayList<StorageServerConnections> lunConnections = new ArrayList<>();
        for (XmlNode connNode : selectNodes(node, LUN_CONNECTION)) {
            StorageServerConnections conn = new StorageServerConnections();
            consumeReadXmlAttribute(connNode, OVF_PREFIX + COLON + LUNS_CONNECTION, val -> conn.setConnection(val));
            consumeReadXmlAttribute(connNode, OVF_PREFIX + COLON + LUNS_IQN, val -> conn.setIqn(val));
            consumeReadXmlAttribute(connNode, OVF_PREFIX + COLON + LUNS_PORT, val -> conn.setPort(val));
            consumeReadXmlAttribute(connNode,
                    XSI_PREFIX + COLON + LUNS_STORAGE_TYPE,
                    val -> conn.setStorageType(StorageType.valueOf(val)));
            consumeReadXmlAttribute(connNode, XSI_PREFIX + COLON + LUNS_PORTAL, val -> conn.setPortal(val));
            // TODO: Username and password should be initilaized by the mapping file.
            // conn.setUsername(FromMap);
            // conn.setPassword(FromMap);
            lunConnections.add(conn);
        }
        luns.setLunConnections(lunConnections);
        lun.setLun(luns);
        DiskVmElement dve = lun.getDiskVmElementForVm(vmBase.getId());
        initGeneralDiskAttributes(node, lun, dve);
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

    @Override
    protected void buildFileReference() {
        XmlNodeList list = selectNodes(_document, "//*/File", _xmlNS);
        for (XmlNode node : list) {
            // If the disk storage type is Cinder then override the disk image with Cinder object, otherwise use the
            // disk image.
            DiskImage disk = new DiskImage();

            // If the OVF is old and does not contain any storage type reference then we assume we can only have disk
            // image.
            if (node.attributes.get("ovf:disk_storage_type") != null) {
                String diskStorageType = node.attributes.get("ovf:disk_storage_type").getValue();
                if (diskStorageType != null) {
                    if (diskStorageType.equals(DiskStorageType.LUN.name())) {
                        LunDisk lun = new LunDisk();
                        lun.setId(OvfParser.getImageGroupIdFromImageFile(node.attributes.get("ovf:href").getValue()));
                        luns.add(lun);
                        continue;
                    } else if (diskStorageType.equals(DiskStorageType.CINDER.name())) {
                        disk = new CinderDisk();
                        if (node.attributes.get("ovf:cinder_volume_type") != null) {
                            String cinderVolumeType = node.attributes.get("ovf:cinder_volume_type").getValue();
                            disk.setCinderVolumeType(cinderVolumeType);
                        }
                    }
                }
            }
            disk.setImageId(new Guid(node.attributes.get("ovf:id").getValue()));
            disk.setId(OvfParser.getImageGroupIdFromImageFile(node.attributes.get("ovf:href").getValue()));
            disk.setDescription(node.attributes.get("ovf:description").getValue());

            _images.add(disk);
        }
    }

    @Override
    public void buildDisk() {
        XmlNodeList list = selectNodes(_document, "//*/Section/Disk");
        for (XmlNode node : list) {
            Guid guid = new Guid(node.attributes.get("ovf:diskId").getValue());
            _images.stream().filter(d -> d.getImageId().equals(guid)).findFirst().ifPresent(img -> readDisk(node, img));
            luns.stream().filter(d -> d.getId().equals(guid)).findFirst().ifPresent(lun -> readLunDisk(node, lun));
        }
    }

    @Override
    protected void readDisk(XmlNode node, DiskImage image) {
        super.readDisk(node, image);

        if (!StringUtils.isEmpty(node.attributes.get("ovf:size").getValue())) {
            image.setSize(convertGigabyteToBytes(Long.parseLong(node.attributes.get("ovf:size").getValue())));
        }
        if (!StringUtils.isEmpty(node.attributes.get("ovf:actual_size").getValue())) {
            image.setActualSizeInBytes(
                    convertGigabyteToBytes(Long.parseLong(node.attributes.get("ovf:actual_size").getValue())));
        }
    }

    @Override
    protected VmNetworkInterface getNetworkInterface(XmlNode node) {
        // prior to 3.0 the instanceId is int , in 3.1 and on this is Guid
        String str = selectSingleNode(node, "rasd:InstanceId", _xmlNS).innerText;
        if (!StringUtils.isNumeric(str)) { // 3.1 and above OVF format
            final Guid guid = new Guid(str);
            VmNetworkInterface iface = interfaces.stream().filter(i -> i.getId().equals(guid)).findFirst().orElse(null);
            if (iface == null) {
                iface = new VmNetworkInterface();
                iface.setId(guid);
                interfaces.add(iface);
            }
            return iface;
        } else { // 3.0 and below OVF format
            VmNetworkInterface iface = new VmNetworkInterface();
            iface.setId(Guid.newGuid());
            interfaces.add(iface);
            return iface;
        }
    }

    @Override
    public void buildReference() {
        super.buildReference();
        buildNicReference();
    }

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
            int nicIdx = 0;
            for (XmlNode node : list) {
                VmNetworkInterface iface = new VmNetworkInterface();
                iface.setId(Guid.newGuid());
                updateSingleNic(node, iface, ++nicIdx);
                interfaces.add(iface);
            }
        }
    }

    protected void readOsSection(XmlNode section) {
        vmBase.setId(new Guid(section.attributes.get("ovf:id").getValue()));
        XmlNode node = selectSingleNode(section, "Description");
        if (node != null) {
            int osId = osRepository.getOsIdByUniqueName(node.innerText);
            vmBase.setOsId(osId);
            setClusterArch(osRepository.getArchitectureFromOS(osId));
        } else {
            setClusterArch(ArchitectureType.undefined);
        }
    }

    protected abstract void setClusterArch(ArchitectureType arch);

    @Override
    protected void readDiskImageItem(XmlNode node) {
        final Guid guid = new Guid(selectSingleNode(node, "rasd:InstanceId", _xmlNS).innerText);
        DiskImage image = _images.stream().filter(d -> d.getImageId().equals(guid)).findFirst().orElse(null);
        if (image == null) {
            return;
        }

        image.setId(OvfParser.getImageGroupIdFromImageFile(selectSingleNode(node,
                "rasd:HostResource",
                _xmlNS).innerText));
        if (StringUtils.isNotEmpty(selectSingleNode(node, "rasd:Parent", _xmlNS).innerText)) {
            image.setParentId(new Guid(selectSingleNode(node, "rasd:Parent", _xmlNS).innerText));
        }

        super.readDiskImageItem(node, image);
    }
}
