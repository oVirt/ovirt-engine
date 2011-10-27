package org.ovirt.engine.core.utils;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

public class ThreadLocalSessionCleanerInterceptor {

    @AroundInvoke
    public Object injectWebContextToThreadLocal(InvocationContext ic) throws Exception {
        try {
            return ic.proceed();
        } finally {
            ThreadLocalParamsContainer.clean();
        }
    }
}
