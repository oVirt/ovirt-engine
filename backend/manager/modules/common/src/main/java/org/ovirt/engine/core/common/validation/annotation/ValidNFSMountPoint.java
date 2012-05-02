package org.ovirt.engine.core.common.validation.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.ovirt.engine.core.common.validation.NfsMountPointConstraint;

@Target({ ANNOTATION_TYPE, METHOD, FIELD, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NfsMountPointConstraint.class)
public @interface ValidNFSMountPoint {
    String message() default "VALIDATION_FIELD_IS_NOT_VALID_LINUX_MOUNT_POINT";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
