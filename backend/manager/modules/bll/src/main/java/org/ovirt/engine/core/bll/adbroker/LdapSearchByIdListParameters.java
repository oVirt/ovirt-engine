package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.compat.Guid;

public class LdapSearchByIdListParameters extends LdapBrokerBaseParameters {
    private java.util.ArrayList<Guid> privateUserIds;

    public java.util.ArrayList<Guid> getUserIds() {
        return privateUserIds;
    }

    private void setUserIds(java.util.ArrayList<Guid> value) {
        privateUserIds = value;
    }

    public LdapSearchByIdListParameters(String domain, java.util.ArrayList<Guid> userIds) {
        super(domain);
        setUserIds(userIds);
    }
}
