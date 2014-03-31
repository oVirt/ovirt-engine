package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class LdapSearchByUserIdListParameters extends LdapSearchByIdListParameters {

    public LdapSearchByUserIdListParameters(String domain, List<Guid> userIds, boolean populateGroups) {
        super(domain, userIds, populateGroups);
    }
}
