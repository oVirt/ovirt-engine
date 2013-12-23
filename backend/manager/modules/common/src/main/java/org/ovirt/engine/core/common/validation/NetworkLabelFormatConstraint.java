package org.ovirt.engine.core.common.validation;

import java.util.Set;
import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidNetworkLabelFormat;

public class NetworkLabelFormatConstraint implements ConstraintValidator<ValidNetworkLabelFormat, Set<String>> {

    @Override
    public boolean isValid(Set<String> labels, ConstraintValidatorContext context) {
        if (labels == null) {
            return true;
        }

        for (String label : labels) {
            if (!Pattern.matches(ValidationUtils.NO_SPECIAL_CHARACTERS, label)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void initialize(ValidNetworkLabelFormat constraintAnnotation) {
        // Unimplemented method, required for interface ConstraintValidator
    }
}
