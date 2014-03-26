package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ovirt.engine.ui.frontend.communication.XsrfRpcRequestBuilder;

import com.google.gwt.rpc.server.RpcServlet;
import com.google.gwt.user.client.rpc.RpcToken;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.server.Util;
import com.google.gwt.user.server.rpc.NoXsrfProtect;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.XsrfProtect;

public abstract class AbstractXsrfProtectedRpcServlet extends RpcServlet {

    /**
     * Serial version UID for serialization.
     */
    static final long serialVersionUID = -7274292100456700624L;

    private RpcToken token;

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

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        extractTokenFromRequest(req);
        super.service(req, resp);
    }

    @SuppressWarnings("unchecked")
    private void extractTokenFromRequest(HttpServletRequest req) {
        List<String> header = Collections.list(req.getHeaders(XsrfRpcRequestBuilder.XSRF_TOKEN_HEADER));
        if (header != null && header.size() == 1) {
            this.token = new XsrfToken(header.get(0));
        }
    }

    @Override
    protected void onAfterRequestDeserialized(RPCRequest rpcRequest) {
        if (shouldValidateXsrfToken(rpcRequest.getMethod())) {
            validateXsrfToken(this.token, rpcRequest.getMethod());
        }
        this.token = null;
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
