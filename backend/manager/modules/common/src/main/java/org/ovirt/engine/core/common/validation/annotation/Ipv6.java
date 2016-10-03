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
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ValidationUtils;

@Target({ ANNOTATION_TYPE, FIELD, METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { })
@Pattern(regexp = ValidationUtils.IPV6_PATTERN)
@Size(max = 45)
public @interface Ipv6 {
    String message() default "BAD_IPV6_ADDRESS_FORMAT";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
