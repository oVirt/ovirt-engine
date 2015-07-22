package org.ovirt.engine.core.common.validation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.ovirt.engine.core.common.validation.NicLabelNicIdOrNicNameIsSetConstraint;

@Constraint(validatedBy = NicLabelNicIdOrNicNameIsSetConstraint.class)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NicLabelNicIdOrNicNameIsSet {
    String message() default "NIC_LABEL_VALIDATION_NIC_ID_OR_NIC_NAME_MUST_BE_SET";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
