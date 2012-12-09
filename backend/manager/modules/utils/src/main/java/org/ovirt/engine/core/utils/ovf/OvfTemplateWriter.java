package org.ovirt.engine.core.utils.ovf;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;

public class OvfTemplateWriter extends OvfWriter {
    protected VmTemplate _vmTemplate;

    public OvfTemplateWriter(XmlDocument document, VmTemplate vmTemplate, List<DiskImage> images) {
        super(document, vmTemplate, images);
        _vmTemplate = vmTemplate;
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.WriteStartElement("Name");
        _writer.WriteRaw(_vmTemplate.getname());
        _writer.WriteEndElement();
        _writer.WriteStartElement("TemplateId");
        _writer.WriteRaw(_vmTemplate.getId().toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("Origin");
        _writer.WriteRaw(_vmTemplate.getorigin() == null ? "" : String.valueOf(_vmTemplate.getorigin().getValue()));
        _writer.WriteEndElement();
        _writer.WriteStartElement("default_display_type");
        _writer.WriteRaw(String.valueOf(_vmTemplate.getdefault_display_type().getValue()));
        _writer.WriteEndElement();
    }

    @Override
    protected void WriteAppList() {
    }

    @Override
    protected void WriteContentItems() {
        // os
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString(OVF_URI, "id", _vmTemplate.getId().toString());
        _writer.WriteAttributeString(OVF_URI, "required", "false");
        _writer.WriteAttributeString(XSI_URI, "type", OVF_PREFIX + ":OperatingSystemSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw("Guest Operating System");
        _writer.WriteEndElement();
        _writer.WriteStartElement("Description");
        _writer.WriteRaw(_vmTemplate.getos().name());
        _writer.WriteEndElement();
        _writer.WriteEndElement();

        // hardware
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString(XSI_URI, "type", OVF_PREFIX + ":VirtualHardwareSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw(String.format("%1$s CPU, %2$s Memeory", _vmTemplate.getnum_of_cpus(),
                _vmTemplate.getmem_size_mb()));
        _writer.WriteEndElement();

        _writer.WriteStartElement("System");
        _writer.WriteStartElement(VSSD_URI, "VirtualSystemType");
        _writer.WriteRaw(String.format("%1$s %2$s", Config.<String> GetValue(ConfigValues.OvfVirtualSystemType),
                Config.<String> GetValue(ConfigValues.VdcVersion)));
        _writer.WriteEndElement();
        _writer.WriteEndElement();

        // item cpu
        _writer.WriteStartElement("Item");
        _writer.WriteStartElement(RASD_URI, "Caption");
        _writer.WriteRaw(String.format("%1$s virtual cpu", _vmTemplate.getnum_of_cpus()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "Description");
        _writer.WriteRaw("Number of virtual CPU");
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "InstanceId");
        _writer.WriteRaw(String.valueOf(++_instanceId));
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "ResourceType");
        _writer.WriteRaw(OvfHardware.CPU);
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "num_of_sockets");
        _writer.WriteRaw(String.valueOf(_vmTemplate.getnum_of_sockets()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "cpu_per_socket");
        _writer.WriteRaw(String.valueOf(_vmTemplate.getcpu_per_socket()));
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // item memory
        _writer.WriteStartElement("Item");
        _writer.WriteStartElement(RASD_URI, "Caption");
        _writer.WriteRaw(String.format("%1$s MB of memory", _vmTemplate.getmem_size_mb()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "Description");
        _writer.WriteRaw("Memory Size");
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "InstanceId");
        _writer.WriteRaw(String.valueOf(++_instanceId));
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "ResourceType");
        _writer.WriteRaw(OvfHardware.Memory);
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "AllocationUnits");
        _writer.WriteRaw("MegaBytes");
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "VirtualQuantity");
        _writer.WriteRaw(String.valueOf(_vmTemplate.getmem_size_mb()));
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // item drive
        for (DiskImage image : _images) {
            _writer.WriteStartElement("Item");
            _writer.WriteStartElement(RASD_URI, "Caption");
            _writer.WriteRaw(image.getDiskAlias());
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "InstanceId");
            _writer.WriteRaw(image.getImageId().toString());
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "ResourceType");
            _writer.WriteRaw(OvfHardware.DiskImage);
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "HostResource");
            _writer.WriteRaw(image.getId() + "/" + image.getImageId());
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "Parent");
            _writer.WriteRaw(image.getParentId().toString());
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "Template");
            _writer.WriteRaw(image.getit_guid().toString());
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "ApplicationList");
            _writer.WriteRaw(image.getappList());
            _writer.WriteEndElement();
            if (image.getstorage_ids() != null && image.getstorage_ids().size() > 0) {
                _writer.WriteStartElement(RASD_URI, "StorageId");
                _writer.WriteRaw(image.getstorage_ids().get(0).toString());
                _writer.WriteEndElement();
            }
            if (image.getstorage_pool_id() != null) {
                _writer.WriteStartElement(RASD_URI, "StoragePoolId");
                _writer.WriteRaw(image.getstorage_pool_id().getValue().toString());
                _writer.WriteEndElement();
            }
            _writer.WriteStartElement(RASD_URI, "CreationDate");
            _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(image.getcreation_date()));
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "LastModified");
            _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(image.getlastModified()));
            _writer.WriteEndElement();
            writeManagedDeviceInfo(_vmTemplate, _writer, image.getId());
            _writer.WriteEndElement(); // item
        }

        // item network
        for (VmNetworkInterface iface : _vmTemplate.getInterfaces()) {
            _writer.WriteStartElement("Item");
            _writer.WriteStartElement(RASD_URI, "Caption");
            String networkName = iface.getNetworkName() != null ? iface.getNetworkName() : "[No Network]";
            _writer.WriteRaw("Ethernet adapter on " + networkName);
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "InstanceId");
            _writer.WriteRaw(iface.getId().toString());
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "ResourceType");
            _writer.WriteRaw(OvfHardware.Network);
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "ResourceSubType");
            if (iface.getType() != null) {
                _writer.WriteRaw(iface.getType().toString());
            }
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "Connection");
            if (iface.getNetworkName() != null) {
                _writer.WriteRaw(iface.getNetworkName());
            }
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "Linked");
            _writer.WriteRaw(String.valueOf(iface.isLinked()));
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "Name");
            _writer.WriteRaw(iface.getName());
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "speed");
            // version prior to 2.3 may not have speed so we get it by type
            if (iface.getSpeed() != null) {
                _writer.WriteRaw(iface.getSpeed().toString());
            } else {
                _writer.WriteRaw(String.valueOf(VmInterfaceType.forValue(
                        iface.getType()).getSpeed()));
            }
            _writer.WriteEndElement();
            writeManagedDeviceInfo(_vmTemplate, _writer, iface.getId());
            _writer.WriteEndElement(); // item
        }

        // item usb
        _writer.WriteStartElement("Item");
        _writer.WriteStartElement(RASD_URI, "Caption");
        _writer.WriteRaw("USB Controller");
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "InstanceId");
        _writer.WriteRaw(String.valueOf(++_instanceId));
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "ResourceType");
        _writer.WriteRaw(OvfHardware.USB);
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "UsbPolicy");
        _writer.WriteRaw((_vmTemplate.getusb_policy()) != null ? _vmTemplate.getusb_policy().toString()
                : UsbPolicy.DISABLED.name());
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // monitors
        writeMonitors(_vmTemplate);
        // CD
        writeCd(_vmTemplate);
        // ummanged devices
        writeOtherDevices(_vmTemplate, _writer);

        // End hardware section
        _writer.WriteEndElement();
    }
}
