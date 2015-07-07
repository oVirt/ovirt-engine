package org.ovirt.engine.core.aaa;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This class wraps a successfully authenticated HTTP request in order to replace the principal used by default by the
 * server with one containing the name of the user that has been authenticated by the our own authentication mechanism.
 */
public class AuthenticatedRequestWrapper extends HttpServletRequestWrapper {
    private Principal principal;

    public AuthenticatedRequestWrapper(HttpServletRequest req, String name) {
        super(req);
        this.principal = new AuthenticatedPrincipal(name);
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public String getRemoteUser() {
        return principal.getName();
    }
}
