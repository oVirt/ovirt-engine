package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
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
    protected void readHardwareSection(XmlNode section) {
        boolean readVirtioSerial = false;
        for (XmlNode node : section.SelectNodes("Item")) {

            switch (node.SelectSingleNode("rasd:ResourceType", _xmlNS).innerText) {
            case OvfHardware.CPU:
                readCpuItem(node);
                break;

            case OvfHardware.Memory:
                readMemoryItem(node);
                break;

            case OvfHardware.DiskImage:
                readDiskImageItem(node);
                break;

            case OvfHardware.Network:
                readNetworkItem(node);
                break;

            case OvfHardware.USB:
                readUsbItem(node);
                break;

            case OvfHardware.Monitor:
                readMonitorItem(node);
                break;

            case OvfHardware.CD:
                readCdItem(node);
                break;

            case OvfHardware.OTHER:
                VmDevice vmDevice = readOtherHardwareItem(node);
                readVirtioSerial = readVirtioSerial ||
                        VmDeviceType.VIRTIOSERIAL.getName().equals(vmDevice.getDevice());
                break;
            }
        }

        if (!readVirtioSerial) {
            addManagedVmDevice(VmDeviceCommonUtils.createVirtioSerialDeviceForVm(_vm.getId()));
        }
    }

    private void readCpuItem(XmlNode node) {
        _vm.getStaticData().setNumOfSockets(
                Integer.parseInt(node.SelectSingleNode("rasd:num_of_sockets", _xmlNS).innerText));
        _vm.getStaticData().setCpuPerSocket(
                Integer.parseInt(node.SelectSingleNode("rasd:cpu_per_socket", _xmlNS).innerText));
    }

    private void readMemoryItem(XmlNode node) {
        _vm.getStaticData().setMemSizeMb(
                Integer.parseInt(node.SelectSingleNode("rasd:VirtualQuantity", _xmlNS).innerText));
    }

    private void readDiskImageItem(XmlNode node) {
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
        VmDevice readDevice = readVmDevice(node, _vm.getStaticData(), image.getId(), Boolean.TRUE);
        image.setPlugged(readDevice.getIsPlugged());
        image.setReadOnly(readDevice.getIsReadOnly());
    }

    private void readNetworkItem(XmlNode node) {
        VmNetworkInterface iface = getNetwotkInterface(node);
        updateSingleNic(node, iface);
        _vm.getInterfaces().add(iface);
        readVmDevice(node, _vm.getStaticData(), iface.getId(), Boolean.TRUE);
    }

    private void readUsbItem(XmlNode node) {
        _vm.getStaticData().setUsbPolicy(
                UsbPolicy.forStringValue(node.SelectSingleNode("rasd:UsbPolicy", _xmlNS).innerText));
    }

    private void readMonitorItem(XmlNode node) {
        _vm.getStaticData().setNumOfMonitors(
                Integer.parseInt(node.SelectSingleNode("rasd:VirtualQuantity", _xmlNS).innerText));
        if (node.SelectSingleNode("rasd:SinglePciQxl", _xmlNS) != null) {
            _vm.setSingleQxlPci(Boolean.parseBoolean(node.SelectSingleNode("rasd:SinglePciQxl", _xmlNS).innerText));
        }
        if (new Version(getVersion()).compareTo(Version.v3_1) >= 0) {
            readVmDevice(node, _vm.getStaticData(), Guid.newGuid(), Boolean.TRUE);
        } else {
            // before v3.1 we had just one monitor item for all the monitors so in this
            // case we need to add monitor devices according to the numOfMonitors field
            for (int i=0; i<_vm.getStaticData().getNumOfMonitors(); ++i) {
                readVmDevice(node, _vm.getStaticData(), Guid.newGuid(), Boolean.TRUE);
            }
        }
    }

    private void readCdItem(XmlNode node) {
        readVmDevice(node, _vm.getStaticData(), Guid.newGuid(), Boolean.TRUE);
    }

    private VmDevice readOtherHardwareItem(XmlNode node) {
        if (node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS) != null
                && StringUtils.isNotEmpty(node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS).innerText)) {
            VmDeviceGeneralType type = VmDeviceGeneralType.forValue(String.valueOf(node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS).innerText));
            String device = String.valueOf(node.SelectSingleNode(OvfProperties.VMD_DEVICE, _xmlNS).innerText);
            // special devices are treated as managed devices but still have the OTHER OVF ResourceType
            if (VmDeviceCommonUtils.isSpecialDevice(device, type)) {
                return readVmDevice(node, _vm.getStaticData(), Guid.newGuid(), Boolean.TRUE);
            } else {
                return readVmDevice(node, _vm.getStaticData(), Guid.newGuid(), Boolean.FALSE);
            }
        } else {
            return readVmDevice(node, _vm.getStaticData(), Guid.newGuid(), Boolean.FALSE);
        }
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
