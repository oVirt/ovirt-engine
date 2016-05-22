package org.ovirt.engine.core.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.archivers.tar.TarInMemoryExport;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OvfUtils {
    private static final String TEMPLATE_ENTITY_TYPE = "<TemplateType>";
    private static final String ENTITY_NAME = "<Name>";
    private static final String END_ENTITY_NAME = "</Name>";
    private static final String OVF_FILE_EXT = ".ovf";
    private static final int GUID_LENGTH = Guid.Empty.toString().length();
    protected static final Logger log = LoggerFactory.getLogger(TarInMemoryExport.class);

    private static String getEntityName(String ovfData) {
        int beginIndexOfEntityName = ovfData.indexOf(ENTITY_NAME) + ENTITY_NAME.length();
        int endIndexOfEntityName = ovfData.indexOf(END_ENTITY_NAME, beginIndexOfEntityName);
        String entityName = ovfData.substring(beginIndexOfEntityName, endIndexOfEntityName);
        return entityName;
    }

    private static VmEntityType getVmEntityType(String ovfData) {
        VmEntityType vmEntityType = VmEntityType.VM;
        int indexOfEntityType = ovfData.indexOf(TEMPLATE_ENTITY_TYPE);
        if (indexOfEntityType != -1) {
            vmEntityType = VmEntityType.TEMPLATE;
        }
        return vmEntityType;
    }

    public static List<Guid> fetchVmDisks(XmlDocument xmlDocument) {
        List<Guid> disksIds = new ArrayList<>();
        XmlNodeList nodeList = xmlDocument.selectNodes("//*/Section");
        XmlNode selectedSection = null;
        for (XmlNode section : nodeList) {
            String value = section.attributes.get("xsi:type").getValue();
            if (value.equals("ovf:DiskSection_Type")) {
                selectedSection = section;
                break;
            }
        }
        if (selectedSection != null) {
            NodeList childNodeList = selectedSection.getChildNodes();
            for (int k = 0; k < childNodeList.getLength(); k++) {
                if (childNodeList.item(k).getLocalName().equals("Disk")) {
                    Node node = childNodeList.item(k).getAttributes().getNamedItem("ovf:fileRef");
                    if (node != null && node.getTextContent() != null) {
                        disksIds.add(Guid.createGuidFromString(node.getTextContent().substring(0, GUID_LENGTH)));
                    }
                }
            }
        }
        return disksIds;
    }

    private static Guid getEntityId(String fileName) {
        return Guid.createGuidFromString(fileName.substring(0, fileName.length() - OVF_FILE_EXT.length()));
    }

    private static OvfEntityData createOvfEntityData(Guid storageDomainId,
            String ovfData,
            VmEntityType vmEntityType,
            String entityName,
            ArchitectureType archType,
            Guid entityId) {
        OvfEntityData ovfEntityData = new OvfEntityData();
        ovfEntityData.setOvfData(ovfData);
        ovfEntityData.setEntityType(vmEntityType);
        ovfEntityData.setEntityName(entityName);
        ovfEntityData.setStorageDomainId(storageDomainId);
        ovfEntityData.setArchitecture(archType);
        ovfEntityData.setEntityId(entityId);
        return ovfEntityData;
    }

    public static List<OvfEntityData> getOvfEntities(byte[] tar,
            List<UnregisteredDisk> unregisteredDisks,
            Guid storageDomainId) {
        List<OvfEntityData> ovfEntityDataFromTar = new ArrayList<>();
        InputStream is = new ByteArrayInputStream(tar);

        log.info("Start fetching OVF files from tar file");
        Map<String, ByteBuffer> filesFromTar;
        try (TarInMemoryExport memoryTar = new TarInMemoryExport(is)) {
            filesFromTar = memoryTar.unTar();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Exception while getting OVFs files from tar file for domain %s",
                    storageDomainId), e);
        }

        for (Entry<String, ByteBuffer> fileEntry : filesFromTar.entrySet()) {
            if (fileEntry.getKey().endsWith(OVF_FILE_EXT)) {
                String ovfData = new String(fileEntry.getValue().array());
                VmEntityType vmType = getVmEntityType(ovfData);
                ArchitectureType archType = null;
                Guid entityId = getEntityId(fileEntry.getKey());
                String vmName = getEntityName(ovfData);
                try {
                    XmlDocument xmlDocument = new XmlDocument(ovfData);
                    archType = getOsSection(xmlDocument);
                    updateUnregisteredDisksWithVMs(unregisteredDisks, entityId, vmName, xmlDocument);
                } catch (Exception e) {
                    log.error("Could not parse VM's disks or architecture, file name: {}, content size: {}, error: {}",
                            fileEntry.getKey(),
                            ovfData.length(),
                            e.getMessage());
                    log.debug("Exception", e);
                    continue;
                }
                // Creates an OVF entity data.
                OvfEntityData ovfEntityData =
                        createOvfEntityData(storageDomainId,
                                ovfData,
                                vmType,
                                vmName,
                                archType,
                                entityId);
                log.info(
                        "Retrieve OVF Entity from storage domain ID '{}' for entity ID '{}', entity name '{}' and VM Type of '{}'",
                        storageDomainId,
                        getEntityId(fileEntry.getKey()),
                        getEntityName(ovfData),
                        vmType.name());
                ovfEntityDataFromTar.add(ovfEntityData);
            } else {
                log.info("File '{}' is not an OVF file, will be ignored.", fileEntry.getKey());
            }
        }
        log.info("Finish to fetch OVF files from tar file. The number of OVF entities are {}",
                ovfEntityDataFromTar.size());
        return ovfEntityDataFromTar;
    }

    public static void updateUnregisteredDisksWithVMs(List<UnregisteredDisk> unregisteredDisks,
            Guid entityId,
            String vmName,
            XmlDocument xmlDocument) {
        for (Guid diskId : fetchVmDisks(xmlDocument)) {
            UnregisteredDisk unregisterDisk = unregisteredDisks.stream()
                    .filter(unregrDisk -> diskId.equals(unregrDisk.getId()))
                    .findAny()
                    .orElse(null);
            VmBase vm = new VmBase();
            vm.setId(entityId);
            vm.setName(vmName);
            if (unregisterDisk != null) {
                unregisterDisk.getVms().add(vm);
            }
        }
    }

    private static ArchitectureType getOsSection(XmlDocument xmlDocument) {
        ArchitectureType archType = null;
        XmlNode content = xmlDocument.selectSingleNode("//*/Content");
        XmlNodeList nodeList = content.selectNodes("Section");
        XmlNode selectedSection = null;
        OsRepository osRepository = SimpleDependencyInjector.getInstance().get(OsRepository.class);
        if (nodeList != null) {
            for (XmlNode section : nodeList) {
                String value = section.attributes.get("xsi:type").getValue();

                if (value.equals("ovf:OperatingSystemSection_Type")) {
                    selectedSection = section;
                    break;
                }
            }
            if (selectedSection != null) {
                int osId = osRepository.getOsIdByUniqueName(selectedSection.innerText);
                archType = osRepository.getArchitectureFromOS(osId);
            }
        }
        return archType;
    }
}
