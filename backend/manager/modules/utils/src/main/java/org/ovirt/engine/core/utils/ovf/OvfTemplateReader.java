package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.BootSequence;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;
import org.ovirt.engine.core.compat.backendcompat.XmlNode;
import org.ovirt.engine.core.compat.backendcompat.XmlNodeList;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class OvfTemplateReader extends OvfReader {
    protected VmTemplate _vmTemplate;

    public OvfTemplateReader(XmlDocument document,
            VmTemplate vmTemplate,
            ArrayList<DiskImage> images,
            ArrayList<VmNetworkInterface> interfaces) {
        super(document, images, interfaces);
        _vmTemplate = vmTemplate;
    }

    @Override
    protected void ReadOsSection(XmlNode section) {
        _vmTemplate.setId(new Guid(section.Attributes.get("ovf:id").getValue()));
        XmlNode node = section.SelectSingleNode("Description");
        if (node != null) {
            _vmTemplate.setos(VmOsType.valueOf(node.InnerText));
        } else {
            _vmTemplate.setos(VmOsType.Unassigned);
        }
    }

    @Override
    protected void ReadHardwareSection(XmlNode section) {
        XmlNodeList list = section.SelectNodes("Item");
        for (XmlNode node : list) {
            int resourceType = Integer.parseInt(node.SelectSingleNode("rasd:ResourceType", _xmlNS).InnerText);

            switch (resourceType) {
            // CPU
            case 3:
                _vmTemplate
                        .setnum_of_sockets(Integer.parseInt(node.SelectSingleNode("rasd:num_of_sockets", _xmlNS).InnerText));
                _vmTemplate
                        .setcpu_per_socket(Integer.parseInt(node.SelectSingleNode("rasd:cpu_per_socket", _xmlNS).InnerText));
                break;

            // Memory
            case 4:
                _vmTemplate
                        .setmem_size_mb(Integer.parseInt(node.SelectSingleNode("rasd:VirtualQuantity", _xmlNS).InnerText));
                break;

            // Image
            case 17:
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
                readVmDevice(node, _vmTemplate, image.getimage_group_id(), Boolean.TRUE);
                break;

            // Network
            case 10:
                final Guid interfaceId = new Guid(node.SelectSingleNode("rasd:InstanceId", _xmlNS).InnerText);
                VmNetworkInterface iface = LinqUtils.firstOrNull(interfaces, new Predicate<VmNetworkInterface>() {
                    @Override
                    public boolean eval(VmNetworkInterface iface) {
                        return iface.getId().equals(interfaceId);
                    }
                });

                if (!StringHelper.isNullOrEmpty(node.SelectSingleNode("rasd:ResourceSubType", _xmlNS).InnerText)) {
                    iface.setType(Integer.parseInt(node.SelectSingleNode("rasd:ResourceSubType", _xmlNS).InnerText));
                }
                iface.setNetworkName(node.SelectSingleNode("rasd:Connection", _xmlNS).InnerText);
                iface.setName(node.SelectSingleNode("rasd:Name", _xmlNS).InnerText);
                iface.setSpeed((node.SelectSingleNode("rasd:speed", _xmlNS) != null) ? Integer
                        .parseInt(node.SelectSingleNode("rasd:speed", _xmlNS).InnerText)
                        : VmInterfaceType.forValue(iface.getType()).getSpeed());
                _vmTemplate.getInterfaces().add(iface);
                readVmDevice(node, _vmTemplate, iface.getId(), Boolean.TRUE);
                break;
            // CDROM
            case 15:
                readVmDevice(node, _vmTemplate, Guid.NewGuid(), Boolean.TRUE);
                break;
            // USB
            case 23:
                _vmTemplate.setusb_policy(UsbPolicy.valueOf(node.SelectSingleNode("rasd:UsbPolicy", _xmlNS).InnerText));
                break;

            // Monitor
            case 20:
                _vmTemplate
                        .setnum_of_monitors(Integer.parseInt(node.SelectSingleNode("rasd:VirtualQuantity", _xmlNS).InnerText));
                readVmDevice(node, _vmTemplate, Guid.NewGuid(), Boolean.TRUE);
                break;
            // OTHER
            case 0:
                readVmDevice(node, _vmTemplate, Guid.NewGuid(), Boolean.FALSE);
                break;

            }
        }
    }

    @Override
    protected void ReadGeneralData() {
        // General Vm
        XmlNode content = _document.SelectSingleNode("//*/Content");

        XmlNode node = content.SelectSingleNode("Name");
        if (node != null) {
            _vmTemplate.setname(node.InnerText);
            name = _vmTemplate.getname();
        }
        node = content.SelectSingleNode("TemplateId");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vmTemplate.setId(new Guid(node.InnerText));
            }
        }
        node = content.SelectSingleNode("Description");
        if (node != null) {
            _vmTemplate.setdescription(node.InnerText);
        }
        node = content.SelectSingleNode("Domain");
        if (node != null) {
            _vmTemplate.setdomain(node.InnerText);
        }
        node = content.SelectSingleNode("CreationDate");
        java.util.Date creationDate = new java.util.Date(0);
        RefObject<java.util.Date> tempRefObject = new RefObject<java.util.Date>(creationDate);
        boolean tempVar = node != null && OvfParser.UtcDateStringToLocaDate(node.InnerText, tempRefObject);
        creationDate = tempRefObject.argvalue;
        if (tempVar) {
            _vmTemplate.setcreation_date(creationDate);
        }
        node = content.SelectSingleNode("ExportDate");
        java.util.Date exportDate = new java.util.Date(0);
        tempRefObject = new RefObject<java.util.Date>(exportDate);
        tempVar = node != null && OvfParser.UtcDateStringToLocaDate(node.InnerText, tempRefObject);
        exportDate = tempRefObject.argvalue;
        if (tempVar) {
            _vmTemplate.setExportDate(exportDate);
        }
        node = content.SelectSingleNode("IsAutoSuspend");
        if (node != null) {
            _vmTemplate.setis_auto_suspend(Boolean.parseBoolean(node.InnerText));
        }
        node = content.SelectSingleNode("TimeZone");
        if (node != null) {
            _vmTemplate.settime_zone(node.InnerText);
        }
        node = content.SelectSingleNode("VmType");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vmTemplate.setvm_type(VmType.forValue(Integer.parseInt(node.InnerText)));
            }
        }
        node = content.SelectSingleNode("default_boot_sequence");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vmTemplate.setdefault_boot_sequence(BootSequence.forValue(Integer.parseInt(node.InnerText)));
            }
        }
        node = content.SelectSingleNode("initrd_url");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vmTemplate.setinitrd_url((node.InnerText));
            }
        }
        node = content.SelectSingleNode("kernel_url");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vmTemplate.setkernel_url((node.InnerText));
            }
        }
        node = content.SelectSingleNode("kernel_params");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vmTemplate.setkernel_params((node.InnerText));
            }
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
        node = content.SelectSingleNode("default_display_type");
        if (node != null) {
            if (!StringHelper.isNullOrEmpty(node.InnerText)) {
                _vmTemplate.setdefault_display_type(DisplayType.forValue(Integer.parseInt(node.InnerText)));
            }
        }
    }
}
