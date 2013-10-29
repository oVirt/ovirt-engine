package org.ovirt.engine.core.authentication.header;

import org.ovirt.engine.core.authentication.Authenticator;
import org.ovirt.engine.core.authentication.AuthenticatorFactory;
import org.ovirt.engine.core.authentication.Configuration;
import org.ovirt.engine.core.authentication.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeaderAuthenticatorFactory implements AuthenticatorFactory {
    private static final Logger log = LoggerFactory.getLogger(HeaderAuthenticatorFactory.class);

    /**
     * The type supported by this factory.
     */
    private static final String TYPE = "header";

    /**
     * The name of the configuration parameter that contains the name of the header.
     */
    private static final String HEADER_PARAMETER = "header";

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
        // Get the name of the header:
        String header = config.getString(HEADER_PARAMETER);
        if (header == null) {
            throw new ConfigurationException(
                "The configuration file \"" + config.getFile().getAbsolutePath() + "\" doesn't contain the " +
                "parameter \"" + config.getAbsoluteKey(HEADER_PARAMETER) + "\" that specifies the name of " +
                "the header containing the remote user name."
            );
        }

        // We are good, create the authenticator:
        return new HeaderAuthenticator(header);
    }
}
