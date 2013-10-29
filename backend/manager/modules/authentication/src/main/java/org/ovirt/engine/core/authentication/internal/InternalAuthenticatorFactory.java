package org.ovirt.engine.core.authentication.internal;

import org.ovirt.engine.core.authentication.Authenticator;
import org.ovirt.engine.core.authentication.AuthenticatorFactory;
import org.ovirt.engine.core.authentication.Configuration;
import org.ovirt.engine.core.authentication.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalAuthenticatorFactory implements AuthenticatorFactory {
    private static final Logger log = LoggerFactory.getLogger(InternalAuthenticatorFactory.class);

    /**
     * The type supported by this factory.
     */
    private static final String TYPE = "internal";

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
        return new InternalAuthenticator();
    }
}
