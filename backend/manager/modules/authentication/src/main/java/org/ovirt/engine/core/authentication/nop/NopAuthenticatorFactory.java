package org.ovirt.engine.core.authentication.nop;

import org.ovirt.engine.core.authentication.Authenticator;
import org.ovirt.engine.core.authentication.AuthenticatorFactory;
import org.ovirt.engine.core.authentication.Configuration;
import org.ovirt.engine.core.authentication.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NopAuthenticatorFactory implements AuthenticatorFactory {
    private static final Logger log = LoggerFactory.getLogger(NopAuthenticatorFactory.class);

    /**
     * The type supported by this factory.
     */
    private static final String TYPE = "nop";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authenticator create(Configuration config) throws ConfigurationException {
        return new NopAuthenticator();
    }
}
