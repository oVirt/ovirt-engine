package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.compat.Guid;

public class LdapGetAdUserByUserIdCommand extends LdapWithConfiguredCredentialsCommandBase {
    private Guid getUserId() {
        return ((LdapSearchByIdParameters) getParameters()).getId();
    }

    public LdapGetAdUserByUserIdCommand(LdapSearchByIdParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        LdapUser user;

        LdapQueryData queryData = new LdapQueryDataImpl();
        queryData.setFilterParameters(new Object[] { getUserId() });
        queryData.setLdapQueryType(LdapQueryType.getUserByGuid);
        queryData.setDomain(getDomain());

        Object searchResult = directorySearcher.findOne(queryData);
        user = populateUserData((LdapUser) searchResult, getDomain());

        if (user != null) {
            GroupsDNQueryGenerator generator = createGroupsGeneratorForUser(user);
            List<LdapQueryData> partialQueries = generator.getLdapQueriesData();
            for (LdapQueryData partialQuery : partialQueries) {
                populateGroup(partialQuery, getDomain(), user.getGroups(), getLoginName(), getPassword());
            }
        }
        setReturnValue(user);
        setSucceeded(true);
    }
}
