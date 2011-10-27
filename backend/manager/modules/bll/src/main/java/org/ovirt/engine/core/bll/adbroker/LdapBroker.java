package org.ovirt.engine.core.bll.adbroker;

public interface LdapBroker {
    LdapReturnValueBase RunAdAction(AdActionType actionType, LdapBrokerBaseParameters parameters);
}
