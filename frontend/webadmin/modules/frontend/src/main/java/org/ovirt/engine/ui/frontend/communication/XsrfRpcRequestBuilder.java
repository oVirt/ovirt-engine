package org.ovirt.engine.ui.frontend.communication;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.XsrfToken;

public class XsrfRpcRequestBuilder extends RpcRequestBuilder {
    public static final String XSRF_TOKEN_HEADER = "OVIRT-XSRF-Token"; //$NON-NLS-1$
    public static final String XSRF_PATH = "xsrf"; //$NON-NLS-1$
    private XsrfToken xsrfToken;

    @Override
    protected void doFinish(RequestBuilder rb) {
        super.doFinish(rb);
        if (xsrfToken != null) {
            rb.setHeader(XSRF_TOKEN_HEADER, xsrfToken.getToken());
        }
    }

    public XsrfToken getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(XsrfToken xsrfToken) {
        this.xsrfToken = xsrfToken;
    }
}
