package org.ovirt.engine.core.utils.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import org.ovirt.engine.core.compat.TransactionScopeOption;

/**
 * Use this annotation on a class to mark that all of its methods require running in a transactional scope. Use it on a
 * method to make sure that only this specific method runs in a transactional scope. Default transaction scope if
 * Required
 */
@Inherited
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@InterceptorBinding
public @interface Transactional {
    @Nonbinding TransactionScopeOption propogation() default TransactionScopeOption.Required;

    @Nonbinding
    boolean readOnly() default false;
}
