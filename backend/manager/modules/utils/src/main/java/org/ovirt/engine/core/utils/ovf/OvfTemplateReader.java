package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;

public class OvfTemplateReader extends OvfReader {
    protected VmTemplate _vmTemplate;

    public OvfTemplateReader(XmlDocument document,
            VmTemplate vmTemplate,
            List<DiskImage> images,
            List<VmNetworkInterface> interfaces,
            OsRepository osRepository) {
        super(document, images, interfaces, vmTemplate, osRepository);
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
        } else {
            _vmTemplate.setClusterArch(ArchitectureType.undefined);
        }
    }

    @Override
    protected void readDiskImageItem(XmlNode node) {
        final Guid guid = new Guid(selectSingleNode(node, "rasd:InstanceId", _xmlNS).innerText);

        DiskImage image = _images.stream().filter(d -> d.getImageId().equals(guid)).findFirst().orElse(null);
        image.setId(OvfParser.getImageGroupIdFromImageFile(selectSingleNode(node,
                "rasd:HostResource",
                _xmlNS).innerText));
        if (StringUtils.isNotEmpty(selectSingleNode(node, "rasd:Parent", _xmlNS).innerText)) {
            image.setParentId(new Guid(selectSingleNode(node, "rasd:Parent", _xmlNS).innerText));
        }
        if (StringUtils.isNotEmpty(selectSingleNode(node, "rasd:Template", _xmlNS).innerText)) {
            image.setImageTemplateId(new Guid(selectSingleNode(node, "rasd:Template", _xmlNS).innerText));
        }
        image.setAppList(selectSingleNode(node, "rasd:ApplicationList", _xmlNS).innerText);
        if (StringUtils.isNotEmpty(selectSingleNode(node, "rasd:StorageId", _xmlNS).innerText)) {
            image.setStorageIds(new ArrayList<>(Arrays.asList(new Guid(selectSingleNode(node,
                    "rasd:StorageId",
                    _xmlNS).innerText))));
        }
        if (StringUtils.isNotEmpty(selectSingleNode(node, "rasd:StoragePoolId", _xmlNS).innerText)) {
            image.setStoragePoolId(new Guid(selectSingleNode(node, "rasd:StoragePoolId", _xmlNS).innerText));
        }
        final Date creationDate = OvfParser.utcDateStringToLocalDate(
                selectSingleNode(node, "rasd:CreationDate", _xmlNS).innerText);
        if (creationDate != null) {
            image.setCreationDate(creationDate);
        }
        final Date lastModified = OvfParser.utcDateStringToLocalDate(
                selectSingleNode(node, "rasd:LastModified", _xmlNS).innerText);
        if (lastModified != null) {
            image.setLastModified(lastModified);
        }
        readManagedVmDevice(node, image.getId());
    }

    @Override
    protected void readGeneralData(XmlNode content) {
        // General Vm
        consumeReadProperty(content, NAME, val -> {
            _vmTemplate.setName(val);
            name = val;
        });
        consumeReadProperty(content, TEMPLATE_ID, val -> _vmTemplate.setId(new Guid(val)));
        consumeReadProperty(content, IS_DISABLED, val -> _vmTemplate.setDisabled(Boolean.parseBoolean(val)));
        consumeReadProperty(content, TRUSTED_SERVICE, val -> _vmTemplate.setTrustedService(Boolean.parseBoolean(val)));
        consumeReadProperty(content, TEMPLATE_TYPE, val -> _vmTemplate.setTemplateType(VmEntityType.valueOf(val)));
        consumeReadProperty(content,
                BASE_TEMPLATE_ID,
                val -> _vmTemplate.setBaseTemplateId(Guid.createGuidFromString(val)),
                () -> {
                    // in case base template is missing, we assume it is a base template
                    _vmTemplate.setBaseTemplateId(_vmTemplate.getId());
                });
        consumeReadProperty(content,
                TEMPLATE_VERSION_NUMBER,
                val -> _vmTemplate.setTemplateVersionNumber(Integer.parseInt(val)));
        consumeReadProperty(content, TEMPLATE_VERSION_NAME, val -> _vmTemplate.setTemplateVersionName(val));
        consumeReadProperty(content, AUTO_STARTUP, val -> _vmTemplate.setAutoStartup(Boolean.parseBoolean(val)));
    }

    @Override
    protected String getDefaultDisplayTypeStringRepresentation() {
        return TEMPLATE_DEFAULT_DISPLAY_TYPE;
    }
}
