package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;

public abstract class OvfOvirtReader extends OvfReader {

    private static final String COLON = ":";
    protected FullEntityOvfData fullEntityOvfData;

    public OvfOvirtReader(XmlDocument document,
            FullEntityOvfData fullEntityOvfData,
            OsRepository osRepository) {
        super(document,
                fullEntityOvfData.getDiskImages(),
                fullEntityOvfData.getLunDisks(),
                fullEntityOvfData.getInterfaces(),
                fullEntityOvfData.getVmBase(),
                osRepository);
        this.fullEntityOvfData = fullEntityOvfData;
    }

    @Override
    protected void readHeader(XmlNode header) {
        super.readHeader(header);
        setVersion(header != null ? header.attributes.get("ovf:version").getValue() : "");
    }

    @Override
    public void buildVirtualSystem() {
        XmlNode virtualSystem = selectSingleNode(_document, "//*/Content");
        consumeReadProperty(virtualSystem, NAME, val -> fullEntityOvfData.getVmBase().setName(val));

        // set ovf version to the ovf object
        fullEntityOvfData.getVmBase().setOvfVersion(getVersion());

        XmlNodeList list = selectNodes(virtualSystem, "Section");
        if (list != null) {
            // The Os need to be read before the hardware
            XmlNode node = getNode(list, "xsi:type", "ovf:OperatingSystemSection_Type");
            if (node != null) {
                readOsSection(node);
            }

            node = getNode(list, "xsi:type", "ovf:VirtualHardwareSection_Type");
            if (node != null) {
                readHardwareSection(node);
            }

            node = getNode(list, "xsi:type", "ovf:SnapshotsSection_Type");
            if (node != null) {
                readSnapshotsSection(node);
            }

            node = getNode(list, "xsi:type", "ovf:AffinityGroupsSection_Type");
            if (node != null) {
                readAffinityGroupsSection(node);
            }

            node = getNode(list, "xsi:type", "ovf:AffinityLabelsSection_Type");
            if (node != null) {
                readAffinityLabelsSection(node);
            }

            node = getNode(list, "xsi:type", "ovf:UserDomainsSection_Type");
            if (node != null) {
                readUserDomainsSection(node);
            }

            node = getNode(list, "xsi:type", "ovf:VmExternalDataSection_Type");
            if (node != null) {
                readExternalDataSection(node);
            }

            node = getNode(list, "xsi:type", "ovf:NumaNodeSection_Type");
            if (node != null) {
                readNumaNodeListSection(node);
            }
        }

        readGeneralData(virtualSystem);
    }

    protected void readGeneralData(XmlNode content) {
        super.readGeneralData(content);
        consumeReadProperty(content, CLUSTER_NAME, val -> fullEntityOvfData.setClusterName(val));
    }

    protected List<Integer> readIntegerList(XmlNode node, String label) {
        List<Integer> integerList = new ArrayList<>();
        XmlNode xmlNode = selectSingleNode(node, label, _xmlNS);
        if (xmlNode != null) {
            String valueList = xmlNode.innerText;
            if (valueList != null && !valueList.isEmpty()) {
                String[] values = valueList.split(",");
                for (String value : values) {
                    integerList.add(Integer.valueOf(value));
                }
            }
        }
        return integerList;
    }


    @Override
    protected void readLunDisk(XmlNode node, LunDisk lun) {
        lun.setDiskVmElements(Collections.singletonList(new DiskVmElement(lun.getId(), fullEntityOvfData.getVmBase().getId())));
        LUNs luns = new LUNs();
        consumeReadXmlAttribute(node, OVF_PREFIX + COLON + LUN_ID, val -> luns.setLUNId(val));
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
            lunConnections.add(conn);
        }
        luns.setLunConnections(lunConnections);
        lun.setLun(luns);
        DiskVmElement dve = lun.getDiskVmElementForVm(fullEntityOvfData.getVmBase().getId());
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

    protected void readAffinityGroupsSection(@SuppressWarnings("unused") XmlNode section) {
        // The affinity group section only has meaning for VMs, and is overridden in OvfVmReader.
    }

    protected void readAffinityLabelsSection(@SuppressWarnings("unused") XmlNode section) {
        // The affinity label section only has meaning for VMs, and is overridden in OvfVmReader.
    }

    protected void readNumaNodeListSection(@SuppressWarnings("unused") XmlNode section) {
        // The numa node list label section only has meaning for VMs, and is overridden in OvfVmReader.
    }


    protected void readUserDomainsSection(@SuppressWarnings("unused") XmlNode section) {
        XmlNodeList list = selectNodes(section, OvfProperties.USER);
        Set<DbUser> dbUsers = new HashSet<>();
        Map<String, Set<String>> userToRoles = new HashMap<>();
        for (XmlNode node : list) {
            String userDomain =
                    selectSingleNode(node, OvfProperties.USER_DOMAIN, _xmlNS).innerText;
            DbUser dbUser = new DbUser();
            dbUser.setLoginName(userDomain.split("@")[0]);
            dbUser.setDomain(userDomain.split("@")[1]);
            dbUsers.add(dbUser);
            XmlNode rolesElement = selectSingleNode(node, OvfProperties.USER_ROLES);
            XmlNodeList roleNodes = selectNodes(rolesElement, OvfProperties.ROLE_NAME);
            Set<String> roleNames = new HashSet<>();
            for (XmlNode roleNode : roleNodes) {
                String roleName = roleNode.innerText;
                roleNames.add(roleName);
            }
            userToRoles.put(dbUser.getLoginName(), roleNames);
        }

        fullEntityOvfData.setDbUsers(dbUsers);
        fullEntityOvfData.setUserToRoles(userToRoles);
    }

    private void readExternalDataSection(@SuppressWarnings("unused") XmlNode section) {
        Map<VmExternalDataKind, String> vmExternalData = fullEntityOvfData.getVmExternalData();
        XmlNodeList list = selectNodes(section, OvfProperties.VM_EXTERNAL_DATA_ITEM);
        for (XmlNode node : list) {
            String kind = node.attributes.get(OvfProperties.VM_EXTERNAL_DATA_KIND).getValue();
            String data = selectSingleNode(node, OvfProperties.VM_EXTERNAL_DATA_CONTENT).innerText;
            vmExternalData.put(VmExternalDataKind.fromExternal(kind), data);
        }
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
                    } else if (diskStorageType.equals(DiskStorageType.MANAGED_BLOCK_STORAGE.name())) {
                        disk = new ManagedBlockStorageDisk();
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
        String str = selectSingleNode(node, VMD_ID, _xmlNS).innerText;
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
        fullEntityOvfData.getVmBase().setId(new Guid(section.attributes.get("ovf:id").getValue()));
        XmlNode node = selectSingleNode(section, "Description");
        if (node != null) {
            int osId = osRepository.getOsIdByUniqueName(node.innerText);
            if ("Alma Linux 8+".equals(node.innerText)) {
                // map AlmaLinux 8+ that was dropped to Other Linux (kernel 4.x)
                osId = 33;
            }
            fullEntityOvfData.getVmBase().setOsId(osId);
            setClusterArch(osRepository.getArchitectureFromOS(osId));
        } else {
            setClusterArch(ArchitectureType.undefined);
        }
    }

    protected abstract void setClusterArch(ArchitectureType arch);

    @Override
    protected void readDiskImageItem(XmlNode node) {
        final Guid guid = new Guid(selectSingleNode(node, VMD_ID, _xmlNS).innerText);
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

    @Override
    protected String adjustHardwareResourceType(String resourceType) {
        switch(resourceType) {
        case OvfHardware.OVIRT_Monitor:
            return OvfHardware.Monitor;
        case OvfHardware.OVIRT_Graphics:
            return OvfHardware.Graphics;
        default:
            return super.adjustHardwareResourceType(resourceType);
        }
    }
}
