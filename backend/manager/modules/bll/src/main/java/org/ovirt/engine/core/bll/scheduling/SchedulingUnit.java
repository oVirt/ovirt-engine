package org.ovirt.engine.core.bll.scheduling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.ovirt.engine.core.common.scheduling.PolicyUnitType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SchedulingUnit {
    String guid();
    String name();
    PolicyUnitType type() default PolicyUnitType.FILTER;
    String description() default "";
    PolicyUnitParameter[] parameters() default {};
}
