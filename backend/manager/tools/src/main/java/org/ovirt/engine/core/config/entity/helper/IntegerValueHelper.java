package org.ovirt.engine.core.config.entity.helper;

import java.util.List;

import org.ovirt.engine.core.config.EngineConfigCLIParser;
import org.ovirt.engine.core.config.entity.ConfigKey;

public class IntegerValueHelper extends BaseValueHelper {

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
        try {
            int intValue = Integer.parseInt(value);

            List<String> validValues = key.getValidValues();
            boolean isValid = false;
            if (validValues.isEmpty()) {
                isValid = true;
            } else {
                for (String validValue : validValues) {
                    boolean isRange = validValue.matches("\\d+\\.\\.\\d+");
                    if (isRange) {
                        int bottom = Integer.parseInt(validValue.substring(0, validValue.indexOf(".")));
                        int top =
                            Integer.parseInt(validValue.substring(validValue.lastIndexOf(".") + 1,
                                                                  validValue.length()));
                        if (intValue >= bottom && intValue <= top) {
                            isValid = true;
                            break;
                        }
                    } else {
                        if (intValue == Integer.parseInt(validValue)) {
                            isValid = true;
                            break;
                        }
                    }
                }
            }
            return new ValidationResult(isValid);
        } catch (NumberFormatException e) {
            // invalid number
            return new ValidationResult(false);
        }
    }

    @Override
    public void setParser(EngineConfigCLIParser parser) {
        // NOP
    }

    @Override
    public String getHelpNote(ConfigKey key) {
        return getHelpNoteByType(key, "numeric");
    }

}
