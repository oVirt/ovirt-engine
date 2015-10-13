package org.ovirt.engine.core.utils.ovf;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Match;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;
import org.ovirt.engine.core.compat.Version;

public class OvfVmWriter extends OvfWriter {
    private static final String EXPORT_ONLY_PREFIX = "exportonly_";
    private OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);

    public OvfVmWriter(VM vm, List<DiskImage> images, Version version) {
        super(vm.getStaticData(), images, version);
        _vm = vm;
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.writeStartElement(OvfProperties.NAME);
        _writer.writeRaw(_vm.getStaticData().getName());
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TEMPLATE_ID);
        _writer.writeRaw(_vm.getStaticData().getVmtGuid().toString());
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TEMPLATE_NAME);
        _writer.writeRaw(_vm.getVmtName());
        _writer.writeEndElement();
        if (_vm.getInstanceTypeId() != null ) {
            _writer.writeStartElement(OvfProperties.INSTANCE_TYPE_ID);
            _writer.writeRaw(_vm.getInstanceTypeId().toString());
            _writer.writeEndElement();
        }
        if (_vm.getImageTypeId() != null ) {
            _writer.writeStartElement(OvfProperties.IMAGE_TYPE_ID);
            _writer.writeRaw(_vm.getImageTypeId().toString());
            _writer.writeEndElement();
        }
        _writer.writeStartElement(OvfProperties.IS_INITIALIZED);
        _writer.writeRaw(String.valueOf(_vm.getStaticData().isInitialized()));
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.ORIGIN);
        _writer.writeRaw(String.valueOf(_vm.getOrigin().getValue()));
        _writer.writeEndElement();
        if (!StringUtils.isBlank(_vm.getAppList())) {
            _writer.writeStartElement(OvfProperties.APPLICATIONS_LIST);
            _writer.writeRaw(_vm.getAppList());
            _writer.writeEndElement();
        }
        if (_vm.getQuotaId() != null) {
            _writer.writeStartElement(OvfProperties.QUOTA_ID);
            _writer.writeRaw(_vm.getQuotaId().toString());
            _writer.writeEndElement();
        }
        _writer.writeStartElement(OvfProperties.VM_DEFAULT_DISPLAY_TYPE);
        _writer.writeRaw(String.valueOf(_vm.getDefaultDisplayType().getValue()));
        _writer.writeEndElement();
        _writer.writeStartElement(OvfProperties.TRUSTED_SERVICE);
        _writer.writeRaw(String.valueOf(_vm.isTrustedService()));
        _writer.writeEndElement();

        if (_vm.getStaticData().getOriginalTemplateGuid() != null) {
            _writer.writeStartElement(OvfProperties.ORIGINAL_TEMPLATE_ID);
            _writer.writeRaw(_vm.getStaticData().getOriginalTemplateGuid().toString());
            _writer.writeEndElement();
        }

        if (_vm.getStaticData().getOriginalTemplateName() != null) {
            _writer.writeStartElement(OvfProperties.ORIGINAL_TEMPLATE_NAME);
            _writer.writeRaw(_vm.getStaticData().getOriginalTemplateName());
            _writer.writeEndElement();
        }

        _writer.writeStartElement(OvfProperties.USE_HOST_CPU);
        _writer.writeRaw(String.valueOf(_vm.getStaticData().isUseHostCpuFlags()));
        _writer.writeEndElement();

        _writer.writeStartElement(OvfProperties.USE_LATEST_VERSION);
        _writer.writeRaw(String.valueOf(_vm.isUseLatestVersion()));
        _writer.writeEndElement();

        OvfLogEventHandler<VmStatic> handler = new VMStaticOvfLogHandler(_vm.getStaticData());
        // Gets a map that its keys are aliases to fields that should be OVF
        // logged.
        Map<String, String> aliasesValuesMap = handler.getAliasesValuesMap();
        for (Map.Entry<String, String> entry : aliasesValuesMap.entrySet()) {
            writeLogEvent(entry.getKey(), entry.getValue());
        }

    }

    private void writeLogEvent(String name, String value) {
        StringBuilder fullNameSB = new StringBuilder(EXPORT_ONLY_PREFIX);
        fullNameSB.append(name);
        _writer.writeStartElement(fullNameSB.toString());
        _writer.writeRaw(value);
        _writer.writeEndElement();

    }

    @Override
    protected void writeAppList() {
        if (_images.size() > 0) {
            if (StringUtils.isBlank(_images.get(0).getAppList())) {
                return;
            }

            String[] apps = _images.get(0).getAppList().split("[,]", -1);
            for (String app : apps) {
                String product = app;
                String version = "";
                Match match = Regex.Match(app, "(.*) ([0-9.]+)", RegexOptions.Singleline | RegexOptions.IgnoreCase);

                if (match.groups().size() > 1) {
                    product = match.groups().get(1).getValue();
                }
                if (match.groups().size() > 2) {
                    version = match.groups().get(2).getValue();
                }

                _writer.writeStartElement("ProductSection");
                _writer.writeAttributeString(OVF_URI, "class", product);
                _writer.writeStartElement("Info");
                _writer.writeRaw(app);
                _writer.writeEndElement();
                _writer.writeStartElement("Product");
                _writer.writeRaw(product);
                _writer.writeEndElement();
                _writer.writeStartElement("Version");
                _writer.writeRaw(version);
                _writer.writeEndElement();
                _writer.writeEndElement();
            }
        }
    }

    @Override
    protected void writeContentItems() {
        // os
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(OVF_URI, "id", vmBase.getId().toString());
        _writer.writeAttributeString(OVF_URI, "required", "false");
        _writer.writeAttributeString(XSI_URI, "type", "ovf:OperatingSystemSection_Type");
        _writer.writeStartElement("Info");
        _writer.writeRaw("Guest Operating System");
        _writer.writeEndElement();
        _writer.writeStartElement("Description");
        _writer.writeRaw(osRepository.getUniqueOsNames().get(vmBase.getOsId()));
        _writer.writeEndElement();
        _writer.writeEndElement();

        // hardware
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", "ovf:VirtualHardwareSection_Type");
        _writer.writeStartElement("Info");
        _writer.writeRaw(String.format("%1$s CPU, %2$s Memeory", _vm.getStaticData().getNumOfCpus(), _vm
                .getStaticData().getMemSizeMb()));
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
        _writer.writeRaw(String.format("%1$s virtual cpu", _vm.getStaticData().getNumOfCpus()));
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
        _writer.writeRaw(String.valueOf(vmBase.getNumOfSockets()));
        _writer.writeEndElement();
        _writer.writeStartElement(RASD_URI, "cpu_per_socket");
        _writer.writeRaw(String.valueOf(vmBase.getCpuPerSocket()));
        _writer.writeEndElement();
        _writer.writeEndElement(); // item

        // item memory
        _writer.writeStartElement("Item");
        _writer.writeStartElement(RASD_URI, "Caption");
        _writer.writeRaw(String.format("%1$s MB of memory", vmBase.getMemSizeMb()));
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
        _writer.writeRaw(String.valueOf(vmBase.getMemSizeMb()));
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
            _writer.writeRaw(OvfParser.createImageFile(image));
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
            _writer.writeStartElement(RASD_URI, "last_modified_date");
            _writer.writeRaw(OvfParser.localDateToUtcDateString(image.getLastModifiedDate()));
            _writer.writeEndElement();
            writeManagedDeviceInfo(vmBase, _writer, image.getId());
            _writer.writeEndElement(); // item
        }

        // item network
        for (VmNetworkInterface iface : _vm.getInterfaces()) {
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
            _writer.writeStartElement(RASD_URI, "MACAddress");
            _writer.writeRaw(iface.getMacAddress());
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
            writeManagedDeviceInfo(vmBase, _writer, iface.getId());
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
        _writer.writeRaw(getBackwardCompatibleUsbPolicy(vmBase.getUsbPolicy()));
        _writer.writeEndElement();
        _writer.writeEndElement(); // item

        // monitors
        writeMonitors(vmBase);
        // graphics
        writeGraphics(vmBase);
        // CD
        writeCd(vmBase);
        // unmanaged devices
        writeOtherDevices(vmBase, _writer);

        // End hardware section
        _writer.writeEndElement();

        writeSnapshotsSection();
    }

    /**
     * Write the snapshots of the VM.<br>
     * If no snapshots were set to be written, this section will not be written.
     */
    private void writeSnapshotsSection() {
        List<Snapshot> snapshots = _vm.getSnapshots();
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }

        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", "ovf:SnapshotsSection_Type");

        for (Snapshot snapshot : snapshots) {
            _writer.writeStartElement("Snapshot");
            _writer.writeAttributeString(OVF_URI, "id", snapshot.getId().toString());
            _writer.writeElement("Type", snapshot.getType().name());
            _writer.writeElement("Description", snapshot.getDescription());
            _writer.writeElement("CreationDate", OvfParser.localDateToUtcDateString(snapshot.getCreationDate()));

            if (!snapshot.getMemoryVolume().isEmpty()) {
                _writer.writeElement("Memory", snapshot.getMemoryVolume());
            }

            if (snapshot.getAppList() != null) {
                _writer.writeElement("ApplicationList", snapshot.getAppList());
            }

            if (snapshot.getVmConfiguration() != null) {
                _writer.writeElement("VmConfiguration",
                        Base64.encodeBase64String(snapshot.getVmConfiguration().getBytes()));
            }

            _writer.writeEndElement();
        }

        _writer.writeEndElement();
    }
}
