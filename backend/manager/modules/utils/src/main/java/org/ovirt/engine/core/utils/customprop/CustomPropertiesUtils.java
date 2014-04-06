package org.ovirt.engine.core.utils.customprop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;

/**
 * Abstract class to ease custom properties handling
 *
 */
public class CustomPropertiesUtils {
    /**
     * Delimiter of each property definition
     */
    protected final static String PROPERTIES_DELIMETER = ";";

    /**
     * Delimiter of property name and value
     */
    protected final static String KEY_VALUE_DELIMETER = "=";

    /**
     * Pattern to separate property definition
     */
    protected final Pattern semicolonPattern;

    /**
     * Regex describing legitimate characters for property name - alphanumeric characters and underscore
     */
    protected final static String LEGITIMATE_CHARACTER_FOR_KEY = "[a-z_A-Z0-9]";

    /**
     * Pattern to validate property name
     */
    protected final Pattern keyPattern;

    /**
     * Regex describing legitimate characters for property value - all except {@code PROPERTIES_DELIMITER}
     */
    protected final static String LEGITIMATE_CHARACTER_FOR_VALUE = "[^" + PROPERTIES_DELIMETER + "]";

    /**
     * Pattern to validate property value
     */
    protected final Pattern valuePattern;

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
     * Pattern to validation properties definition
     */
    protected final Pattern validationPattern;

    /**
     * List defining syntax error during properties validation
     */
    protected final List<ValidationError> invalidSyntaxValidationError;

    /**
     * Constructor has package access to enable testing, but class cannot be instantiated outside package
     */
    CustomPropertiesUtils() {
        semicolonPattern = Pattern.compile(PROPERTIES_DELIMETER);
        keyPattern = Pattern.compile("(" + LEGITIMATE_CHARACTER_FOR_KEY + ")+");
        valuePattern = Pattern.compile("(" + LEGITIMATE_CHARACTER_FOR_VALUE + ")*");
        validationPattern = Pattern.compile(VALIDATION_STR);
        invalidSyntaxValidationError = Arrays.asList(new ValidationError(ValidationFailureReason.SYNTAX_ERROR, ""));
    }

    /**
     * Returns supported cluster levels. Method should be used only for testing.
     *
     * @return supported cluster levels
     */
    public Set<Version> getSupportedClusterLevels() {
        Set<Version> versions = Config.<HashSet<Version>> getValue(ConfigValues.SupportedClusterLevels);
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
        return !validationPattern.matcher(properties).matches();
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
                if (key == null || !keyPattern.matcher(key).matches()) {
                    // syntax error in property name
                    error = true;
                    break;
                }

                if (!valuePattern.matcher(StringUtils.defaultString(e.getValue())).matches()) {
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
    protected void parsePropertiesRegex(String properties, Map<String, Pattern> keysToRegex) {
        if (StringUtils.isEmpty(properties)) {
            return;
        }

        String[] propertiesStrs = semicolonPattern.split(properties);

        // Property is in the form of key=regex
        for (String property : propertiesStrs) {

            Pattern pattern = null;
            String[] propertyParts = property.split(KEY_VALUE_DELIMETER, 2);
            if (propertyParts.length == 1) {
                // there is no value(regex) for the property - we assume in that case that any value is allowed except
                // for the properties delimiter
                pattern = valuePattern;
            } else {
                pattern = Pattern.compile(propertyParts[1]);
            }

            keysToRegex.put(propertyParts[0], pattern);
        }
    }

    /**
     * Splits the validation errors list to lists of missing keys and wrong key values
     *
     * @param errorsList
     * @param missingKeysList
     * @param wrongKeyValues
     */
    protected void separateValidationErrorsList(List<ValidationError> errorsList,
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
                new HashMap<ValidationFailureReason, List<ValidationError>>();
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

    /**
     * Converts device custom properties from string to map.
     *
     * @param properties
     *            specified device properties
     * @exception IllegalArgumentException
     *                if specified properties has syntax errors
     * @return map containing all device custom properties ({@code LinkedHashMap} is used to ensure properties order is
     *         constant)
     */
    public Map<String, String> convertProperties(String properties) {
        if (syntaxErrorInProperties(properties)) {
            throw new IllegalArgumentException("Invalid properties syntax!");
        }

        Map<String, String> map = new LinkedHashMap<String, String>();
        if (StringUtils.isNotEmpty(properties)) {
            String keyValuePairs[] = semicolonPattern.split(properties);
            for (String keyValuePairStr : keyValuePairs) {
                String[] pairParts = keyValuePairStr.split(KEY_VALUE_DELIMETER, 2);
                String key = pairParts[0];
                // property value may be null
                String value = pairParts[1];
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * Converts device custom properties from map to string.
     *
     * @param properties
     *            specified device properties
     * @exception IllegalArgumentException
     *                if specified properties has syntax errors
     * @return string containing all properties in map
     */
    public String convertProperties(Map<String, String> properties) {
        if (syntaxErrorInProperties(properties)) {
            throw new IllegalArgumentException("Invalid properties syntax!");
        }

        StringBuilder sb = new StringBuilder();
        if (properties != null && !properties.isEmpty()) {
            for (Map.Entry<String, String> e : properties.entrySet()) {
                sb.append(e.getKey());
                sb.append(KEY_VALUE_DELIMETER);
                sb.append(StringUtils.defaultString(e.getValue()));
                sb.append(PROPERTIES_DELIMETER);
            }
            // remove last PROPERTIES_DELIMETER
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
}
