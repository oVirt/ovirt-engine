package org.ovirt.engine.core.common.utils.customprop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.exceptions.InitializationException;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

/**
 * Helper methods to help parse and validate custom VM (predefined and user defined) properties. These methods are used
 * by vdsbroker and bll modules
 *
 */
public class VmPropertiesUtils extends CustomPropertiesUtils {

    private static final VmPropertiesUtils instance = new VmPropertiesUtils();

    private Map<Version, Map<String, String>> predefinedProperties;
    private Map<Version, Map<String, String>> userdefinedProperties;
    private Map<Version, Map<String, String>> allVmProperties;

    public static VmPropertiesUtils getInstance() {
        return instance;
    }

    public void init() throws InitializationException {
        try {
            predefinedProperties = new HashMap<>();
            userdefinedProperties = new HashMap<>();
            allVmProperties = new HashMap<>();
            Set<Version> versions = getSupportedClusterLevels();
            for (Version version : versions) {
                predefinedProperties.put(version, new HashMap<>());
                userdefinedProperties.put(version, new HashMap<>());
                allVmProperties.put(version, new HashMap<>());
                parsePropertiesRegex(getPredefinedVMProperties(version), predefinedProperties.get(version));
                parsePropertiesRegex(getUserDefinedVMProperties(version), userdefinedProperties.get(version));
                allVmProperties.get(version).putAll(predefinedProperties.get(version));
                allVmProperties.get(version).putAll(userdefinedProperties.get(version));
            }
        } catch (Throwable ex) {
            throw new InitializationException(ex);
        }
    }

    public String getUserDefinedVMProperties(Version version) {
        return Config.getValue(ConfigValues.UserDefinedVMProperties, version.getValue());
    }

    public String getPredefinedVMProperties(Version version) {
        return Config.getValue(ConfigValues.PredefinedVMProperties, version.getValue());
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

        public String getUserDefinedProperties() {
            return userDefinedProperties;
        }
    }

    /**
     * Parses a string containing user defined and predefined custom properties and returns VMCustomProperties object
     * that contains the properties separated to two strings - one for the predefined properties and one for the user
     * defined properties
     */
    public VMCustomProperties parseProperties(Version version, String propertiesStr) {
        Map<String, String> userDefinedPropertiesMap = new HashMap<>();
        Map<String, String> predefinedPropertiesMap = new HashMap<>();

        convertCustomPropertiesStrToMaps(version, propertiesStr, predefinedPropertiesMap, userDefinedPropertiesMap);
        return new VMCustomProperties(convertProperties(predefinedPropertiesMap),
                convertProperties(userDefinedPropertiesMap));
    }

    /**
     * Validates a properties field value (checks if its format matches key1=val1;key2=val2;....)
     *
     * @return a list of validation errors. if there are no errors - the list will be empty
     */
    public List<ValidationError> validateVmProperties(Version version, String properties) {
        if (syntaxErrorInProperties(properties)) {
            return invalidSyntaxValidationError;
        }
        return validateVmProperties(version, convertProperties(properties));
    }

    public List<ValidationError> validateVmProperties(Version version, Map<String, String> properties) {
        return validateProperties(allVmProperties.get(version), properties);
    }

    /**
     * Get a map containing all the VM custom properties
     *
     * @return map containing the VM custom properties
     */
    public Map<String, String> getVMProperties(Version version, VmBase vmBase) {
        separateCustomPropertiesToUserAndPredefined(version, vmBase);
        Map<String, String> map = new HashMap<>();
        getPredefinedProperties(version, vmBase, map);
        getUserDefinedProperties(version, vmBase, map);

        return map;
    }

    public Map<Version, Map<String, String>> getAllVmProperties() {
        return allVmProperties;
    }

    private void getPredefinedProperties(Version version, VmBase vmBase, Map<String, String> propertiesMap) {
        String predefinedProperties = vmBase.getPredefinedProperties();
        getVMProperties(version, propertiesMap, predefinedProperties);
    }

    private void getUserDefinedProperties(Version version, VmBase vmBase, Map<String, String> propertiesMap) {
        String UserDefinedProperties = vmBase.getUserDefinedProperties();
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
    private void getVMProperties(Version version, Map<String, String> propertiesMap, String vmPropertiesFieldValue) {
        // format of properties is key1=val1,key2=val2,key3=val3,key4=val4
        if (StringHelper.isNullOrEmpty(vmPropertiesFieldValue)) {
            return;
        }

        propertiesMap.putAll(convertProperties(vmPropertiesFieldValue, allVmProperties.get(version)));
    }

    private void convertCustomPropertiesStrToMaps(Version version, String propertiesValue,
            Map<String, String> predefinedPropertiesMap, Map<String, String> userDefinedPropertiesMap) {
        Map<String, String> propertiesMap =
                convertProperties(propertiesValue, allVmProperties.get(version));
        Set<Entry<String, String>> propertiesEntries = propertiesMap.entrySet();

        // Go over all the properties - if the key of the property exists in the
        // predefined key set -
        // add it to the predefined map, otherwise - add it to the user defined
        // map

        Set<String> predefinedPropertiesKeys = predefinedProperties.get(version).keySet();
        Set<String> userdefinedPropertiesKeys = userdefinedProperties.get(version).keySet();
        for (Entry<String, String> propertiesEntry : propertiesEntries) {
            String propertyKey = propertiesEntry.getKey();
            String propertyValue = Objects.toString(propertiesEntry.getValue(), "");
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
     */
    public String customProperties(String predefinedProperties, String userDefinedProperties) {
        String predefined = predefinedProperties == null ? "" : predefinedProperties;
        String userDefined = userDefinedProperties == null ? "" : userDefinedProperties;

        StringBuilder result = new StringBuilder();
        result.append(predefined);
        if (!predefined.isEmpty() && !userDefined.isEmpty()) {
            result.append(PROPERTIES_DELIMETER);
        }
        result.append(userDefined);
        return result.toString();
    }

    public void separateCustomPropertiesToUserAndPredefined(Version version, VmBase vmBase) {
        String customProperties = vmBase.getCustomProperties();
        VMCustomProperties properties = parseProperties(version, customProperties);
        vmBase.setPredefinedProperties(properties.getPredefinedProperties());
        vmBase.setUserDefinedProperties(properties.getUserDefinedProperties());
    }

    /**
     * Returns the string describing format of VM properties specification
     *
     * @return the string describing format of VM properties specification
     */
    public String getVmPropSpec() {
        return VALIDATION_STR;
    }

    public boolean validateVmProperties(Version version, String properties, List<String> message) {
        List<ValidationError> validationErrors = validateVmProperties(version, properties);
        if (!validationErrors.isEmpty()) {
            handleCustomPropertiesError(validationErrors, message);
            return false;
        }
        return true;
    }
}
