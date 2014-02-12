package org.ovirt.engine.core.aaa;

/**
 * This class contains the result of an authentication negotiation, consisting of flag indicating if the authentication
 * succeeded and the name of the authenticated user.
 */
public class NegotiationResult {
    /**
     * Flag indicating if the authentication process has succeeded.
     */
    private boolean authenticated;

    /**
     * The name of the authenticated user.
     */
    private String name;

    public NegotiationResult(boolean authenticated, String name) {
        this.authenticated = authenticated;
        this.name = name;
    }

    /**
     * Returns {@code true} iff the authentication process has succeeded.
     */
    public boolean isAuthenticated() {
        return authenticated;
    }

    /**
     * Returns the name of the authenticated user.
     *
     * @return the name of the authenticated entity or {@code null} if the authentication didn't succeed
     */
    public String getName() {
        return name;
    }
}
