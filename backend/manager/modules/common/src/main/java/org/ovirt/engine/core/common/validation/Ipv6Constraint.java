package org.ovirt.engine.core.common.validation;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.Ipv6;

public class Ipv6Constraint implements ConstraintValidator<Ipv6, String> {

    private static final Pattern ipv6Pattern = Pattern.compile(ValidationUtils.IPV6_PATTERN);

    @Override
    public void initialize(Ipv6 constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || ipv6Pattern.matcher(value).matches();
    }

}
