package org.ovirt.engine.core.common.validation.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.ovirt.engine.core.common.validation.MaskConstraint;

@Target(FIELD)
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = MaskConstraint.class)
public @interface Mask {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
