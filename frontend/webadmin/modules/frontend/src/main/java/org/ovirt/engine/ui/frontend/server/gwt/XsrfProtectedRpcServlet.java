package org.ovirt.engine.ui.frontend.server.gwt;

import java.lang.reflect.Method;

import javax.servlet.http.HttpSession;

import com.google.gwt.user.client.rpc.RpcToken;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.util.tools.shared.StringUtils;

public class XsrfProtectedRpcServlet extends AbstractXsrfProtectedRpcServlet {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 5287411961020889823L;

    @Override
    protected void validateXsrfToken(RpcToken token, Method method) {
        if (token == null) {
            throw new RpcTokenException("XSRF token missing"); //$NON-NLS-1$
        }
        String expectedToken;
        HttpSession session = getThreadLocalRequest().getSession();
        expectedToken = StringUtils.toHexString(
                (byte[]) session.getAttribute(OvirtXsrfTokenServiceServlet.XSRF_TOKEN));
        XsrfToken xsrfToken = (XsrfToken) token;

        if (!expectedToken.equals(xsrfToken.getToken())) {
            throw new RpcTokenException("Invalid XSRF token"); //$NON-NLS-1$
        }
    }
}
