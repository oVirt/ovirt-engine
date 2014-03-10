package org.ovirt.engine.core.aaa.nop;

import org.ovirt.engine.core.aaa.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This authenticator blindly accepts any user, without any check, useful only for testing environments.
 */
public class NopAuthenticator extends Authenticator {
    private static final Logger log = LoggerFactory.getLogger(NopAuthenticator.class);

    public NopAuthenticator() {
    }

    @Override
    public void authenticate(String name, String password) {
    }

    @Override
    public void init() {
    }
}
