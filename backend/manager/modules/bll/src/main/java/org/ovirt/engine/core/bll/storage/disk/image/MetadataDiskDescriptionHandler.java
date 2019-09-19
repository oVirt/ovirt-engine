package org.ovirt.engine.core.bll.storage.disk.image;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.utils.JsonHelper;

@Singleton
public class MetadataDiskDescriptionHandler {

    private static final String DISK_ALIAS = "DiskAlias";
    private static final String DISK_DESCRIPTION = "DiskDescription";
    private static final String DISK_ENCODING = "Enc";
    private static final String[] DESCRIPTION_FIELDS_PRIORITY = {DISK_ALIAS, DISK_DESCRIPTION};
    private static final Pattern ASCII_PATTERN = Pattern.compile(ValidationUtils.ONLY_ASCII_OR_NONE);

    // We are intentionally using UTF_16LE so we can encode and decode on any machine.
    private static final Charset CHARSET = StandardCharsets.UTF_16LE;

    // Encoding with HEX which is based on utf-16LE gives a minimum of 4 bytes per character.
    private static final int MIN_ENCODING_RATIO_WITH_HEX_USING_UTF_16LE = 4;

    // Encoding with HEX which is based on utf-16LE gives a maximum of 8 bytes per character.
    private static final int MAX_ENCODING_RATIO_WITH_HEX_USING_UTF_16LE = 8;

    /**
     * We can utilize 210 bytes from the disk metadata for its description.
     * Since we use JSON, the actual number of bytes we can use is 208 (2 bytes for the braces).
     */
    private static final int METADATA_DESCRIPTION_MAX_LENGTH = 208;

    @Inject
    private AuditLogDirector auditLogDirector;

    /**
     * Creates and returns a Json string containing the disk alias, description and encoding.
     * Since there's not always enough space for both alias and description, they are added according to the priority
     * noted by DESCRIPTION_FIELDS_PRIORITY.
     * The disk alias and description are preserved in the disk meta data. If the meta data will be added with more
     * fields, UpdateDiskCommand should be changed accordingly.
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
            descriptionAvailableLength = addFieldToDescriptionMap(descriptionMap, fieldName, fieldValue, 1 << priority,
                    descriptionAvailableLength);
            boolean fieldValueContainsOnlyAscii = stringMatchesAsciiPattern(fieldValue);
            if ((fieldValueContainsOnlyAscii && descriptionAvailableLength <= 0) ||
                    (!fieldValueContainsOnlyAscii &&
                            descriptionAvailableLength < MAX_ENCODING_RATIO_WITH_HEX_USING_UTF_16LE)) {
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
        } else {
            // The field's value that should have been stored (before truncation, if one was performed).
            String fullFieldValue = stringMatchesAsciiPattern(fieldValue) ? fieldValue : encodeDiskProperty(fieldValue);
            if (storedFieldValue.length() < fullFieldValue.length()) {
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
        int fieldPriority, int descriptionAvailableLength) {
        boolean encodeField = false;
        String curFieldValue = fieldValue;
        if (!stringMatchesAsciiPattern(curFieldValue)) {
            // This field contains non-ASCII characters and thus should be encoded.
            encodeField = true;
            curFieldValue = encodeDiskProperty(curFieldValue);
            descriptionAvailableLength -= addEditEncodingField(descriptionMap, fieldPriority);
        }
        if ((descriptionAvailableLength -= calculateJsonFieldPotentialOverhead(fieldName, descriptionMap)) > 0) {
            // There's enough space for the field's overhead in the metadata + at least one character from its value.
            if (curFieldValue.length() > descriptionAvailableLength) {
                curFieldValue = truncateDiskProperty(fieldValue, descriptionAvailableLength, encodeField);
            }
            descriptionMap.put(fieldName, curFieldValue);
            return descriptionAvailableLength - curFieldValue.length();
        }
        return descriptionAvailableLength;
    }

    private boolean stringMatchesAsciiPattern(String str) {
        return ASCII_PATTERN.matcher(str).matches();
    }

    /**
     * Edits the encoding field in case there's already an encoded field in the map, or adds a new one in case it's
     * the first one to be encoded. The encoding field is a number that represents the fields that were encoded.
     * If a bit is on, the corresponding field in DESCRIPTION_FIELDS_PRIORITY is encoded.
     * For example, Enc=2 (10 in binary) means that the second field in DESCRIPTION_FIELDS_PRIORITY (DISK_DESCRIPTION)
     * is encoded. Enc=3 (11 in binary) means that the first and the second fields are both encoded.
     * The method returns the number of characters spent by editing/adding the encoding field.
     */
    private int addEditEncodingField(Map<String, Object> descriptionMap, int fieldPriority) {
        String oldEncodingFieldValue = (String) descriptionMap.get(DISK_ENCODING);
        if (oldEncodingFieldValue == null) {
            // This is the first field to be encoded.
            String fieldPriorityStr = String.valueOf(fieldPriority);
            descriptionMap.put(DISK_ENCODING, fieldPriorityStr);
            return generateJsonField(DISK_ENCODING, fieldPriorityStr).length();
        }
        // There's already an encoded field in the map.
        String newEncodingFieldValue = String.valueOf(Integer.parseInt(oldEncodingFieldValue) | fieldPriority);
        descriptionMap.put(DISK_ENCODING, newEncodingFieldValue);
        return newEncodingFieldValue.length() - oldEncodingFieldValue.length();
    }

    private String encodeDiskProperty(String diskProperty) {
        return Hex.encodeHexString(diskProperty.getBytes(CHARSET));
    }

    private int calculateJsonFieldPotentialOverhead(String fieldName, Map<String, Object> descriptionMap) {
        return generateJsonField(fieldName, StringUtils.EMPTY).length() +
                (descriptionMap.isEmpty() ? 0 : 1); // for the comma.
    }

    protected String generateJsonField(String fieldName, String fieldValue) {
        return String.format("\"%s\":\"%s\"", fieldName, fieldValue);
    }

    private String truncateDiskProperty(String diskProperty, int maxLength, boolean encodeProperty) {
        if (encodeProperty) {
            // We encode with HEX based on utf-16LE, so that each char is encoded to 4-8 bytes.
            // Thus, we can save iterations by truncating the original string by 4.
            diskProperty = diskProperty.substring(0, maxLength / MIN_ENCODING_RATIO_WITH_HEX_USING_UTF_16LE);
            String encodedProperty = StringUtils.EMPTY;
            for (int i = diskProperty.length(); i > 0; --i) {
                encodedProperty = encodeDiskProperty(diskProperty.substring(0, i));
                if (encodedProperty.length() <= maxLength) {
                    break;
                }
            }
            return encodedProperty;
        }
        return diskProperty.substring(0, maxLength);
    }

    private void auditLogDiskFieldTruncated(String diskAlias, String fieldName) {
        AuditLogable logable = createDiskEvent(diskAlias);
        logable.addCustomValue("DiskFieldName", fieldName);
        auditLogDirector.log(logable, AuditLogType.FAILED_TO_STORE_ENTIRE_DISK_FIELD_IN_DISK_DESCRIPTION_METADATA);
    }

    private void auditLogDiskFieldTruncatedAndOthersWereLost(String diskAlias,
            String fieldName,
            String diskFieldsNames) {
        AuditLogable logable = createDiskEvent(diskAlias);
        logable.addCustomValue("DiskFieldName", fieldName);
        logable.addCustomValue("DiskFieldsNames", diskFieldsNames);
        auditLogDirector.log(logable,
                AuditLogType.FAILED_TO_STORE_ENTIRE_DISK_FIELD_AND_REST_OF_FIELDS_IN_DISK_DESCRIPTION_METADATA);
    }

    private void auditLogFailedToStoreDiskFields(String diskAlias, String diskFieldsNames) {
        AuditLogable logable = createDiskEvent(diskAlias);
        logable.addCustomValue("DiskFieldsNames", diskFieldsNames);
        auditLogDirector.log(logable, AuditLogType.FAILED_TO_STORE_DISK_FIELDS_IN_DISK_DESCRIPTION_METADATA);
    }

    private AuditLogable createDiskEvent(String diskAlias) {
        AuditLogable logable = new AuditLogableImpl();
        logable.addCustomValue("DiskAlias", diskAlias);
        return logable;
    }

    public void enrichDiskByJsonDescription(String jsonDiskDescription, Disk disk)
            throws IOException, DecoderException {
        Map<String, Object> diskDescriptionMap = JsonHelper.jsonToMap(jsonDiskDescription);
        String encodingStr = (String) diskDescriptionMap.get(DISK_ENCODING);
        if (encodingStr != null) {
            int encoding = Integer.parseInt(encodingStr);
            for (int priority = 0; priority < DESCRIPTION_FIELDS_PRIORITY.length; ++priority) {
                if (((1 << priority) & encoding) != 0) {
                    // The field is encoded and thus needs to be decoded.
                    String fieldName = DESCRIPTION_FIELDS_PRIORITY[priority];
                    String fieldValue = (String) diskDescriptionMap.get(fieldName);
                    diskDescriptionMap.put(fieldName, decodeDiskProperty(fieldValue));
                }
            }
        }

        // Reaching here means that no DecoderException was thrown by any of the calls to decodeDiskProperty.
        // This way we can promise that we save both diskAlias and diskDescription to the disk or non of them.
        disk.setDiskAlias((String) diskDescriptionMap.get(DISK_ALIAS));
        disk.setDiskDescription((String) diskDescriptionMap.get(DISK_DESCRIPTION));
    }

    private String decodeDiskProperty(String diskProperty) throws DecoderException {
        return new String(Hex.decodeHex(diskProperty.toCharArray()), CHARSET);
    }
}
