package org.ovirt.engine.core.bll.adbroker;

import java.util.List;

import org.ovirt.engine.core.common.utils.ExternalId;

public class LdapSearchByUserIdListParameters extends LdapSearchByIdListParameters {
    private boolean performGroupsQueryInsideCmd = true;

    public LdapSearchByUserIdListParameters(String domain, List<ExternalId> userIds) {
        super(domain, userIds);
    }

    public LdapSearchByUserIdListParameters(String domain, List<ExternalId> userIds, boolean performGroupsQueryInsideCmd) {
        super(domain, userIds);
        this.performGroupsQueryInsideCmd = performGroupsQueryInsideCmd;
    }

    public boolean getPerformGroupsQueryInsideCmd() {
        return performGroupsQueryInsideCmd;
    }
}
