package org.ovirt.engine.core.utils.customprop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ovirt.engine.core.compat.StringHelper;

public class SimpleCustomPropertiesUtil extends CustomPropertiesUtils {

    private static final SimpleCustomPropertiesUtil instance = new SimpleCustomPropertiesUtil();

    public static SimpleCustomPropertiesUtil getInstance() {
        return instance;
    }

    /**
     * validate a map of specific custom properties against provided regex map
     * @param regExMap
     *      <key, regex> map
     * @param properties
     *      <key, value> map, custom properties to validate
     * @return
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

        Set<ValidationError> errorsSet = new HashSet<ValidationError>();
        for (Entry<String, String> e : properties.entrySet()) {
            String key = e.getKey();
            if (key == null || !regExMap.containsKey(key)) {
                errorsSet.add(new ValidationError(ValidationFailureReason.KEY_DOES_NOT_EXIST, key));
                continue;
            }

            if (!StringHelper.defaultString(e.getValue()).matches(regExMap.get(key))) {
                errorsSet.add(new ValidationError(ValidationFailureReason.INCORRECT_VALUE, key));
                continue;
            }
        }
        List<ValidationError> results = new ArrayList<ValidationError>();
        results.addAll(errorsSet);
        return results;
    }
}
