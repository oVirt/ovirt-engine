package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.List;
import java.util.Properties;

import org.ovirt.engine.core.compat.Guid;

public class LdapSearchByIdListParameters extends LdapBrokerBaseParameters {
    private boolean populateGroups;
    private List<Guid> ids;

    public LdapSearchByIdListParameters(Properties configuration, String domain, List<Guid> ids, boolean populateGroups) {
        super(configuration, domain);
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
