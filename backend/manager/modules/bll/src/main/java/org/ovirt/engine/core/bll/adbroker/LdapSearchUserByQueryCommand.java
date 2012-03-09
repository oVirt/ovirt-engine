package org.ovirt.engine.core.bll.adbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.AdUser;

public class LdapSearchUserByQueryCommand extends LdapSearchGroupsByQueryCommand {
    protected LdapQueryData getLdapQueryData() {
        return ((LdapSearchByQueryParameters) getParameters()).getLdapQueryData();
    }

    public LdapSearchUserByQueryCommand(LdapSearchByQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        final List<AdUser> userList = new ArrayList<AdUser>();

        @SuppressWarnings("unchecked")
        final List<AdUser> usersList = (List<AdUser>) directorySearcher.FindAll(getLdapQueryData());
        for (final AdUser searchResult : usersList) {
            {
                AdUser user = populateUserData(searchResult, getLdapQueryData().getDomain());
                userList.add(user);
            }
        }
        setReturnValue(userList);
        setSucceeded(true);
    }
}
