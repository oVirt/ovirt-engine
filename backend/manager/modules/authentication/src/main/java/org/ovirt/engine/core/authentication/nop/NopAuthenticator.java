package org.ovirt.engine.core.authentication.nop;

import org.ovirt.engine.core.authentication.AuthenticationResult;
import org.ovirt.engine.core.authentication.PasswordAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This authenticator blindly accepts any user, without any check, useful only for testing environments.
 */
public class NopAuthenticator implements PasswordAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(NopAuthenticator.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationResult<?> authenticate(String name, String password) {
        return new NopAuthenticationResult();
    }
}
