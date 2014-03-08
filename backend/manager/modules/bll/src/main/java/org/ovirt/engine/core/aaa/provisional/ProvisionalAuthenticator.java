package org.ovirt.engine.core.aaa.provisional;

import org.ovirt.engine.core.aaa.AuthenticationResult;
import org.ovirt.engine.core.aaa.PasswordAuthenticator;
import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapBroker;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapReturnValueBase;
import org.ovirt.engine.core.bll.adbroker.LdapUserPasswordBaseParameters;
import org.ovirt.engine.core.bll.adbroker.UserAuthenticationResult;

/**
 * This authenticator implementation is a bridge between the new directory interface and the existing LDAP
 * infrastructure. It will exist only while the engine is migrated to use the new authentication interfaces, then it
 * will be removed.
 */
public class ProvisionalAuthenticator extends PasswordAuthenticator {

    /**
     * The reference to the LDAP broker that implements the authentication.
     */
    private LdapBroker broker;


    public ProvisionalAuthenticator() {
    }

    @Override
    public void init() {
        broker = LdapFactory.getInstance(getProfileName());
        context.put(ExtensionProperties.AUTHOR, "The oVirt Project");
        context.put(ExtensionProperties.EXTENSION_NAME, "Internal Kerberos/LDAP authentication (Built-in)");
        context.put(ExtensionProperties.LICENSE, "ASL 2.0");
        context.put(ExtensionProperties.HOME, "http://www.ovirt.org");
        context.put(ExtensionProperties.VERSION, "N/A");

    }



    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationResult authenticate(String name, String password) {
        LdapReturnValueBase ldapResult = broker.runAdAction(
            AdActionType.AuthenticateUser,
                new LdapUserPasswordBaseParameters(getProfileName(), name, password)
        );
        UserAuthenticationResult authResult = (UserAuthenticationResult) ldapResult.getReturnValue();
        return new ProvisionalAuthenticationResult(getProfileName(), authResult);
    }

}
