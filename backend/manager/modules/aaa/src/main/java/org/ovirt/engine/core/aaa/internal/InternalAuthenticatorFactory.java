package org.ovirt.engine.core.aaa.internal;

import org.ovirt.engine.core.aaa.Authenticator;
import org.ovirt.engine.core.aaa.AuthenticatorFactory;
import org.ovirt.engine.core.extensions.mgr.Configuration;
import org.ovirt.engine.core.extensions.mgr.ConfigurationException;
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
