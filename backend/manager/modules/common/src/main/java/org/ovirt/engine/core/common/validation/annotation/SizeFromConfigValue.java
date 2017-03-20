package org.ovirt.engine.core.common.validation.annotation;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.validation.SizeFromConfigValueValidator;

@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SizeFromConfigValueValidator.class)
public @interface SizeFromConfigValue {

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    ConfigValues minConfig() default ConfigValues.Invalid;

    ConfigValues maxConfig() default ConfigValues.Invalid;

    int min() default -1;

    int max() default -1;
}
