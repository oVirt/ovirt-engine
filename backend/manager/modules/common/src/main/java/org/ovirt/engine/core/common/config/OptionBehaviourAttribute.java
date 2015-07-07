package org.ovirt.engine.core.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionBehaviourAttribute {
    OptionBehaviour behaviour();

    ConfigValues dependentOn() default ConfigValues.Invalid;

    String realValue() default "";
}
