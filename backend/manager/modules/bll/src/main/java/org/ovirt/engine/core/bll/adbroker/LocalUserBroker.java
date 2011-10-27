package org.ovirt.engine.core.bll.adbroker;

public class LocalUserBroker extends LdapBrokerBase {
    @Override
    protected String getBrokerType() {
        return "LU";
    }
}
