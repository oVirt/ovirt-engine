package org.ovirt.engine.core.bll;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NonTransactiveCommandAttribute {
    /**
     * Flag for determining if a compensation context will be created during command execution, although it will be run
     * without a wrapping transaction
     *
     * @return true if to create compensation context, false if not
     */
    boolean forceCompensation() default false;
}
