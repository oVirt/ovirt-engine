package org.ovirt.engine.core.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.HostnameOrIp;

public class HostnameOrIPConstraint implements ConstraintValidator<HostnameOrIp, String> {

    @Override
    public void initialize(HostnameOrIp constraintAnnotation) {
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return name == null || ValidationUtils.validHostname(name);
    }

}
