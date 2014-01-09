package org.ovirt.engine.core.bll.adbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.utils.ExternalId;

public class InternalGetAdUserByUserIdListCommand extends InternalBrokerCommandBase {
    private List<ExternalId> getUserIds() {
        return ((LdapSearchByIdListParameters) getParameters()).getIds();
    }

    public InternalGetAdUserByUserIdListCommand(LdapSearchByIdListParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery() {
        List<LdapUser> results = new ArrayList<LdapUser>();
        for (ExternalId id : getUserIds()) {
            LdapUser user = InternalBrokerUtils.getUserById(id);
            if (user != null) {
                results.add(user);
            }
        }
        setReturnValue(results);
        setSucceeded(true);
    }

}
