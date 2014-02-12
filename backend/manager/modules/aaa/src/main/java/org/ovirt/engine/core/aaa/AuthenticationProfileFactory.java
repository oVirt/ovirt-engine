package org.ovirt.engine.core.aaa;

import org.ovirt.engine.core.extensions.mgr.Configuration;
import org.ovirt.engine.core.extensions.mgr.ConfigurationException;
import org.ovirt.engine.core.extensions.mgr.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An authentication profile is just a pair composed of an authenticator and a directory, so this class just looks up
 * those two objects when the profile is created.
 */
public class AuthenticationProfileFactory implements Factory<AuthenticationProfile> {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationProfileFactory.class);

    // Names of the parameters:
    private static final String AUTHENTICATOR_PARAMETER = "authenticator";
    private static final String DIRECTORY_PARAMETER = "directory";
    private static final String NAME_PARAMETER = "name";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return "default";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationProfile create(Configuration config) throws ConfigurationException {
        // Get and check the profile name:
        String name = config.getString(NAME_PARAMETER);
        if (name == null) {
            throw new ConfigurationException(
                "The authentication profile described in configuration " +
                "file \"" + config.getFile().getAbsoluteFile() + "\" can't be " +
                "created because the mandatory parameter \"" + config.getAbsoluteKey(NAME_PARAMETER) + "\" hasn't " +
                "been provided."
            );
        }

        // Create the authenticator:
        Configuration authenticatorView = config.getView(AUTHENTICATOR_PARAMETER);
        Authenticator authenticator = AuthenticatorManager.getInstance().parseAuthenticator(authenticatorView);

        // Create the directory:
        Configuration directoryView = config.getView(DIRECTORY_PARAMETER);
        Directory directory = DirectoryManager.getInstance().parseDirectory(directoryView);

        // Create the new profile:
        return new AuthenticationProfile(name, authenticator, directory);
    }
}
