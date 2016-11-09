package org.ovirt.engine.core.common.validation.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.utils.ValidationUtils;

@Target({ ANNOTATION_TYPE, FIELD, METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { })
@Pattern(regexp = ValidationUtils.IPV4_OR_IPV6_PATTERN, message = "BAD_IPV4_OR_IPV6_ADDRESS_FORMAT")

public @interface Ipv4OrIpv6 {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
