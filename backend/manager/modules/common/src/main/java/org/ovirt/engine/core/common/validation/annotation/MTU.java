package org.ovirt.engine.core.common.validation.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.ovirt.engine.core.common.validation.MTUValidator;

@Target(FIELD)
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = MTUValidator.class)
public @interface MTU {
    String message() default "MTU_VALUE_INVALID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
