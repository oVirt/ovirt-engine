package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import com.google.gwt.user.client.rpc.RpcToken;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.XsrfToken;
import com.google.gwt.user.server.Util;
import com.google.gwt.user.server.rpc.XsrfTokenServiceServlet;
import com.google.gwt.util.tools.shared.Md5Utils;
import com.google.gwt.util.tools.shared.StringUtils;

public class XsrfProtectedRpcServlet extends AbstractXsrfProtectedRpcServlet {

    // Can't use the one from XsrfTokenServiceServlet as it is not public.
    static final String COOKIE_NAME_NOT_SET_ERROR_MSG =
            "Session cookie name not set! Use context-param to specify session cookie name"; //$NON-NLS-1$

    String sessionCookieName = null;

    /**
     * Default constructor.
     */
    public XsrfProtectedRpcServlet() {
        this(null);
    }

    /**
     * Constructor with session cookie name.
     * @param cookieName The session cookie name.
     */
    public XsrfProtectedRpcServlet(String cookieName) {
        this.sessionCookieName = cookieName;
    }

    /**
     * Constructor with delegate.
     * @param delegate The delegate object
     */
    public XsrfProtectedRpcServlet(Object delegate) {
        this(delegate, null);
    }

    /**
     * Constructor with cookie name and delegate.
     * @param delegate The delegate object
     * @param cookieName The name of the session cookie.
     */
    public XsrfProtectedRpcServlet(Object delegate, String cookieName) {
        super(delegate);
        this.sessionCookieName = cookieName;
    }

    @Override
    public void init() throws ServletException {
        // do not overwrite if value is supplied in constructor
        if (sessionCookieName == null) {
            // servlet configuration precedes context configuration
            sessionCookieName = getServletConfig().getInitParameter(XsrfTokenServiceServlet.COOKIE_NAME_PARAM);
            if (sessionCookieName == null) {
                sessionCookieName = getServletContext().getInitParameter(XsrfTokenServiceServlet.COOKIE_NAME_PARAM);
            }
            if (sessionCookieName == null) {
                throw new IllegalStateException(COOKIE_NAME_NOT_SET_ERROR_MSG);
            }
        }
    }

    @Override
    protected void validateXsrfToken(RpcToken token, Method method) {
        if (token == null) {
            throw new RpcTokenException("XSRF token missing"); //$NON-NLS-1$
        }
        Cookie sessionCookie = Util.getCookie(getThreadLocalRequest(), sessionCookieName, false);
        if (sessionCookie == null || sessionCookie.getValue() == null
                || sessionCookie.getValue().length() == 0) {
            throw new RpcTokenException("Session cookie is missing or empty! " + //$NON-NLS-1$
                    "Unable to verify XSRF cookie"); //$NON-NLS-1$
        }

        String expectedToken;
        try {
            expectedToken = StringUtils.toHexString(
                    Md5Utils.getMd5Digest(sessionCookie.getValue().getBytes("UTF8"))); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new RpcTokenException("Unable to determine XSRF token from cookie"); //$NON-NLS-1$
        }
        XsrfToken xsrfToken = (XsrfToken) token;

        if (!expectedToken.equals(xsrfToken.getToken())) {
            throw new RpcTokenException("Invalid XSRF token"); //$NON-NLS-1$
        }
    }
}
