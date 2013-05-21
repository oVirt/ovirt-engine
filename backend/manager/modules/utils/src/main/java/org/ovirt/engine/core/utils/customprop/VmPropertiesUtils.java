package org.ovirt.engine.core.utils.customprop;

import java.util.ArrayList;
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
import org.ovirt.engine.core.utils.exceptions.InitializationException;

/**
 * Helper methods to help parse and validate custom VM (predefined and user defined) properties. These
 * methods are used by vdsbroker and bll modules
 *
 */
public class VmPropertiesUtils extends CustomPropertiesUtils {

    private static VmPropertiesUtils vmPropertiesUtils = null;

    static {
        vmPropertiesUtils = new VmPropertiesUtils();
    }

    private Map<Version, Map<String, Pattern>> predefinedProperties;
    private Map<Version, Map<String, Pattern>> userdefinedProperties;
    private Map<Version, String> allVmProperties;

    public static VmPropertiesUtils getInstance() {
        return vmPropertiesUtils;
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
                parsePropertiesRegex(predefinedVMPropertiesStr, predefinedProperties.get(version));
                parsePropertiesRegex(userDefinedVMPropertiesStr, userdefinedProperties.get(version));
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

    public void separeteCustomPropertiesToUserAndPredefined(Version version, VmStatic vmStatic) {
        String customProperties = vmStatic.getCustomProperties();
        VMCustomProperties properties = parseProperties(version, customProperties);
        vmStatic.setPredefinedProperties(properties.getPredefinedProperties());
        vmStatic.setUserDefinedProperties(properties.getUseDefinedProperties());
    }

    /**
     * Returns the string describing format of VM properties specification
     *
     * @return the string describing format of VM properties specification
     */
    public String getVmPropSpec() {
        return VALIDATION_STR;
    }
}
