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

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.exceptions.InitializationException;

/**
 * Helper methods to help parse and validate predefined and UserDefined(user defined) properties. These methods are used
 * by vdsbroker and bll modules
 *
 */
public class VmPropertiesUtils {

    private static VmPropertiesUtils vmPropertiesUtils = null;

    public static VmPropertiesUtils getInstance() {
        if (vmPropertiesUtils == null) {
            synchronized (VmPropertiesUtils.class) {
                if (vmPropertiesUtils == null) {
                    vmPropertiesUtils = new VmPropertiesUtils();
                }
            }
        }
        return vmPropertiesUtils;
    }

    private final Pattern SEMICOLON_PATTERN = Pattern.compile(";");
    private final String PROPERTIES_DELIMETER = ";";
    private final String KEY_VALUE_DELIMETER = "=";

    private final String LEGITIMATE_CHARACTER_FOR_KEY = "[a-z_A-Z0-9]";
    private final String LEGITIMATE_CHARACTER_FOR_VALUE = "[^" + PROPERTIES_DELIMETER + "]"; // all characters
                                                                                                    // but the delimiter
                                                                                                    // are allowed
    private final Pattern VALUE_PATTERN = Pattern.compile("(" + LEGITIMATE_CHARACTER_FOR_VALUE + ")+");

    // private static final Pattern KEY_VALIDATION_PATTERN = Pattern.compile("(" + LEGITIMATE_CHARACTER + ")+");

    // properties are in form of key1=val1;key2=val2; .... key and include alpha numeric characters and _
    private final String KEY_VALUE_REGEX_STR = "((" + LEGITIMATE_CHARACTER_FOR_KEY + ")+)=(("
            + LEGITIMATE_CHARACTER_FOR_VALUE + ")+)";

    // frontend can pass custom values in the form of "key=value" or "key1=value1;... key-n=value_n" (if there is only
    // one key-value, no ; is attached to it
    private final Pattern VALIDATION_PATTERN = Pattern.compile(KEY_VALUE_REGEX_STR + "(;" + KEY_VALUE_REGEX_STR
            + ")*;?");

    private final List<ValidationError> invalidSyntaxValidationError =
            Arrays.asList(new ValidationError(ValidationFailureReason.SYNTAX_ERROR, ""));

    private Map<Version, Map<String, Pattern>> predefinedProperties;
    private Map<Version, Map<String, Pattern>> userdefinedProperties;
    private Map<Version, String> allVmProperties;

    // Defines why validation failed
    public enum ValidationFailureReason {
        KEY_DOES_NOT_EXIST,
        INCORRECT_VALUE,
        SYNTAX_ERROR,
        DUPLICATE_KEY,
        NO_ERROR
    };

    public class ValidationError {
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

    public void init() throws InitializationException {
        try {
            predefinedProperties = new HashMap<Version, Map<String, Pattern>>();
            userdefinedProperties = new HashMap<Version, Map<String, Pattern>>();
            allVmProperties = new HashMap<Version, String>();
            Set<Version> versions = getSupportedClusterLevels();
            String predefinedVMPropertiesStr, userDefinedVMPropertiesStr;
            StringBuilder sb;
            for (Version version : versions) {
                predefinedVMPropertiesStr = getPredefinedVMProperties(version);
                userDefinedVMPropertiesStr = getUserdefinedVMProperties(version);
                sb = new StringBuilder("");
                sb.append(predefinedVMPropertiesStr);
                if (!predefinedVMPropertiesStr.isEmpty() && !userDefinedVMPropertiesStr.isEmpty()) {
                    sb.append(";");
                }
                sb.append(userDefinedVMPropertiesStr);
                allVmProperties.put(version, sb.toString());

                predefinedProperties.put(version, new HashMap<String, Pattern>());
                userdefinedProperties.put(version, new HashMap<String, Pattern>());
                parseVMPropertiesRegex(predefinedVMPropertiesStr, predefinedProperties.get(version));
                parseVMPropertiesRegex(userDefinedVMPropertiesStr, userdefinedProperties.get(version));
            }
        } catch (Throwable ex) {
            throw new InitializationException(ex);
        }
    }

    public String getUserdefinedVMProperties(Version version) {
        return Config.<String> GetValue(ConfigValues.UserDefinedVMProperties, version.getValue());
    }

    public String getPredefinedVMProperties(Version version) {
        return Config.<String> GetValue(ConfigValues.PredefinedVMProperties, version.getValue());
    }

    public Set<Version> getSupportedClusterLevels() {
        Set<Version> versions = Config.<java.util.HashSet<Version>> GetValue(ConfigValues.SupportedClusterLevels);
        return versions;
    }

    public class VMCustomProperties {
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
    public VMCustomProperties parseProperties(Version version, String propertiesStr) {
        HashMap<String, String> userDefinedPropertiesMap = new HashMap<String, String>();
        HashMap<String, String> predefinedPropertiesMap = new HashMap<String, String>();

        convertCustomPropertiesStrToMaps(version, propertiesStr, predefinedPropertiesMap, userDefinedPropertiesMap);
        return new VMCustomProperties(vmPropertiesToString(predefinedPropertiesMap),
                vmPropertiesToString(userDefinedPropertiesMap));
    }

    /**
     * Validates all the custom properties of a VM
     *
     * @param vmStatic
     * @return
     */
    public List<ValidationError> validateVMProperties(Version version, VmStatic vmStatic) {
        List<ValidationError> errors = validateProperties(version, vmStatic.getCustomProperties());
        return errors;
    }

    private void parseVMPropertiesRegex(String properties, Map<String, Pattern> keysToRegex) {
        if (StringUtils.isEmpty(properties)) {
            return;
        }

        String[] propertiesStrs = SEMICOLON_PATTERN.split(properties);

        // Property is in the form of key=regex
        for (String property : propertiesStrs) {

            Pattern pattern = null;
            String[] propertyParts = property.split(KEY_VALUE_DELIMETER, 2);
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
    private List<ValidationError> validateProperties(Version version, String properties) {

        if (StringUtils.isEmpty(properties)) { // No errors in case of empty value
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
        List<ValidationError> result = populateVMProperties(version, properties, map);
        return result;
    }

    private boolean syntaxErrorInProperties(String properties) {
        return !VALIDATION_PATTERN.matcher(properties).matches();
    }

    private boolean isValueValid(Version version, String key, String value) {
        // Checks that the value for the given property is valid by running by trying to perform
        // regex validation
        Pattern userDefinedPattern = userdefinedProperties.get(version).get(key);
        Pattern predefinedPattern = predefinedProperties.get(version).get(key);

        return (userDefinedPattern != null && userDefinedPattern.matcher(value).matches())
                || (predefinedPattern != null && predefinedPattern.matcher(value).matches());
    }

    /**
     * Get a map containing all the VM custom properties
     *
     * @return map containing the VM custom properties
     */
    public Map<String, String> getVMProperties(Version version, VmStatic vmStatic) {
        separeteCustomPropertiesToUserAndPredefined(version, vmStatic);
        Map<String, String> map = new HashMap<String, String>();
        getPredefinedProperties(version, vmStatic, map);
        getUserDefinedProperties(version, vmStatic, map);

        return map;
    }

    public Map<Version, String> getAllVmProperties() {
        return allVmProperties;
    }

    /**
     * Get a map containing user defined properties from VM
     *
     * @param vm
     *            vm to get the map for
     * @return map containing the UserDefined properties
     */
    public Map<String, String> getUserDefinedProperties(Version version, VmStatic vmStatic) {
        Map<String, String> map = new HashMap<String, String>();
        getUserDefinedProperties(version, vmStatic, map);
        return map;
    }

    /**
     * Gets a map containing the predefined properties from VM
     *
     * @param vm
     *            vm to get the map for
     * @return map containing the vm properties
     */
    public Map<String, String> getPredefinedProperties(Version version, VmStatic vmStatic) {
        Map<String, String> map = new HashMap<String, String>();
        getPredefinedProperties(version, vmStatic, map);
        return map;
    }

    private void getPredefinedProperties(Version version, VmStatic vmStatic, Map<String, String> propertiesMap) {
        String predefinedProperties = vmStatic.getPredefinedProperties();
        getVMProperties(version, propertiesMap, predefinedProperties);
    }

    private void getUserDefinedProperties(Version version, VmStatic vmStatic, Map<String, String> propertiesMap) {
        String UserDefinedProperties = vmStatic.getUserDefinedProperties();
        getVMProperties(version, propertiesMap, UserDefinedProperties);
    }

    /**
     * Converts VM properties field value (either UserDefined or defined) to a map containing property names & values
     *
     * @param propertiesMap
     *            map that will hold the conversion result
     * @param vmPropertiesFieldValue
     *            the string value that contains the properties
     */
    public void getVMProperties(Version version, Map<String, String> propertiesMap, String vmPropertiesFieldValue) {
        // format of properties is key1=val1,key2=val2,key3=val3,key4=val4
        if (StringUtils.isEmpty(vmPropertiesFieldValue)) {
            return;
        }

        populateVMProperties(version, vmPropertiesFieldValue, propertiesMap);

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
    private List<ValidationError> populateVMProperties(Version version, String vmPropertiesFieldValue,
            Map<String, String> propertiesMap) {
        Set<ValidationError> errorsSet = new HashSet<VmPropertiesUtils.ValidationError>();
        List<ValidationError> results = new ArrayList<VmPropertiesUtils.ValidationError>();
        if (!StringUtils.isEmpty(vmPropertiesFieldValue)) {
            String keyValuePairs[] = SEMICOLON_PATTERN.split(vmPropertiesFieldValue);

            for (String keyValuePairStr : keyValuePairs) {
                String[] pairParts = keyValuePairStr.split(KEY_VALUE_DELIMETER, 2);
                String key = pairParts[0];
                String value = pairParts[1];
                if (propertiesMap.containsKey(key)) {
                    errorsSet.add(new ValidationError(ValidationFailureReason.DUPLICATE_KEY, key));
                    continue;
                }
                if (!keyExistsInVersion(predefinedProperties, version, key)
                        && !keyExistsInVersion(userdefinedProperties, version, key)) {
                    errorsSet.add(new ValidationError(ValidationFailureReason.KEY_DOES_NOT_EXIST, key));
                    continue;
                }

                if (!isValueValid(version, key, value)) {
                    errorsSet.add(new ValidationError(ValidationFailureReason.INCORRECT_VALUE, key));
                    continue;
                }
                propertiesMap.put(key, value);
            }
        }
        results.addAll(errorsSet);
        return results;
    }

    protected boolean keyExistsInVersion(Map<Version, Map<String, Pattern>> propertiesMap, Version version, String key) {
        return propertiesMap.get(version).containsKey(key);
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

    private void convertCustomPropertiesStrToMaps(Version version, String propertiesValue,
            Map<String, String> predefinedPropertiesMap, Map<String, String> userDefinedPropertiesMap) {
        Map<String, String> propertiesMap = new HashMap<String, String>();
        populateVMProperties(version, propertiesValue, propertiesMap);
        Set<Entry<String, String>> propertiesEntries = propertiesMap.entrySet();

        // Go over all the properties - if the key of the property exists in the
        // predefined key set -
        // add it to the predefined map, otherwise - add it to the user defined
        // map

        Set<String> predefinedPropertiesKeys = predefinedProperties.get(version).keySet();
        Set<String> userdefinedPropertiesKeys = userdefinedProperties.get(version).keySet();
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
    public String customProperties(String predefinedProperties, String userDefinedProperties) {
        StringBuilder result = new StringBuilder();
        result.append((StringUtils.isEmpty(predefinedProperties)) ? "" : predefinedProperties);
        result.append((result.length() == 0) ? "" : ";");
        result.append((StringUtils.isEmpty(userDefinedProperties)) ? "" : userDefinedProperties);
        return result.toString();
    }

    /**
     * Splits the validation errors list to lists of missing keys and wrong key values
     *
     * @param errorsList
     * @param missingKeysList
     * @param wrongKeyValues
     */
    private void separateValidationErrorsList(List<ValidationError> errorsList,
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
    public List<String> generateErrorMessages(List<ValidationError> errorsList,
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

    public void separeteCustomPropertiesToUserAndPredefined(Version version, VmStatic vmStatic) {
        String customProperties = vmStatic.getCustomProperties();
        VMCustomProperties properties = parseProperties(version, customProperties);
        vmStatic.setPredefinedProperties(properties.getPredefinedProperties());
        vmStatic.setUserDefinedProperties(properties.getUseDefinedProperties());
    }
}
