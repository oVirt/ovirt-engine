package org.ovirt.engine.core.bll;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It marks commands {@link CommandBase#validate()} method of which is supposed to be executed in the same DB
 * transaction as {@link CommandBase#executeCommand()} method.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateSupportsTransaction {
}
