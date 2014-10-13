package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public abstract class LdapWithConfiguredCredentialsCommandBase extends LdapBrokerCommandBase {


    protected LdapWithConfiguredCredentialsCommandBase(LdapBrokerBaseParameters parameters) {
        super(parameters);
    }

    @Override
    protected void initCredentials(String domain) {
        setLoginName(configuration.getProperty("config.AdUserName"));
        setPassword(configuration.getProperty("config.AdUserPassword"));
        if (getLoginName().contains("@")) {
            String userDomain = getLoginName().split("@")[1].toLowerCase();
            setAuthenticationDomain(userDomain);
        } else {
            setAuthenticationDomain(domain);
        }
    }
}
