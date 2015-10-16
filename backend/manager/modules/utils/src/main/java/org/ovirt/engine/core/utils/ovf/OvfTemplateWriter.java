package org.ovirt.engine.core.utils.ovf;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Version;

public class OvfTemplateWriter extends OvfWriter {
    protected VmTemplate _vmTemplate;
    private OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);

    public OvfTemplateWriter(VmTemplate vmTemplate, List<DiskImage> images, Version version) {
        super(vmTemplate, images, version);
        _vmTemplate = vmTemplate;
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.writeStartElement(OvfProperties.NAME);
        _writer.writeRaw(_vmTemplate.getName());
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TEMPLATE_ID);
        _writer.writeRaw(_vmTemplate.getId().toString());
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.ORIGIN);
        _writer.writeRaw(_vmTemplate.getOrigin() == null ? "" : String.valueOf(_vmTemplate.getOrigin().getValue()));
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TEMPLATE_DEFAULT_DISPLAY_TYPE);
        _writer.writeRaw(String.valueOf(_vmTemplate.getDefaultDisplayType().getValue()));
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.IS_DISABLED);
        _writer.writeRaw(String.valueOf(_vmTemplate.isDisabled()));
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TRUSTED_SERVICE);
        _writer.writeRaw(String.valueOf(_vmTemplate.isTrustedService()));
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TEMPLATE_TYPE);
        _writer.writeRaw(_vmTemplate.getTemplateType().name());
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.BASE_TEMPLATE_ID);
        _writer.writeRaw(_vmTemplate.getBaseTemplateId().toString());
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TEMPLATE_VERSION_NUMBER);
        _writer.writeRaw(String.valueOf(_vmTemplate.getTemplateVersionNumber()));
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TEMPLATE_VERSION_NAME);
        _writer.writeRaw(_vmTemplate.getTemplateVersionName());
        _writer.writeEndElement();
        _writer.writeStartElement("AutoStartup"); // aka highly available
        _writer.writeRaw(String.valueOf(_vmTemplate.isAutoStartup()));
        _writer.writeEndElement();
    }

    @Override
    protected void writeAppList() {
    }

    @Override
    protected void writeContentItems() {
        // os
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(OVF_URI, "id", _vmTemplate.getId().toString());
        _writer.writeAttributeString(OVF_URI, "required", "false");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":OperatingSystemSection_Type");
        _writer.writeStartElement("Info");
        _writer.writeRaw("Guest Operating System");
        _writer.writeEndElement();
        _writer.writeStartElement("Description");
        _writer.writeRaw(osRepository.getUniqueOsNames().get(_vmTemplate.getOsId()));
        _writer.writeEndElement();
        _writer.writeEndElement();

        // hardware
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", OVF_PREFIX + ":VirtualHardwareSection_Type");
        _writer.writeStartElement("Info");
        _writer.writeRaw(String.format("%1$s CPU, %2$s Memeory", _vmTemplate.getNumOfCpus(),
                _vmTemplate.getMemSizeMb()));
        _writer.writeEndElement();

        _writer.writeStartElement("System");
        _writer.writeStartElement(VSSD_URI, "VirtualSystemType");
        _writer.writeRaw(String.format("%1$s %2$s", Config.<String>getValue(ConfigValues.OvfVirtualSystemType),
                Config.<String>getValue(ConfigValues.VdcVersion)));
        _writer.writeEndElement();
        _writer.writeEndElement();

        // item cpu
        _writer.writeStartElement("Item");
        _writer.writeStartElement(RASD_URI, "Caption");
        _writer.writeRaw(String.format("%1$s virtual cpu", _vmTemplate.getNumOfCpus()));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "Description");
        _writer.writeRaw("Number of virtual CPU");
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "InstanceId");
        _writer.writeRaw(String.valueOf(++_instanceId));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "ResourceType");
        _writer.writeRaw(OvfHardware.CPU);
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "num_of_sockets");
        _writer.writeRaw(String.valueOf(_vmTemplate.getNumOfSockets()));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "cpu_per_socket");
        _writer.writeRaw(String.valueOf(_vmTemplate.getCpuPerSocket()));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "threads_per_cpu");
        _writer.writeRaw(String.valueOf(_vmTemplate.getThreadsPerCpu()));
        _writer.writeEndElement();
        _writer.writeEndElement(); // item

        // item memory
        _writer.writeStartElement("Item");
        _writer.writeStartElement(RASD_URI, "Caption");
        _writer.writeRaw(String.format("%1$s MB of memory", _vmTemplate.getMemSizeMb()));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "Description");
        _writer.writeRaw("Memory Size");
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "InstanceId");
        _writer.writeRaw(String.valueOf(++_instanceId));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "ResourceType");
        _writer.writeRaw(OvfHardware.Memory);
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "AllocationUnits");
        _writer.writeRaw("MegaBytes");
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "VirtualQuantity");
        _writer.writeRaw(String.valueOf(_vmTemplate.getMemSizeMb()));
        _writer.writeEndElement();
        _writer.writeEndElement(); // item

        // item drive
        for (DiskImage image : _images) {
            _writer.writeStartElement("Item");
            _writer.writeStartElement(RASD_URI, "Caption");
            _writer.writeRaw(getBackwardCompatibleDiskAlias(image.getDiskAlias()));
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "InstanceId");
            _writer.writeRaw(image.getImageId().toString());
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "ResourceType");
            _writer.writeRaw(OvfHardware.DiskImage);
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "HostResource");
            _writer.writeRaw(image.getId() + "/" + image.getImageId());
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "Parent");
            _writer.writeRaw(image.getParentId().toString());
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "Template");
            _writer.writeRaw(image.getImageTemplateId().toString());
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "ApplicationList");
            _writer.writeRaw(image.getAppList());
            _writer.writeEndElement();
            if (image.getStorageIds() != null && image.getStorageIds().size() > 0) {
                _writer.writeStartElement(RASD_URI, "StorageId");
                _writer.writeRaw(image.getStorageIds().get(0).toString());
                _writer.writeEndElement();
            }
            if (image.getStoragePoolId() != null) {
                _writer.writeStartElement(RASD_URI, "StoragePoolId");
                _writer.writeRaw(image.getStoragePoolId().toString());
                _writer.writeEndElement();
            }
            _writer.writeStartElement(RASD_URI, "CreationDate");
            _writer.writeRaw(OvfParser.localDateToUtcDateString(image.getCreationDate()));
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "LastModified");
            _writer.writeRaw(OvfParser.localDateToUtcDateString(image.getLastModified()));
            _writer.writeEndElement();
            writeManagedDeviceInfo(_vmTemplate, _writer, image.getId());
            _writer.writeEndElement(); // item
        }

        // item network
        for (VmNetworkInterface iface : _vmTemplate.getInterfaces()) {
            _writer.writeStartElement("Item");
            _writer.writeStartElement(RASD_URI, "Caption");
            String networkName = iface.getNetworkName() != null ? iface.getNetworkName() : "[No Network]";
            _writer.writeRaw("Ethernet adapter on " + networkName);
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "InstanceId");
            _writer.writeRaw(iface.getId().toString());
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "ResourceType");
            _writer.writeRaw(OvfHardware.Network);
            _writer.writeEndElement();

            _writer.writeStartElement(RASD_URI, "OtherResourceType");
            if (StringUtils.isNotEmpty(iface.getVnicProfileName())) {
                _writer.writeRaw(iface.getVnicProfileName());
            }
            _writer.writeEndElement();

            _writer.writeStartElement(RASD_URI, "ResourceSubType");
            if (iface.getType() != null) {
                _writer.writeRaw(iface.getType().toString());
            }
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "Connection");
            if (iface.getNetworkName() != null) {
                _writer.writeRaw(iface.getNetworkName());
            }
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "Linked");
            _writer.writeRaw(String.valueOf(iface.isLinked()));
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "Name");
            _writer.writeRaw(iface.getName());
            _writer.writeEndElement();
            _writer.writeStartElement(RASD_URI, "speed");
            // version prior to 2.3 may not have speed so we get it by type
            if (iface.getSpeed() != null) {
                _writer.writeRaw(iface.getSpeed().toString());
            } else {
                _writer.writeRaw(String.valueOf(VmInterfaceType.forValue(
                        iface.getType()).getSpeed()));
            }
            _writer.writeEndElement();
            writeManagedDeviceInfo(_vmTemplate, _writer, iface.getId());
            _writer.writeEndElement(); // item
        }

        // item usb
        _writer.writeStartElement("Item");
        _writer.writeStartElement(RASD_URI, "Caption");
        _writer.writeRaw("USB Controller");
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "InstanceId");
        _writer.writeRaw(String.valueOf(++_instanceId));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "ResourceType");
        _writer.writeRaw(OvfHardware.USB);
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "UsbPolicy");
        _writer.writeRaw(getBackwardCompatibleUsbPolicy(_vmTemplate.getUsbPolicy()));
        _writer.writeEndElement();
        _writer.writeEndElement(); // item

        // monitors
        writeMonitors(_vmTemplate);
        // graphics
        writeGraphics(_vmTemplate);
        // CD
        writeCd(_vmTemplate);
        // unmanaged devices
        writeOtherDevices(_vmTemplate, _writer);

        // End hardware section
        _writer.writeEndElement();
    }
}
