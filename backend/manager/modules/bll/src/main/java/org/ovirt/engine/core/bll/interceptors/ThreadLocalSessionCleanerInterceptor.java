package org.ovirt.engine.core.bll.interceptors;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;

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
