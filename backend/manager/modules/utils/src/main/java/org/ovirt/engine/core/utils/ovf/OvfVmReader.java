package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.compat.backendcompat.XmlNode;
import org.ovirt.engine.core.compat.backendcompat.XmlNodeList;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

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
    protected void ReadOsSection(XmlNode section) {
        _vm.getStaticData().setId(new Guid(section.Attributes.get("ovf:id").getValue()));
        XmlNode node = section.SelectSingleNode("Description");
        if (node != null) {
            _vm.getStaticData().setos(VmOsType.valueOf(node.InnerText));
        } else {
            _vm.getStaticData().setos(VmOsType.Unassigned);
        }
    }

    @Override
    protected void ReadHardwareSection(XmlNode section) {
        XmlNodeList list = section.SelectNodes("Item");
        for (XmlNode node : list) {
            String resourceType = node.SelectSingleNode("rasd:ResourceType", _xmlNS).InnerText;

            if (StringHelper.EqOp(resourceType, OvfHardware.CPU)) {
                _vm.getStaticData().setnum_of_sockets(
                        Integer.parseInt(node.SelectSingleNode("rasd:num_of_sockets", _xmlNS).InnerText));
                _vm.getStaticData().setcpu_per_socket(
                        Integer.parseInt(node.SelectSingleNode("rasd:cpu_per_socket", _xmlNS).InnerText));
            } else if (StringHelper.EqOp(resourceType, OvfHardware.Memory)) {
                _vm.getStaticData().setmem_size_mb(
                        Integer.parseInt(node.SelectSingleNode("rasd:VirtualQuantity", _xmlNS).InnerText));
            } else if (StringHelper.EqOp(resourceType, OvfHardware.DiskImage)) {
                final Guid guid = new Guid(node.SelectSingleNode("rasd:InstanceId", _xmlNS).InnerText);

                DiskImage image = LinqUtils.firstOrNull(_images, new Predicate<DiskImage>() {
                    @Override
                    public boolean eval(DiskImage diskImage) {
                        return diskImage.getImageId().equals(guid);
                    }
                });
                image.setId(OvfParser.GetImageGrupIdFromImageFile(node.SelectSingleNode(
                        "rasd:HostResource", _xmlNS).InnerText));
                if (!StringHelper.isNullOrEmpty(node.SelectSingleNode("rasd:Parent", _xmlNS).InnerText)) {
                    image.setParentId(new Guid(node.SelectSingleNode("rasd:Parent", _xmlNS).InnerText));
                }
                if (!StringHelper.isNullOrEmpty(node.SelectSingleNode("rasd:Template", _xmlNS).InnerText)) {
                    image.setit_guid(new Guid(node.SelectSingleNode("rasd:Template", _xmlNS).InnerText));
                }
                image.setappList(node.SelectSingleNode("rasd:ApplicationList", _xmlNS).InnerText);

                XmlNode storageNode = node.SelectSingleNode("rasd:StorageId", _xmlNS);
                if (storageNode != null &&
                        !StringHelper.isNullOrEmpty(storageNode.InnerText)) {
                    image.setstorage_ids(new ArrayList<Guid>(Arrays.asList(new Guid(storageNode.InnerText))));
                }
                if (!StringHelper.isNullOrEmpty(node.SelectSingleNode("rasd:StoragePoolId", _xmlNS).InnerText)) {
                    image.setstorage_pool_id(new Guid(node.SelectSingleNode("rasd:StoragePoolId", _xmlNS).InnerText));
                }
                final Date creationDate = OvfParser.UtcDateStringToLocaDate(
                        node.SelectSingleNode("rasd:CreationDate", _xmlNS).InnerText);
                if (creationDate == null) {
                    image.setcreation_date(creationDate);
                }
                final Date lastModified = OvfParser.UtcDateStringToLocaDate(
                        node.SelectSingleNode("rasd:LastModified", _xmlNS).InnerText);
                if (lastModified != null) {
                    image.setlastModified(lastModified);
                }
                final Date last_modified_date = OvfParser.UtcDateStringToLocaDate(
                        node.SelectSingleNode("rasd:last_modified_date", _xmlNS).InnerText);
                if (last_modified_date != null) {
                    image.setlast_modified_date(last_modified_date);
                }
                readVmDevice(node, _vm.getStaticData(), image.getId(), Boolean.TRUE);
            } else if (StringHelper.EqOp(resourceType, OvfHardware.Network)) {
                VmNetworkInterface iface = getNetwotkInterface(node);
                updateSingleNic(node, iface);
                _vm.getInterfaces().add(iface);
                readVmDevice(node, _vm.getStaticData(), iface.getId(), Boolean.TRUE);
            } else if (StringHelper.EqOp(resourceType, OvfHardware.USB)) {
                _vm.getStaticData().setusb_policy(
                        UsbPolicy.forStringValue(node.SelectSingleNode("rasd:UsbPolicy", _xmlNS).InnerText));
            } else if (StringHelper.EqOp(resourceType, OvfHardware.Monitor)) {
                _vm.getStaticData().setnum_of_monitors(
                        Integer.parseInt(node.SelectSingleNode("rasd:VirtualQuantity", _xmlNS).InnerText));
                readVmDevice(node, _vm.getStaticData(), Guid.NewGuid(), Boolean.TRUE);
            } else if (StringHelper.EqOp(resourceType, OvfHardware.CD)) {
                readVmDevice(node, _vm.getStaticData(), Guid.NewGuid(), Boolean.TRUE);
            } else if (StringHelper.EqOp(resourceType, OvfHardware.OTHER)) {
                if (node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS) != null
                        && !StringHelper.isNullOrEmpty(node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS).InnerText)) {
                    String type = String.valueOf(node.SelectSingleNode(OvfProperties.VMD_TYPE, _xmlNS).InnerText);
                    String device = String.valueOf(node.SelectSingleNode(OvfProperties.VMD_DEVICE, _xmlNS).InnerText);
                    // special devices are treated as managed devices but still have the OTHER OVF ResourceType
                    if (VmDeviceCommonUtils.isSpecialDevice(device, type)) {
                        readVmDevice(node, _vm.getStaticData(), Guid.NewGuid(), Boolean.TRUE);
                    } else {
                        readVmDevice(node, _vm.getStaticData(), Guid.NewGuid(), Boolean.FALSE);
                    }
                } else {
                    readVmDevice(node, _vm.getStaticData(), Guid.NewGuid(), Boolean.FALSE);
                }
            }
        }
    }

    @Override
    protected void readGeneralData(XmlNode content) {
        // General Vm
        XmlNode node = content.SelectSingleNode("Name");
        if (node != null) {
            _vm.getStaticData().setvm_name(node.InnerText);
            name = _vm.getStaticData().getvm_name();
        }
        node = content.SelectSingleNode("TemplateId");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vm.getStaticData().setvmt_guid(new Guid(node.InnerText));
            }
        }
        node = content.SelectSingleNode("TemplateName");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vm.setVmtName(node.InnerText);
            }
        }
        node = content.SelectSingleNode("IsInitilized");
        if (node != null) {
            _vm.getStaticData().setis_initialized(Boolean.parseBoolean(node.InnerText));
        }
        node = content.SelectSingleNode("TimeZone");
        if (node != null) {
            _vm.getStaticData().settime_zone(node.InnerText);
        }
        node = content.SelectSingleNode("IsStateless");
        if (node != null) {
            _vm.getStaticData().setis_stateless(Boolean.parseBoolean(node.InnerText));
        }
        node = content.SelectSingleNode("quota_id");
        if (node != null) {
            _vm.getStaticData().setQuotaId(new Guid(node.InnerText));
        }
        OvfLogEventHandler<VmStatic> handler = new VMStaticOvfLogHandler(_vm.getStaticData());

        // Gets a list of all the aliases of the fields that should be logged in
        // ovd For each one of these fields, the proper value will be read from
        // the ovf and field in vm static
        List<String> aliases = handler.getAliases();
        for (String alias : aliases) {
            String value = readEventLogValue(content, alias);
            if (!StringHelper.isNullOrEmpty(value)) {
                handler.addValueForAlias(alias, value);

            }
        }

        node = content.SelectSingleNode("app_list");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vm.setAppList(node.InnerText);
            }
        }
        // if no app list in VM, get it from one of the leafs
        else if(_images != null && _images.size() > 0) {
            int root = GetFirstImage(_images, _images.get(0));
            if (root != -1) {
                for(int i=0; i<_images.size(); i++) {
                    int x = GetNextImage(_images, _images.get(i));
                    if (x == -1) {
                        _vm.setAppList(_images.get(i).getappList());
                    }
                }
            } else {
                _vm.setAppList(_images.get(0).getappList());
            }
        }

        node = content.SelectSingleNode("DefaultDisplayType");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vm.setDefaultDisplayType(DisplayType.forValue(Integer.parseInt(node.InnerText)));
            }
        }

        node = content.SelectSingleNode("MinAllocatedMem");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vm.setMinAllocatedMem(Integer.parseInt(node.InnerText));
            }
        }

    }

    // function returns the index of the image that has no parent
    private static int GetFirstImage(java.util.ArrayList<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (curr.getParentId().equals(images.get(i).getImageId())) {
                return i;
            }
        }
        return -1;
    }

    // function returns the index of image that is it's child
    private static int GetNextImage(java.util.ArrayList<DiskImage> images, DiskImage curr) {
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
            return node.InnerText;
        }
        return null;
    }

    protected void readSnapshotsSection(XmlNode section) {
        XmlNodeList list = section.SelectNodes("Snapshot");
        ArrayList<Snapshot> snapshots = new ArrayList<Snapshot>();
        _vm.setSnapshots(snapshots);

        for (XmlNode node : list) {
            XmlNode vmConfiguration = node.SelectSingleNode("VmConfiguration", _xmlNS);
            Snapshot snapshot = new Snapshot(vmConfiguration != null);
            snapshot.setId(new Guid(node.Attributes.get("ovf:id").getValue()));
            snapshot.setVmId(_vm.getId());
            snapshot.setType(SnapshotType.valueOf(node.SelectSingleNode("Type", _xmlNS).InnerText));
            snapshot.setStatus(SnapshotStatus.OK);
            snapshot.setDescription(node.SelectSingleNode("Description", _xmlNS).InnerText);

            final Date creationDate = OvfParser.UtcDateStringToLocaDate(node.SelectSingleNode("CreationDate", _xmlNS).InnerText);
            if (creationDate != null) {
                snapshot.setCreationDate(creationDate);
            }

            snapshot.setVmConfiguration(vmConfiguration == null
                    ? null : new String(Base64.decodeBase64(vmConfiguration.InnerText)));

            XmlNode appList = node.SelectSingleNode("ApplicationList", _xmlNS);
            if (appList != null) {
                snapshot.setAppList(appList.InnerText);
            }

            snapshots.add(snapshot);
        }
    }

    @Override
    protected void buildNicReference() {
    }
}
