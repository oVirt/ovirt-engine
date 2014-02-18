package org.ovirt.engine.core.aaa;

import org.ovirt.engine.core.extensions.mgr.Configuration;
import org.ovirt.engine.core.extensions.mgr.ConfigurationException;
import org.ovirt.engine.core.extensions.mgr.Factory;

/**
 * This is just a concrete realization of the generic interface intended to simplify things for developers of
 * authenticator factories.
 */
public abstract class AuthenticatorFactory implements Factory<Authenticator> {

    private static final String PROFILE_NAME_KEY = "ovirt.engine.aaa.authn.profile.name";

    public Authenticator create(Configuration config) throws ConfigurationException {
        return createImpl(config.getString(PROFILE_NAME_KEY), config);
    }

    protected abstract Authenticator createImpl(String profileName, Configuration config) throws ConfigurationException;

}
