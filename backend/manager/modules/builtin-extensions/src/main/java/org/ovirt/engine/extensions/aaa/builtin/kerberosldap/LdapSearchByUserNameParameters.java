package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.Properties;

public class LdapSearchByUserNameParameters extends LdapBrokerBaseParameters {
    private String privateUserName;

    public String getUserName() {
        return privateUserName;
    }

    private void setUserName(String value) {
        privateUserName = value;
    }

    public LdapSearchByUserNameParameters(Properties configuration, String sessionId, String domain, String userName) {
        super(configuration, sessionId, domain);
        setUserName(userName);
    }
}
