package org.ovirt.engine.core.bll;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.utils.JsonHelper;

public class MetadataDiskDescriptionHandler {

    private static final String DISK_ALIAS = "DiskAlias";
    private static final String DISK_DESCRIPTION = "DiskDescription";

    private MetadataDiskDescriptionHandler() {
    }

    /**
     * Creates and returns a Json string containing the disk alias and the disk description. The disk alias and
     * description are preserved in the disk meta data. If the meta data will be added with more fields
     * UpdateVmDiskCommand should be changed accordingly.
     */
    public static String getJsonDiskDescription(Disk disk) throws IOException {
        Map<String, Object> description = new TreeMap<>();
        description.put(DISK_ALIAS, disk.getDiskAlias());
        description.put(DISK_DESCRIPTION, disk.getDiskDescription() != null ? disk.getDiskDescription() : "");
        return JsonHelper.mapToJson(description, false);
    }

    public static void enrichDiskByJsonDescription(String jsonDiskDescription, Disk disk) throws IOException {
        Map<String, Object> diskDescriptionMap = JsonHelper.jsonToMap(jsonDiskDescription);
        disk.setDiskAlias((String) diskDescriptionMap.get(DISK_ALIAS));
        disk.setDiskDescription((String) diskDescriptionMap.get(DISK_DESCRIPTION));
    }
}
