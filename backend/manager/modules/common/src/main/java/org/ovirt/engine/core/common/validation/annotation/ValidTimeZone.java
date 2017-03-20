package org.ovirt.engine.core.common.validation.annotation;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.ovirt.engine.core.common.validation.TimeZoneValidator;

@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TimeZoneValidator.class)
public @interface ValidTimeZone {

    String message() default "ACTION_TYPE_FAILED_INVALID_TIMEZONE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
