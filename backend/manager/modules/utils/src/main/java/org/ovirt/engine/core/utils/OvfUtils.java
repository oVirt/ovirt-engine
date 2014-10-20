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
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.archivers.tar.TarInMemoryExport;
import org.ovirt.engine.core.utils.ovf.xml.XmlDocument;
import org.ovirt.engine.core.utils.ovf.xml.XmlNode;
import org.ovirt.engine.core.utils.ovf.xml.XmlNodeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OvfUtils {
    private final static String TEMPLATE_ENTITY_TYPE = "<TemplateType>";
    private final static String ENTITY_NAME = "<Name>";
    private final static String END_ENTITY_NAME = "</Name>";
    private final static String OVF_FILE_EXT = ".ovf";
    protected final static Logger log = LoggerFactory.getLogger(TarInMemoryExport.class);
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

    public static List<OvfEntityData> getOvfEntities(byte[] tar, Guid storageDomainId) {
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
                try {
                    XmlDocument xmlDocument = new XmlDocument(ovfData);
                    archType = getOsSection(xmlDocument);
                } catch (Exception e) {
                    log.error("Could not parse architecture type for VM: {}", e.getMessage());
                    log.debug("Exception", e);
                    continue;
                }
                // Creates an OVF entity data.
                OvfEntityData ovfEntityData =
                        createOvfEntityData(storageDomainId,
                                ovfData,
                                vmType,
                                getEntityName(ovfData),
                                archType,
                                getEntityId(fileEntry.getKey()));
                log.info("Retrieve OVF Entity from storage domain ID '{}' for entity ID '{}', entity name '{}' and VM Type of '{}'",
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

    private static ArchitectureType getOsSection(XmlDocument xmlDocument) {
        ArchitectureType archType = null;
        XmlNode content = xmlDocument.SelectSingleNode("//*/Content");
        XmlNodeList nodeList = content.SelectNodes("Section");
        XmlNode selectedSection = null;
        OsRepository osRepository = SimpleDependecyInjector.getInstance().get(OsRepository.class);
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
