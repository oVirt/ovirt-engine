package org.ovirt.engine.core.bll.adbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

/**
 * This command Responcible to bring large amount of data from Active Directory with smallest amount of Active directory
 * Queries. First - all users retrieved from AD. All groups of all users gathered together. All parent groups of all
 * groups retrieved by single query. Maximum number of AD queries equal to AD tree depth + 1(for users query).
 */
public class LdapGetAdUserByUserIdListCommand extends LdapBrokerCommandBase {

    private java.util.ArrayList<Guid> getUserIds() {
        return ((LdapSearchByIdListParameters) getParameters()).getUserIds();
    }

    public LdapGetAdUserByUserIdListCommand(LdapSearchByIdListParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQuery(DirectorySearcher directorySearcher) {
        PopulateUsers();
        PopulateGroups();
        setSucceeded(true);
    }

    /**
     * Bring all users data from ldap provider
     */
    private void PopulateUsers() {
        List<LdapQueryData> queries = GenerateUsersQuery();
        List<LdapUser> results = new ArrayList<LdapUser>();
        for (LdapQueryData queryData : queries) {
            java.util.ArrayList<LdapUser> tempUsers = (java.util.ArrayList<LdapUser>) LdapFactory
                    .getInstance(getDomain())
                    .RunAdAction(AdActionType.SearchUserByQuery,
                            new LdapSearchByQueryParameters(getParameters().getSessionId(), getDomain(), queryData))
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

        UsersObjectGuidQueryGenerator generator = new UsersObjectGuidQueryGenerator();
        for (Guid id : getUserIds()) {
            generator.add(id);
        }
        return generator.getLdapQueriesData(getDomain());
    }

    private void PopulateGroups() {
        try {
            boolean performPopulate = true;
            List<LdapUser> users = (List<LdapUser>) getReturnValue();
            if (getParameters() instanceof LdapSearchByUserIdListParameters) {
                LdapSearchByUserIdListParameters params = (LdapSearchByUserIdListParameters) getParameters();
                performPopulate = params.getPerformGroupsQueryInsideCmd();
            }
            if (performPopulate) {
                LdapBrokerUtils.performGroupPopulationForUsers(users,
                        getLoginName(),
                        getPassword(),
                        getDomain(),
                        new ArrayList<ad_groups>());
            }

        } catch (RuntimeException ex) {
            log.infoFormat("GetAdUserByUserIdListCommand failed. Exception: {0}", ex);
        }

    }

    private static Log log = LogFactory.getLog(LdapGetAdUserByUserIdListCommand.class);
}
