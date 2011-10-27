package org.ovirt.engine.core.bll.adbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AdUser;
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
        List<AdUser> results = new ArrayList<AdUser>();
        for (Guid guid : getUserIds()) {
            AdUser user = InternalBrokerUtils.getUserByUserGuid(guid);
            if (user != null) {
                results.add(user);
            }
        }
        setReturnValue(results);
        setSucceeded(true);
    }

}
