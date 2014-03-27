package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.List;

import org.ovirt.engine.core.common.utils.ExternalId;

public class LdapSearchByIdListParameters extends LdapBrokerBaseParameters {
    private List<ExternalId> ids;
    private boolean populateGroups;

    public LdapSearchByIdListParameters(String domain, List<ExternalId> ids, boolean populateGroups) {
        super(domain);
        this.ids = ids;
        this.populateGroups = populateGroups;
    }

    public List<ExternalId> getIds() {
        return ids;
    }

    public void setIds(List<ExternalId> ids) {
        this.ids = ids;
    }

    public boolean isPopulateGroups() {
        return populateGroups;
    }

    public void setPopulateGroups(boolean value) {
        this.populateGroups = value;
    }
}
