package org.ovirt.engine.core.bll.adbroker;

import org.ovirt.engine.core.common.utils.ExternalId;

public class LdapSearchByIdParameters extends LdapBrokerBaseParameters {
    private ExternalId id;

    public LdapSearchByIdParameters(String domain, ExternalId id) {
        super(domain);
        this.id = id;
    }

    public LdapSearchByIdParameters(String sessionId, String domain, ExternalId id) {
        super(sessionId, domain);
        this.id = id;
    }

    public ExternalId getId() {
        return id;
    }

    public void setId(ExternalId id) {
        this.id = id;
    }
}
