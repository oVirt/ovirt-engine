package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.adbroker.LdapBrokerBase;

public class InternalBrokerImpl extends LdapBrokerBase {
    @Override
    protected String getBrokerType() {
        return "Internal";
    }
}

