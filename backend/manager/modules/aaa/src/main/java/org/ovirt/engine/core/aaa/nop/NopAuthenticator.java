package org.ovirt.engine.core.aaa.nop;

import org.ovirt.engine.core.aaa.AuthenticationResult;
import org.ovirt.engine.core.aaa.PasswordAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This authenticator blindly accepts any user, without any check, useful only for testing environments.
 */
public class NopAuthenticator extends PasswordAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(NopAuthenticator.class);

    public NopAuthenticator() {
    }

    @Override
    public AuthenticationResult authenticate(String name, String password) {
        return new NopAuthenticationResult();
    }

    @Override
    public void init() {
    }

}
