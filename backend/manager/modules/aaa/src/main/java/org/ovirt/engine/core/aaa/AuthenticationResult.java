package org.ovirt.engine.core.aaa;

import java.util.List;

/**
 * This class represents a result returned by an Authenticator
 */
public abstract class AuthenticationResult {

    /**
     * Returns whether the authentication is successful
     * @return
     */
    public abstract boolean isSuccessful();

    /**
     * Resolves the detailed information into engine messages
     * @return
     */
    public abstract List<String> resolveMessage();
}
