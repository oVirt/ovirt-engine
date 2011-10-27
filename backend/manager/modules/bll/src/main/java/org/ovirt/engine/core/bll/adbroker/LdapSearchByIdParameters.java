package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.compat.Guid;

public class LdapSearchByIdParameters extends LdapBrokerBaseParameters {
    private Guid privateId;

    public Guid getId() {
        return privateId;
    }

    private void setId(Guid value) {
        privateId = value;
    }

    public LdapSearchByIdParameters(String domain, Guid id) {
        super(domain);
        setId(id);
    }

    public LdapSearchByIdParameters(String sessionId, String domain, Guid id) {
        super(sessionId, domain);
        setId(id);
    }
}
