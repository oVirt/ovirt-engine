package org.ovirt.engine.core.common.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.ovirt.engine.core.common.validation.NetworkIdOrNetworkNameIsSetConstraint;

@Constraint(validatedBy = NetworkIdOrNetworkNameIsSetConstraint.class)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NetworkIdOrNetworkNameIsSet {
    String message() default "VALIDATION_NETWORK_ID_OR_NETWORK_NAME_MUST_BE_SET";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
