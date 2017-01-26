package org.ovirt.engine.core.utils.ovf;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.compat.Match;
import org.ovirt.engine.core.compat.Regex;
import org.ovirt.engine.core.compat.RegexOptions;
import org.ovirt.engine.core.compat.Version;

public class OvfVmWriter extends OvfWriter {
    private static final String EXPORT_ONLY_PREFIX = "exportonly_";
    private VM _vm;

    public OvfVmWriter(VM vm, List<DiskImage> images, Version version) {
        super(vm.getStaticData(), images, version);
        _vm = vm;
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.writeElement(OvfProperties.NAME, _vm.getStaticData().getName());
        _writer.writeElement(OvfProperties.TEMPLATE_ID, _vm.getStaticData().getVmtGuid().toString());
        _writer.writeElement(OvfProperties.TEMPLATE_NAME, _vm.getVmtName());
        if (_vm.getInstanceTypeId() != null ) {
            _writer.writeElement(OvfProperties.INSTANCE_TYPE_ID, _vm.getInstanceTypeId().toString());
        }
        if (_vm.getImageTypeId() != null ) {
            _writer.writeElement(OvfProperties.IMAGE_TYPE_ID, _vm.getImageTypeId().toString());
        }
        _writer.writeElement(OvfProperties.IS_INITIALIZED, String.valueOf(_vm.getStaticData().isInitialized()));
        _writer.writeElement(OvfProperties.ORIGIN, String.valueOf(_vm.getOrigin().getValue()));
        if (!StringUtils.isBlank(_vm.getAppList())) {
            _writer.writeElement(OvfProperties.APPLICATIONS_LIST, _vm.getAppList());
        }
        if (_vm.getQuotaId() != null) {
            _writer.writeElement(OvfProperties.QUOTA_ID, _vm.getQuotaId().toString());
        }
        _writer.writeElement(OvfProperties.VM_DEFAULT_DISPLAY_TYPE, String.valueOf(_vm.getDefaultDisplayType().getValue()));
        _writer.writeElement(OvfProperties.TRUSTED_SERVICE, String.valueOf(_vm.isTrustedService()));

        if (_vm.getStaticData().getOriginalTemplateGuid() != null) {
            _writer.writeElement(OvfProperties.ORIGINAL_TEMPLATE_ID, _vm.getStaticData().getOriginalTemplateGuid().toString());
        }

        if (_vm.getStaticData().getOriginalTemplateName() != null) {
            _writer.writeElement(OvfProperties.ORIGINAL_TEMPLATE_NAME, _vm.getStaticData().getOriginalTemplateName());
        }

        _writer.writeElement(OvfProperties.USE_HOST_CPU, String.valueOf(_vm.getStaticData().isUseHostCpuFlags()));
        _writer.writeElement(OvfProperties.USE_LATEST_VERSION, String.valueOf(_vm.isUseLatestVersion()));

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
        _writer.writeElement(fullNameSB.toString(), value);
    }

    @Override
    protected Integer maxNumOfVcpus() {
        return VmCpuCountHelper.calcMaxVCpu(_vm, getVersion());
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
                Match match = Regex.match(app, "(.*) ([0-9.]+)", RegexOptions.Singleline | RegexOptions.IgnoreCase);

                if (match.groups().size() > 1) {
                    product = match.groups().get(1).getValue();
                }
                if (match.groups().size() > 2) {
                    version = match.groups().get(2).getValue();
                }

                _writer.writeStartElement("ProductSection");
                _writer.writeAttributeString(OVF_URI, "class", product);
                _writer.writeElement("Info", app);
                _writer.writeElement("Product", product);
                _writer.writeElement("Version", version);
                _writer.writeEndElement();
            }
        }
    }

    @Override
    protected void writeContentItems() {
        super.writeContentItems();
        writeSnapshotsSection();
    }

    @Override
    protected void writeMacAddress(VmNetworkInterface iface) {
        _writer.writeStartElement(RASD_URI, "MACAddress");
        _writer.writeRaw(iface.getMacAddress());
        _writer.writeEndElement();
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
