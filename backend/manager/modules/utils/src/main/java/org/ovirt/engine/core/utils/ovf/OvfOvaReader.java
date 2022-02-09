package org.ovirt.engine.core.utils.ovf;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.xml.XmlAttribute;
import org.ovirt.engine.core.utils.ovf.xml.XmlAttributeCollection;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OvfOvaReader extends OvfReader {
    private static final Logger log = LoggerFactory.getLogger(OvfReader.class);

    private VmBase vm;
    /** Maps ovf:id to the other attributes of File reference */
    private Map<String, XmlAttributeCollection> fileIdToFileAttributes;

    public OvfOvaReader(XmlDocument document,
            FullEntityOvfData fullEntityOvfData,
            VmBase vm,
            OsRepository osRepository) {
        super(document,
                fullEntityOvfData.getDiskImages(),
                Collections.emptyList(),
                fullEntityOvfData.getInterfaces(),
                vm,
                osRepository);
        this.vm = vm;
        fileIdToFileAttributes = new HashMap<>();
    }

    @Override
    public void buildVirtualSystem() {
        XmlNode virtualSystem = selectSingleNode(_document, "//*/VirtualSystem");
        final String virtualSystemId = virtualSystem.attributes.get("ovf:id").getValue();
        consumeReadProperty(virtualSystem,
                NAME,
                val -> vm.setName(val),
                () -> vm.setName(virtualSystemId));
        try {
            vm.setId(Guid.createGuidFromString(virtualSystemId));
        } catch (Exception e) {
            vm.setId(Guid.newGuid());
        }
        XmlNode os = selectSingleNode(virtualSystem, "OperatingSystemSection");
        readOsSection(os);
        XmlNode hardware = selectSingleNode(virtualSystem, "VirtualHardwareSection");
        readHardwareSection(hardware);
        readGeneralData(virtualSystem);
    }

    @Override
    protected String getDefaultDisplayTypeStringRepresentation() {
        return VM_DEFAULT_DISPLAY_TYPE;
    }

    protected void readOsSection(XmlNode section) {
        XmlAttribute ovirtOsId = section.attributes.get("ovirt:id");
        int osId;
        if (ovirtOsId != null) {
            int parsedInt = Integer.parseInt(ovirtOsId.getValue());
            // map AlmaLinux 8+ that was dropped to Other Linux (kernel 4.x)
            osId = parsedInt == 1502 ? 33 : parsedInt;
        } else {
            osId = mapOsId(section.attributes.get("ovf:id").getValue());
        }
        vm.setOsId(osId);
        setClusterArch(osRepository.getArchitectureFromOS(osId));
    }

    protected int mapOsId(String ovfOsId) {
        switch (ovfOsId) {
        case "67": // Windows XP
        case "71": // Windows XP 63-Bit
        case "72": // Windows XP Embedded
            return 1; // Windows XP
        case "69":
            return 3; // Windows 2003
        case "70":
            return 10; // Windows 2003x64
        case "76":
            return 4; // Windows 2008
        case "77":
            return 16; // Windows 2008x64
        case "103":
            return 17; // Windows 2008R2x64
        case "105":
            return 11; // Windows 7
        case "114":
            return 20; // Windows 8
        case "115":
            return 21; // Windows 8x64
        case "113":
            return 23; // Windows 2012x64
        case "116":
            return 25; // Windows 2012R2x64
        case "120":
            return 26; // Windows 10
        case "121":
            return 27; // Windows 10x64
        case "117":
            return 29; // Windows 2016x64
        case "122":
            return 31; // Windows 2019x64
        case "125":
            return 35; // RedHat CoreOS
        case "42":
            return 1500; // FreeBSD
        case "78":
            return 1501; // FreeBSDx64
        case "36": // Linux
        case "79": // RedHat Enterprise Linux
        case "80": // RedHat Enterprise Linux x64
        case "82": // SUSE
        case "83": // SUSEx64
        case "84": // SLES
        case "85": // SLESx64
        case "89": // Mandriva
        case "90": // Mandrivax64
        case "91": // TurboLinux
        case "92": // TurboLinux x64
        case "93": // Ubuntu
        case "94": // Ubuntu x64
        case "95": // Debian
        case "96": // Debian x64
        case "97": // Linux 2.4.x
        case "98": // Linux 2.4.x x64
        case "99": // Linux 2.6.x
        case "100": // Linux 2.6.x x64
        case "101": // Linux x64
        case "106": // CentOS
        case "107": // CentOSx64
        case "108": // Oracle Linux
        case "109": // Oracle Linux x64
            return 5; // Other Linux
        default:
            return 0; // Other
        }
    }

    protected abstract void setClusterArch(ArchitectureType arch);

    protected void buildFileReference() {
        // TODO: read information needed for extracting the disks
        XmlNodeList list = selectNodes(_document, "//*/File", _xmlNS);
        for (XmlNode node : list) {
            fileIdToFileAttributes.put(node.attributes.get("ovf:id").getValue(), node.attributes);
        }
    }

    @Override
    public void buildDisk() {
        XmlNode diskSection = selectSingleNode(_document, "//*/DiskSection");
        XmlNodeList list = diskSection.selectNodes("Disk");
        for (XmlNode node : list) {
            readDisk(node, null);
        }
    }

    @Override
    protected void readDisk(XmlNode node, DiskImage image) {
        String diskId = node.attributes.get("ovf:diskId").getValue();
        String fileRef = node.attributes.get("ovf:fileRef").getValue();

        // If the disk storage type is Cinder then override the disk image with Cinder object,
        // otherwise use the disk image.
        image = new DiskImage();

        // If the OVF is old and does not contain any storage type reference then we assume we can only have disk
        // image.
        if (node.attributes.get("ovf:disk_storage_type") != null) {
            String diskStorageType = node.attributes.get("ovf:disk_storage_type").getValue();
            if (diskStorageType != null && diskStorageType.equals(DiskStorageType.CINDER.name())) {
                image = new CinderDisk();
                if (node.attributes.get("ovf:cinder_volume_type") != null) {
                    String cinderVolumeType = node.attributes.get("ovf:cinder_volume_type").getValue();
                    image.setCinderVolumeType(cinderVolumeType);
                }
            }
        }
        try {
            image.setImageId(new Guid(fileRef));
        } catch (Exception ex) {
            log.warn("could not retrieve volume id of {} from ovf, generating new guid", fileRef);
            image.setImageId(Guid.newGuid());
        }
        try {
            image.setId(new Guid(diskId));
        } catch (Exception ex) {
            log.warn("could not retrieve disk id of {} from ovf, generating new guid", diskId);
            image.setId(Guid.newGuid());
        }
        XmlAttribute description = node.attributes.get("ovf:description");
        image.setDescription(description != null ? description.getValue() : diskId);
        XmlAttribute capacity = node.attributes.get("ovf:capacity");
        XmlAttribute capacityUnits = node.attributes.get("ovf:capacityAllocationUnits");
        long virtualSize = Long.parseLong(capacity.getValue());
        if (capacityUnits != null) {
            virtualSize *= parseCapacityUnit(capacityUnits.getValue());
        }
        image.setSize(virtualSize);
        XmlAttribute populatedSize = node.attributes.get("ovf:populatedSize");
        if (populatedSize != null) {
            image.setActualSizeInBytes(Long.parseLong(populatedSize.getValue()));
        } else {
            XmlAttribute actualSize = fileIdToFileAttributes.get(fileRef).get("ovf:size");
            if (actualSize != null) {
                image.setActualSizeInBytes(Long.parseLong(actualSize.getValue()));
            } else {
                log.warn("didn't find disk provisioned size thus allocating the virtual size");
                image.setActualSizeInBytes(virtualSize);
            }
        }

        super.readDisk(node, image);

        if (StringUtils.isEmpty(image.getDiskAlias())) {
            image.setDiskAlias(diskId);
        }

        image.setRemotePath(fileIdToFileAttributes.get(fileRef).get("ovf:href").getValue());

        _images.add(image);
    }

    /**
     * Parse allocation units of the form "byte * x * y^z"
     * The format is defined in:
     * DSP0004: Common Information Model (CIM) Infrastructure,
     * ANNEX C.1 Programmatic Units
     *
     * We conform only to the subset of the format specification and
     * base-units must be bytes.
     */
    protected static long parseCapacityUnit(String capacityUnit) {
        String[] elements = capacityUnit.split("[*]");
        if (!elements[0].trim().equals("byte")) {
            log.error("Base-unit of the capacity unit must be in bytes, found: {}", capacityUnit);
            throw new IllegalArgumentException("Unsupported capacity unit, must be in bytes");
        }
        return Arrays.stream(elements).skip(1).map(String::trim).mapToLong(element -> {
            if (element.matches("\\d+")) {
                return Long.parseLong(element);
            }
            if (element.matches("\\d+\\^\\d+")) {
                int indexOfSeparator = element.indexOf('^');
                return (long) Math.pow(
                        Integer.parseInt(element.substring(0, indexOfSeparator)),
                        Integer.parseInt(element.substring(indexOfSeparator + 1)));
            }
            log.error("Unsupported capacity unit: {}", capacityUnit);
            throw new IllegalArgumentException("Unsupported capacity unit: " + capacityUnit);
        }).reduce(1, (a, b) -> a * b);
    }

    protected void readDiskImageItem(XmlNode node) {
        XmlNode hostResourceNode = selectSingleNode(node, "rasd:HostResource", _xmlNS);
        String hostResourceText = hostResourceNode.innerText;
        String diskId = hostResourceText.substring(hostResourceText.lastIndexOf('/') + 1);
        DiskImage image = _images.stream()
                .filter(d -> d.getDescription().equals(diskId))
                .findFirst()
                .orElse(null);

        if (image == null) {
            return;
        }

        super.readDiskImageItem(node, image);
    }

    @Override
    protected VmNetworkInterface getNetworkInterface(XmlNode node) {
        VmNetworkInterface iface = new VmNetworkInterface();
        iface.setId(Guid.newGuid());
        interfaces.add(iface);
        return iface;
    }

    @Override
    protected void updateSingleNic(XmlNode node, VmNetworkInterface iface, int nicIdx) {
        super.updateSingleNic(node, iface, nicIdx);
        XmlNode macNode = selectSingleNode(node, "rasd:MACAddress", _xmlNS);
        iface.setMacAddress(macNode != null ? macNode.innerText : null);
    }

    @Override
    protected VolumeType getDefaultVolumeType() {
        return VolumeType.Sparse;
    }
}
