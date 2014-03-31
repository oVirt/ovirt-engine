package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class LdapSearchByIdListParameters extends LdapBrokerBaseParameters {
    private boolean populateGroups;
    private List<Guid> ids;

    public LdapSearchByIdListParameters(String domain, List<Guid> ids, boolean populateGroups) {
        super(domain);
        this.ids = ids;
        this.populateGroups = populateGroups;
    }

    public List<Guid> getIds() {
        return ids;
    }

    public void setIds(List<Guid> ids) {
        this.ids = ids;
    }

    public boolean isPopulateGroups() {
        return populateGroups;
    }

    public void setPopulateGroups(boolean value) {
        this.populateGroups = value;
    }
}
