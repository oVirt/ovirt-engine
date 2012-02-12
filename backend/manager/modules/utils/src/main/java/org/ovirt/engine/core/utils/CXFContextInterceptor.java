package org.ovirt.engine.core.utils;

import javax.annotation.Resource;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpSession;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class CXFContextInterceptor {

    @Resource
    WebServiceContext wsContext;

    private static Log log = LogFactory.getLog(CXFContextInterceptor.class);

    @AroundInvoke
    public Object injectWebContextToThreadLocal(InvocationContext ic) throws Exception {
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
