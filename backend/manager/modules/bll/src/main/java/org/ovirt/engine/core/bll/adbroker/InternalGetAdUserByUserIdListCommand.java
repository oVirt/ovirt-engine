package org.ovirt.engine.core.bll.adbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.compat.Guid;



public class InternalGetAdUserByUserIdListCommand extends InternalBrokerCommandBase {
    private java.util.ArrayList<Guid> getUserIds() {
        return ((LdapSearchByIdListParameters) getParameters()).getUserIds();
    }

    public InternalGetAdUserByUserIdListCommand(LdapSearchByIdListParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteQuery() {
        List<LdapUser> results = new ArrayList<LdapUser>();
        for (Guid guid : getUserIds()) {
            LdapUser user = InternalBrokerUtils.getUserByUserGuid(guid);
            if (user != null) {
                results.add(user);
            }
        }
        setReturnValue(results);
        setSucceeded(true);
    }

}
