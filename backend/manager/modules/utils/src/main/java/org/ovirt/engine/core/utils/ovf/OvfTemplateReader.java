package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.compat.backendcompat.XmlNode;
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
    protected void readMonitorItem(XmlNode node) {
        super.readMonitorItem(node);
        readManagedVmDevice(node, Guid.newGuid());
    }

    @Override
    protected void readDiskImageItem(XmlNode node) {
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
        readManagedVmDevice(node, image.getId());
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
