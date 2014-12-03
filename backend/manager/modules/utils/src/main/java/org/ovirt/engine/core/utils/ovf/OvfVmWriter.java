package org.ovirt.engine.core.utils.ovf;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
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
        _writer.WriteStartElement(OvfProperties.NAME);
        _writer.WriteRaw(_vm.getStaticData().getName());
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.TEMPLATE_ID);
        _writer.WriteRaw(_vm.getStaticData().getVmtGuid().toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.TEMPLATE_NAME);
        _writer.WriteRaw(_vm.getVmtName());
        _writer.WriteEndElement();
        if (_vm.getInstanceTypeId() != null ) {
            _writer.WriteStartElement(OvfProperties.INSTANCE_TYPE_ID);
            _writer.WriteRaw(_vm.getInstanceTypeId().toString());
            _writer.WriteEndElement();
        }
        if (_vm.getImageTypeId() != null ) {
            _writer.WriteStartElement(OvfProperties.IMAGE_TYPE_ID);
            _writer.WriteRaw(_vm.getImageTypeId().toString());
            _writer.WriteEndElement();
        }
        _writer.WriteStartElement(OvfProperties.IS_INITIALIZED);
        _writer.WriteRaw(String.valueOf(_vm.getStaticData().isInitialized()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.ORIGIN);
        _writer.WriteRaw(String.valueOf(_vm.getOrigin().getValue()));
        _writer.WriteEndElement();
        if (!StringUtils.isBlank(_vm.getAppList())) {
            _writer.WriteStartElement(OvfProperties.APPLICATIONS_LIST);
            _writer.WriteRaw(_vm.getAppList());
            _writer.WriteEndElement();
        }
        if (_vm.getQuotaId() != null) {
            _writer.WriteStartElement(OvfProperties.QUOTA_ID);
            _writer.WriteRaw(_vm.getQuotaId().toString());
            _writer.WriteEndElement();
        }
        _writer.WriteStartElement(OvfProperties.VM_DEFAULT_DISPLAY_TYPE);
        _writer.WriteRaw(String.valueOf(_vm.getDefaultDisplayType().getValue()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(OvfProperties.TRUSTED_SERVICE);
        _writer.WriteRaw(String.valueOf(_vm.isTrustedService()));
        _writer.WriteEndElement();

        if (_vm.getStaticData().getOriginalTemplateGuid() != null) {
            _writer.WriteStartElement(OvfProperties.ORIGINAL_TEMPLATE_ID);
            _writer.WriteRaw(_vm.getStaticData().getOriginalTemplateGuid().toString());
            _writer.WriteEndElement();
        }

        if (_vm.getStaticData().getOriginalTemplateName() != null) {
            _writer.WriteStartElement(OvfProperties.ORIGINAL_TEMPLATE_NAME);
            _writer.WriteRaw(_vm.getStaticData().getOriginalTemplateName());
            _writer.WriteEndElement();
        }

        _writer.WriteStartElement(OvfProperties.USE_LATEST_VERSION);
        _writer.WriteRaw(String.valueOf(_vm.isUseLatestVersion()));
        _writer.WriteEndElement();

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
        _writer.WriteStartElement(fullNameSB.toString());
        _writer.WriteRaw(value);
        _writer.WriteEndElement();

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

                _writer.WriteStartElement("ProductSection");
                _writer.WriteAttributeString(OVF_URI, "class", product);
                _writer.WriteStartElement("Info");
                _writer.WriteRaw(app);
                _writer.WriteEndElement();
                _writer.WriteStartElement("Product");
                _writer.WriteRaw(product);
                _writer.WriteEndElement();
                _writer.WriteStartElement("Version");
                _writer.WriteRaw(version);
                _writer.WriteEndElement();
                _writer.WriteEndElement();
            }
        }
    }

    @Override
    protected void writeContentItems() {
        // os
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString(OVF_URI, "id", vmBase.getId().toString());
        _writer.WriteAttributeString(OVF_URI, "required", "false");
        _writer.WriteAttributeString(XSI_URI, "type", "ovf:OperatingSystemSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw("Guest Operating System");
        _writer.WriteEndElement();
        _writer.WriteStartElement("Description");
        _writer.WriteRaw(osRepository.getUniqueOsNames().get(vmBase.getOsId()));
        _writer.WriteEndElement();
        _writer.WriteEndElement();

        // hardware
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString(XSI_URI, "type", "ovf:VirtualHardwareSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw(String.format("%1$s CPU, %2$s Memeory", _vm.getStaticData().getNumOfCpus(), _vm
                .getStaticData().getMemSizeMb()));
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
        _writer.WriteRaw(String.format("%1$s virtual cpu", _vm.getStaticData().getNumOfCpus()));
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
        _writer.WriteRaw(String.valueOf(vmBase.getNumOfSockets()));
        _writer.WriteEndElement();
        _writer.WriteStartElement(RASD_URI, "cpu_per_socket");
        _writer.WriteRaw(String.valueOf(vmBase.getCpuPerSocket()));
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // item memory
        _writer.WriteStartElement("Item");
        _writer.WriteStartElement(RASD_URI, "Caption");
        _writer.WriteRaw(String.format("%1$s MB of memory", vmBase.getMemSizeMb()));
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
        _writer.WriteRaw(String.valueOf(vmBase.getMemSizeMb()));
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
            _writer.WriteRaw(OvfParser.CreateImageFile(image));
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
            _writer.WriteStartElement(RASD_URI, "last_modified_date");
            _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(image.getLastModifiedDate()));
            _writer.WriteEndElement();
            writeManagedDeviceInfo(vmBase, _writer, image.getId());
            _writer.WriteEndElement(); // item
        }

        // item network
        for (VmNetworkInterface iface : _vm.getInterfaces()) {
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
            _writer.WriteStartElement(RASD_URI, "MACAddress");
            _writer.WriteRaw(iface.getMacAddress());
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
            writeManagedDeviceInfo(vmBase, _writer, iface.getId());
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
        _writer.WriteRaw(getBackwardCompatibleUsbPolicy(vmBase.getUsbPolicy()));
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // monitors
        writeMonitors(vmBase);
        // graphics
        writeGraphics(vmBase);
        // CD
        writeCd(vmBase);
        // ummanged devices
        writeOtherDevices(vmBase, _writer);

        // End hardware section
        _writer.WriteEndElement();

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

        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString(XSI_URI, "type", "ovf:SnapshotsSection_Type");

        for (Snapshot snapshot : snapshots) {
            _writer.WriteStartElement("Snapshot");
            _writer.WriteAttributeString(OVF_URI, "id", snapshot.getId().toString());
            _writer.writeElement("Type", snapshot.getType().name());
            _writer.writeElement("Description", snapshot.getDescription());
            _writer.writeElement("CreationDate", OvfParser.LocalDateToUtcDateString(snapshot.getCreationDate()));

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

            _writer.WriteEndElement();
        }

        _writer.WriteEndElement();
    }
}
