package org.ovirt.engine.core.utils.customprop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
public abstract class CustomPropertiesUtils {
    protected final Pattern SEMICOLON_PATTERN = Pattern.compile(";");
    protected final String PROPERTIES_DELIMETER = ";";
    protected final String KEY_VALUE_DELIMETER = "=";

    protected final String LEGITIMATE_CHARACTER_FOR_KEY = "[a-z_A-Z0-9]";
    protected final String LEGITIMATE_CHARACTER_FOR_VALUE = "[^" + PROPERTIES_DELIMETER + "]"; // all characters
                                                                                               // but the delimiter
                                                                                               // are allowed
    protected final Pattern VALUE_PATTERN = Pattern.compile("(" + LEGITIMATE_CHARACTER_FOR_VALUE + ")+");

    // properties are in form of key1=val1;key2=val2; .... key and include alpha numeric characters and _
    protected final String KEY_VALUE_REGEX_STR = "((" + LEGITIMATE_CHARACTER_FOR_KEY + ")+)=(("
            + LEGITIMATE_CHARACTER_FOR_VALUE + ")+)";

    // frontend can pass custom values in the form of "key=value" or "key1=value1;... key-n=value_n" (if there is only
    // one key-value, no ; is attached to it
    protected final String VALIDATION_STR = KEY_VALUE_REGEX_STR + "(;" + KEY_VALUE_REGEX_STR + ")*;?";
    protected final Pattern VALIDATION_PATTERN = Pattern.compile(VALIDATION_STR);

    protected final List<ValidationError> invalidSyntaxValidationError =
            Arrays.asList(new ValidationError(ValidationFailureReason.SYNTAX_ERROR, ""));

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

    public Set<Version> getSupportedClusterLevels() {
        Set<Version> versions = Config.<java.util.HashSet<Version>> GetValue(ConfigValues.SupportedClusterLevels);
        return versions;
    }

    public boolean syntaxErrorInProperties(String properties) {
        return !VALIDATION_PATTERN.matcher(properties).matches();
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
                new HashMap<CustomPropertiesUtils.ValidationFailureReason, List<ValidationError>>();
        separateValidationErrorsList(errorsList, resultMap);
        for (Map.Entry<ValidationFailureReason, String> entry : failureReasonsToVdcBllMessagesMap.entrySet()) {
            List<ValidationError> errorsListForReason = resultMap.get(entry.getKey());
            String vdcBllMessage = entry.getValue();
            String formatString = failureReasonsToParameterFormatStrings.get(entry.getKey());
            addMessage(result, vdcBllMessage, formatString, errorsListForReason);
        }
        return result;
    }

    protected static void addMessage(List<String> resultMessages,
            String vdcBllMessage,
            String formatString, List<ValidationError> errorsList) {
        if (errorsList != null && !errorsList.isEmpty()) {
            String keys = getCommaDelimitedKeys(errorsList);
            resultMessages.add(vdcBllMessage);
            resultMessages.add(String.format(formatString, keys));
        }
    }

    protected static String getCommaDelimitedKeys(List<ValidationError> validationErrors) {
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

    protected void parsePropertiesRegex(String properties, Map<String, Pattern> keysToRegex) {
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

}
