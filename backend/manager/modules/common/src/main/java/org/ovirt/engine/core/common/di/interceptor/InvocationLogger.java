package org.ovirt.engine.core.common.di.interceptor;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

@InterceptorBinding
@Target({METHOD, TYPE})
@Retention(RUNTIME)
public @interface InvocationLogger {
    Level value() default Level.DEBUG;

    enum Level {
        DEBUG,
        INFO,
    }
}
