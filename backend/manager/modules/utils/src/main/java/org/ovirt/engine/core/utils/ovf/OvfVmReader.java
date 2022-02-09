package org.ovirt.engine.core.utils.ovf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.businessentities.NumaTuneMode;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.utils.customprop.VmPropertiesUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;

public class OvfVmReader extends OvfOvirtReader {
    private static final String EXPORT_ONLY_PREFIX = "exportonly_";
    protected VM _vm;

    public OvfVmReader(XmlDocument document,
            VM vm,
            FullEntityOvfData fullEntityOvfData,
            OsRepository osRepository) {
        super(document, fullEntityOvfData, osRepository);
        _vm = vm;
        _vm.setInterfaces(fullEntityOvfData.getInterfaces());
    }

    @Override
    protected void updateSingleNic(XmlNode node, VmNetworkInterface iface, int nicIdx) {
        super.updateSingleNic(node, iface, nicIdx);
        XmlNode macAddress = selectSingleNode(node, "rasd:MACAddress", _xmlNS);
        iface.setMacAddress(macAddress != null ? macAddress.innerText : null);
    }

    @Override
    protected void readGeneralData(XmlNode content) {
        super.readGeneralData(content);
        consumeReadProperty(content, TEMPLATE_ID, val -> _vm.getStaticData().setVmtGuid(new Guid(val)));
        consumeReadProperty(content, TEMPLATE_NAME, val -> _vm.setVmtName(val));
        consumeReadProperty(content, INSTANCE_TYPE_ID, val -> _vm.setInstanceTypeId(new Guid(val)));
        consumeReadProperty(content, IMAGE_TYPE_ID, val -> _vm.setImageTypeId(new Guid(val)));
        consumeReadProperty(content, IS_INITIALIZED, val -> _vm.setInitialized(Boolean.parseBoolean(val)));
        consumeReadProperty(content, QUOTA_ID, val -> _vm.setQuotaId(new Guid(val)));
        consumeReadProperty(content, CPU_PINNING, _vm::setCpuPinning);
        if (!StringUtils.isBlank(_vm.getCpuPinning())) {
            _vm.setCpuPinningPolicy(CpuPinningPolicy.MANUAL);
        }

        OvfLogEventHandler<VmStatic> handler = new VMStaticOvfLogHandler(_vm.getStaticData());
        // Gets a list of all the aliases of the fields that should be logged in
        // ovd For each one of these fields, the proper value will be read from
        // the ovf and field in vm static
        List<String> aliases = handler.getAliases();
        for (String alias : aliases) {
            String value = readEventLogValue(content, alias);
            if (StringUtils.isNotEmpty(value)) {
                handler.addValueForAlias(alias, value);

            }
        }

        // {@link VM#predefinedProperties} and {@link VM#userDefinedProperties}
        // are being set in the above alias handling, we need to update custom properties
        // to keep them consistent
        _vm.setCustomProperties(VmPropertiesUtils.getInstance().customProperties(_vm.getPredefinedProperties(),
                _vm.getUserDefinedProperties()));

        consumeReadProperty(content,
                BOOT_TIME,
                val -> _vm.setBootTime(OvfParser.utcDateStringToLocalDate(val)),
                () -> _vm.setBootTime(null));
        consumeReadProperty(content,
                DOWNTIME,
                val -> _vm.setDowntime(Long.parseLong(val)),
                () -> _vm.setDowntime(0));
        consumeReadProperty(content, APPLICATIONS_LIST, val -> _vm.setAppList(val), () -> {
            // if no app list in VM, get it from one of the leafs
            if (_images != null && _images.size() > 0) {
                int root = getFirstImage(_images, _images.get(0));
                if (root != -1) {
                    for (int i = 0; i < _images.size(); i++) {
                        int x = getNextImage(_images, _images.get(i));
                        if (x == -1) {
                            _vm.setAppList(_images.get(i).getAppList());
                        }
                    }
                } else {
                    _vm.setAppList(_images.get(0).getAppList());
                }
            }
        });
        consumeReadProperty(content, TRUSTED_SERVICE, val -> _vm.setTrustedService(Boolean.parseBoolean(val)));
        consumeReadProperty(content, ORIGINAL_TEMPLATE_ID, val -> _vm.setOriginalTemplateGuid(new Guid(val)));
        consumeReadProperty(content, ORIGINAL_TEMPLATE_NAME, val -> _vm.getStaticData().setOriginalTemplateName(val));
        consumeReadProperty(content, USE_LATEST_VERSION, val -> _vm.setUseLatestVersion(Boolean.parseBoolean(val)));
        consumeReadProperty(content, STOP_TIME, val -> _vm.setLastStopTime(OvfParser.utcDateStringToLocalDate(val)));
    }

    @Override
    protected String getDefaultDisplayTypeStringRepresentation() {
        return VM_DEFAULT_DISPLAY_TYPE;
    }

    // function returns the index of the image that has no parent
    private static int getFirstImage(List<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (curr.getParentId().equals(images.get(i).getImageId())) {
                return i;
            }
        }
        return -1;
    }

    // function returns the index of image that is it's child
    private static int getNextImage(List<DiskImage> images, DiskImage curr) {
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getParentId().equals(curr.getImageId())) {
                return i;
            }
        }
        return -1;
    }

    private String readEventLogValue(XmlNode content, String name) {
        StringBuilder fullNameSB = new StringBuilder(EXPORT_ONLY_PREFIX);
        fullNameSB.append(name);
        XmlNode node = selectSingleNode(content, fullNameSB.toString());
        if (node != null) {
            return node.innerText;
        }
        return null;
    }


    protected void readNumaNodeListSection(XmlNode section) {
        XmlNodeList list = selectNodes(section, "NumaNode");
        List<VmNumaNode> vmNumaNodes = new ArrayList<>();
        _vm.setvNumaNodeList(vmNumaNodes);

        for (XmlNode node : list) {
            VmNumaNode vmNumaNode = new VmNumaNode();
            XmlNode id = selectSingleNode(node, "id", _xmlNS);
            if (id != null) {
                vmNumaNode.setId(new Guid(id.innerText));
            }
            vmNumaNode.setIndex(Integer.valueOf(selectSingleNode(node, "Index", _xmlNS).innerText));
            vmNumaNode.setCpuIds(readIntegerList(node, "cpuIdList"));
            vmNumaNode.setVdsNumaNodeList(readIntegerList(node, "vdsNumaNodeList"));
            vmNumaNode.setMemTotal(Long.valueOf(selectSingleNode(node, "MemTotal", _xmlNS).innerText));
            XmlNode numaTuneMode = selectSingleNode(node, NUMA_TUNE_MODE, _xmlNS);
            if (numaTuneMode != null) {
                vmNumaNode.setNumaTuneMode(NumaTuneMode.forValue(numaTuneMode.innerText));
            }
            vmNumaNodes.add(vmNumaNode);
        }
    }


    @Override
    protected void readSnapshotsSection(XmlNode section) {
        XmlNodeList list = selectNodes(section, "Snapshot");
        List<Snapshot> snapshots = new ArrayList<>();
        _vm.setSnapshots(snapshots);

        Map<Guid, List<DiskImage>> snapshotIdToDiskImagesMap = new HashMap<>();

        _images.forEach(diskImage -> snapshotIdToDiskImagesMap.computeIfAbsent(diskImage.getImage().getSnapshotId(),
                imagesList -> new ArrayList<>()).add(diskImage));

        for (XmlNode node : list) {
            XmlNode vmConfiguration = selectSingleNode(node, "VmConfiguration", _xmlNS);
            Snapshot snapshot = new Snapshot(vmConfiguration != null);
            snapshot.setId(new Guid(node.attributes.get("ovf:id").getValue()));
            snapshot.setVmId(_vm.getId());
            snapshot.setType(SnapshotType.valueOf(selectSingleNode(node, "Type", _xmlNS).innerText));
            snapshot.setStatus(SnapshotStatus.OK);
            snapshot.setDescription(selectSingleNode(node, "Description", _xmlNS).innerText);
            XmlNode memory = selectSingleNode(node, "Memory", _xmlNS);
            if (memory != null) {
                List<Guid> guids = Guid.createGuidListFromString(memory.innerText);
                snapshot.setMemoryDiskId(guids.get(2));
                snapshot.setMetadataDiskId(guids.get(4));
            }

            final Date creationDate =
                    OvfParser.utcDateStringToLocalDate(selectSingleNode(node, "CreationDate", _xmlNS).innerText);
            if (creationDate != null) {
                snapshot.setCreationDate(creationDate);
            }

            setDiskImageActiveBySnapshotType(snapshotIdToDiskImagesMap, snapshot);

            snapshot.setVmConfiguration(vmConfiguration == null
                    ? null : new String(Base64.decodeBase64(vmConfiguration.innerText)));

            XmlNode appList = selectSingleNode(node, "ApplicationList", _xmlNS);
            if (appList != null) {
                snapshot.setAppList(appList.innerText);
            }

            snapshots.add(snapshot);
        }
    }

    private void setDiskImageActiveBySnapshotType(Map<Guid, List<DiskImage>> diskImageMap, Snapshot snapshot) {
        List<DiskImage> diskImages = diskImageMap.get(snapshot.getId());
        if (diskImages == null) {
            return;
        }

        boolean isActiveSnapshotType = snapshot.getType() == SnapshotType.ACTIVE || snapshot.getType() == SnapshotType.PREVIEW;

        diskImages.forEach(diskImage -> diskImage.setActive(isActiveSnapshotType));
        diskImageMap.remove(snapshot.getId());
    }

    @Override
    protected void readAffinityGroupsSection(XmlNode section) {
        XmlNodeList list = selectNodes(section, OvfProperties.AFFINITY_GROUP);
        List<AffinityGroup> affinityGroups = new ArrayList<>();
        for (XmlNode node : list) {
            String affinityGroupName = node.attributes.get("ovf:name").innerText;
            AffinityGroup affinityGroup = new AffinityGroup();
            affinityGroup.setName(affinityGroupName);
            affinityGroups.add(affinityGroup);
        }

        fullEntityOvfData.setAffinityGroups(affinityGroups);
    }

    @Override
    protected void readAffinityLabelsSection(XmlNode section) {
        XmlNodeList list = selectNodes(section, OvfProperties.AFFINITY_LABEL);
        List<Label> affinityLabels = new ArrayList<>();
        for (XmlNode node : list) {
            String affinityLabelName = node.attributes.get("ovf:name").innerText;
            LabelBuilder builder = new LabelBuilder();
            Label label = builder.name(affinityLabelName).build();
            affinityLabels.add(label);
        }

        fullEntityOvfData.setAffinityLabels(affinityLabels);
    }

    @Override
    protected void buildNicReference() {
    }

    protected void setClusterArch(ArchitectureType arch) {
        _vm.setClusterArch(arch);
    }
}
