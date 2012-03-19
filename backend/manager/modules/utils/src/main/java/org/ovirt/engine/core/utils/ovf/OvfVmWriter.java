package org.ovirt.engine.core.utils.ovf;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Match;
import org.ovirt.engine.core.compat.RefObject;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.backendcompat.XmlDocument;

public class OvfVmWriter extends OvfWriter {
    private static final String EXPORT_ONLY_PREFIX = "exportonly_";

    public OvfVmWriter(RefObject<XmlDocument> document, VM vm, java.util.ArrayList<DiskImage> images) {
        super(document, vm.getStaticData(), images);
        _vm = vm;
    }

    @Override
    protected void WriteGeneralData() {
        _writer.WriteStartElement("Name");
        _writer.WriteRaw(_vm.getStaticData().getvm_name());
        _writer.WriteEndElement();
        _writer.WriteStartElement("TemplateId");
        _writer.WriteRaw(_vm.getStaticData().getvmt_guid().toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("TemplateName");
        _writer.WriteRaw(_vm.getvmt_name().toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("Description");
        _writer.WriteRaw(vmBase.getdescription());
        _writer.WriteEndElement();
        _writer.WriteStartElement("Domain");
        _writer.WriteRaw(vmBase.getdomain());
        _writer.WriteEndElement();
        _writer.WriteStartElement("CreationDate");
        _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(vmBase.getcreation_date()));
        _writer.WriteEndElement();
        _writer.WriteStartElement("ExportDate");
        _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(new java.util.Date()));
        _writer.WriteEndElement();
        _writer.WriteStartElement("IsInitilized");
        _writer.WriteRaw((new Boolean(_vm.getStaticData().getis_initialized())).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("IsAutoSuspend");
        _writer.WriteRaw((new Boolean(vmBase.getis_auto_suspend())).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("TimeZone");
        _writer.WriteRaw(vmBase.gettime_zone());
        _writer.WriteEndElement();
        _writer.WriteStartElement("IsStateless");
        _writer.WriteRaw((new Boolean(vmBase.getis_stateless())).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("Origin");
        _writer.WriteRaw((new Integer(_vm.getorigin().getValue())).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("default_boot_sequence");
        _writer.WriteRaw((new Integer(_vm.getdefault_boot_sequence().getValue())).toString());
        _writer.WriteEndElement();

        if (!StringHelper.isNullOrEmpty(_vm.getinitrd_url())) {
            _writer.WriteStartElement("initrd_url");
            _writer.WriteRaw(_vm.getinitrd_url());
            _writer.WriteEndElement();
        }
        if (!StringHelper.isNullOrEmpty(_vm.getkernel_url())) {
            _writer.WriteStartElement("kernel_url");
            _writer.WriteRaw(_vm.getkernel_url());
            _writer.WriteEndElement();
        }
        if (!StringHelper.isNullOrEmpty(_vm.getkernel_params())) {
            _writer.WriteStartElement("kernel_params");
            _writer.WriteRaw(_vm.getkernel_params());
            _writer.WriteEndElement();
        }
        if (!StringHelper.isNullOrEmpty(_vm.getapp_list())) {
            _writer.WriteStartElement("app_list");
            _writer.WriteRaw(_vm.getapp_list());
            _writer.WriteEndElement();
        }
        _writer.WriteStartElement("VmType");
        _writer.WriteRaw((new Integer(_vm.getvm_type().getValue())).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("DefaultDisplayType");
        _writer.WriteRaw((new Integer(_vm.getdefault_display_type().getValue())).toString());
        _writer.WriteEndElement();

        _writer.WriteStartElement("MinAllocatedMem");
        _writer.WriteRaw((new Integer(_vm.getMinAllocatedMem())).toString());
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
            if (StringHelper.isNullOrEmpty(_images.get(0).getappList())) {
                return;
            }

            String[] apps = _images.get(0).getappList().split("[,]", -1);
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
                _writer.WriteAttributeString("ovf", "class", null, product);
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
        _writer.WriteAttributeString("ovf", "id", null, vmBase.getId().toString());
        _writer.WriteAttributeString("ovf", "required", null, "false");
        _writer.WriteAttributeString("xsi", "type", null, "ovf:OperatingSystemSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw("Guest Operating System");
        _writer.WriteEndElement();
        _writer.WriteStartElement("Description");
        _writer.WriteRaw(vmBase.getos().name());
        _writer.WriteEndElement();
        _writer.WriteEndElement();

        // hardware
        _writer.WriteStartElement("Section");
        _writer.WriteAttributeString("xsi", "type", null, "ovf:VirtualHardwareSection_Type");
        _writer.WriteStartElement("Info");
        _writer.WriteRaw(String.format("%1$s CPU, %2$s Memeory", _vm.getStaticData().getnum_of_cpus(), _vm
                .getStaticData().getmem_size_mb()));
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
        _writer.WriteRaw(String.format("%1$s virtual cpu", _vm.getStaticData().getnum_of_cpus()));
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
        _writer.WriteRaw((new Integer(vmBase.getnum_of_sockets())).toString());
        _writer.WriteEndElement();
        _writer.WriteStartElement("rasd:cpu_per_socket");
        _writer.WriteRaw((new Integer(vmBase.getcpu_per_socket())).toString());
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // item memory
        _writer.WriteStartElement("Item");
        _writer.WriteStartElement("rasd:Caption");
        _writer.WriteRaw(String.format("%1$s MB of memory", vmBase.getmem_size_mb()));
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
        _writer.WriteRaw((new Integer(vmBase.getmem_size_mb())).toString());
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
            _writer.WriteRaw(OvfParser.CreateImageFile(image));
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
            _writer.WriteStartElement("rasd:last_modified_date");
            _writer.WriteRaw(OvfParser.LocalDateToUtcDateString(image.getlast_modified_date()));
            _writer.WriteEndElement();
            writeManagedDeviceInfo(vmBase, _writer, image.getimage_group_id());
            _writer.WriteEndElement(); // item
        }

        // item network
        for (VmNetworkInterface iface : _vm.getInterfaces()) {
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
            _writer.WriteStartElement("rasd:MACAddress");
            _writer.WriteRaw(iface.getMacAddress());
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
            writeManagedDeviceInfo(vmBase, _writer, iface.getId());
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
        _writer.WriteRaw(vmBase.getusb_policy().toString());
        _writer.WriteEndElement();
        _writer.WriteEndElement(); // item

        // monitors
        writeMonitors(vmBase);
        // CD
        writeCd(vmBase);
        // ummanged devices
        writeUnmanagedDevices(vmBase, _writer);
    }
}
