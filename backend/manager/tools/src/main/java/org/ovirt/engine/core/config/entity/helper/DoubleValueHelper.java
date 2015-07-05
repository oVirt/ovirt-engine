package org.ovirt.engine.core.config.entity.helper;

import org.ovirt.engine.core.config.EngineConfigCLIParser;
import org.ovirt.engine.core.config.entity.ConfigKey;

public class DoubleValueHelper extends BaseValueHelper {
    @Override
    public String getValue(String value) {
        return value;
    }

    @Override
    public String setValue(String value) {
        return value;
    }


    @Override public ValidationResult validate(ConfigKey key, String value) {
       try {
           Double doubleValue = Double.parseDouble(value);
           return  new ValidationResult(true);
       } catch (NumberFormatException e) {
           // invalid number
           return new ValidationResult(false);
       }
    }

    @Override public void setParser(EngineConfigCLIParser parser) {
        // NOP
    }

    @Override public String getHelpNote(ConfigKey key) {
        return getHelpNoteByType(key, "numeric");
    }
}
