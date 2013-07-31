package org.ovirt.engine.core.config.entity.helper;

import java.util.List;

import org.ovirt.engine.core.config.EngineConfigCLIParser;
import org.ovirt.engine.core.config.entity.ConfigKey;

public class StringValueHelper extends BaseValueHelper {

    @Override
    public String getValue(String value) {
        return value;
    }

    @Override
    public String setValue(String value) {
        return value;
    }

    @Override
    public ValidationResult validate(ConfigKey key, String value) {
        List<String> validValues = key.getValidValues();

        boolean isValid = false;
        String details = "";
        if (validValues.isEmpty()) {
            isValid = true;
        } else {
            isValid = validValues.contains(value);
            if (!isValid) {
                details = "Valid values are "+key.getValidValues();
            }
        }
        return new ValidationResult(isValid, details);
    }

    @Override
    public void setParser(EngineConfigCLIParser parser) {
        // NOP
    }

    @Override
    public String getHelpNote(ConfigKey key) {
        return getHelpNoteByType(key, "string");
    }

}
