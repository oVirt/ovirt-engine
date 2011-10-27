package org.ovirt.engine.core.common.config;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionBehaviourAttribute {
    OptionBehaviour behaviour();

    ConfigValues dependentOn() default ConfigValues.Invalid;

    String realValue() default "";
}
