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
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
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
        XmlNode node = section.SelectSingleNode("Description");
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

        XmlNode storageNode = node.SelectSingleNode("rasd:StorageId", _xmlNS);
        if (storageNode != null &&
                StringUtils.isNotEmpty(storageNode.innerText)) {
            image.setStorageIds(new ArrayList<Guid>(Arrays.asList(new Guid(storageNode.innerText))));
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
        final Date last_modified_date = OvfParser.UtcDateStringToLocaDate(
                node.SelectSingleNode("rasd:last_modified_date", _xmlNS).innerText);
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
        iface.setMacAddress((node.SelectSingleNode("rasd:MACAddress", _xmlNS) != null) ? node.SelectSingleNode(
                "rasd:MACAddress", _xmlNS).innerText : "");
    }

    @Override
    protected void readGeneralData(XmlNode content) {
        // General Vm
        XmlNode node = content.SelectSingleNode(OvfProperties.NAME);
        if (node != null) {
            _vm.getStaticData().setName(node.innerText);
            name = _vm.getStaticData().getName();
        }
        node = content.SelectSingleNode(OvfProperties.TEMPLATE_ID);
        if (node != null) {
            if (StringUtils.isNotEmpty(node.innerText)) {
                _vm.getStaticData().setVmtGuid(new Guid(node.innerText));
            }
        }
        node = content.SelectSingleNode(OvfProperties.TEMPLATE_NAME);
        if (node != null) {
            if (StringUtils.isNotEmpty(node.innerText)) {
                _vm.setVmtName(node.innerText);
            }
        }
        node = content.SelectSingleNode(OvfProperties.INSTANCE_TYPE_ID);
        if (node != null) {
            if (StringUtils.isNotEmpty(node.innerText)) {
                _vm.setInstanceTypeId(new Guid(node.innerText));
            }
        }
        node = content.SelectSingleNode(OvfProperties.IMAGE_TYPE_ID);
        if (node != null) {
            if (StringUtils.isNotEmpty(node.innerText)) {
                _vm.setImageTypeId(new Guid(node.innerText));
            }
        }
        node = content.SelectSingleNode(OvfProperties.IS_INITIALIZED);
        if (node != null) {
            _vm.getStaticData().setInitialized(Boolean.parseBoolean(node.innerText));
        }
        node = content.SelectSingleNode(OvfProperties.QUOTA_ID);
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

        node = content.SelectSingleNode(OvfProperties.APPLICATIONS_LIST);
        if (node != null) {
            if (StringUtils.isNotEmpty(node.innerText)) {
                _vm.setAppList(node.innerText);
            }
        }
        // if no app list in VM, get it from one of the leafs
        else if(_images != null && _images.size() > 0) {
            int root = GetFirstImage(_images, _images.get(0));
            if (root != -1) {
                for(int i=0; i<_images.size(); i++) {
                    int x = GetNextImage(_images, _images.get(i));
                    if (x == -1) {
                        _vm.setAppList(_images.get(i).getAppList());
                    }
                }
            } else {
                _vm.setAppList(_images.get(0).getAppList());
            }
        }
       node = content.SelectSingleNode(OvfProperties.TRUSTED_SERVICE);
       if (node != null) {
           _vm.setTrustedService(Boolean.parseBoolean(node.innerText));
       }

        node = content.SelectSingleNode(OvfProperties.ORIGINAL_TEMPLATE_ID);
        if (node != null) {
            _vm.getStaticData().setOriginalTemplateGuid(new Guid(node.innerText));
        }

        node = content.SelectSingleNode(OvfProperties.ORIGINAL_TEMPLATE_NAME);
        if (node != null) {
            _vm.getStaticData().setOriginalTemplateName(node.innerText);
        }

        node = content.SelectSingleNode(OvfProperties.USE_LATEST_VERSION);
        if (node != null) {
            _vm.setUseLatestVersion(Boolean.parseBoolean(node.innerText));
        }

        node = content.SelectSingleNode(OvfProperties.USE_HOST_CPU);
        if (node != null) {
            _vm.setUseHostCpuFlags(Boolean.parseBoolean(node.innerText));
        }
    }

    @Override
    protected String getDefaultDisplayTypeStringRepresentation() {
        return OvfProperties.VM_DEFAULT_DISPLAY_TYPE;
    }

    // function returns the index of the image that has no parent
    private static int GetFirstImage(ArrayList<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (curr.getParentId().equals(images.get(i).getImageId())) {
                return i;
            }
        }
        return -1;
    }

    // function returns the index of image that is it's child
    private static int GetNextImage(ArrayList<DiskImage> images, DiskImage curr) {
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
        XmlNode node = content.SelectSingleNode(fullNameSB.toString());
        if (node != null) {
            return node.innerText;
        }
        return null;
    }

    @Override
    protected void readSnapshotsSection(XmlNode section) {
        XmlNodeList list = section.SelectNodes("Snapshot");
        ArrayList<Snapshot> snapshots = new ArrayList<Snapshot>();
        _vm.setSnapshots(snapshots);

        for (XmlNode node : list) {
            XmlNode vmConfiguration = node.SelectSingleNode("VmConfiguration", _xmlNS);
            Snapshot snapshot = new Snapshot(vmConfiguration != null);
            snapshot.setId(new Guid(node.attributes.get("ovf:id").getValue()));
            snapshot.setVmId(_vm.getId());
            snapshot.setType(SnapshotType.valueOf(node.SelectSingleNode("Type", _xmlNS).innerText));
            snapshot.setStatus(SnapshotStatus.OK);
            snapshot.setDescription(node.SelectSingleNode("Description", _xmlNS).innerText);
            XmlNode memory = node.SelectSingleNode("Memory", _xmlNS);
            if (memory != null) {
                snapshot.setMemoryVolume(memory.innerText);
            }

            final Date creationDate = OvfParser.UtcDateStringToLocaDate(node.SelectSingleNode("CreationDate", _xmlNS).innerText);
            if (creationDate != null) {
                snapshot.setCreationDate(creationDate);
            }

            snapshot.setVmConfiguration(vmConfiguration == null
                    ? null : new String(Base64.decodeBase64(vmConfiguration.innerText)));

            XmlNode appList = node.SelectSingleNode("ApplicationList", _xmlNS);
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
