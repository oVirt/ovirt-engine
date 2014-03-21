package org.ovirt.engine.ui.frontend.server.gwt;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.ui.frontend.communication.XsrfRpcRequestBuilder;
import com.google.gwt.user.client.rpc.RpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.server.Util;
import com.google.gwt.user.server.rpc.NoXsrfProtect;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.XsrfProtect;

public abstract class AbstractXsrfProtectedRpcServlet extends RpcRemoteOracleServlet {

    /**
     * Serial version UID for serialization.
     */
    static final long serialVersionUID = -7274292100456700624L;

    /**
     * The default constructor used by service implementations that extend this class. The servlet will delegate AJAX
     * requests to the appropriate method in the subclass.
     */
    public AbstractXsrfProtectedRpcServlet() {
        super();
    }

    /**
     * The wrapping constructor used by service implementations that are separate from this class. The servlet will
     * delegate AJAX requests to the appropriate method in the given object.
     *
     * @param delegate
     *            The delegate object.
     */
    public AbstractXsrfProtectedRpcServlet(Object delegate) {
        super(delegate);
    }

    private XsrfToken extractTokenFromRequest() {
        List<String> header =
                Collections.list(getThreadLocalRequest().getHeaders(XsrfRpcRequestBuilder.XSRF_TOKEN_HEADER));
        XsrfToken result = null;
        if (header != null && header.size() == 1) {
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

    /**
     * Override this method to perform XSRF token verification.
     *
     * @param token
     *            {@link RpcToken} included with an RPC request.
     * @param method
     *            method being invoked via this RPC call.
     */
    protected abstract void validateXsrfToken(RpcToken token, Method method);
}
