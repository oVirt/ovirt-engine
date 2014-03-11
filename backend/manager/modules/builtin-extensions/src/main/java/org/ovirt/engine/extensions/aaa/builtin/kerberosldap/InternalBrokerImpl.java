package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public class InternalBrokerImpl extends LdapBrokerBase {
    @Override
    protected String getBrokerType() {
        return "Internal";
    }
}

