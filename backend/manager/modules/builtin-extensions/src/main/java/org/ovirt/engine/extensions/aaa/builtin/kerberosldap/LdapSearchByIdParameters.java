package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import org.ovirt.engine.core.compat.Guid;

public class LdapSearchByIdParameters extends LdapBrokerBaseParameters {
    private Guid id;

    public LdapSearchByIdParameters(String domain, Guid id) {
        super(domain);
        this.id = id;
    }

    public LdapSearchByIdParameters(String sessionId, String domain, Guid id) {
        super(sessionId, domain);
        this.id = id;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }
}
