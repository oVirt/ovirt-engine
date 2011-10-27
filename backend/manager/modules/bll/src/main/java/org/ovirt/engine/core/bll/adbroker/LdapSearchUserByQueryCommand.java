package org.ovirt.engine.core.bll.adbroker;

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
        java.util.ArrayList<AdUser> userList = new java.util.ArrayList<AdUser>();

        java.util.List usersList = directorySearcher.FindAll(getLdapQueryData());
        {
            for (Object searchResult : usersList) {
                {
                    AdUser user = populateUserData((AdUser) searchResult, getLdapQueryData().getDomain());
                    userList.add(user);
                }
            }
        }
        setReturnValue(userList);
        setSucceeded(true);
    }
}
