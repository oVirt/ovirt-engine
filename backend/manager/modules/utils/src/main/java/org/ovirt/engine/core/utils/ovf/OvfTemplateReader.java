package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.compat.backendcompat.XmlNode;
import org.ovirt.engine.core.compat.backendcompat.XmlNodeList;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class OvfTemplateReader extends OvfReader {
    protected VmTemplate _vmTemplate;
    private final OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);

    public OvfTemplateReader(XmlDocument document,
            VmTemplate vmTemplate,
            ArrayList<DiskImage> images,
            ArrayList<VmNetworkInterface> interfaces) {
        super(document, images, interfaces, vmTemplate);
        _vmTemplate = vmTemplate;
    }

    @Override
    protected void readOsSection(XmlNode section) {
        _vmTemplate.setId(new Guid(section.attributes.get("ovf:id").getValue()));
        XmlNode node = section.SelectSingleNode("Description");
        if (node != null) {
            int osId = osRepository.getOsIdByUniqueName(node.innerText);
            _vmTemplate.setOsId(osId);
            _vmTemplate.setClusterArch(osRepository.getArchitectureFromOS(osId));
        }
        else {
            _vmTemplate.setClusterArch(ArchitectureType.undefined);
        }
    }

    @Override
    protected void readHardwareSection(XmlNode section) {
        boolean readVirtioSerial = false;
        XmlNodeList list = section.SelectNodes("Item");
        for (XmlNode node : list) {
            int resourceType = Integer.parseInt(node.SelectSingleNode("rasd:ResourceType", _xmlNS).innerText);

            switch (resourceType) {
            // CPU
            case 3:
                _vmTemplate
                        .setNumOfSockets(Integer.parseInt(node.SelectSingleNode("rasd:num_of_sockets", _xmlNS).innerText));
                _vmTemplate
                        .setCpuPerSocket(Integer.parseInt(node.SelectSingleNode("rasd:cpu_per_socket", _xmlNS).innerText));
                break;

            // Memory
            case 4:
                _vmTemplate
                        .setMemSizeMb(Integer.parseInt(node.SelectSingleNode("rasd:VirtualQuantity", _xmlNS).innerText));
                break;

            // Image
            case 17:
                final Guid guid = new Guid(node.SelectSingleNode("rasd:InstanceId", _xmlNS).innerText);

                DiskImage image = LinqUtils.firstOrNull(_images, new Predicate<DiskImage>() {
                    @Override
                    public boolean eval(DiskImage diskImage) {
                        return diskImage.getImageId().equals(guid);
                    }
                });
                image.setId(OvfParser.GetImageGrupIdFromImageFile(node.SelectSingleNode(
                        "rasd:HostResource", _xmlNS).innerText));
                if (StringUtils.isNotEmpty(node.SelectSingleNode("rasd:Parent", _xmlNS).innerText)) {
                    image.setParentId(new Guid(node.SelectSingleNode("rasd:Parent", _xmlNS).innerText));
                }
                if (StringUtils.isNotEmpty(node.SelectSingleNode("rasd:Template", _xmlNS).innerText)) {
                    image.setImageTemplateId(new Guid(node.SelectSingleNode("rasd:Template", _xmlNS).innerText));
                }
                image.setAppList(node.SelectSingleNode("rasd:ApplicationList", _xmlNS).innerText);
                if (StringUtils.isNotEmpty(node.SelectSingleNode("rasd:StorageId", _xmlNS).innerText)) {
                    image.setStorageIds(new ArrayList<Guid>(Arrays.asList(new Guid(node.SelectSingleNode("rasd:StorageId",
                            _xmlNS).innerText))));
                }
                if (StringUtils.isNotEmpty(node.SelectSingleNode("rasd:StoragePoolId", _xmlNS).innerText)) {
                    image.setStoragePoolId(new Guid(node.SelectSingleNode("rasd:StoragePoolId", _xmlNS).innerText));
                }
                final Date creationDate = OvfParser.UtcDateStringToLocaDate(
                        node.SelectSingleNode("rasd:CreationDate", _xmlNS).innerText);
                if (creationDate != null) {
                    image.setCreationDate(creationDate);
                }
                final Date lastModified = OvfParser.UtcDateStringToLocaDate(
                        node.SelectSingleNode("rasd:LastModified", _xmlNS).innerText);
                if (lastModified != null) {
                    image.setLastModified(lastModified);
                }
                readVmDevice(node, _vmTemplate, image.getId(), Boolean.TRUE);
                break;

            // Network
            case 10:
                VmNetworkInterface iface = getNetwotkInterface(node);
                if (StringUtils.isNotEmpty(node.SelectSingleNode("rasd:ResourceSubType", _xmlNS).innerText)) {
                    iface.setType(Integer.parseInt(node.SelectSingleNode("rasd:ResourceSubType", _xmlNS).innerText));
                }

                String resourceSubNetworkName = node.SelectSingleNode(OvfProperties.VMD_CONNECTION, _xmlNS).innerText;
                iface.setNetworkName(StringUtils.defaultIfEmpty(resourceSubNetworkName, null));

                XmlNode vnicProfileNameNode = node.SelectSingleNode(OvfProperties.VMD_VNIC_PROFILE_NAME, _xmlNS);
                iface.setVnicProfileName(vnicProfileNameNode == null ? null
                        : StringUtils.defaultIfEmpty(vnicProfileNameNode.innerText, null));

                XmlNode linkedNode = node.SelectSingleNode(OvfProperties.VMD_LINKED, _xmlNS);
                iface.setLinked(linkedNode == null ? true : Boolean.valueOf(linkedNode.innerText));
                iface.setName(node.SelectSingleNode("rasd:Name", _xmlNS).innerText);
                iface.setSpeed((node.SelectSingleNode("rasd:speed", _xmlNS) != null) ? Integer
                        .parseInt(node.SelectSingleNode("rasd:speed", _xmlNS).innerText)
                        : VmInterfaceType.forValue(iface.getType()).getSpeed());
                _vmTemplate.getInterfaces().add(iface);
                readVmDevice(node, _vmTemplate, iface.getId(), Boolean.TRUE);
                break;
            // CDROM
            case 15:
                readVmDevice(node, _vmTemplate, Guid.newGuid(), Boolean.TRUE);
                break;
            // USB
            case 23:
                _vmTemplate.setUsbPolicy(UsbPolicy.forStringValue(node.SelectSingleNode("rasd:UsbPolicy", _xmlNS).innerText));
                break;

            // Monitor
            case 20:
                _vmTemplate
                        .setNumOfMonitors(Integer.parseInt(node.SelectSingleNode("rasd:VirtualQuantity", _xmlNS).innerText));
                if (node.SelectSingleNode("rasd:SinglePciQxl", _xmlNS) != null) {
                    _vmTemplate.setSingleQxlPci(Boolean.parseBoolean(node.SelectSingleNode("rasd:SinglePciQxl", _xmlNS).innerText));
                }
                readVmDevice(node, _vmTemplate, Guid.newGuid(), Boolean.TRUE);
                break;
            // OTHER
            case 0:
                boolean addAsManaged = false;
                if (node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS) != null
                        && StringUtils.isNotEmpty(node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS).innerText)) {
                    VmDeviceGeneralType type = VmDeviceGeneralType.forValue(node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS).innerText);
                    String device = node.SelectSingleNode(OvfProperties.VMD_DEVICE, _xmlNS).innerText;
                    // special devices are treated as managed devices but still have the OTHER OVF ResourceType
                    addAsManaged = VmDeviceCommonUtils.isSpecialDevice(device, type);
                }
                VmDevice vmDevice = readVmDevice(node, _vmTemplate, Guid.newGuid(), addAsManaged);
                readVirtioSerial = readVirtioSerial ||
                        VmDeviceType.VIRTIOSERIAL.getName().equals(vmDevice.getDevice());
                break;

            }
        }

        if (!readVirtioSerial) {
            addManagedVmDevice(VmDeviceCommonUtils.createVirtioSerialDeviceForVm(_vmTemplate.getId()));
        }
    }

    @Override
    protected void readGeneralData(XmlNode content) {
        // General Vm
        XmlNode node = content.SelectSingleNode(OvfProperties.NAME);
        if (node != null) {
            _vmTemplate.setName(node.innerText);
            name = _vmTemplate.getName();
        }
        node = content.SelectSingleNode(OvfProperties.TEMPLATE_ID);
        if (node != null) {
            if (StringUtils.isNotEmpty(node.innerText)) {
                _vmTemplate.setId(new Guid(node.innerText));
            }
        }

        node = content.SelectSingleNode(OvfProperties.IS_DISABLED);
        if (node != null) {
            _vmTemplate.setDisabled(Boolean.parseBoolean(node.innerText));
        }

        node = content.SelectSingleNode(OvfProperties.TRUSTED_SERVICE);
        if (node != null) {
            _vmTemplate.setTrustedService(Boolean.parseBoolean(node.innerText));
        }

        node = content.SelectSingleNode(OvfProperties.TEMPLATE_TYPE);
        if (node != null) {
            _vmTemplate.setTemplateType(VmEntityType.valueOf(node.innerText));
        }

        node = content.SelectSingleNode(OvfProperties.BASE_TEMPLATE_ID);
        if (node != null) {
            _vmTemplate.setBaseTemplateId(Guid.createGuidFromString(node.innerText));
        } else {
            // in case base template is missing, we assume it is a base template
            _vmTemplate.setBaseTemplateId(_vmTemplate.getId());
        }

        node = content.SelectSingleNode(OvfProperties.TEMPLATE_VERSION_NUMBER);
        if (node != null) {
            _vmTemplate.setTemplateVersionNumber(Integer.parseInt(node.innerText));
        }

        node = content.SelectSingleNode(OvfProperties.TEMPLATE_VERSION_NAME);
        if (node != null) {
            _vmTemplate.setTemplateVersionName(node.innerText);
        }

        node = content.SelectSingleNode("AutoStartup");
        if (node != null) {
            _vmTemplate.setAutoStartup(Boolean.parseBoolean(node.innerText));
        }
    }

    @Override
    protected String getDefaultDisplayTypeStringRepresentation() {
        return OvfProperties.TEMPLATE_DEFAULT_DISPLAY_TYPE;
    }
}
