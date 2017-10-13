package org.ovirt.engine.core.config.entity.helper;

import org.ovirt.engine.core.config.EngineConfigCLIParser;
import org.ovirt.engine.core.config.entity.ConfigKey;

public class IntegerMultipleValueHelper extends BaseValueHelper {
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
        if (value.endsWith(",")) {
            return new ValidationResult(false);
        }

        String[] strings = value.split(",");

        for (String s : strings) {
            try {
                Integer.parseInt(s);
            } catch (NumberFormatException e) {
                // invalid number
                return new ValidationResult(false);
            }
        }

        return new ValidationResult(true);
    }

    @Override
    public void setParser(EngineConfigCLIParser parser) {
        // NOP
    }

    @Override
    public String getHelpNote(ConfigKey key) {
        return getHelpNoteByType(key, "list of integers");
    }
}
