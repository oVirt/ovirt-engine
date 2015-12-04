package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;

public class OvfVmReader extends OvfReader {
    private static final String EXPORT_ONLY_PREFIX = "exportonly_";
    protected VM _vm;

    public OvfVmReader(XmlDocument document,
            VM vm,
            ArrayList<DiskImage> images,
            ArrayList<VmNetworkInterface> interfaces) {
        super(document, images, interfaces, vm.getStaticData());
        _vm = vm;
        _vm.setInterfaces(interfaces);
    }

    @Override
    protected void readOsSection(XmlNode section) {
        _vm.getStaticData().setId(new Guid(section.attributes.get("ovf:id").getValue()));
        XmlNode node = selectSingleNode(section, "Description");
        if (node != null) {
            int osId = osRepository.getOsIdByUniqueName(node.innerText);
            _vm.getStaticData().setOsId(osId);
            _vm.setClusterArch(osRepository.getArchitectureFromOS(osId));
        }
        else {
            _vm.setClusterArch(ArchitectureType.undefined);
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

        XmlNode storageNode = selectSingleNode(node, "rasd:StorageId", _xmlNS);
        if (storageNode != null &&
                StringUtils.isNotEmpty(storageNode.innerText)) {
            image.setStorageIds(new ArrayList<>(Arrays.asList(new Guid(storageNode.innerText))));
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
        final Date last_modified_date = OvfParser.utcDateStringToLocaDate(
                selectSingleNode(node, "rasd:last_modified_date", _xmlNS).innerText);
        if (last_modified_date != null) {
            image.setLastModifiedDate(last_modified_date);
        }
        VmDevice readDevice = readManagedVmDevice(node, image.getId());
        image.setPlugged(readDevice.getIsPlugged());
        image.setReadOnly(readDevice.getIsReadOnly());
    }

    @Override
    protected void updateSingleNic(XmlNode node, VmNetworkInterface iface) {
        super.updateSingleNic(node, iface);
        iface.setMacAddress((selectSingleNode(node, "rasd:MACAddress", _xmlNS) != null) ? selectSingleNode(node,
                "rasd:MACAddress", _xmlNS).innerText : "");
    }

    @Override
    protected void readGeneralData(XmlNode content) {
        // General Vm
        XmlNode node = selectSingleNode(content, OvfProperties.NAME);
        if (node != null) {
            _vm.getStaticData().setName(node.innerText);
            name = _vm.getStaticData().getName();
        }
        node = selectSingleNode(content, OvfProperties.TEMPLATE_ID);
        if (node != null) {
            if (StringUtils.isNotEmpty(node.innerText)) {
                _vm.getStaticData().setVmtGuid(new Guid(node.innerText));
            }
        }
        node = selectSingleNode(content, OvfProperties.TEMPLATE_NAME);
        if (node != null) {
            if (StringUtils.isNotEmpty(node.innerText)) {
                _vm.setVmtName(node.innerText);
            }
        }
        node = selectSingleNode(content, OvfProperties.INSTANCE_TYPE_ID);
        if (node != null) {
            if (StringUtils.isNotEmpty(node.innerText)) {
                _vm.setInstanceTypeId(new Guid(node.innerText));
            }
        }
        node = selectSingleNode(content, OvfProperties.IMAGE_TYPE_ID);
        if (node != null) {
            if (StringUtils.isNotEmpty(node.innerText)) {
                _vm.setImageTypeId(new Guid(node.innerText));
            }
        }
        node = selectSingleNode(content, OvfProperties.IS_INITIALIZED);
        if (node != null) {
            _vm.getStaticData().setInitialized(Boolean.parseBoolean(node.innerText));
        }
        node = selectSingleNode(content, OvfProperties.QUOTA_ID);
        if (node != null) {
            Guid quotaId = new Guid(node.innerText);
            if (!Guid.Empty.equals(quotaId)) {
                _vm.getStaticData().setQuotaId(quotaId);
            }
        }
        OvfLogEventHandler<VmStatic> handler = new VMStaticOvfLogHandler(_vm.getStaticData());

        // Gets a list of all the aliases of the fields that should be logged in
        // ovd For each one of these fields, the proper value will be read from
        // the ovf and field in vm static
        List<String> aliases = handler.getAliases();
        for (String alias : aliases) {
            String value = readEventLogValue(content, alias);
            if (StringUtils.isNotEmpty(value)) {
                handler.addValueForAlias(alias, value);

            }
        }

        // {@link VM#predefinedProperties} and {@link VM#userDefinedProperties}
        // are being set in the above alias handling, we need to update custom properties
        // to keep them consistent
        _vm.setCustomProperties(VmPropertiesUtils.getInstance().customProperties(_vm.getPredefinedProperties(), _vm.getUserDefinedProperties()));

        node = selectSingleNode(content, OvfProperties.APPLICATIONS_LIST);
        if (node != null) {
            if (StringUtils.isNotEmpty(node.innerText)) {
                _vm.setAppList(node.innerText);
            }
        }
        // if no app list in VM, get it from one of the leafs
        else if(_images != null && _images.size() > 0) {
            int root = getFirstImage(_images, _images.get(0));
            if (root != -1) {
                for(int i=0; i<_images.size(); i++) {
                    int x = getNextImage(_images, _images.get(i));
                    if (x == -1) {
                        _vm.setAppList(_images.get(i).getAppList());
                    }
                }
            } else {
                _vm.setAppList(_images.get(0).getAppList());
            }
        }
       node = selectSingleNode(content, OvfProperties.TRUSTED_SERVICE);
       if (node != null) {
           _vm.setTrustedService(Boolean.parseBoolean(node.innerText));
       }

        node = selectSingleNode(content, OvfProperties.ORIGINAL_TEMPLATE_ID);
        if (node != null) {
            _vm.getStaticData().setOriginalTemplateGuid(new Guid(node.innerText));
        }

        node = selectSingleNode(content, OvfProperties.ORIGINAL_TEMPLATE_NAME);
        if (node != null) {
            _vm.getStaticData().setOriginalTemplateName(node.innerText);
        }

        node = selectSingleNode(content, OvfProperties.USE_LATEST_VERSION);
        if (node != null) {
            _vm.setUseLatestVersion(Boolean.parseBoolean(node.innerText));
        }

        node = selectSingleNode(content, OvfProperties.USE_HOST_CPU);
        if (node != null) {
            _vm.setUseHostCpuFlags(Boolean.parseBoolean(node.innerText));
        }
    }

    @Override
    protected String getDefaultDisplayTypeStringRepresentation() {
        return OvfProperties.VM_DEFAULT_DISPLAY_TYPE;
    }

    // function returns the index of the image that has no parent
    private static int getFirstImage(ArrayList<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (curr.getParentId().equals(images.get(i).getImageId())) {
                return i;
            }
        }
        return -1;
    }

    // function returns the index of image that is it's child
    private static int getNextImage(ArrayList<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getParentId().equals(curr.getImageId())) {
                return i;
            }
        }
        return -1;
    }

    private String readEventLogValue(XmlNode content, String name) {
        StringBuilder fullNameSB = new StringBuilder(EXPORT_ONLY_PREFIX);
        fullNameSB.append(name);
        XmlNode node = selectSingleNode(content, fullNameSB.toString());
        if (node != null) {
            return node.innerText;
        }
        return null;
    }

    @Override
    protected void readSnapshotsSection(XmlNode section) {
        XmlNodeList list = selectNodes(section, "Snapshot");
        ArrayList<Snapshot> snapshots = new ArrayList<>();
        _vm.setSnapshots(snapshots);

        for (XmlNode node : list) {
            XmlNode vmConfiguration = selectSingleNode(node, "VmConfiguration", _xmlNS);
            Snapshot snapshot = new Snapshot(vmConfiguration != null);
            snapshot.setId(new Guid(node.attributes.get("ovf:id").getValue()));
            snapshot.setVmId(_vm.getId());
            snapshot.setType(SnapshotType.valueOf(selectSingleNode(node, "Type", _xmlNS).innerText));
            snapshot.setStatus(SnapshotStatus.OK);
            snapshot.setDescription(selectSingleNode(node, "Description", _xmlNS).innerText);
            XmlNode memory = selectSingleNode(node, "Memory", _xmlNS);
            if (memory != null) {
                snapshot.setMemoryVolume(memory.innerText);
            }

            final Date creationDate = OvfParser.utcDateStringToLocaDate(selectSingleNode(node, "CreationDate", _xmlNS).innerText);
            if (creationDate != null) {
                snapshot.setCreationDate(creationDate);
            }

            snapshot.setVmConfiguration(vmConfiguration == null
                    ? null : new String(Base64.decodeBase64(vmConfiguration.innerText)));

            XmlNode appList = selectSingleNode(node, "ApplicationList", _xmlNS);
            if (appList != null) {
                snapshot.setAppList(appList.innerText);
            }

            snapshots.add(snapshot);
        }
    }

    @Override
    protected void buildNicReference() {
    }
}
