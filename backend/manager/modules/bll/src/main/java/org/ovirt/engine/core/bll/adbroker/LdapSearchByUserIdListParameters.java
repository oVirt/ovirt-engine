/**
 *
 */
package org.ovirt.engine.core.bll.adbroker;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Guid;

public class LdapSearchByUserIdListParameters extends LdapSearchByIdListParameters {


    private boolean performGroupsQueryInsideCmd = true;
    /**
     * @param domain
     * @param userIds
     */
    public LdapSearchByUserIdListParameters(String domain, ArrayList<Guid> userIds) {
        super(domain, userIds);
    }

    public LdapSearchByUserIdListParameters(String domain, ArrayList<Guid> userIds, boolean performGroupsQueryInsideCmd) {
        super(domain, userIds);
        this.performGroupsQueryInsideCmd = performGroupsQueryInsideCmd;
    }

    public boolean getPerformGroupsQueryInsideCmd() {
        return performGroupsQueryInsideCmd;
    }


}
