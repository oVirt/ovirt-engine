package org.ovirt.engine.core.utils.ovf;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;

public abstract class OvfOvirtReader extends OvfReader {

    private VmBase vmBase;

    public OvfOvirtReader(XmlDocument document,
            List<DiskImage> images,
            List<VmNetworkInterface> interfaces,
            VmBase vmBase,
            OsRepository osRepository) {
        super(document, images, interfaces, vmBase, osRepository);
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
                if (diskStorageType != null && diskStorageType.equals(DiskStorageType.CINDER.name())) {
                    disk = new CinderDisk();
                    if (node.attributes.get("ovf:cinder_volume_type") != null) {
                        String cinderVolumeType = node.attributes.get("ovf:cinder_volume_type").getValue();
                        disk.setCinderVolumeType(cinderVolumeType);
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
            }
            return iface;
        } else { // 3.0 and below OVF format
            return new VmNetworkInterface();
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
            for (XmlNode node : list) {
                VmNetworkInterface iface = new VmNetworkInterface();
                iface.setId(Guid.newGuid());
                updateSingleNic(node, iface);
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
