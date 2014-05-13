package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.security.GeneralSecurityException;

import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;

public abstract class LdapWithConfiguredCredentialsCommandBase extends LdapBrokerCommandBase {


    protected LdapWithConfiguredCredentialsCommandBase(LdapBrokerBaseParameters parameters) {
        super(parameters);
    }

    @Override
    protected void initCredentials(String domain) {
        setLoginName(configuration.getProperty("config.AdUserName"));
        try {
            setPassword(EngineEncryptionUtils.decrypt(configuration.getProperty("config.AdUserPassword")));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        if (getLoginName().contains("@")) {
            String userDomain = getLoginName().split("@")[1].toLowerCase();
            setAuthenticationDomain(userDomain);
        } else {
            setAuthenticationDomain(domain);
        }
    }
}
