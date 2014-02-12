package org.ovirt.engine.core.aaa;

import java.security.Principal;

/**
 * This class is a simple implementation of the {@link java.security.Principal} interface used by the authenticated
 * request wrapper to give access to the user name to web applications.
 */
public class AuthenticatedPrincipal implements Principal {
    private String name;

    public AuthenticatedPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
