package org.ovirt.engine.core.utils.ovf;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.utils.VmCpuCountHelper;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class OvfVmWriter extends OvfOvirtWriter {
    private static final String EXPORT_ONLY_PREFIX = "exportonly_";
    private VM vm;
    private Map<Guid, DiskImage> memoryDisks;


    // Since the writer does not have access to the DAO we transfer the 'memoryDisks' parameter which contains
    // a mapping between the memory disks of all snapshots to the corresponding DiskImage object which contains
    // the full info needed to write in the OVF (storage domain ID, image ID, volume ID)
    public OvfVmWriter(VM vm, FullEntityOvfData fullEntityOvfData, Version version, OsRepository osRepository,
            Map<Guid, DiskImage> memoryDisks) {
        super(fullEntityOvfData, version, osRepository);
        this.vm = vm;
        this.memoryDisks = memoryDisks;
    }

    @Override
    protected void writeGeneralData() {
        super.writeGeneralData();
        _writer.writeElement(TEMPLATE_ID, vm.getVmtGuid().toString());
        _writer.writeElement(TEMPLATE_NAME, vm.getVmtName());
        if (vm.getInstanceTypeId() != null ) {
            _writer.writeElement(INSTANCE_TYPE_ID, vm.getInstanceTypeId().toString());
        }
        if (vm.getImageTypeId() != null ) {
            _writer.writeElement(IMAGE_TYPE_ID, vm.getImageTypeId().toString());
        }
        _writer.writeElement(IS_INITIALIZED, String.valueOf(vm.isInitialized()));
        _writer.writeElement(ORIGIN, String.valueOf(vm.getOrigin().getValue()));
        if (!StringUtils.isBlank(vm.getAppList())) {
            _writer.writeElement(APPLICATIONS_LIST, vm.getAppList());
        }
        if (vm.getQuotaId() != null) {
            _writer.writeElement(QUOTA_ID, vm.getQuotaId().toString());
        }
        _writer.writeElement(VM_DEFAULT_DISPLAY_TYPE, String.valueOf(vm.getDefaultDisplayType().getValue()));
        _writer.writeElement(TRUSTED_SERVICE, String.valueOf(vm.isTrustedService()));

        if (vm.getOriginalTemplateGuid() != null) {
            _writer.writeElement(ORIGINAL_TEMPLATE_ID, vm.getOriginalTemplateGuid().toString());
        }

        if (vm.getOriginalTemplateName() != null) {
            _writer.writeElement(ORIGINAL_TEMPLATE_NAME, vm.getOriginalTemplateName());
        }

        if (vm.getCpuPinning() != null) {
            _writer.writeElement(CPU_PINNING, vm.getCpuPinning());
        }

        _writer.writeElement(USE_LATEST_VERSION, String.valueOf(vm.isUseLatestVersion()));

        OvfLogEventHandler<VmStatic> handler = new VMStaticOvfLogHandler(vm.getStaticData());
        // Gets a map that its keys are aliases to fields that should be OVF
        // logged.
        Map<String, String> aliasesValuesMap = handler.getAliasesValuesMap();
        for (Map.Entry<String, String> entry : aliasesValuesMap.entrySet()) {
            writeLogEvent(entry.getKey(), entry.getValue());
        }

        if (vm.getLastStopTime() != null) {
            _writer.writeElement(STOP_TIME, OvfParser.localDateToUtcDateString(vm.getLastStopTime()));
        }

        if (vm.getBootTime() != null) {
            _writer.writeElement(BOOT_TIME, OvfParser.localDateToUtcDateString(vm.getBootTime()));
            _writer.writeElement(DOWNTIME, String.valueOf(vm.getDowntime()));
        }

        writeAffinityGroups();
        writeAffinityLabels();
        writeNumaNodeList();
    }

    private void writeLogEvent(String name, String value) {
        StringBuilder fullNameSB = new StringBuilder(EXPORT_ONLY_PREFIX);
        fullNameSB.append(name);
        _writer.writeElement(fullNameSB.toString(), value);
    }

    @Override
    protected Integer maxNumOfVcpus() {
        return VmCpuCountHelper.calcMaxVCpu(vm, getVersion());
    }

    @Override
    protected void writeHardware() {
        super.writeHardware();
        writeSnapshotsSection();
    }

    protected boolean isSpecialDevice(VmDevice vmDevice) {
        return VmDeviceCommonUtils.isSpecialDevice(vmDevice.getDevice(), vmDevice.getType(), true);
    }

    /**
     * Write the numa nodes of the VM.<br>
     * If no numa nodes were set to be written, this section will not be written.
     */
    private void writeNumaNodeList() {
        List<VmNumaNode> vmNumaNodes = vm.getvNumaNodeList();

        if (vmNumaNodes == null || vmNumaNodes.isEmpty()) {
            return;
        }
        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", "ovf:NumaNodeSection_Type");

        for (VmNumaNode vmNumaNode : vmNumaNodes) {
            _writer.writeStartElement("NumaNode");
            if (vmNumaNode.getId() != null) {
                _writer.writeElement("id", String.valueOf(vmNumaNode.getId()));
            }
            _writer.writeElement("Index", String.valueOf(vmNumaNode.getIndex()));
            writeIntegerList("cpuIdList", vmNumaNode.getCpuIds());
            writeIntegerList("vdsNumaNodeList", vmNumaNode.getVdsNumaNodeList());
            _writer.writeElement("MemTotal", String.valueOf(vmNumaNode.getMemTotal()));
            _writer.writeElement(NUMA_TUNE_MODE, vmNumaNode.getNumaTuneMode().getValue());
            _writer.writeEndElement();
        }
        _writer.writeEndElement();
    }

    /**
     * Write the snapshots of the VM.<br>
     * If no snapshots were set to be written, this section will not be written.
     */
    private void writeSnapshotsSection() {
        List<Snapshot> snapshots = vm.getSnapshots();
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }

        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", "ovf:SnapshotsSection_Type");

        for (Snapshot snapshot : snapshots) {
            _writer.writeStartElement("Snapshot");
            _writer.writeAttributeString(getOvfUri(), "id", snapshot.getId().toString());
            _writer.writeElement("Type", snapshot.getType().name());
            _writer.writeElement("Description", snapshot.getDescription());
            _writer.writeElement("CreationDate", OvfParser.localDateToUtcDateString(snapshot.getCreationDate()));

            if (snapshot.containsMemory()) {
                DiskImage memoryDump = memoryDisks.get(snapshot.getMemoryDiskId());
                DiskImage memoryConf = memoryDisks.get(snapshot.getMetadataDiskId());
                String memoryVolume = String.format("%1$s,%2$s,%3$s,%4$s,%5$s,%6$s",
                        memoryDump.getStorageIds().get(0),
                        memoryDump.getStoragePoolId(),
                        memoryDump.getId(),
                        memoryDump.getImageId(),
                        memoryConf.getId(),
                        memoryConf.getImageId());
                _writer.writeElement("Memory", memoryVolume);
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

    private void writeAffinityGroups() {
        List<AffinityGroup> affinityGroups = fullEntityOvfData.getAffinityGroups();
        if (affinityGroups == null || affinityGroups.isEmpty()) {
            return;
        }

        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", "ovf:AffinityGroupsSection_Type");

        affinityGroups.forEach(affinityGroup -> {
            _writer.writeStartElement(OvfProperties.AFFINITY_GROUP);
            _writer.writeAttributeString(getOvfUri(), "name", affinityGroup.getName());
            _writer.writeEndElement();
        });

        _writer.writeEndElement();
    }

    private void writeAffinityLabels() {
        List<Label> affinityLabelsNames = fullEntityOvfData.getAffinityLabels();
        if (affinityLabelsNames == null || affinityLabelsNames.isEmpty()) {
            return;
        }

        _writer.writeStartElement("Section");
        _writer.writeAttributeString(XSI_URI, "type", "ovf:AffinityLabelsSection_Type");

        affinityLabelsNames.forEach(label -> {
            _writer.writeStartElement(OvfProperties.AFFINITY_LABEL);
            _writer.writeAttributeString(getOvfUri(), "name", label.getName());
            _writer.writeEndElement();
        });

        _writer.writeEndElement();
    }
}
