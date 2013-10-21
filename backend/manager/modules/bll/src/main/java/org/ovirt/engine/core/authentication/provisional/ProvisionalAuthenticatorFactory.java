package org.ovirt.engine.core.authentication.provisional;

import org.ovirt.engine.core.authentication.Authenticator;
import org.ovirt.engine.core.authentication.AuthenticatorFactory;
import org.ovirt.engine.core.authentication.Configuration;
import org.ovirt.engine.core.authentication.ConfigurationException;
import org.ovirt.engine.core.bll.adbroker.LdapBroker;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This is the factory for the bridge between the new authentication interfaces and the existing LDAP infrastructure. It
 * will exist only while the engine is migrated to use the new authentication interfaces, then it will be removed.
 */
public class ProvisionalAuthenticatorFactory implements AuthenticatorFactory {
    private Log log = LogFactory.getLog(ProvisionalAuthenticatorFactory.class);

    // The names of the parameters:
    private static final String DOMAIN_PARAMETER = "domain";

    @Override
    public String getType() {
        return "provisional";
    }

    @Override
    public Authenticator create(Configuration config) throws ConfigurationException {
        // Get the name of the domain from the configuration:
        String domain = config.getInheritedString(DOMAIN_PARAMETER);
        if (domain == null) {
            throw new ConfigurationException(
                "The authenticator described in configuration " +
                "file \"" + config.getFile().getAbsolutePath() + "\" can't be created because the " +
                "parameter \"" + config.getAbsoluteKey(DOMAIN_PARAMETER) + "\" doesn't have a value."
            );
        }

        // Check that the domain is defined in the database:
        LdapBroker broker = LdapFactory.getInstance(domain);
        if (broker == null) {
            throw new ConfigurationException(
                "The authenticator described in configuration " +
                "file \"" + config.getFile().getAbsolutePath() + "\" can't be created because the " +
                "domain \"" + domain + "\" doesn't exist in the database."
            );
        }

        // Create the authenticator:
        return new ProvisionalAuthenticator(domain, broker);
    }
}
