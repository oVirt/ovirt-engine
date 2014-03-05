package org.ovirt.engine.core.bll.adbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapUser;

public class LdapSearchUserByQueryCommand extends LdapSearchGroupsByQueryCommand {
    protected LdapQueryData getLdapQueryData() {
        return ((LdapSearchByQueryParameters) getParameters()).getLdapQueryData();
    }

    public LdapSearchUserByQueryCommand(LdapSearchByQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        final List<LdapUser> userList = new ArrayList<LdapUser>();

        @SuppressWarnings("unchecked")
        final List<LdapUser> usersList = (List<LdapUser>) directorySearcher.findAll(getLdapQueryData());
        for (final LdapUser searchResult : usersList) {
            {
                LdapUser user = populateUserData(searchResult, getLdapQueryData().getDomain(), false);
                userList.add(user);
            }
        }
        setReturnValue(userList);
        setSucceeded(true);
    }
}
