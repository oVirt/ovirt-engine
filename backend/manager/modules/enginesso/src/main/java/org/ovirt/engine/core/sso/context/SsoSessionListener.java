package org.ovirt.engine.core.sso.context;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.ovirt.engine.core.sso.utils.SsoConstants;
import org.ovirt.engine.core.sso.utils.SsoSession;
import org.ovirt.engine.core.sso.utils.TokenCleanupUtility;

public class SsoSessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        se.getSession().setAttribute(SsoConstants.OVIRT_SSO_SESSION, new SsoSession(se.getSession()));
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        TokenCleanupUtility.cleanupExpiredTokens(se.getSession().getServletContext());
    }
}
