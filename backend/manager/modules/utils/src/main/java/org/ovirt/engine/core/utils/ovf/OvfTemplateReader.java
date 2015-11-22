package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;

public class OvfTemplateReader extends OvfReader {
    protected VmTemplate _vmTemplate;
    private final OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);

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
        XmlNode node = selectSingleNode(section, "Description");
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
    protected void readDiskImageItem(XmlNode node) {
        final Guid guid = new Guid(selectSingleNode(node, "rasd:InstanceId", _xmlNS).innerText);

        DiskImage image = _images.stream().filter(d -> d.getImageId().equals(guid)).findFirst().orElse(null);
        image.setId(OvfParser.getImageGroupIdFromImageFile(selectSingleNode(node,
                "rasd:HostResource", _xmlNS).innerText));
        if (StringUtils.isNotEmpty(selectSingleNode(node, "rasd:Parent", _xmlNS).innerText)) {
            image.setParentId(new Guid(selectSingleNode(node, "rasd:Parent", _xmlNS).innerText));
        }
        if (StringUtils.isNotEmpty(selectSingleNode(node, "rasd:Template", _xmlNS).innerText)) {
            image.setImageTemplateId(new Guid(selectSingleNode(node, "rasd:Template", _xmlNS).innerText));
        }
        image.setAppList(selectSingleNode(node, "rasd:ApplicationList", _xmlNS).innerText);
        if (StringUtils.isNotEmpty(selectSingleNode(node, "rasd:StorageId", _xmlNS).innerText)) {
            image.setStorageIds(new ArrayList<>(Arrays.asList(new Guid(selectSingleNode(node, "rasd:StorageId",
                    _xmlNS).innerText))));
        }
        if (StringUtils.isNotEmpty(selectSingleNode(node, "rasd:StoragePoolId", _xmlNS).innerText)) {
            image.setStoragePoolId(new Guid(selectSingleNode(node, "rasd:StoragePoolId", _xmlNS).innerText));
        }
        final Date creationDate = OvfParser.utcDateStringToLocaDate(
                selectSingleNode(node, "rasd:CreationDate", _xmlNS).innerText);
        if (creationDate != null) {
            image.setCreationDate(creationDate);
        }
        final Date lastModified = OvfParser.utcDateStringToLocaDate(
                selectSingleNode(node, "rasd:LastModified", _xmlNS).innerText);
        if (lastModified != null) {
            image.setLastModified(lastModified);
        }
        readManagedVmDevice(node, image.getId());
    }

    @Override
    protected void readGeneralData(XmlNode content) {
        // General Vm
        XmlNode node = selectSingleNode(content, OvfProperties.NAME);
        if (node != null) {
            _vmTemplate.setName(node.innerText);
            name = _vmTemplate.getName();
        }
        node = selectSingleNode(content, OvfProperties.TEMPLATE_ID);
        if (node != null) {
            if (StringUtils.isNotEmpty(node.innerText)) {
                _vmTemplate.setId(new Guid(node.innerText));
            }
        }

        node = selectSingleNode(content, OvfProperties.IS_DISABLED);
        if (node != null) {
            _vmTemplate.setDisabled(Boolean.parseBoolean(node.innerText));
        }

        node = selectSingleNode(content, OvfProperties.TRUSTED_SERVICE);
        if (node != null) {
            _vmTemplate.setTrustedService(Boolean.parseBoolean(node.innerText));
        }

        node = selectSingleNode(content, OvfProperties.TEMPLATE_TYPE);
        if (node != null) {
            _vmTemplate.setTemplateType(VmEntityType.valueOf(node.innerText));
        }

        node = selectSingleNode(content, OvfProperties.BASE_TEMPLATE_ID);
        if (node != null) {
            _vmTemplate.setBaseTemplateId(Guid.createGuidFromString(node.innerText));
        } else {
            // in case base template is missing, we assume it is a base template
            _vmTemplate.setBaseTemplateId(_vmTemplate.getId());
        }

        node = selectSingleNode(content, OvfProperties.TEMPLATE_VERSION_NUMBER);
        if (node != null) {
            _vmTemplate.setTemplateVersionNumber(Integer.parseInt(node.innerText));
        }

        node = selectSingleNode(content, OvfProperties.TEMPLATE_VERSION_NAME);
        if (node != null) {
            _vmTemplate.setTemplateVersionName(node.innerText);
        }

        node = selectSingleNode(content, "AutoStartup");
        if (node != null) {
            _vmTemplate.setAutoStartup(Boolean.parseBoolean(node.innerText));
        }
    }

    @Override
    protected String getDefaultDisplayTypeStringRepresentation() {
        return OvfProperties.TEMPLATE_DEFAULT_DISPLAY_TYPE;
    }
}
