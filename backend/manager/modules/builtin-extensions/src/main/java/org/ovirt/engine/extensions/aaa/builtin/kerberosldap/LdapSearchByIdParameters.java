package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.Properties;

import org.ovirt.engine.core.compat.Guid;

public class LdapSearchByIdParameters extends LdapBrokerBaseParameters {
    private Guid id;

    public LdapSearchByIdParameters(Properties configuration, String domain, Guid id) {
        super(configuration, domain);
        this.id = id;
    }

    public LdapSearchByIdParameters(Properties configuration, String sessionId, String domain, Guid id) {
        super(configuration, sessionId, domain);
        this.id = id;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }
}
