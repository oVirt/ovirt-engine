package org.ovirt.engine.core.bll.interceptors;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.ovirt.engine.core.utils.CorrelationIdTracker;

public class CorrelationIdTrackerInterceptor {

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ic) throws Exception {
        try {
            return ic.proceed();
        } finally {
            CorrelationIdTracker.clean();
        }
    }
}
