package org.ovirt.engine.ui.frontend.server.gwt;

import java.security.SecureRandom;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class XsrfTokenGeneratorHttpSessionListener implements HttpSessionListener {

    /**
     * The number of bytes in the token.
     */
    private static final int TOKEN_SIZE = 32;


    /**
     * The random source.
     */
    private SecureRandom random = new SecureRandom();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        byte[] tokenBytes = new byte[TOKEN_SIZE];
        //nextBytes is thread safe.
        random.nextBytes(tokenBytes);
        event.getSession().setAttribute(OvirtXsrfTokenServiceServlet.XSRF_TOKEN, tokenBytes);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent arg0) {
        //Do nothing, the session is cleaned up.
    }

}
