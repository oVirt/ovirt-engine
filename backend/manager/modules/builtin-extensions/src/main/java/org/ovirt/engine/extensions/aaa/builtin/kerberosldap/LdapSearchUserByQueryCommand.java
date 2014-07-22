package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.aaa.LdapUser;

public class LdapSearchUserByQueryCommand extends LdapSearchGroupsByQueryCommand {
    private boolean populateGroups;

    protected LdapQueryData getLdapQueryData() {
        return ((LdapSearchByQueryParameters) getParameters()).getLdapQueryData();
    }

    public LdapSearchUserByQueryCommand(LdapSearchByQueryParameters parameters) {
        super(parameters);
        populateGroups = parameters.isPopulateGroups();
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        final List<LdapUser> userList = new ArrayList<LdapUser>();

        @SuppressWarnings("unchecked")
        final List<LdapUser> usersList = (List<LdapUser>) directorySearcher.findAll(getLdapQueryData());
        for (final LdapUser searchResult : usersList) {
            {
                LdapUser user =
                        populateUserData(searchResult, getLdapQueryData().getDomain(), populateGroups);
                userList.add(user);
            }
        }
        setReturnValue(userList);
        setSucceeded(true);
    }
}
