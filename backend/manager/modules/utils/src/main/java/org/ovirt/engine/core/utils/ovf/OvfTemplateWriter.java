package org.ovirt.engine.core.utils.ovf;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Version;

public class OvfTemplateWriter extends OvfWriter {
    protected VmTemplate _vmTemplate;
    private OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);

    public OvfTemplateWriter(VmTemplate vmTemplate, List<DiskImage> images, Version version) {
        super(vmTemplate, images, version);
        _vmTemplate = vmTemplate;
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.WriteStartElement(OvfProperties.NAME);
        _writer.WriteRaw(_vmTemplate.getName());
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.TEMPLATE_ID);
        _writer.WriteRaw(_vmTemplate.getId().toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.ORIGIN);
        _writer.WriteRaw(_vmTemplate.getOrigin() == null ? "" : String.valueOf(_vmTemplate.getOrigin().getValue()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.TEMPLATE_DEFAULT_DISPLAY_TYPE);
        _writer.WriteRaw(String.valueOf(_vmTemplate.getDefaultDisplayType().getValue()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.IS_DISABLED);
        _writer.WriteRaw(String.valueOf(_vmTemplate.isDisabled()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.TRUSTED_SERVICE);
        _writer.WriteRaw(String.valueOf(_vmTemplate.isTrustedService()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.TEMPLATE_TYPE);
        _writer.WriteRaw(_vmTemplate.getTemplateType().name());
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.BASE_TEMPLATE_ID);
        _writer.WriteRaw(_vmTemplate.getBaseTemplateId().toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.TEMPLATE_VERSION_NUMBER);
        _writer.WriteRaw(String.valueOf(_vmTemplate.getTemplateVersionNumber()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.TEMPLATE_VERSION_NAME);
        _writer.WriteRaw(_vmTemplate.getTemplateVersionName());
        _writer.WriteEndElement();
        _writer.WriteStartElement("AutoStartup"); // aka highly available
        _writer.WriteRaw(String.valueOf(_vmTemplate.isAutoStartup()));
        _writer.WriteEndElement();
    }

    @Override
    protected void writeAppList() {
    }

    @Override
    protected void writeContentItems() {
        // os
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString(OVF_URI, "id", _vmTemplate.getId().toString());
        _writer.WriteAttributeString(OVF_URI, "required", "false");
        _writer.WriteAttributeString(XSI_URI, "type", OVF_PREFIX + ":OperatingSystemSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw("Guest Operating System");
        _writer.WriteEndElement();
        _writer.WriteStartElement("Description");
        _writer.WriteRaw(osRepository.getUniqueOsNames().get(_vmTemplate.getOsId()));
        _writer.WriteEndElement();
        _writer.WriteEndElement();

        // hardware
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString(XSI_URI, "type", OVF_PREFIX + ":VirtualHardwareSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw(String.format("%1$s CPU, %2$s Memeory", _vmTemplate.getNumOfCpus(),
                _vmTemplate.getMemSizeMb()));
        _writer.WriteEndElement();

        _writer.WriteStartElement("System");
        _writer.WriteStartElement(VSSD_URI, "VirtualSystemType");
        _writer.WriteRaw(String.format("%1$s %2$s", Config.<String> getValue(ConfigValues.OvfVirtualSystemType),
                Config.<String> getValue(ConfigValues.VdcVersion)));
        _writer.WriteEndElement();
        _writer.WriteEndElement();

        // item cpu
        _writer.WriteStartElement("Item");
        _writer.WriteStartElement(RASD_URI, "Caption");
        _writer.WriteRaw(String.format("%1$s virtual cpu", _vmTemplate.getNumOfCpus()));
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
        _writer.WriteRaw(String.valueOf(_vmTemplate.getNumOfSockets()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "cpu_per_socket");
        _writer.WriteRaw(String.valueOf(_vmTemplate.getCpuPerSocket()));
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // item memory
        _writer.WriteStartElement("Item");
        _writer.WriteStartElement(RASD_URI, "Caption");
        _writer.WriteRaw(String.format("%1$s MB of memory", _vmTemplate.getMemSizeMb()));
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
        _writer.WriteRaw(String.valueOf(_vmTemplate.getMemSizeMb()));
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // item drive
        for (DiskImage image : _images) {
            _writer.WriteStartElement("Item");
            _writer.WriteStartElement(RASD_URI, "Caption");
            _writer.WriteRaw(getBackwardCompatibleDiskAlias(image.getDiskAlias()));
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
            _writer.WriteRaw(image.getImageTemplateId().toString());
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "ApplicationList");
            _writer.WriteRaw(image.getAppList());
            _writer.WriteEndElement();
            if (image.getStorageIds() != null && image.getStorageIds().size() > 0) {
                _writer.WriteStartElement(RASD_URI, "StorageId");
                _writer.WriteRaw(image.getStorageIds().get(0).toString());
                _writer.WriteEndElement();
            }
            if (image.getStoragePoolId() != null) {
                _writer.WriteStartElement(RASD_URI, "StoragePoolId");
                _writer.WriteRaw(image.getStoragePoolId().toString());
                _writer.WriteEndElement();
            }
            _writer.WriteStartElement(RASD_URI, "CreationDate");
            _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(image.getCreationDate()));
            _writer.WriteEndElement();
            _writer.WriteStartElement(RASD_URI, "LastModified");
            _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(image.getLastModified()));
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

            _writer.WriteStartElement(RASD_URI, "OtherResourceType");
            if (StringUtils.isNotEmpty(iface.getVnicProfileName())) {
                _writer.WriteRaw(iface.getVnicProfileName());
            }
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
        _writer.WriteRaw(getBackwardCompatibleUsbPolicy(_vmTemplate.getUsbPolicy()));
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // monitors
        writeMonitors(_vmTemplate);
        // graphics
        writeGraphics(vmBase);
        // CD
        writeCd(_vmTemplate);
        // ummanged devices
        writeOtherDevices(_vmTemplate, _writer);

        // End hardware section
        _writer.WriteEndElement();
    }
}
