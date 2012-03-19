package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
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
        super(document, images, interfaces);
        _vm = vm;
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
                // DiskImage image = null; //LINQ _images.FirstOrDefault(img =>
                // img.image_guid == guid);
                DiskImage image = LinqUtils.firstOrNull(_images, new Predicate<DiskImage>() {
                    @Override
                    public boolean eval(DiskImage diskImage) {
                        return diskImage.getId().equals(guid);
                    }
                });
                String drive = node.SelectSingleNode("rasd:Caption", _xmlNS).InnerText;
                if (drive.startsWith("Drive ")) {
                    image.setinternal_drive_mapping(drive.substring(6));
                }
                image.setimage_group_id(OvfParser.GetImageGrupIdFromImageFile(node.SelectSingleNode(
                        "rasd:HostResource", _xmlNS).InnerText));
                if (!StringHelper.isNullOrEmpty(node.SelectSingleNode("rasd:Parent", _xmlNS).InnerText)) {
                    image.setParentId(new Guid(node.SelectSingleNode("rasd:Parent", _xmlNS).InnerText));
                }
                if (!StringHelper.isNullOrEmpty(node.SelectSingleNode("rasd:Template", _xmlNS).InnerText)) {
                    image.setit_guid(new Guid(node.SelectSingleNode("rasd:Template", _xmlNS).InnerText));
                }
                image.setappList(node.SelectSingleNode("rasd:ApplicationList", _xmlNS).InnerText);
                if (!StringHelper.isNullOrEmpty(node.SelectSingleNode("rasd:StorageId", _xmlNS).InnerText)) {
                    image.setstorage_ids(new ArrayList<Guid>(Arrays.asList(new Guid(node.SelectSingleNode("rasd:StorageId", _xmlNS).InnerText))));
                }
                if (!StringHelper.isNullOrEmpty(node.SelectSingleNode("rasd:StoragePoolId", _xmlNS).InnerText)) {
                    image.setstorage_pool_id(new Guid(node.SelectSingleNode("rasd:StoragePoolId", _xmlNS).InnerText));
                }
                java.util.Date creationDate = new java.util.Date(0);
                RefObject<java.util.Date> tempRefObject = new RefObject<java.util.Date>(creationDate);
                boolean tempVar = OvfParser.UtcDateStringToLocaDate(
                        node.SelectSingleNode("rasd:CreationDate", _xmlNS).InnerText, tempRefObject);
                creationDate = tempRefObject.argvalue;
                if (tempVar) {
                    image.setcreation_date(creationDate);
                }
                java.util.Date lastModified = new java.util.Date(0);
                RefObject<java.util.Date> tempRefObject2 = new RefObject<java.util.Date>(lastModified);
                boolean tempVar2 = OvfParser.UtcDateStringToLocaDate(
                        node.SelectSingleNode("rasd:LastModified", _xmlNS).InnerText, tempRefObject2);
                lastModified = tempRefObject2.argvalue;
                if (tempVar2) {
                    image.setlastModified(lastModified);
                }
                java.util.Date last_modified_date = new java.util.Date(0);
                RefObject<java.util.Date> tempRefObject3 = new RefObject<java.util.Date>(last_modified_date);
                boolean tempVar3 = OvfParser.UtcDateStringToLocaDate(
                        node.SelectSingleNode("rasd:last_modified_date", _xmlNS).InnerText, tempRefObject3);
                last_modified_date = tempRefObject3.argvalue;
                if (tempVar3) {
                    image.setlast_modified_date(last_modified_date);
                }
                readVmDevice(node, _vm.getStaticData(), image.getimage_group_id(), Boolean.TRUE);
            } else if (StringHelper.EqOp(resourceType, OvfHardware.Network)) {
                final Guid guid = new Guid(node.SelectSingleNode("rasd:InstanceId", _xmlNS).InnerText);
                VmNetworkInterface iface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNetworkInterface>() {
                    @Override
                    public boolean eval(VmNetworkInterface iface) {
                        return iface.getId().equals(guid);
                    }
                });

                if (!StringHelper.isNullOrEmpty(node.SelectSingleNode("rasd:ResourceSubType", _xmlNS).InnerText)) {
                    iface.setType(Integer.parseInt(node.SelectSingleNode("rasd:ResourceSubType", _xmlNS).InnerText));
                }
                iface.setNetworkName(node.SelectSingleNode("rasd:Connection", _xmlNS).InnerText);
                iface.setName(node.SelectSingleNode("rasd:Name", _xmlNS).InnerText);
                iface.setMacAddress((node.SelectSingleNode("rasd:MACAddress", _xmlNS) != null) ? node.SelectSingleNode(
                        "rasd:MACAddress", _xmlNS).InnerText : "");
                iface.setSpeed((node.SelectSingleNode("rasd:speed", _xmlNS) != null) ? Integer
                        .parseInt(node.SelectSingleNode("rasd:speed", _xmlNS).InnerText)
                        : VmInterfaceType.forValue(iface.getType()).getSpeed());
                _vm.getInterfaces().add(iface);
                readVmDevice(node, _vm.getStaticData(), iface.getId(), Boolean.TRUE);
            } else if (StringHelper.EqOp(resourceType, OvfHardware.USB)) {
                _vm.getStaticData().setusb_policy(
                        UsbPolicy.valueOf(node.SelectSingleNode("rasd:UsbPolicy", _xmlNS).InnerText));
            } else if (StringHelper.EqOp(resourceType, OvfHardware.Monitor)) {
                _vm.getStaticData().setnum_of_monitors(
                        Integer.parseInt(node.SelectSingleNode("rasd:VirtualQuantity", _xmlNS).InnerText));
                readVmDevice(node, _vm.getStaticData(), Guid.NewGuid(), Boolean.TRUE);
            } else if (StringHelper.EqOp(resourceType, OvfHardware.CD)) {
                readVmDevice(node, _vm.getStaticData(), Guid.NewGuid(), Boolean.TRUE);
            } else if (StringHelper.EqOp(resourceType, OvfHardware.OTHER)) {
                readVmDevice(node, _vm.getStaticData(), Guid.NewGuid(), Boolean.FALSE);
            }

        }
    }

    @Override
    protected void ReadGeneralData() {
        // General Vm
        XmlNode content = _document.SelectSingleNode("//*/Content");

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
                _vm.setvmt_name(node.InnerText);
            }
        }
        node = content.SelectSingleNode("Description");
        if (node != null) {
            _vm.getStaticData().setdescription(node.InnerText);
        }
        node = content.SelectSingleNode("Domain");
        if (node != null) {
            _vm.getStaticData().setdomain(node.InnerText);
        }
        node = content.SelectSingleNode("CreationDate");
        java.util.Date creationDate = new java.util.Date(0);
        RefObject<java.util.Date> tempRefObject = new RefObject<java.util.Date>(creationDate);
        boolean tempVar = node != null && OvfParser.UtcDateStringToLocaDate(node.InnerText, tempRefObject);
        creationDate = tempRefObject.argvalue;
        if (tempVar) {
            _vm.getStaticData().setcreation_date(creationDate);
        }
        node = content.SelectSingleNode("ExportDate");
        java.util.Date exportDate = new java.util.Date(0);
        tempRefObject = new RefObject<java.util.Date>(exportDate);
        tempVar = node != null && OvfParser.UtcDateStringToLocaDate(node.InnerText, tempRefObject);
        exportDate = tempRefObject.argvalue;
        if (tempVar) {
            _vm.getStaticData().setExportDate(exportDate);
        }
        node = content.SelectSingleNode("IsInitilized");
        if (node != null) {
            _vm.getStaticData().setis_initialized(Boolean.parseBoolean(node.InnerText));
        }
        node = content.SelectSingleNode("IsAutoSuspend");
        if (node != null) {
            _vm.getStaticData().setis_auto_suspend(Boolean.parseBoolean(node.InnerText));
        }
        node = content.SelectSingleNode("TimeZone");
        if (node != null) {
            _vm.getStaticData().settime_zone(node.InnerText);
        }
        node = content.SelectSingleNode("IsStateless");
        if (node != null) {
            _vm.getStaticData().setis_stateless(Boolean.parseBoolean(node.InnerText));
        }

        XmlNodeList list = content.SelectNodes("Section");
        for (XmlNode section : list) {
            String value = section.Attributes.get("xsi:type").getValue();

            if (StringHelper.EqOp(value, "ovf:OperatingSystemSection_Type")) {
                ReadOsSection(section);

            }
            else if (StringHelper.EqOp(value, "ovf:VirtualHardwareSection_Type")) {
                ReadHardwareSection(section);
            }
        }

        node = content.SelectSingleNode("Origin");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vm.setorigin(OriginType.forValue(Integer.parseInt(node.InnerText)));
            }
        }
        node = content.SelectSingleNode("initrd_url");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vm.setinitrd_url((node.InnerText));
            }
        }
        node = content.SelectSingleNode("default_boot_sequence");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vm.setdefault_boot_sequence(BootSequence.forValue(Integer.parseInt(node.InnerText)));
            }
        }

        node = content.SelectSingleNode("kernel_url");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vm.setkernel_url((node.InnerText));
            }
        }
        node = content.SelectSingleNode("kernel_params");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vm.setkernel_params((node.InnerText));
            }
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
                _vm.setapp_list(node.InnerText);
            }
        }
        // if no app list in VM, get it from one of the leafs
        else if(_images != null && _images.size() > 0) {
            int root = GetFirstImage(_images, _images.get(0));
            if (root != -1) {
                for(int i=0; i<_images.size(); i++) {
                    int x = GetNextImage(_images, _images.get(i));
                    if (x == -1) {
                        _vm.setapp_list(_images.get(i).getappList());
                    }
                }
            } else {
                _vm.setapp_list(_images.get(0).getappList());
            }
        }
        node = content.SelectSingleNode("VmType");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vm.setvm_type(VmType.forValue(Integer.parseInt(node.InnerText)));
            }
        }
        node = content.SelectSingleNode("DefaultDisplayType");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vm.setdefault_display_type(DisplayType.forValue(Integer.parseInt(node.InnerText)));
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
            if (curr.getParentId().equals(images.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    // function returns the index of image that is it's child
    private static int GetNextImage(java.util.ArrayList<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getParentId().equals(curr.getId())) {
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
}
