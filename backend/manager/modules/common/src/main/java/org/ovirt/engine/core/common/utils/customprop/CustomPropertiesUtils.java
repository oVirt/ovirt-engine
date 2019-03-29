package org.ovirt.engine.core.common.utils.customprop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;

/**
 * Abstract class to ease custom properties handling
 *
 */
public class CustomPropertiesUtils {
    /**
     * Delimiter of each property definition
     */
    protected static final String PROPERTIES_DELIMETER = ";";

    /**
     * Delimiter of property name and value
     */
    protected static final String KEY_VALUE_DELIMETER = "=";

    /**
     * Regex describing legitimate characters for property name - alphanumeric characters and underscore
     */
    protected static final String LEGITIMATE_CHARACTER_FOR_KEY = "[a-z_A-Z0-9]";

    /**
     * Regex describing property name
     */
    protected static final String KEY_REGEX = "(" + LEGITIMATE_CHARACTER_FOR_KEY + ")+";

    /**
     * Regex describing legitimate characters for property value - all except {@code PROPERTIES_DELIMITER}
     */
    protected static final String LEGITIMATE_CHARACTER_FOR_VALUE = "[^" + PROPERTIES_DELIMETER + "]";

    /**
     * Regex describing property value
     */
    protected static final String VALUE_REGEX = "(" + LEGITIMATE_CHARACTER_FOR_VALUE + ")*";

    /**
     * Regex describing property definition - key=value
     */
    protected static final String KEY_VALUE_REGEX_STR = "((" + LEGITIMATE_CHARACTER_FOR_KEY + ")+)=(("
            + LEGITIMATE_CHARACTER_FOR_VALUE + ")*)";

    /**
     * Regex describing properties definition. They can be in the from of "key=value" or "key1=value1;... key-n=value_n"
     * (last {@code ;} character can be omitted)
     */
    protected static final String VALIDATION_STR = "(" + KEY_VALUE_REGEX_STR + "(;" + KEY_VALUE_REGEX_STR + ")*;?)?";

    /**
     * List defining syntax error during properties validation
     */
    protected final List<ValidationError> invalidSyntaxValidationError;

    /**
     * Constructor has package access to enable testing, but class cannot be instantiated outside package
     */
    protected CustomPropertiesUtils() {
        invalidSyntaxValidationError = Arrays.asList(new ValidationError(ValidationFailureReason.SYNTAX_ERROR, ""));
    }

    /**
     * Returns supported cluster levels. Method should be used only for testing.
     *
     * @return supported cluster levels
     */
    public Set<Version> getSupportedClusterLevels() {
        Set<Version> versions = Config.<Set<Version>> getValue(ConfigValues.SupportedClusterLevels);
        return versions;
    }

    /**
     * Test if custom properties contains syntax error
     *
     * @param properties
     *            custom properties
     * @return returns {@code true} if custom properties contains syntax error, otherwise {@code false}
     */
    public boolean syntaxErrorInProperties(String properties) {
        return properties != null && !properties.matches(VALIDATION_STR);
    }

    /**
     * Test if custom properties contains syntax error
     *
     * @param properties
     *            custom properties
     * @return returns {@code true} if custom properties contains syntax error, otherwise {@code false}
     */
    public boolean syntaxErrorInProperties(Map<String, String> properties) {
        boolean error = false;
        if (properties != null && !properties.isEmpty()) {
            for (Map.Entry<String, String> e : properties.entrySet()) {
                String key = e.getKey();
                if (key == null || !key.matches(KEY_REGEX)) {
                    // syntax error in property name
                    error = true;
                    break;
                }

                if (!Objects.toString(e.getValue(), "").matches(VALUE_REGEX)) {
                    // syntax error in property value
                    error = true;
                    break;
                }
            }
        }
        return error;
    }

    /**
     * Converts properties specification from {@code String} to {@code Map<String, Pattern}
     */
    protected void parsePropertiesRegex(String properties, Map<String, String> keysToRegex) {
        if (StringHelper.isNullOrEmpty(properties)) {
            return;
        }

        String[] propertiesStrs = properties.split(PROPERTIES_DELIMETER);

        // Property is in the form of key=regex
        for (String property : propertiesStrs) {

            String pattern = null;
            String[] propertyParts = property.split(KEY_VALUE_DELIMETER, 2);
            if (propertyParts.length == 1) {
                // there is no value(regex) for the property - we assume in that case that any value is allowed except
                // for the properties delimiter
                pattern = VALUE_REGEX;
            } else {
                pattern = propertyParts[1];
            }

            keysToRegex.put(propertyParts[0], pattern);
        }
    }

    /**
     * Splits the validation errors list to lists of missing keys and wrong key values
     */
    protected void separateValidationErrorsList(List<ValidationError> errorsList,
            Map<ValidationFailureReason, List<ValidationError>> resultMap) {
        if (errorsList == null || errorsList.isEmpty()) {
            return;
        }

        for (ValidationError error : errorsList) {
            List<ValidationError> errorsForReason = resultMap.get(error.getReason());
            if (errorsForReason == null) {
                errorsForReason = new ArrayList<>();
                resultMap.put(error.getReason(), errorsForReason);
            }
            errorsForReason.add(error);
        }
    }

    /**
     * validate a map of specific custom properties against provided regex map
     * @param regExMap
     *      [key, regex] map
     * @param properties
     *      [key, value] map, custom properties to validate
     */
    public List<ValidationError> validateProperties(Map<String, String> regExMap,
            Map<String, String> properties) {

        if (properties == null || properties.isEmpty()) {
            // No errors in case of empty value
            return Collections.emptyList();
        }

        if (syntaxErrorInProperties(properties)) {
            return invalidSyntaxValidationError;
        }

        Set<ValidationError> errorsSet = new HashSet<>();
        Set<String> foundKeys = new HashSet<>();
        for (Entry<String, String> e : properties.entrySet()) {
            String key = e.getKey();
            if (foundKeys.contains(key)) {
                errorsSet.add(new ValidationError(ValidationFailureReason.DUPLICATE_KEY, key));
                continue;
            }
            foundKeys.add(key);

            if (key == null || !regExMap.containsKey(key)) {
                errorsSet.add(new ValidationError(ValidationFailureReason.KEY_DOES_NOT_EXIST, key));
                continue;
            }

            String value = e.getValue() == null ? "" : e.getValue();
            if (!value.matches(regExMap.get(key))) {
                errorsSet.add(new ValidationError(ValidationFailureReason.INCORRECT_VALUE, key));
                continue;
            }
        }
        List<ValidationError> results = new ArrayList<>();
        results.addAll(errorsSet);
        return results;
    }

    /**
     * Generates an error message to be displayed by frontend
     *
     * @param validationErrors
     *            list of errors appeared during validation
     * @param message
     *            list of error messages to display
     */
    public void handleCustomPropertiesError(List<ValidationError> validationErrors, List<String> message) {
        // No errors
        if (validationErrors == null || validationErrors.isEmpty()) {
            return;
        }
        // Syntax error is one of the most severe errors, and should be returned without checking the rest of the errors
        if (validationErrors.size() == 1 && validationErrors.get(0).getReason() == ValidationFailureReason.SYNTAX_ERROR) {
            message.add(ValidationFailureReason.SYNTAX_ERROR.getErrorMessage().name());
            return;
        }

        // Check all the errors and for each error add it ands its arguments to the returned list
        Map<ValidationFailureReason, List<ValidationError>> resultMap =
                new HashMap<>();
        separateValidationErrorsList(validationErrors, resultMap);

        for (ValidationFailureReason reason : ValidationFailureReason.values()) {
            List<ValidationError> errorsListForReason = resultMap.get(reason);
            if (errorsListForReason != null && !errorsListForReason.isEmpty()) {
                String keys = getCommaDelimitedKeys(errorsListForReason);
                message.add(reason.getErrorMessage().name());
                message.add(reason.formatErrorMessage(keys));
            }
        }
    }

    /**
     * Returns string containing comma separated list of all property names appeared in error list
     *
     * @param validationErrors
     *            error list
     * @return string containing comma separated list of all property names appeared in error list
     */
    protected String getCommaDelimitedKeys(List<ValidationError> validationErrors) {
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

    protected Map<String, String> convertProperties(String properties, Map<String, String> regExMap) {
        Map<String, String> map = new LinkedHashMap<>();
        if (!StringHelper.isNullOrEmpty(properties)) {
            String[] keyValuePairs = properties.split(PROPERTIES_DELIMETER);
            for (String keyValuePairStr : keyValuePairs) {
                String[] pairParts = keyValuePairStr.split(KEY_VALUE_DELIMETER, 2);
                String key = pairParts[0];
                String value = pairParts[1];
                map.put(key, value);
            }
        }

        if (regExMap != null) {
            for (ValidationError error : validateProperties(regExMap, map)) {
                map.remove(error.getKeyName());
            }
        }

        return map;
    }

    /**
     * Converts device custom properties from string to map.
     *
     * @param properties
     *            specified device properties
     * @return map containing all device custom properties ({@code LinkedHashMap} is used to ensure properties order is
     *         constant)
     * @exception IllegalArgumentException
     *                if specified properties has syntax errors
     */
    public Map<String, String> convertProperties(String properties) {
        return convertProperties(properties, null);
    }

    /**
     * Converts device custom properties from map to string.
     *
     * @param properties
     *            specified device properties
     * @return string containing all properties in map
     * @exception IllegalArgumentException
     *                if specified properties has syntax errors
     */
    public String convertProperties(Map<String, String> properties) {
        StringBuilder sb = new StringBuilder();
        if (properties != null && !properties.isEmpty()) {
            for (Map.Entry<String, String> e : properties.entrySet()) {
                sb.append(e.getKey());
                sb.append(KEY_VALUE_DELIMETER);
                sb.append(Objects.toString(e.getValue(), ""));
                sb.append(PROPERTIES_DELIMETER);
            }
            // remove last PROPERTIES_DELIMETER
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
