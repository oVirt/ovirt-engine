package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.Properties;


public class LdapUserPasswordBaseParameters extends LdapBrokerBaseParameters {

    public LdapUserPasswordBaseParameters(Properties configuration,
            String userName,
            String password) {
        super(configuration, configuration.getProperty("ovirt.engine.aaa.authn.authz.plugin"));
        setLoginName(userName);
        setPassword(password);
    }
}
