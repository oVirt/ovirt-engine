package org.ovirt.engine.core.config.entity.helper;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.ovirt.engine.core.config.entity.ConfigKey;

public class CustomPropertiesValueHelper extends StringValueHelper {

    @Override
    public ValidationResult validate(ConfigKey key, String value) {
        String[] keyValuePairs = value.split(";");
        for (int counter = 0; counter < keyValuePairs.length; counter++) {
           String keyValuePair = keyValuePairs[counter];
           String[] parts = keyValuePair.split("=", 2);
           if (parts.length != 2) {
               return new ValidationResult(false, "The entered value is in imporper format. " + keyValuePair + " cannot be used for custom properties definition.\nA string of key=value pair should be used instead, where the value should be a correct regex expression");
           }
           try {
               Pattern.compile(parts[1]);
           } catch (PatternSyntaxException ex) {
               return new ValidationResult(false, "The entered value is in imporper format. " + parts[1] + " must be a valid regex expression");
           }

        }
        return new ValidationResult(true);
    }
}
