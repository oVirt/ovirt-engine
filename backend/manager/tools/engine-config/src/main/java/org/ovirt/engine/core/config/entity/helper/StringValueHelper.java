package org.ovirt.engine.core.config.entity.helper;

import java.util.List;

import org.ovirt.engine.core.config.EngineConfigCLIParser;
import org.ovirt.engine.core.config.entity.ConfigKey;

public class StringValueHelper implements ValueHelper {

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
        if (validValues.isEmpty()) {
            isValid = true;
        } else {
            isValid = validValues.contains(value);
        }
        return new ValidationResult(isValid);
    }

    @Override
    public void setParser(EngineConfigCLIParser parser) {
        // NOP
    }
}
