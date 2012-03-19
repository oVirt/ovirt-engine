package org.ovirt.engine.core.utils.ovf;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;

public class OvfTemplateWriter extends OvfWriter {
    protected VmTemplate _vmTemplate;

    public OvfTemplateWriter(RefObject<XmlDocument> document, VmTemplate vmTemplate, List<DiskImage> images) {
        super(document, vmTemplate, images);
        _vmTemplate = vmTemplate;
    }

    @Override
    protected void WriteGeneralData() {
        _writer.WriteStartElement("Name");
        _writer.WriteRaw(_vmTemplate.getname());
        _writer.WriteEndElement();
        _writer.WriteStartElement("TemplateId");
        _writer.WriteRaw(_vmTemplate.getId().toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("Description");
        _writer.WriteRaw(_vmTemplate.getdescription());
        _writer.WriteEndElement();
        _writer.WriteStartElement("Domain");
        _writer.WriteRaw(_vmTemplate.getdomain());
        _writer.WriteEndElement();
        _writer.WriteStartElement("CreationDate");
        _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(_vmTemplate.getcreation_date()));
        _writer.WriteEndElement();
        _writer.WriteStartElement("ExportDate");
        _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(new java.util.Date()));
        _writer.WriteEndElement();
        _writer.WriteStartElement("IsAutoSuspend");
        _writer.WriteRaw((new Boolean(_vmTemplate.getis_auto_suspend())).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("TimeZone");
        _writer.WriteRaw(_vmTemplate.gettime_zone());
        _writer.WriteEndElement();
        _writer.WriteStartElement("VmType");
        _writer.WriteRaw((new Integer(_vmTemplate.getvm_type().getValue())).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("default_display_type");
        _writer.WriteRaw((new Integer(_vmTemplate.getdefault_display_type().getValue())).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("default_boot_sequence");
        _writer.WriteRaw((new Integer(_vmTemplate.getdefault_boot_sequence().getValue())).toString());
        _writer.WriteEndElement();

        if (!StringHelper.isNullOrEmpty(_vmTemplate.getinitrd_url())) {
            _writer.WriteStartElement("initrd_url");
            _writer.WriteRaw(_vmTemplate.getinitrd_url());
            _writer.WriteEndElement();
        }
        if (!StringHelper.isNullOrEmpty(_vmTemplate.getkernel_url())) {
            _writer.WriteStartElement("kernel_url");
            _writer.WriteRaw(_vmTemplate.getkernel_url());
            _writer.WriteEndElement();
        }
        if (!StringHelper.isNullOrEmpty(_vmTemplate.getkernel_params())) {
            _writer.WriteStartElement("kernel_params");
            _writer.WriteRaw(_vmTemplate.getkernel_params());
            _writer.WriteEndElement();
        }
    }

    @Override
    protected void WriteAppList() {
    }

    @Override
    protected void WriteContentItems() {
        // os
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString("ovf", "id", null, _vmTemplate.getId().toString());
        _writer.WriteAttributeString("ovf", "required", null, "false");
        _writer.WriteAttributeString("xsi", "type", null, "ovf:OperatingSystemSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw("Guest Operating System");
        _writer.WriteEndElement();
        _writer.WriteStartElement("Description");
        _writer.WriteRaw(_vmTemplate.getos().name());
        _writer.WriteEndElement();
        _writer.WriteEndElement();

        // hardware
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString("xsi", "type", null, "ovf:VirtualHardwareSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw(String.format("%1$s CPU, %2$s Memeory", _vmTemplate.getnum_of_cpus(),
                _vmTemplate.getmem_size_mb()));
        _writer.WriteEndElement();

        _writer.WriteStartElement("System");
        _writer.WriteStartElement("vssd:VirtualSystemType");
        _writer.WriteRaw(String.format("%1$s %2$s", Config.<String> GetValue(ConfigValues.OvfVirtualSystemType),
                Config.<String> GetValue(ConfigValues.VdcVersion)));
        _writer.WriteEndElement();
        _writer.WriteEndElement();

        // item cpu
        _writer.WriteStartElement("Item");
        _writer.WriteStartElement("rasd:Caption");
        _writer.WriteRaw(String.format("%1$s virtual cpu", _vmTemplate.getnum_of_cpus()));
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:Description");
        _writer.WriteRaw("Number of virtual CPU");
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:InstanceId");
        _writer.WriteRaw(((Integer) (++_instanceId)).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:ResourceType");
        _writer.WriteRaw(OvfHardware.CPU);
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:num_of_sockets");
        _writer.WriteRaw((new Integer(_vmTemplate.getnum_of_sockets())).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:cpu_per_socket");
        _writer.WriteRaw((new Integer(_vmTemplate.getcpu_per_socket())).toString());
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // item memory
        _writer.WriteStartElement("Item");
        _writer.WriteStartElement("rasd:Caption");
        _writer.WriteRaw(String.format("%1$s MB of memory", _vmTemplate.getmem_size_mb()));
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:Description");
        _writer.WriteRaw("Memory Size");
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:InstanceId");
        _writer.WriteRaw(((Integer) (++_instanceId)).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:ResourceType");
        _writer.WriteRaw(OvfHardware.Memory);
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:AllocationUnits");
        _writer.WriteRaw("MegaBytes");
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:VirtualQuantity");
        _writer.WriteRaw((new Integer(_vmTemplate.getmem_size_mb())).toString());
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // item drive
        for (DiskImage image : _images) {
            _writer.WriteStartElement("Item");
            _writer.WriteStartElement("rasd:Caption");
            _writer.WriteRaw(String.format("Drive %1$s", image.getinternal_drive_mapping()));
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:InstanceId");
            _writer.WriteRaw(image.getId().toString());
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:ResourceType");
            _writer.WriteRaw(OvfHardware.DiskImage);
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:HostResource");
            _writer.WriteRaw(image.getimage_group_id() + "/" + image.getId());
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:Parent");
            _writer.WriteRaw(image.getParentId().toString());
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:Template");
            _writer.WriteRaw(image.getit_guid().toString());
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:ApplicationList");
            _writer.WriteRaw(image.getappList());
            _writer.WriteEndElement();
            if (image.getstorage_ids() != null && image.getstorage_ids().size() > 0) {
                _writer.WriteStartElement("rasd:StorageId");
                _writer.WriteRaw(image.getstorage_ids().get(0).toString());
                _writer.WriteEndElement();
            }
            if (image.getstorage_pool_id() != null) {
                _writer.WriteStartElement("rasd:StoragePoolId");
                _writer.WriteRaw(image.getstorage_pool_id().getValue().toString());
                _writer.WriteEndElement();
            }
            _writer.WriteStartElement("rasd:CreationDate");
            _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(image.getcreation_date()));
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:LastModified");
            _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(image.getlastModified()));
            _writer.WriteEndElement();
            writeManagedDeviceInfo(_vmTemplate, _writer, image.getimage_group_id());
            _writer.WriteEndElement(); // item
        }

        // item network
        for (VmNetworkInterface iface : _vmTemplate.getInterfaces()) {
            _writer.WriteStartElement("Item");
            _writer.WriteStartElement("rasd:Caption");
            _writer.WriteRaw("Ethernet adapter on " + iface.getNetworkName());
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:InstanceId");
            _writer.WriteRaw(iface.getId().toString());
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:ResourceType");
            _writer.WriteRaw(OvfHardware.Network);
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:ResourceSubType");
            if (iface.getType() != null) {
                _writer.WriteRaw(iface.getType().toString());
            }
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:Connection");
            _writer.WriteRaw(iface.getNetworkName());
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:Name");
            _writer.WriteRaw(iface.getName());
            _writer.WriteEndElement();
            _writer.WriteStartElement("rasd:speed");
            // version prior to 2.3 may not have speed so we get it by type
            if (iface.getSpeed() != null) {
                _writer.WriteRaw(iface.getSpeed().toString());
            } else {
                _writer.WriteRaw(Integer.toString(VmInterfaceType.forValue(
                        iface.getType()).getSpeed()));
            }
            _writer.WriteEndElement();
            writeManagedDeviceInfo(_vmTemplate, _writer, iface.getId());
            _writer.WriteEndElement(); // item
        }

        // item usb
        _writer.WriteStartElement("Item");
        _writer.WriteStartElement("rasd:Caption");
        _writer.WriteRaw("USB Controller");
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:InstanceId");
        _writer.WriteRaw(((Integer) (++_instanceId)).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:ResourceType");
        _writer.WriteRaw(OvfHardware.USB);
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:UsbPolicy");
        _writer.WriteRaw(_vmTemplate.getusb_policy().toString());
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // monitors
        writeMonitors(_vmTemplate);
        // CD
        writeCd(_vmTemplate);
        // ummanged devices
        writeUnmanagedDevices(_vmTemplate, _writer);

    }
}
