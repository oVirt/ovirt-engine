package org.ovirt.engine.core.utils.ovf;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Match;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;

public class OvfVmWriter extends OvfWriter {
    private static final String EXPORT_ONLY_PREFIX = "exportonly_";

    public OvfVmWriter(VM vm, List<DiskImage> images) {
        super(vm.getStaticData(), images);
        _vm = vm;
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.WriteStartElement("Name");
        _writer.WriteRaw(_vm.getStaticData().getName());
        _writer.WriteEndElement();
        _writer.WriteStartElement("TemplateId");
        _writer.WriteRaw(_vm.getStaticData().getVmtGuid().toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("TemplateName");
        _writer.WriteRaw(_vm.getVmtName());
        _writer.WriteEndElement();
        _writer.WriteStartElement("IsInitilized");
        _writer.WriteRaw(String.valueOf(_vm.getStaticData().isInitialized()));
        _writer.WriteEndElement();
        _writer.WriteStartElement("IsStateless");
        _writer.WriteRaw(String.valueOf(vmBase.isStateless()));
        _writer.WriteEndElement();
        _writer.WriteStartElement("Origin");
        _writer.WriteRaw(String.valueOf(_vm.getOrigin().getValue()));
        _writer.WriteEndElement();
        if (!StringUtils.isBlank(_vm.getAppList())) {
            _writer.WriteStartElement("app_list");
            _writer.WriteRaw(_vm.getAppList());
            _writer.WriteEndElement();
        }
        if (_vm.getQuotaId() != null) {
            _writer.WriteStartElement("quota_id");
            _writer.WriteRaw(_vm.getQuotaId().toString());
            _writer.WriteEndElement();
        }
        _writer.WriteStartElement("DefaultDisplayType");
        _writer.WriteRaw(String.valueOf(_vm.getDefaultDisplayType().getValue()));
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
    protected void WriteAppList() {
        if (_images.size() > 0) {
            if (StringUtils.isBlank(_images.get(0).getAppList())) {
                return;
            }

            String[] apps = _images.get(0).getAppList().split("[,]", -1);
            for (String app : apps) {
                String product = app;
                String version = "";
                Match match = Regex.Match(app, "(.*) ([0-9.]+)", RegexOptions.Singleline | RegexOptions.IgnoreCase);

                if (match.Groups().size() > 1) {
                    product = match.Groups().get(1).getValue(); // match.Groups[1].getValue();
                }
                if (match.Groups().size() > 2) {
                    version = match.Groups().get(2).getValue(); // match.Groups[2].getValue();
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
    protected void WriteContentItems() {
        // os
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString(OVF_URI, "id", vmBase.getId().toString());
        _writer.WriteAttributeString(OVF_URI, "required", "false");
        _writer.WriteAttributeString(XSI_URI, "type", "ovf:OperatingSystemSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw("Guest Operating System");
        _writer.WriteEndElement();
        _writer.WriteStartElement("Description");
        _writer.WriteRaw(vmBase.getOs().name());
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
        _writer.WriteRaw(String.format("%1$s %2$s", Config.<String> GetValue(ConfigValues.OvfVirtualSystemType),
                Config.<String> GetValue(ConfigValues.VdcVersion)));
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
            _writer.WriteRaw(image.getDiskAlias());
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
                _writer.WriteRaw(image.getStoragePoolId().getValue().toString());
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
        _writer.WriteRaw(vmBase.getUsbPolicy() != null ? vmBase.getUsbPolicy().toString() : UsbPolicy.DISABLED.name());
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // monitors
        writeMonitors(vmBase);
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
