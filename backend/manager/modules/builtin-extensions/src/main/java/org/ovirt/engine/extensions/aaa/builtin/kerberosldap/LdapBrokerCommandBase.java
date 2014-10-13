package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.common.businessentities.aaa.LdapGroup;
import org.ovirt.engine.core.common.businessentities.aaa.LdapUser;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.extensions.aaa.builtin.kerberosldap.utils.kerberos.AuthenticationResult;

public abstract class LdapBrokerCommandBase extends BrokerCommandBase {

    protected static final Map<AuthenticationResult, Integer> resultsMap = new HashMap<>();

    static {
        resultsMap.put(AuthenticationResult.CANNOT_FIND_LDAP_SERVER_FOR_DOMAIN,
                Authn.AuthResult.REMOTE_UNAVAILABLE);
        resultsMap.put(AuthenticationResult.CLIENT_NOT_FOUND_IN_KERBEROS_DATABASE,
                Authn.AuthResult.CREDENTIALS_INCORRECT);
        resultsMap.put(AuthenticationResult.CLOCK_SKEW_TOO_GREAT,
                Authn.AuthResult.GENERAL_ERROR);
        resultsMap.put(AuthenticationResult.CONNECTION_ERROR,
                Authn.AuthResult.REMOTE_UNAVAILABLE);
        resultsMap.put(AuthenticationResult.CONNECTION_TIMED_OUT,
                Authn.AuthResult.TIMED_OUT);
        resultsMap.put(AuthenticationResult.DNS_COMMUNICATION_ERROR,
                Authn.AuthResult.REMOTE_UNAVAILABLE);
        resultsMap.put(AuthenticationResult.DNS_ERROR,
                Authn.AuthResult.REMOTE_UNAVAILABLE);
        resultsMap.put(AuthenticationResult.INTERNAL_KERBEROS_ERROR,
                Authn.AuthResult.GENERAL_ERROR);
        resultsMap.put(AuthenticationResult.INVALID_CREDENTIALS,
                Authn.AuthResult.CREDENTIALS_INVALID);
        resultsMap.put(AuthenticationResult.NO_KDCS_FOUND,
                Authn.AuthResult.REMOTE_UNAVAILABLE);
        resultsMap.put(AuthenticationResult.NO_USER_INFORMATION_WAS_FOUND_FOR_USER,
                Authn.AuthResult.CREDENTIALS_INCORRECT);
        resultsMap.put(AuthenticationResult.OTHER,
                Authn.AuthResult.GENERAL_ERROR);
        resultsMap.put(AuthenticationResult.PASSWORD_EXPIRED,
                Authn.AuthResult.CREDENTIALS_EXPIRED);
        resultsMap.put(AuthenticationResult.USER_ACCOUNT_DISABLED_OR_LOCKED,
                Authn.AuthResult.ACCOUNT_LOCKED);
        resultsMap.put(AuthenticationResult.WRONG_REALM,
                Authn.AuthResult.CREDENTIALS_INCORRECT);
    }

    private Map<String, LdapGroup> globalGroupsDict = new HashMap<>();
    protected Properties configuration;

    public Properties getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }

    @Override
    protected String getPROTOCOL() {
        return "LDAP://";
    }

    protected LdapBrokerCommandBase(LdapUserPasswordBaseParameters parameters) {
        super(parameters);
        setAuthenticationDomain(getDomain());
        setConfiguration(parameters.getConfiguration());
    }

    protected LdapBrokerCommandBase(LdapBrokerBaseParameters parameters) {
        super(parameters);
        setConfiguration(parameters.getConfiguration());
        initCredentials(parameters.getDomain());
    }

    protected void initCredentials(String domain) {
        setUserDomainCredentials(domain);
    }

    protected void setUserDomainCredentials(String domain) {
        setLoginName(configuration.getProperty("config.AdUserName"));
        setPassword(configuration.getProperty("config.AdUserPassword"));
        if (getLoginName().contains("@")) {
            String userDomain = getLoginName().split("@")[1].toLowerCase();
            setAuthenticationDomain(userDomain);
        } else {
            setAuthenticationDomain(domain);
        }
    }

    @Override
    public LdapReturnValueBase execute() {
        boolean exceptionOccurred = true;
        try {
            log.debugFormat("Running LDAP command: {0}", getClass().getName());
            String loginNameForKerberos =
                    LdapBrokerUtils.modifyLoginNameForKerberos(getLoginName(), getAuthenticationDomain(), configuration);
            LdapCredentials ldapCredentials = new LdapCredentials(loginNameForKerberos, getPassword());
            DirectorySearcher directorySearcher = new DirectorySearcher(configuration, ldapCredentials);
            executeQuery(directorySearcher);
            exceptionOccurred = directorySearcher.getException() != null;
        }
 finally {
            if (exceptionOccurred) {
                log.error(String.format("Failed to run command %s. Domain is %s. User is %s.",
                        getClass().getSimpleName(), getDomain(), getLoginName()));
                _ldapReturnValue.setExceptionString(VdcBllMessages.FAILED_TO_RUN_LDAP_QUERY.name());
                _ldapReturnValue.setSucceeded(false);
            }
        }
        return _ldapReturnValue;
    }

    protected abstract void executeQuery(DirectorySearcher directorySearcher);

    protected LdapUser populateUserData(LdapUser user, String domain) {
        return populateUserData(user, domain, true, true);
    }

    protected LdapUser populateUserData(LdapUser user,
            String domain,
            boolean populateGroups,
            boolean populateGroupsRecursively) {
        if (user == null) {
            return null;
        }
        user.setDomainControler(domain);

        // Getting the groups
        HashMap<String, LdapGroup> groupsDict = new HashMap<String, LdapGroup>();

        GroupsDNQueryGenerator generator = new GroupsDNQueryGenerator();
        proceedGroupsSearchResult(null, user.getMemberof(), groupsDict, generator);
        user.setGroups(groupsDict);
        if (user.getUserName() != null && !user.getUserName().contains("@")) {
            user.setUserName(user.getUserName() + "@" + user.getDomainControler());
        }

        if (populateGroupsRecursively || populateGroups) {
            if (generator.getHasValues()) {
                List<LdapQueryData> partialQueries = generator.getLdapQueriesData();
                for (LdapQueryData currQueryData : partialQueries) {
                    populateGroup(currQueryData,
                            getAuthenticationDomain(),
                            groupsDict,
                            getLoginName(),
                            getPassword(),
                            populateGroupsRecursively
                            );
                }
            }
        }
        user.setGroups(groupsDict);
        return user;
    }

    protected void populateGroup(LdapQueryData queryData,
                                 String domain,
                                 Map<String, LdapGroup> groupsDict,
                                 String loginName,
                                 String password, boolean populateGroupsRecursively) {
        try {
            GroupsDNQueryGenerator generator = new GroupsDNQueryGenerator();
            List<GroupSearchResult> searchResultCollection =
                    LdapBrokerUtils.performGroupQuery(configuration, loginName, password, domain, queryData);
            if (searchResultCollection != null) {
                for (GroupSearchResult searchResult : searchResultCollection) {
                    ProceedGroupsSearchResult(searchResult, groupsDict, generator);
                }
            }
            // If generator has results, it means there are parent groups
            if (generator.getHasValues() && populateGroupsRecursively) {
                List<LdapQueryData> partialQueries = generator.getLdapQueriesData();
                for (LdapQueryData partialQuery : partialQueries) {
                    populateGroup(partialQuery, domain, groupsDict, loginName, password, populateGroupsRecursively);
                }
            }
        } catch (RuntimeException e) {
            log.infoFormat("populateGroup failed. Exception: {0}", e);
        }
    }

    private void ProceedGroupsSearchResult(GroupSearchResult groupsResult,
            Map<String, LdapGroup> groupsDict, GroupsDNQueryGenerator generator) {
        List<String> groupsList = groupsResult.getMemberOf();
        LdapGroup group = new LdapGroup();
        group.setid(groupsResult.getId());
        group.setname(LdapBrokerUtils.generateGroupDisplayValue(groupsResult.getDistinguishedName()));
        group.setMemberOf(groupsResult.getMemberOf());
        group.setDistinguishedName(groupsResult.getDistinguishedName());
        groupsDict.put(group.getname(), group);
        globalGroupsDict.put(group.getname(), group);
        proceedGroupsSearchResult(groupsResult.getId(), groupsList, groupsDict, generator);
    }

    private void proceedGroupsSearchResult(String groupId, List<String> groupDNList,
            Map<String, LdapGroup> groupsDict, GroupsDNQueryGenerator generator) {
        if (groupDNList == null) {
            return;
        }
        for (String groupDN : groupDNList) {
            String groupName = LdapBrokerUtils.generateGroupDisplayValue(groupDN);
            if (!groupsDict.containsKey(groupName)) {
                LdapGroup group = globalGroupsDict.get(groupDN);
                if (group == null) {
                    generator.add(groupDN);
                } else {
                    groupsDict.put(groupName, group);
                }
            }
        }
    }

    protected GroupsDNQueryGenerator createGroupsGeneratorForUser(LdapUser user) {
        List<String> dnsList = new ArrayList<String>();
        for (LdapGroup adGroup : user.getGroups().values()) {
            dnsList.add(adGroup.getDistinguishedName());
        }
        GroupsDNQueryGenerator generator = new GroupsDNQueryGenerator(new HashSet<String>(dnsList));
        return generator;
    }

    private static final Log log = LogFactory.getLog(LdapBrokerCommandBase.class);
}
