package org.ovirt.engine.core.authentication.provisional;

import org.ovirt.engine.core.authentication.AuthenticationResult;
import org.ovirt.engine.core.authentication.PasswordAuthenticator;
import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapBroker;
import org.ovirt.engine.core.bll.adbroker.LdapReturnValueBase;
import org.ovirt.engine.core.bll.adbroker.LdapUserPasswordBaseParameters;
import org.ovirt.engine.core.bll.adbroker.UserAuthenticationResult;

/**
 * This authenticator implementation is a bridge between the new directory interface and the existing LDAP
 * infrastructure. It will exist only while the engine is migrated to use the new authentication interfaces, then it
 * will be removed.
 */
public class ProvisionalAuthenticator implements PasswordAuthenticator {
    /**
     * The name of the domain.
     */
    private String domain;

    /**
     * The reference to the LDAP broker that implements the authentication.
     */
    private LdapBroker broker;

    public ProvisionalAuthenticator(String domain, LdapBroker broker) {
        this.domain = domain;
        this.broker = broker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationResult<?> authenticate(String name, String password) {
        LdapReturnValueBase ldapResult = broker.runAdAction(
            AdActionType.AuthenticateUser,
            new LdapUserPasswordBaseParameters(domain, name, password)
        );
        UserAuthenticationResult authResult = (UserAuthenticationResult) ldapResult.getReturnValue();
        return new ProvisionalAuthenticationResult(authResult);
    }
}
