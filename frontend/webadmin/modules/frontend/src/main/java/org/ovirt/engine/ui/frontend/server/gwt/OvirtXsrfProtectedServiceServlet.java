package org.ovirt.engine.ui.frontend.server.gwt;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.ovirt.engine.ui.frontend.communication.XsrfRpcRequestBuilder;

import com.google.gwt.user.client.rpc.RpcToken;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.server.Util;
import com.google.gwt.user.server.rpc.NoXsrfProtect;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.XsrfProtect;
import com.google.gwt.user.server.rpc.XsrfProtectedServiceServlet;
import com.google.gwt.util.tools.shared.StringUtils;

public class OvirtXsrfProtectedServiceServlet extends XsrfProtectedServiceServlet {

    private static final long serialVersionUID = 1802731419400198238L;

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

    private XsrfToken extractTokenFromRequest() {
        List<String> header =
                Collections.list(getThreadLocalRequest().getHeaders(XsrfRpcRequestBuilder.XSRF_TOKEN_HEADER));
        XsrfToken result = null;
        if (header.size() == 1) {
            result = new XsrfToken(header.get(0));
        }
        return result;
    }

    @Override
    protected void onAfterRequestDeserialized(RPCRequest rpcRequest) {
        if (shouldValidateXsrfToken(rpcRequest.getMethod())) {
            validateXsrfToken(extractTokenFromRequest(), rpcRequest.getMethod());
        }
    }

    /**
     * Override this method to change default XSRF enforcement logic.
     *
     * @param method
     *            Method being invoked
     * @return {@code true} if XSRF token should be verified, {@code false} otherwise
     */
    protected boolean shouldValidateXsrfToken(Method method) {
        return Util.isMethodXsrfProtected(method, XsrfProtect.class,
                NoXsrfProtect.class, RpcToken.class);
    }

}
