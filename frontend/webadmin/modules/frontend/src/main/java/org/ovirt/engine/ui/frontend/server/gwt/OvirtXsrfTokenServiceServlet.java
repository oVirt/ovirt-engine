package org.ovirt.engine.ui.frontend.server.gwt;

import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.server.rpc.XsrfTokenServiceServlet;
import com.google.gwt.util.tools.shared.StringUtils;

public class OvirtXsrfTokenServiceServlet extends XsrfTokenServiceServlet {

    /**
     * serial version UID.
     */
    private static final long serialVersionUID = 1854606938563216502L;

    /**
     * The name of the attribute in the {@code HttpSession} that stores the value.
     */
    public static final String XSRF_TOKEN = "XSRF_TOKEN"; //$NON-NLS-1$

    @Override
    public void init() {
        //Do NOT call super.init(), we are fully overriding the token generation method.
    }

    @Override
    public XsrfToken getNewXsrfToken() {
        return new XsrfToken(generateTokenValueResponse());
    }

    /**
     * Generate the token based on a random value.
     * @return A hex based representation of the token value.
     */
    private String generateTokenValueResponse() {
        return StringUtils.toHexString((byte[]) getThreadLocalRequest().getSession().getAttribute(XSRF_TOKEN));
    }
}
