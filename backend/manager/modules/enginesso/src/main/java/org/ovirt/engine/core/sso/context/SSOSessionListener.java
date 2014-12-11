package org.ovirt.engine.core.sso.context;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.ovirt.engine.core.sso.utils.SSOConstants;
import org.ovirt.engine.core.sso.utils.SSOSession;
import org.ovirt.engine.core.sso.utils.TokenCleanupUtility;

public class SSOSessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        se.getSession().setAttribute(SSOConstants.OVIRT_SSO_SESSION, new SSOSession(se.getSession()));
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        TokenCleanupUtility.cleanupExpiredTokens(se.getSession().getServletContext());
    }
}
