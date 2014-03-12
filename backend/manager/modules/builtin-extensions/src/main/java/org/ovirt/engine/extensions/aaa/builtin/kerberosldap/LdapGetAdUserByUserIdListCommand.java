package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.utils.ExternalId;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This command Responsible to bring large amount of data from Active Directory with smallest amount of Active directory
 * Queries. First - all users retrieved from AD. All groups of all users gathered together. All parent groups of all
 * groups retrieved by single query. Maximum number of AD queries equal to AD tree depth + 1(for users query).
 */
public class LdapGetAdUserByUserIdListCommand extends LdapBrokerCommandBase {

    private List<ExternalId> getUserIds() {
        return ((LdapSearchByIdListParameters) getParameters()).getIds();
    }

    private boolean populateGroups;

    public LdapGetAdUserByUserIdListCommand(LdapSearchByIdListParameters parameters) {
        super(parameters);
        populateGroups = parameters.isPopulateGroups();

    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        PopulateUsers();
        setSucceeded(true);
    }

    /**
     * Bring all users data from ldap provider
     */
    private void PopulateUsers() {
        List<LdapQueryData> queries = GenerateUsersQuery();
        List<LdapUser> results = new ArrayList<LdapUser>();
        for (LdapQueryData queryData : queries) {
            ArrayList<LdapUser> tempUsers = (ArrayList<LdapUser>) LdapFactory
                    .getInstance(getDomain())
                    .runAdAction(AdActionType.SearchUserByQuery,
                            new LdapSearchByQueryParameters(getParameters().getSessionId(), getDomain(), queryData, populateGroups))
                    .getReturnValue();
            if (tempUsers != null) {
                results.addAll(tempUsers);
            }
        }
        setReturnValue(results);
    }

    /**
     * Generate Queries to search all users
     *
     * @return
     */
    private List<LdapQueryData> GenerateUsersQuery() {

        UsersObjectIdQueryGenerator generator = new UsersObjectIdQueryGenerator();
        for (ExternalId id : getUserIds()) {
            generator.add(id);
        }
        return generator.getLdapQueriesData(getDomain());
    }

    private static final Log log = LogFactory.getLog(LdapGetAdUserByUserIdListCommand.class);
}
