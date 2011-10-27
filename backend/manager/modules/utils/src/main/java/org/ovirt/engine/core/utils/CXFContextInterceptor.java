package org.ovirt.engine.core.utils;

import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.jboss.injection.WebServiceContextProxy;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

public class CXFContextInterceptor {

    // should take the context as a resource but doesn't work
    // @Resource WebServiceContext wsContext;

    private static LogCompat log = LogFactoryCompat.getLog(CXFContextInterceptor.class);

    @AroundInvoke
    public Object injectWebContextToThreadLocal(InvocationContext ic) throws Exception {
        WebServiceContext wsContext = new WebServiceContextProxy();
        MessageContext mc = wsContext.getMessageContext();
        HttpSession session = ((javax.servlet.http.HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST))
                .getSession();
        session.setMaxInactiveInterval(Config.<Integer> GetValue(ConfigValues.UserSessionTimeOutInterval) * 60);
        if (log.isDebugEnabled()) {
            log.debug("session id=" + session.getId());
        }
        ThreadLocalParamsContainer.setHttpSessionId(session.getId());
        return ic.proceed();
    }
}
