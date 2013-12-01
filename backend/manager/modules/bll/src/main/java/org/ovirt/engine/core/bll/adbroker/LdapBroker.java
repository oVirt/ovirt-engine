package org.ovirt.engine.core.bll.adbroker;

public interface LdapBroker {
    LdapReturnValueBase runAdAction(AdActionType actionType, LdapBrokerBaseParameters parameters);
}
