package org.ovirt.engine.core.common.validation.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.ovirt.engine.core.common.validation.HostedEngineUpdateValidator;

@Target({ ANNOTATION_TYPE, TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HostedEngineUpdateValidator.class)
public @interface HostedEngineUpdate {

    String message() default "ACTION_TYPE_FAILED_EDITING_HOSTED_ENGINE_IS_DISABLED";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
