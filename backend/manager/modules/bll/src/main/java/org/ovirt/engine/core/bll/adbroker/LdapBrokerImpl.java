package org.ovirt.engine.core.bll.adbroker;

public class LdapBrokerImpl extends LdapBrokerBase {
    @Override
    protected String getBrokerType() {
        return "Ldap";
    }
}
