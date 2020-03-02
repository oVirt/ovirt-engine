package org.ovirt.engine.core.bll.scheduling;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnTimerMethodAnnotation {

    String value();

    boolean transactional() default false;

    boolean allowsConcurrent() default true;
}
