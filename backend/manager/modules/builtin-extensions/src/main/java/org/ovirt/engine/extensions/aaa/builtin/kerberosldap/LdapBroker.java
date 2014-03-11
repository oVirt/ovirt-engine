package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

public interface LdapBroker {
    LdapReturnValueBase runAdAction(AdActionType actionType, LdapBrokerBaseParameters parameters);
}
