package org.ovirt.engine.core.utils.vmproperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.utils.MultiValueMapUtils;

/**
 * Helper methods to help parse and validate predefined and UserDefined(user defined) properties. These methods are used
 * by vdsbroker and bll modules
 *
 */
public class VmPropertiesUtils {

    private static final Pattern SEMICOLON_PATTERN = Pattern.compile(";");
    private static final String PROPERTIES_DELIMETER = ";";
    private static final String KEY_VALUE_DELIMETER = "=";

    private static final String LEGITIMATE_CHARACTER_FOR_KEY = "[a-z_A-Z0-9]";
    private static final String LEGITIMATE_CHARACTER_FOR_VALUE = "[^" + PROPERTIES_DELIMETER + "]"; // all characters
                                                                                                    // but the delimiter
                                                                                                    // are allowed
    private static final Pattern KEY_PATTERN = Pattern.compile("(" + LEGITIMATE_CHARACTER_FOR_KEY + ")+");
    private static final Pattern VALUE_PATTERN = Pattern.compile("(" + LEGITIMATE_CHARACTER_FOR_VALUE + ")+");

    // private static final Pattern KEY_VALIDATION_PATTERN = Pattern.compile("(" + LEGITIMATE_CHARACTER + ")+");

    // properties are in form of key1=val1;key2=val2; .... key and include alpha numeric characters and _
    private static final String KEY_VALUE_REGEX_STR = "((" + LEGITIMATE_CHARACTER_FOR_KEY + ")+)=(("
            + LEGITIMATE_CHARACTER_FOR_VALUE + ")+)";

    // frontend can pass custom values in the form of "key=value" or "key1=value1;... key-n=value_n" (if there is only
    // one key-value, no ; is attached to it
    private static final Pattern VALIDATION_PATTERN = Pattern.compile(KEY_VALUE_REGEX_STR + "(;" + KEY_VALUE_REGEX_STR
            + ")*;?");

    private static final List<ValidationError> invalidSyntaxValidationError =
            Arrays.asList(new ValidationError(ValidationFailureReason.SYNTAX_ERROR, ""));
    private static Map<String, Pattern> predefinedProperties = new HashMap<String, Pattern>();
    private static Map<String, Pattern> userdefinedProperties = new HashMap<String, Pattern>();

    // Defines why validation failed
    public static enum ValidationFailureReason {
        KEY_DOES_NOT_EXIST,
        INCORRECT_VALUE,
        SYNTAX_ERROR,
        DUPLICATE_KEY,
        NO_ERROR
    };

    public static class ValidationError {
        private final ValidationFailureReason reason;
        private final String keyName;

        public ValidationError(ValidationFailureReason reason, String keyName) {
            this.reason = reason;
            this.keyName = keyName;
        }

        public ValidationFailureReason getReason() {
            return reason;
        }

        public String getKeyName() {
            return keyName;
        }

        @Override
        public boolean equals(Object other) {
            if (other != null && other instanceof ValidationError) {
                ValidationError otherError = (ValidationError) other;
                return keyName.equals(otherError.getKeyName()) && reason == otherError.getReason();
            }
            return false;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((keyName == null) ? 0 : keyName.hashCode());
            result = prime * result + ((reason == null) ? 0 : reason.hashCode());
            return result;
        }

        public boolean isOK() {
            return reason == ValidationFailureReason.NO_ERROR;
        }
    }

    static {
        String predefinedVMPropertiesStr = Config.<String> GetValue(ConfigValues.PredefinedVMProperties, "3.0");
        parseVMPropertiesRegex(predefinedVMPropertiesStr, predefinedProperties);

        String userDefinedVMPropertiesStr = Config.<String> GetValue(ConfigValues.UserDefinedVMProperties, "3.0");
        parseVMPropertiesRegex(userDefinedVMPropertiesStr, userdefinedProperties);

    }

    public static class VMCustomProperties {
        private final String predefinedProperties;
        private final String userDefinedProperties;

        public VMCustomProperties(String predefinedProperties, String userDefinedProperties) {
            this.predefinedProperties = predefinedProperties;
            this.userDefinedProperties = userDefinedProperties;
        }

        public String getPredefinedProperties() {
            return predefinedProperties;
        }

        public String getUseDefinedProperties() {
            return userDefinedProperties;
        }
    }

    /**
     * Parses a string containing user defined and predefined custom properties and returns VMCustomProperties object
     * that contains the properties separated to two strings - one for the predefined properties and one for the user
     * defined properties
     *
     * @param propertiesStr
     * @return
     */
    public static VMCustomProperties parseProperties(String propertiesStr) {
        HashMap<String, String> userDefinedPropertiesMap = new HashMap<String, String>();
        HashMap<String, String> predefinedPropertiesMap = new HashMap<String, String>();

        convertCustomPropertiesStrToMaps(propertiesStr, predefinedPropertiesMap, userDefinedPropertiesMap);
        return new VMCustomProperties(vmPropertiesToString(predefinedPropertiesMap),
                vmPropertiesToString(userDefinedPropertiesMap));
    }

    /**
     * Validates all the custom properties of a VM
     *
     * @param vmStatic
     * @return
     */
    public static List<ValidationError> validateVMProperties(VmStatic vmStatic) {
        List<ValidationError> errors = validateProperties(vmStatic.getCustomProperties());
        return errors;
    }

    private static void parseVMPropertiesRegex(String properties, Map<String, Pattern> keysToRegex) {
        if (StringHelper.isNullOrEmpty(properties)) {
            return;
        }

        String[] propertiesStrs = SEMICOLON_PATTERN.split(properties);

        // Property is in the form of key=regex
        for (String property : propertiesStrs) {

            Pattern pattern = null;
            String[] propertyParts = property.split(KEY_VALUE_DELIMETER);
            if (propertyParts.length == 1) { // there is no value(regex) for the property - we assume
                // in that case that any value is allowed except for the properties delimeter
                pattern = VALUE_PATTERN;
            } else {
                pattern = Pattern.compile(propertyParts[1]);
            }

            keysToRegex.put(propertyParts[0], pattern);

        }

    }

    /**
     * Validates a properties field value (checks if its format matches key1=val1;key2=val2;....)
     *
     * @param fieldValue
     * @return a list of validation errors. if there are no errors - the list will be empty
     */
    private static List<ValidationError> validateProperties(String properties) {

        if (StringHelper.isNullOrEmpty(properties)) { // No errors in case of empty value
            return Collections.emptyList();
        }
        if (syntaxErrorInProperties(properties)) {
            return invalidSyntaxValidationError;
        }
        // Transform the VM custom properties from string value to hash map
        // check the following for each one of the keys:
        // 1. Check if the key exists in either the predefined or the userdefined key sets
        // 2. Check if the value of the key is valid
        // In case either 1 or 2 fails, add an error to the errors list
        Map<String, String> map = new HashMap<String, String>();
        List<ValidationError> result = populateVMProperties(properties, map);
        return result;
    }

    private static boolean syntaxErrorInProperties(String properties) {

        return !VALIDATION_PATTERN.matcher(properties).matches();

    }

    private static boolean isValueValid(String key, String value) {
        // Checks that the value for the given property is valid by running by trying to perform
        // regex validation
        Pattern userDefinedPattern = userdefinedProperties.get(key);
        Pattern predefinedPattern = predefinedProperties.get(key);
        return (userDefinedPattern != null && userDefinedPattern.matcher(value).matches())
                || (predefinedPattern != null && predefinedPattern.matcher(value).matches());
    }

    /**
     * Get a map containing all the VM custom properties
     *
     * @return map containing the VM custom properties
     */
    public static Map<String, String> getVMProperties(VmStatic vmStatic) {
        separeteCustomPropertiesToUserAndPredefined(vmStatic);
        Map<String, String> map = new HashMap<String, String>();
        getPredefinedProperties(vmStatic, map);
        getUserDefinedProperties(vmStatic, map);
        return map;
    }

    /**
     * Get a map containing user defined properties from VM
     *
     * @param vm
     *            vm to get the map for
     * @return map containing the UserDefined properties
     */
    public static Map<String, String> getUserDefinedProperties(VmStatic vmStatic) {
        Map<String, String> map = new HashMap<String, String>();
        getUserDefinedProperties(vmStatic, map);
        return map;
    }

    /**
     * Gets a map containing the predefined properties from VM
     *
     * @param vm
     *            vm to get the map for
     * @return map containing the vm properties
     */
    public static Map<String, String> getPredefinedProperties(VmStatic vmStatic) {
        Map<String, String> map = new HashMap<String, String>();
        getPredefinedProperties(vmStatic, map);
        return map;
    }

    private static void getPredefinedProperties(VmStatic vmStatic, Map<String, String> propertiesMap) {
        String predefinedProperties = vmStatic.getPredefinedProperties();
        getVMProperties(propertiesMap, predefinedProperties);
    }

    private static void getUserDefinedProperties(VmStatic vmStatic, Map<String, String> propertiesMap) {
        String UserDefinedProperties = vmStatic.getUserDefinedProperties();
        getVMProperties(propertiesMap, UserDefinedProperties);
    }

    /**
     * Converts VM properties field value (either UserDefined or defined) to a map containing property names & values
     *
     * @param propertiesMap
     *            map that will hold the conversion result
     * @param vmPropertiesFieldValue
     *            the string value that contains the properties
     */
    public static void getVMProperties(Map<String, String> propertiesMap, String vmPropertiesFieldValue) {
        // format of properties is key1=val1,key2=val2,key3=val3,key4=val4
        if (StringHelper.isNullOrEmpty(vmPropertiesFieldValue)) {
            return;
        }

        populateVMProperties(vmPropertiesFieldValue, propertiesMap);

    }

    /**
     * Parses a string of VM properties to a map of key,value
     *
     * @param vmPropertiesFieldValue
     *            the string to parse
     * @param propertiesMap
     *            the filled map
     * @return list of errors during parsing (currently holds list of duplicate keys)
     */
    private static List<ValidationError> populateVMProperties(String vmPropertiesFieldValue,
            Map<String, String> propertiesMap) {
        Set<ValidationError> errorsSet = new HashSet<VmPropertiesUtils.ValidationError>();
        List<ValidationError> results = new ArrayList<VmPropertiesUtils.ValidationError>();
        if (!StringHelper.isNullOrEmpty(vmPropertiesFieldValue)) {
            String keyValuePairs[] = SEMICOLON_PATTERN.split(vmPropertiesFieldValue);

            for (String keyValuePairStr : keyValuePairs) {
                String[] pairParts = keyValuePairStr.split(KEY_VALUE_DELIMETER, 2);
                String key = pairParts[0];
                String value = pairParts[1];
                if (propertiesMap.containsKey(key)) {
                    errorsSet.add(new ValidationError(ValidationFailureReason.DUPLICATE_KEY, key));
                    continue;
                }
                if (!predefinedProperties.containsKey(key) && !userdefinedProperties.containsKey(key)) {
                    errorsSet.add(new ValidationError(ValidationFailureReason.KEY_DOES_NOT_EXIST, key));
                    continue;
                }

                if (!isValueValid(key, value)) {
                    errorsSet.add(new ValidationError(ValidationFailureReason.INCORRECT_VALUE, key));
                    continue;
                }
                propertiesMap.put(key, value);
            }
        }
        results.addAll(errorsSet);
        return results;
    }

    private static String vmPropertiesToString(Map<String, String> propertiesMap) {
        if (propertiesMap == null || propertiesMap.size() == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        Set<Entry<String, String>> entries = propertiesMap.entrySet();
        Iterator<Entry<String, String>> iterator = entries.iterator();
        Entry<String, String> entry = iterator.next();
        result.append(entry.getKey()).append("=").append(entry.getValue());
        while (iterator.hasNext()) {
            result.append(";");
            entry = iterator.next();
            if (entry != null) {
                result.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return result.toString();
    }

    private static void convertCustomPropertiesStrToMaps(String propertiesValue,
            Map<String, String> predefinedPropertiesMap, Map<String, String> userDefinedPropertiesMap) {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        populateVMProperties(propertiesValue, propertiesMap);
        Set<Entry<String, String>> propertiesEntries = propertiesMap.entrySet();

        // Go over all the properties - if the key of the property exists in the
        // predefined key set -
        // add it to the predefined map, otherwise - add it to the user defined
        // map

        Set<String> predefinedPropertiesKeys = predefinedProperties.keySet();
        Set<String> userdefinedPropertiesKeys = userdefinedProperties.keySet();
        for (Entry<String, String> propertiesEntry : propertiesEntries) {
            String propertyKey = propertiesEntry.getKey();
            String propertyValue = propertiesEntry.getValue();
            if (predefinedPropertiesKeys.contains(propertyKey)) {
                predefinedPropertiesMap.put(propertyKey, propertyValue);
            }
            if (userdefinedPropertiesKeys.contains(propertyKey)) {
                userDefinedPropertiesMap.put(propertyKey, propertyValue);
            }
        }

    }

    /**
     * Composes custom properties string from predefined properties and user defined properties strings
     *
     * @param predefinedProperties
     * @param userDefinedProperties
     * @return
     */
    public static String customProperties(String predefinedProperties, String userDefinedProperties) {
        StringBuilder result = new StringBuilder();
        result.append((StringHelper.isNullOrEmpty(predefinedProperties)) ? "" : predefinedProperties);
        result.append((result.length() == 0) ? "" : ";");
        result.append((StringHelper.isNullOrEmpty(userDefinedProperties)) ? "" : userDefinedProperties);
        return result.toString();
    }

    /**
     * Splits the validation errors list to lists of missing keys and wrong key values
     *
     * @param errorsList
     * @param missingKeysList
     * @param wrongKeyValues
     */
    public static void separateValidationErrorsList(List<ValidationError> errorsList,
            Map<ValidationFailureReason, List<ValidationError>> resultMap) {
        if (errorsList == null || errorsList.isEmpty()) {
            return;
        }

        for (ValidationError error : errorsList) {
            MultiValueMapUtils.addToMap(error.getReason(), error, resultMap);
        }
    }

    /**
     * Generates an error message to be displayed by frontend
     *
     * @param errorsList
     *            list of errors obtained from parsing
     * @param syntaxErrorMessage
     *            error to be displayed in case of syntax error
     * @param failureReasonsToVdcBllMessagesMap
     *            map of errors to vdc bll messages (for presentation of errors from AppErrors.resx)
     * @param failureReasonsToParameterFormatStrings
     *            map of errors to format string (for presentation of values for the errors are defined in
     *            AppError.resx)
     * @return
     */
    public static List<String> generateErrorMessages(List<ValidationError> errorsList,
            String syntaxErrorMessage, Map<ValidationFailureReason, String> failureReasonsToVdcBllMessagesMap,
            Map<ValidationFailureReason, String> failureReasonsToParameterFormatStrings) {
        // No errors , the returned list reported to client will be empty
        if (errorsList == null || errorsList.isEmpty()) {
            return Collections.emptyList();
        }
        // Syntax error is the most severe error, and should be returned without checking the rest of the errors
        if (errorsList.size() == 1 && errorsList.get(0).getReason() == ValidationFailureReason.SYNTAX_ERROR) {
            return Arrays.asList(syntaxErrorMessage);
        }
        // Check all the errors and for each error add it ands its arguments to the returned list
        List<String> result = new ArrayList<String>();
        Map<ValidationFailureReason, List<ValidationError>> resultMap =
                new HashMap<VmPropertiesUtils.ValidationFailureReason, List<ValidationError>>();
        separateValidationErrorsList(errorsList, resultMap);
        for (Map.Entry<ValidationFailureReason, String> entry : failureReasonsToVdcBllMessagesMap.entrySet()) {
            List<ValidationError> errorsListForReason = resultMap.get(entry.getKey());
            String vdcBllMessage = entry.getValue();
            String formatString = failureReasonsToParameterFormatStrings.get(entry.getKey());
            addMessage(result, vdcBllMessage, formatString, errorsListForReason);
        }
        return result;
    }

    private static void addMessage(List<String> resultMessages,
            String vdcBllMessage,
            String formatString, List<ValidationError> errorsList) {
        if (errorsList != null && !errorsList.isEmpty()) {
            String keys = getCommaDelimitedKeys(errorsList);
            resultMessages.add(vdcBllMessage);
            resultMessages.add(String.format(formatString, keys));
        }
    }

    private static String getCommaDelimitedKeys(List<ValidationError> validationErrors) {
        if (validationErrors == null || validationErrors.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        Iterator<ValidationError> iterator = validationErrors.iterator();

        for (int counter = 0; counter < validationErrors.size() - 1; counter++) {
            ValidationError error = iterator.next();
            sb.append(error.getKeyName()).append(",");
        }

        ValidationError error = iterator.next();
        sb.append(error.getKeyName());

        return sb.toString();

    }

    public static void separeteCustomPropertiesToUserAndPredefined(VmStatic vmStatic) {
        String customProperties = vmStatic.getCustomProperties();
        VMCustomProperties properties = VmPropertiesUtils.parseProperties(customProperties);
        vmStatic.setPredefinedProperties(properties.getPredefinedProperties());
        vmStatic.setUserDefinedProperties(properties.getUseDefinedProperties());
    }
}
