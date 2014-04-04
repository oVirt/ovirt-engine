package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.List;
import java.util.Properties;

import org.ovirt.engine.api.extensions.Base;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.Extension;
import org.ovirt.engine.api.extensions.aaa.Authn;

/**
 * This authenticator implementation is a bridge between the new directory interface and the existing LDAP
 * infrastructure. It will exist only while the engine is migrated to use the new authentication interfaces, then it
 * will be removed.
 */
public class KerberosLdapAuthn implements Extension {

    /**
     * The reference to the LDAP broker that implements the authentication.
     */
    private LdapBroker broker;
    private ExtMap context;
    private Properties configuration;



    public KerberosLdapAuthn() {
    }

    @Override
    public void invoke(ExtMap input, ExtMap output) {
        try {
            if (input.get(Base.InvokeKeys.COMMAND).equals(Base.InvokeCommands.INITIALIZE)) {
                doInit(input, output);
            } else if (input.get(Base.InvokeKeys.COMMAND).equals(Authn.InvokeCommands.AUTHENTICATE_CREDENTIALS)) {
                doAuthenticate(input, output);
            } else {
                output.put(Base.InvokeKeys.RESULT, Base.InvokeResult.UNSUPPORTED);
            }
            output.putIfAbsent(Base.InvokeKeys.RESULT, Base.InvokeResult.SUCCESS);
        } catch (Exception ex) {
            output.mput(Base.InvokeKeys.RESULT, Base.InvokeResult.FAILED).
                    mput(Base.InvokeKeys.MESSAGE, ex.getMessage());
        }
    }


    private void doInit(ExtMap inputMap, ExtMap outputMap) {
        context = inputMap.<ExtMap> get(Base.InvokeKeys.CONTEXT);
        configuration = context.<Properties> get(Base.ContextKeys.CONFIGURATION);
        broker = LdapFactory.getInstance(getAuthzName());
        context.<List<String>> get(
                Base.ContextKeys.CONFIGURATION_SENSITIVE_KEYS
                ).add("config.authn.user.password");
        context.mput(
                Base.ContextKeys.AUTHOR,
                "The oVirt Project").mput(
                Base.ContextKeys.EXTENSION_NAME,
                "Kerberos/Ldap Authn (Built-in)"
                ).mput(
                        Base.ContextKeys.LICENSE,
                        "ASL 2.0"
                ).mput(
                        Base.ContextKeys.HOME_URL,
                        "http://www.ovirt.org"
                ).mput(
                        Base.ContextKeys.VERSION,
                        "N/A"
                ).mput(
                        Authn.ContextKeys.CAPABILITIES,
                        Authn.Capabilities.AUTHENTICATE_PASSWORD
                ).mput(
                        Base.ContextKeys.BUILD_INTERFACE_VERSION,
                        Base.INTERFACE_VERSION_CURRENT);

        KerberosManager.getInstance();
        UsersDomainsCacheManagerService.getInstance().init();
    }



    /**
     * {@inheritDoc}
     */
    private void doAuthenticate(ExtMap input, ExtMap output) {
        broker.runAdAction(
            AdActionType.AuthenticateUser,
                new LdapUserPasswordBaseParameters(input, output)
            );
        // Putting these keys anyway, it's up to BLL to decide if to use them or not
        output.mput(
                Authn.InvokeKeys.USER_MESSAGE,
                configuration.getProperty("config.change.password.msg")
                ).mput(
                        Authn.InvokeKeys.CREDENTIALS_CHANGE_URL,
                        configuration.getProperty("config.change.password.url"
                      )
                );
    }

    private String getAuthzName() {
        return configuration.getProperty("ovirt.engine.aaa.authn.authz.plugin");
    }

}
