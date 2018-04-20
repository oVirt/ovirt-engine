package org.ovirt.engine.core.config.entity.helper;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.config.entity.ConfigKey;
import org.ovirt.engine.core.utils.pm.FenceConfigHelper;

public class FenceConfigValueHelper extends StringValueHelper{
    @Override
    public ValidationResult validate(ConfigKey configKey, String value) {
        String key = configKey.getKey();
        return validate(key, value);
    }

    public ValidationResult validate(String key, String value) {
        if (StringUtils.isBlank(value)) {
            return new ValidationResult(false, "This key value cannot be empty.");
        }
        String validator = FenceConfigHelper.getValidator(key);
        if (StringUtils.isNotEmpty(validator)) {
            if (! value.matches(validator)) {
                return new ValidationResult(false,
                        String.format("The entered key value is invalid. Value should match expression '%s'. Example: %s", validator, FenceConfigHelper.getValidatorExample(key)));
            } else {
                return new ValidationResult(true);
            }
        }
        return new ValidationResult(false,
                String.format("The entered key %s is invalid.", key));
    }
}
