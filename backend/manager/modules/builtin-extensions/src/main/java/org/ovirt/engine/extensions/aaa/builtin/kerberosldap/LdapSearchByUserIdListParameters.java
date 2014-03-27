package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.List;

import org.ovirt.engine.core.common.utils.ExternalId;

public class LdapSearchByUserIdListParameters extends LdapSearchByIdListParameters {

    public LdapSearchByUserIdListParameters(String domain, List<ExternalId> userIds) {
        super(domain, userIds, false);
    }

    public LdapSearchByUserIdListParameters(String domain, List<ExternalId> userIds, boolean populateGroups) {
        super(domain, userIds, populateGroups);
    }
}
