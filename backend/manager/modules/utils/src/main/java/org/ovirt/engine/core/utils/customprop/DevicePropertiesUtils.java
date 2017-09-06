package org.ovirt.engine.core.utils.customprop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.customprop.CustomPropertiesUtils;
import org.ovirt.engine.core.common.utils.customprop.ValidationError;
import org.ovirt.engine.core.common.utils.customprop.ValidationFailureReason;
import org.ovirt.engine.core.common.utils.exceptions.InitializationException;
import org.ovirt.engine.core.compat.Version;

/**
 * A class providing helper methods to work with device custom properties
 */
public class DevicePropertiesUtils extends CustomPropertiesUtils {
    /**
     * Singleton instance of the class
     */
    private static final DevicePropertiesUtils devicePropertiesUtils;

    static {
        devicePropertiesUtils = new DevicePropertiesUtils();
    }

    /**
     * Prefix for device type definition
     */
    private static final String TYPE_PREFIX_STR = "\\{type=(";

    /**
     * Length of prefix for device type definition
     */
    private static final int TYPE_PREFIX_LEN = 6;

    /**
     * Prefix for device properties specification
     */
    private static final String PROP_PREFIX_STR = ");prop=\\{";

    /**
     * Length of prefix for device properties specification
     */
    private static final int PROP_PREFIX_LEN = 7;

    /**
     * Suffix of device type properties specification
     */
    private static final String PROP_SUFFIX_STR = "\\}\\}[;]?";

    /**
     * Device type delimiter for validation parsing pattern
     */
    private static final String DEV_TYPE_DELIM = "|";

    /**
     * String to create pattern to parse device custom properties specification
     */
    private final String devicePropSplitStr;

    /**
     * Pattern to parse device custom properties specification
     */
    private final Pattern devicePropSplitPattern;

    /**
     * String to create pattern to validate device custom properties specification
     */
    private final String devicePropValidationStr;

    /**
     * Pattern to validate device custom properties specification
     */
    private final Pattern devicePropValidationPattern;

    /**
     * Map of device custom properties for each version and device type
     */
    private Map<Version, EnumMap<VmDeviceGeneralType, Map<String, String>>> deviceProperties;

    /**
     * List of device types for which custom properties can be set
     */
    private final List<VmDeviceGeneralType> supportedDeviceTypes;

    /**
     * Error thrown if device custom properties are to be set for a device with UNKNOWN type
     */
    protected final List<ValidationError> invalidDeviceTypeValidationError;

    /**
     * Creates an instance and initializes device custom properties patterns. Constructor is package visible for testing
     * purposes.
     */
    DevicePropertiesUtils() {
        // init supported device types
        supportedDeviceTypes = new ArrayList<>();
        for (VmDeviceGeneralType t : VmDeviceGeneralType.values()) {
            // custom properties cannot be set for UNKNOWN
            if (t != VmDeviceGeneralType.UNKNOWN) {
                supportedDeviceTypes.add(t);
            }
        }

        StringBuilder sb = new StringBuilder();

        // init pattern to parse properties
        sb.append(TYPE_PREFIX_STR);
        for (VmDeviceGeneralType t : supportedDeviceTypes) {
            sb.append(t.getValue());
            sb.append(DEV_TYPE_DELIM);
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(PROP_PREFIX_STR);
        sb.append(VALIDATION_STR);
        sb.append(PROP_SUFFIX_STR);
        devicePropSplitStr = sb.toString();
        devicePropSplitPattern = Pattern.compile(devicePropSplitStr);

        // init pattern to validate device properties definition
        sb = new StringBuilder();
        for (VmDeviceGeneralType t : supportedDeviceTypes) {
            sb.append("((");
            sb.append(TYPE_PREFIX_STR);
            sb.append(t.getValue());
            sb.append(PROP_PREFIX_STR);
            sb.append(VALIDATION_STR);
            sb.append(PROP_SUFFIX_STR);
            sb.append(")?)");
        }
        devicePropValidationStr = sb.toString();
        devicePropValidationPattern = Pattern.compile(devicePropValidationStr);

        invalidDeviceTypeValidationError =
                Collections.singletonList(new ValidationError(ValidationFailureReason.INVALID_DEVICE_TYPE, ""));
    }

    /**
     * Returns device custom properties definition for specified version. It exists for testing purposes only.
     *
     * @param version
     *            specified version
     * @return device custom properties definition for specified version
     */
    String getCustomDeviceProperties(Version version) {
        return Config.getValue(ConfigValues.CustomDeviceProperties, version.getValue());
    }

    /**
     * Returns instance of the class
     *
     * @return instance of the class
     */
    public static DevicePropertiesUtils getInstance() {
        return devicePropertiesUtils;
    }

    /**
     * Loads device custom properties definition
     *
     * @throws InitializationException
     *             if an error occured during device custom properties definition loading
     */
    public void init() throws InitializationException {
        try {
            Set<Version> versions = getSupportedClusterLevels();
            String devicePropertiesStr;
            deviceProperties = new HashMap<>();
            for (Version version : versions) {
                // load device properties
                devicePropertiesStr = getCustomDeviceProperties(version);
                deviceProperties.put(version, new EnumMap<>(VmDeviceGeneralType.class));
                Matcher typeMatcher = devicePropSplitPattern.matcher(devicePropertiesStr);
                while (typeMatcher.find()) {
                    String dcpStr = typeMatcher.group();
                    // device type definition starts with "{type="
                    int start = TYPE_PREFIX_LEN;
                    int end = dcpStr.length() - 1;
                    if (dcpStr.endsWith(PROPERTIES_DELIMETER)) {
                        // remove trailing ;
                        end--;
                    }
                    dcpStr = dcpStr.substring(start, end);
                    int idx = dcpStr.indexOf(PROPERTIES_DELIMETER);
                    VmDeviceGeneralType type = VmDeviceGeneralType.forValue(dcpStr.substring(0, idx));
                    // properties definition for device starts with ";prop={"
                    String propStr = dcpStr.substring(idx + PROP_PREFIX_LEN, dcpStr.length() - 1);
                    Map<String, String> props = new HashMap<>();
                    parsePropertiesRegex(propStr, props);
                    deviceProperties.get(version).put(type, props);
                }
            }
        } catch (Exception ex) {
            throw new InitializationException(ex);
        }
    }

    /**
     * Returns set of device types which have defined properties in specified version
     *
     * @param version
     *            version of the cluster that the VM containing the device is in
     */
    public Set<VmDeviceGeneralType> getDeviceTypesWithProperties(Version version) {
        EnumMap<VmDeviceGeneralType, Map<String, String>> map = deviceProperties.get(version);
        if (map.isEmpty()) {
            // no device type has any properties
            return Collections.emptySet();
        } else {
            // prevent client to modify
            return Collections.unmodifiableSet(map.keySet());
        }
    }

    /**
     * Returns map of device properties [property name, pattern to validate property value] for specified version and
     * device type
     *
     * @param version
     *            version of the cluster that the VM containing the device is in
     * @param type
     *            specified device type
     * @return map of device properties
     */
    public Map<String, String> getDeviceProperties(Version version, VmDeviceGeneralType type) {
        Map<String, String> map = deviceProperties.get(version).get(type);
        if (map == null) {
            // no defined properties for specified type
            map = new HashMap<>();
        } else {
            // prevent client to modify map of properties
            map = new HashMap<>(map);
        }
        return map;
    }

    /**
     * Validates the custom properties of a device. These errors are checked during validation:
     * <ol>
     *   <li>Device custom properties are supported in version</li>
     *   <li>Device custom properties can be assigned to specified type</li>
     *   <li>Device custom properties syntax is ok</li>
     *   <li>Device custom property is defined for specified version and type</li>
     *   <li>Device custom property value matches its value constrains</li>
     * </ol>
     *
     * @param version
     *            version of the cluster that the VM containing the device is in
     * @param type
     *            type of a device
     * @param properties
     *            device custom properties
     * @return list of errors appeared during validation
     */
    public List<ValidationError> validateProperties(Version version, VmDeviceGeneralType type,
            Map<String, String> properties) {

        if (properties == null || properties.isEmpty()) {
            // No errors in case of empty value
            return Collections.emptyList();
        }

        if (!supportedDeviceTypes.contains(type)) {
            return invalidDeviceTypeValidationError;
        }

        if (syntaxErrorInProperties(properties)) {
            return invalidSyntaxValidationError;
        }

        List<ValidationError> results = new ArrayList<>();
        Set<ValidationError> errorsSet = new HashSet<>();

        for (Map.Entry<String, String> e : properties.entrySet()) {
            String key = e.getKey();
            if (key == null || !deviceProperties.get(version).get(type).containsKey(key)) {
                errorsSet.add(new ValidationError(ValidationFailureReason.KEY_DOES_NOT_EXIST, key));
                continue;
            }

            String value = StringUtils.defaultString(e.getValue());
            if (!value.matches(deviceProperties.get(version).get(type).get(key))) {
                errorsSet.add(new ValidationError(ValidationFailureReason.INCORRECT_VALUE, key));
                continue;
            }
        }
        results.addAll(errorsSet);
        return results;
    }

    /**
     * Validates device custom properties definition
     *
     * @param propDef
     *            device custom properties definition
     * @return {@code true} if device custom properties definition is valid, otherwise {@code false}
     */
    public boolean isDevicePropertiesDefinitionValid(String propDef) {
        return devicePropValidationPattern.matcher(propDef).matches();
    }

    /**
     * Returns the string describing format of custom device properties specification
     *
     * @return the string describing format of custom device properties specification
     */
    public String getDevicePropertiesDefinition() {
        return devicePropSplitStr;
    }
}
