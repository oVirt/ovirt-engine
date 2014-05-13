package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.List;
import java.util.Properties;

import org.ovirt.engine.core.compat.Guid;

public class LdapSearchByUserIdListParameters extends LdapSearchByIdListParameters {

    public LdapSearchByUserIdListParameters(Properties configuration, String domain, List<Guid> userIds, boolean populateGroups) {
        super(configuration, domain, userIds, populateGroups);
    }
}
