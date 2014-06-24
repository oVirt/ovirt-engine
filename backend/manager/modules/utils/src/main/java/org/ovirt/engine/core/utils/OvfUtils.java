package org.ovirt.engine.core.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.archivers.tar.TarInMemoryExport;

public class OvfUtils {
    private final static String TEMPLATE_ENTITY_TYPE = "<TemplateType>";
    private final static String ENTITY_NAME = "<Name>";
    private final static String END_ENTITY_NAME = "</Name>";
    private final static String OVF_FILE_EXT = ".ovf";

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
            Guid entityId) {
        OvfEntityData ovfEntityData = new OvfEntityData();
        ovfEntityData.setOvfData(ovfData);
        ovfEntityData.setEntityType(vmEntityType);
        ovfEntityData.setEntityName(entityName);
        ovfEntityData.setStorageDomainId(storageDomainId);
        ovfEntityData.setEntityId(entityId);
        return ovfEntityData;
    }

    public static List<OvfEntityData> getOvfEntities(byte[] tar, Guid storageDomainId) {
        List<OvfEntityData> ovfEntityDataFromTar = new ArrayList<>();
        InputStream is = new ByteArrayInputStream(tar);

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

                // Creates an OVF entity data.
                OvfEntityData ovfEntityData =
                        createOvfEntityData(storageDomainId,
                                ovfData,
                                getVmEntityType(ovfData),
                                getEntityName(ovfData),
                                getEntityId(fileEntry.getKey()));
                ovfEntityDataFromTar.add(ovfEntityData);
            }
        }

        return ovfEntityDataFromTar;
    }
}
