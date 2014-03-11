package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public class LdapUserPasswordBaseParameters extends LdapBrokerBaseParameters {
    public LdapUserPasswordBaseParameters(String domain, String loginName, String password) {
        super(domain);
        setLoginName(loginName);
        setPassword(password);
    }
}
