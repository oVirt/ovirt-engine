package org.ovirt.engine.core.common.validation.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.ovirt.engine.core.common.validation.NetworkInterfaceValidator;

@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = NetworkInterfaceValidator.class)
@Documented
public @interface ValidNetworkConfiguration {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
