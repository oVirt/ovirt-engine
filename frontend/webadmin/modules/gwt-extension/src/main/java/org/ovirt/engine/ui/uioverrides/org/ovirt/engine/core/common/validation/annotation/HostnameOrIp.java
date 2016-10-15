package org.ovirt.engine.core.common.validation.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ANNOTATION_TYPE, METHOD, FIELD, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface HostnameOrIp {
    String message() default "VALIDATION_FIELD_NOT_MATCHING_HOSTNAME_OR_IP_PATTERN";

    Class<?>[] groups() default {};
}
