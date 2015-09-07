package org.ovirt.engine.core.bll;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.JsonHelper;

public class MetadataDiskDescriptionHandler {

    private static final MetadataDiskDescriptionHandler instance = new MetadataDiskDescriptionHandler();
    private static final String DISK_ALIAS = "DiskAlias";
    private static final String DISK_DESCRIPTION = "DiskDescription";
    private static final String[] DESCRIPTION_FIELDS_PRIORITY = {DISK_ALIAS, DISK_DESCRIPTION};

    /**
     * We can utilize 210 bytes from the disk metadata for its description.
     * Since we use JSON, the actual number of bytes we can use is 208 (2 bytes for the braces).
     */
    private static final int METADATA_DESCRIPTION_MAX_LENGTH = 208;

    private MetadataDiskDescriptionHandler() {
    }

    public static MetadataDiskDescriptionHandler getInstance() {
        return instance;
    }

    /**
     * Creates and returns a Json string containing the disk alias, description and encoding.
     * Since there's not always enough space for both alias and description, they are added according to the priority
     * noted by DESCRIPTION_FIELDS_PRIORITY.
     * The disk alias and description are preserved in the disk meta data. If the meta data will be added with more
     * fields, UpdateVmDiskCommand should be changed accordingly.
     */
    public String generateJsonDiskDescription(Disk disk) throws IOException {
        Map<String, Object> description = new TreeMap<>();
        description.put(DISK_ALIAS, disk.getDiskAlias());
        description.put(DISK_DESCRIPTION, StringUtils.defaultString(disk.getDiskDescription()));
        return JsonHelper.mapToJson(generateJsonDiskDescription(description, DESCRIPTION_FIELDS_PRIORITY), false);
    }

    private Map<String, Object> generateJsonDiskDescription(Map<String, Object> descriptionFields,
            String... descriptionFieldsPriority) {
        Map<String, Object> descriptionMap = new TreeMap<>();
        int descriptionAvailableLength = METADATA_DESCRIPTION_MAX_LENGTH;
        for (int priority = 0; priority < descriptionFieldsPriority.length; ++priority) {
            String fieldName = descriptionFieldsPriority[priority];
            String fieldValue = descriptionFields.get(fieldName).toString();
            descriptionAvailableLength = addFieldToDescriptionMap(descriptionMap, fieldName, fieldValue,
                    descriptionAvailableLength);
            if (descriptionAvailableLength <= 0) {
                // Storage space limitation reached.
                if (!auditLogIfFieldWasNotAddedSuccessfully(descriptionFields, descriptionMap, fieldName, fieldValue,
                        descriptionFieldsPriority, priority)) {
                    break;
                }
            }
        }
        return descriptionMap;
    }

    /**
     * Returns true iff the field noted by fieldName was added successfully.
     */
    private boolean auditLogIfFieldWasNotAddedSuccessfully(Map<String, Object> descriptionFields,
            Map<String, Object> descriptionMap, String fieldName, String fieldValue, String[] descriptionFieldsPriority,
            int fieldPriorityIndex) {
        String diskAlias = descriptionFields.get(DISK_ALIAS).toString();
        String storedFieldValue = (String) descriptionMap.get(fieldName);
        if (storedFieldValue == null) {
            // The field wasn't stored due to a space limitation.
            auditLogFailedToStoreDiskFields(
                    diskAlias, getDiskFieldsNamesLeft(descriptionFieldsPriority, fieldPriorityIndex));
            return false;
        } else if (storedFieldValue.length() < fieldValue.length()) {
            // The field was stored but it was truncated due to a space limitation.
            if (descriptionFieldsPriority.length - fieldPriorityIndex > 1) {
                // At least one field will be entirely dropped.
                auditLogDiskFieldTruncatedAndOthersWereLost(diskAlias, fieldName,
                        getDiskFieldsNamesLeft(descriptionFieldsPriority, fieldPriorityIndex + 1));
            } else {
                // Only the last field was truncated.
                auditLogDiskFieldTruncated(diskAlias, fieldName);
            }
            return false;
        }
        return true;
    }

    private String getDiskFieldsNamesLeft(String[] descriptionFieldsPriority, int priority) {
        String[] descriptionFieldsLeft =
                Arrays.copyOfRange(descriptionFieldsPriority, priority, descriptionFieldsPriority.length);
        return StringUtils.join(descriptionFieldsLeft, ", ", 0, descriptionFieldsLeft.length);
    }

    /**
     * Adds the given field to the map and returns the up to date descriptionAvailableLength (number of bytes left to
     * store in the disk metadata description field).
     */
    private int addFieldToDescriptionMap(Map<String, Object> descriptionMap, String fieldName, String fieldValue,
            int descriptionAvailableLength) {
        if ((descriptionAvailableLength -= calculateJsonFieldPotentialOverhead(fieldName, descriptionMap)) > 0) {
            // There's enough space for the field's overhead in the metadata + at least one character from its value.
            if (fieldValue.length() > descriptionAvailableLength) {
                fieldValue = fieldValue.substring(0, descriptionAvailableLength);
            }
            descriptionMap.put(fieldName, fieldValue);
            return descriptionAvailableLength - fieldValue.length();
        }
        return descriptionAvailableLength;
    }

    private int calculateJsonFieldPotentialOverhead(String fieldName, Map<String, Object> descriptionMap) {
        return String.format("\"%s\":\"\"", fieldName).length() +
                (descriptionMap.isEmpty() ? 0 : 1); // for the comma.
    }

    private void auditLogDiskFieldTruncated(String diskAlias, String fieldName) {
        Map<String, String> customValues = new HashMap<>();
        customValues.put("DiskAlias", diskAlias);
        customValues.put("DiskFieldName", fieldName);
        auditLog(customValues, AuditLogType.FAILED_TO_STORE_ENTIRE_DISK_FIELD_IN_DISK_DESCRIPTION_METADATA);
    }

    private void auditLogDiskFieldTruncatedAndOthersWereLost(String diskAlias, String fieldName,
            String diskFieldsNames) {
        Map<String, String> customValues = new HashMap<>();
        customValues.put("DiskAlias", diskAlias);
        customValues.put("DiskFieldName", fieldName);
        customValues.put("DiskFieldsNames", diskFieldsNames);
        auditLog(customValues,
                AuditLogType.FAILED_TO_STORE_ENTIRE_DISK_FIELD_AND_REST_OF_FIELDS_IN_DISK_DESCRIPTION_METADATA);
    }

    private void auditLogFailedToStoreDiskFields(String diskAlias, String diskFieldsNames) {
        Map<String, String> customValues = new HashMap<>();
        customValues.put("DiskAlias", diskAlias);
        customValues.put("DiskFieldsNames", diskFieldsNames);
        auditLog(customValues, AuditLogType.FAILED_TO_STORE_DISK_FIELDS_IN_DISK_DESCRIPTION_METADATA);
    }

    private void auditLog(Map<String, String> customValues, AuditLogType auditLogType) {
        AuditLogableBase auditLogableBase = new AuditLogableBase();
        for (Map.Entry<String, String> customValue : customValues.entrySet()) {
            auditLogableBase.addCustomValue(customValue.getKey(), customValue.getValue());
        }
        getAuditLogDirector().log(auditLogableBase, auditLogType);
    }

    public void enrichDiskByJsonDescription(String jsonDiskDescription, Disk disk) throws IOException {
        Map<String, Object> diskDescriptionMap = JsonHelper.jsonToMap(jsonDiskDescription);
        disk.setDiskAlias((String) diskDescriptionMap.get(DISK_ALIAS));
        disk.setDiskDescription((String) diskDescriptionMap.get(DISK_DESCRIPTION));
    }

    protected AuditLogDirector getAuditLogDirector() {
        return new AuditLogDirector();
    }
}
