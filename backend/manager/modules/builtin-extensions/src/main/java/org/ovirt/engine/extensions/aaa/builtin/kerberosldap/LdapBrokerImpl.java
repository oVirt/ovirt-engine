package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public class LdapBrokerImpl extends LdapBrokerBase {
    @Override
    protected String getBrokerType() {
        return "Ldap";
    }
}
