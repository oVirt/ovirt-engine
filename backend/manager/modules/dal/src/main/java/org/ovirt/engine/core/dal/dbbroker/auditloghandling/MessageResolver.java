package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.compat.backendcompat.TypeCompat;

/**
 * The class is responsible to resolve a message which might contain variables into a clear text, where the variables
 * are replaced with the expect value, either it was provided by an attribute of {@link AuditLogable} or by
 * {@link AuditLogable#getCustomValues()}
 */
public class MessageResolver {

    /**
     * A pattern to match for variables within message, i.e. ${<alphanumeric/>...}
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{\\w*\\}");
    static final String UNKNOWN_VARIABLE_VALUE = "<UNKNOWN>";
    private static final String UNKNOWN_REASON_VALUE =
            " No reason was returned for this operation failure. See logs for further details.";
    private static final String REASON_TOKEN = "reason";
    private static final String OPTIONAL_REASON_TOKEN = "optionalreason";

    /**
     * Resolves a message which contains place holders by replacing them with the value from the map.
     *
     * @param message
     *            A text representing a message with place holders
     * @param values
     *            a map of the place holder to its values
     * @return a resolved message
     */
    public static String resolveMessage(String message, Map<String, String> values) {
        Matcher matcher = VARIABLE_PATTERN.matcher(message);

        StringBuffer buffer = new StringBuffer();
        String value;
        String token;
        while (matcher.find()) {
            token = matcher.group();

            // remove leading ${ and trailing }
            token = token.substring(2, token.length() - 1).toLowerCase();

            // get value from value map
            value = values.get(token);
            if (value == null || value.isEmpty()) {
                // replace value with UNKNOWN_VARIABLE_VALUE if value not defined
                switch (token) {
                case REASON_TOKEN:
                    value = UNKNOWN_REASON_VALUE;
                    break;
                case OPTIONAL_REASON_TOKEN:
                    value = "";
                    break;
                default:
                    value = UNKNOWN_VARIABLE_VALUE;
                }
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(value)); // put the value into message
        }

        // append the rest of the message
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    static String resolveMessage(String message, AuditLogable logable) {
        String returnValue = message;
        if (logable != null) {
            Map<String, String> map = getAvailableValues(message, logable);
            returnValue = resolveMessage(message, map);
        }
        return returnValue;
    }

    private static Set<String> resolvePlaceHolders(String message) {
        Set<String> result = new HashSet<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(message);

        String token;
        while (matcher.find()) {
            token = matcher.group();

            // remove leading ${ and trailing }
            token = token.substring(2, token.length() - 1);
            result.add(token.toLowerCase());
        }
        return result;
    }

    private static Map<String, String> getAvailableValues(String message, AuditLogable logable) {
        Map<String, String> returnValue = new HashMap<>(logable.getCustomValues());
        Set<String> attributes = resolvePlaceHolders(message);
        if (attributes != null && attributes.size() > 0) {
            TypeCompat.getPropertyValues(logable, attributes, returnValue);
        }
        return returnValue;
    }
}
