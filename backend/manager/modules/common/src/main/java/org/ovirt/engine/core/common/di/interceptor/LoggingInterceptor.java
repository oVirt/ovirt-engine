package org.ovirt.engine.core.common.di.interceptor;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

public abstract class LoggingInterceptor {

    @AroundInvoke
    Object apply(InvocationContext ctx) throws Exception {
        long start = System.currentTimeMillis();
        try {
            return ctx.proceed();
        } finally {
            log(
                "method: {}, params: {}, timeElapsed: {}ms",
                ctx.getMethod().getName(),
                ctx.getParameters(),
                System.currentTimeMillis() - start);
        }
    }

    abstract void log(String msg, Object... args);
}
