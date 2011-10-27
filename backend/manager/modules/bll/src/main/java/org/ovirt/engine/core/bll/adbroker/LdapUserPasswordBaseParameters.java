package org.ovirt.engine.core.bll.adbroker;

public class LdapUserPasswordBaseParameters extends LdapBrokerBaseParameters {
    public LdapUserPasswordBaseParameters(String domain, String loginName, String password) {
        super(domain);
        setLoginName(loginName);
        setPassword(password);
    }
}
