package org.ovirt.engine.ui.frontend.server.gwt;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

@WebListener
public class XsrfTokenGeneratorHttpSessionListener implements HttpSessionListener {
    private static final Logger log = Logger.getLogger(XsrfTokenGeneratorHttpSessionListener.class);

    /**
     * The number of bytes in the token.
     */
    private static final int TOKEN_SIZE = 32;


    /**
     * The random source.
     */
    private SecureRandom random;

    public XsrfTokenGeneratorHttpSessionListener() {
        try {
            random = SecureRandom.getInstance("SHA1PRNG"); //$NON-NLS-1$
        } catch (NoSuchAlgorithmException e) {
            log.error("Unable to initialize XSRF token random generator", e); //$NON-NLS-1$
            //Stop the startup.
            throw new RuntimeException(e);
        }
    }

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
