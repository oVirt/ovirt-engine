package org.ovirt.engine.core.common.validation.annotation;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.ovirt.engine.core.common.validation.SerialNumberPolicyValidator;

@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SerialNumberPolicyValidator.class)
public @interface ValidSerialNumberPolicy {

    String message() default "ACTION_TYPE_FAILED_INVALID_SERIAL_NUMBER";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
