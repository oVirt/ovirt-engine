package org.ovirt.engine.core.utils.timer;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OnTimerMethodAnnotation {

    String value();
}
