package org.ovirt.engine.core.common.validation;

import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.annotation.NumberOfMonitors;

public class NumberOfMonitorsConstraint implements ConstraintValidator<NumberOfMonitors, Integer> {

    @Override
    public void initialize(NumberOfMonitors constraintAnnotation) {
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        // Is fetched every time to support the reloadability of the config value
        final List<String> validValues = Config.getValue(ConfigValues.ValidNumOfMonitors);
        return validValues.contains(value.toString());
    }

}
