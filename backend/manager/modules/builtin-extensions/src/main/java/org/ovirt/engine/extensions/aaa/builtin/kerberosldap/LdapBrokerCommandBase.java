package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.api.extensions.AAAExtensionException.AAAExtensionError;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.utils.ExternalId;
import org.ovirt.engine.core.utils.kerberos.AuthenticationResult;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public abstract class LdapBrokerCommandBase extends BrokerCommandBase {

    protected static final EnumMap<AuthenticationResult, AAAExtensionError> authResultToExceptionMap =
            new EnumMap<>(AuthenticationResult.class);

    static {
        authResultToExceptionMap.put(AuthenticationResult.CANNOT_FIND_LDAP_SERVER_FOR_DOMAIN,
                AAAExtensionError.SERVER_IS_NOT_AVAILABLE);
        authResultToExceptionMap.put(AuthenticationResult.CLIENT_NOT_FOUND_IN_KERBEROS_DATABASE,
                AAAExtensionError.INCORRECT_CREDENTIALS);
        authResultToExceptionMap.put(AuthenticationResult.CLOCK_SKEW_TOO_GREAT,
                AAAExtensionError.GENERAL_ERROR);
        authResultToExceptionMap.put(AuthenticationResult.CONNECTION_ERROR,
                AAAExtensionError.SERVER_IS_NOT_AVAILABLE);
        authResultToExceptionMap.put(AuthenticationResult.CONNECTION_TIMED_OUT,
                AAAExtensionError.TIMED_OUT);
        authResultToExceptionMap.put(AuthenticationResult.DNS_COMMUNICATION_ERROR,
                AAAExtensionError.SERVER_IS_NOT_AVAILABLE);
        authResultToExceptionMap.put(AuthenticationResult.DNS_ERROR,
                AAAExtensionError.SERVER_IS_NOT_AVAILABLE);
        authResultToExceptionMap.put(AuthenticationResult.INTERNAL_KERBEROS_ERROR,
                AAAExtensionError.GENERAL_ERROR);
        authResultToExceptionMap.put(AuthenticationResult.INVALID_CREDENTIALS,
                AAAExtensionError.INCORRECT_CREDENTIALS);
        authResultToExceptionMap.put(AuthenticationResult.NO_KDCS_FOUND,
                AAAExtensionError.SERVER_IS_NOT_AVAILABLE);
        authResultToExceptionMap.put(AuthenticationResult.NO_USER_INFORMATION_WAS_FOUND_FOR_USER,
                AAAExtensionError.INCORRECT_CREDENTIALS);
        authResultToExceptionMap.put(AuthenticationResult.OTHER,
                AAAExtensionError.GENERAL_ERROR);
        authResultToExceptionMap.put(AuthenticationResult.PASSWORD_EXPIRED,
                AAAExtensionError.CREDENTIALS_EXPIRED);
        authResultToExceptionMap.put(AuthenticationResult.USER_ACCOUNT_DISABLED_OR_LOCKED,
                AAAExtensionError.LOCKED_OR_DISABLED_ACCOUNT);
        authResultToExceptionMap.put(AuthenticationResult.WRONG_REALM,
                AAAExtensionError.INCORRECT_CREDENTIALS);
    }

    private Map<String, LdapGroup> globalGroupsDict = new HashMap<>();

    @Override
    protected String getPROTOCOL() {
        return "LDAP://";
    }

    protected LdapBrokerCommandBase(LdapUserPasswordBaseParameters parameters) {
        super(parameters);
        setAuthenticationDomain(getDomain());
    }

    protected LdapBrokerCommandBase(LdapBrokerBaseParameters parameters) {
        super(parameters);
        initCredentials(parameters.getDomain());
    }

    protected void initCredentials(String domain) {
        setUserDomainCredentials(domain);
    }

    protected void setUserDomainCredentials(String domain) {
        Domain domainObject = UsersDomainsCacheManagerService.getInstance().getDomain(domain);
        if (domainObject != null) {
            setLoginName(domainObject.getUserName());
            setPassword(domainObject.getPassword());
            if (getLoginName().contains("@")) {
                String userDomain = getLoginName().split("@")[1].toLowerCase();
                setAuthenticationDomain(userDomain);
            } else {
                setAuthenticationDomain(domain);
            }
        }
    }

    @Override
    public LdapReturnValueBase execute() {
        boolean exceptionOccurred = true;
        try {
            log.debugFormat("Running LDAP command: {0}", getClass().getName());
            String loginNameForKerberos =
                    LdapBrokerUtils.modifyLoginNameForKerberos(getLoginName(), getAuthenticationDomain());
            LdapCredentials ldapCredentials = new LdapCredentials(loginNameForKerberos, getPassword());
            DirectorySearcher directorySearcher = new DirectorySearcher(ldapCredentials);
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

    protected void handleRootDSEFailure(DirectorySearcher directorySearcher) {
        // Supposed to handle rootDSEFailure - default implementation does nothing. Subclasses may override this
        // behavior
    }

    protected abstract void executeQuery(DirectorySearcher directorySearcher);

    protected LdapUser populateUserData(LdapUser user, String domain) {
        return populateUserData(user, domain, true);
    }

    protected LdapUser populateUserData(LdapUser user, String domain, boolean populateGroups) {
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

        if (populateGroups) {
            if (generator.getHasValues()) {
                List<LdapQueryData> partialQueries = generator.getLdapQueriesData();
                for (LdapQueryData currQueryData : partialQueries) {
                    populateGroup(currQueryData,
                            getAuthenticationDomain(),
                            groupsDict,
                            getLoginName(),
                            getPassword());
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
                                 String password) {
        try {
            GroupsDNQueryGenerator generator = new GroupsDNQueryGenerator();
            List<GroupSearchResult> searchResultCollection =
                    LdapBrokerUtils.performGroupQuery(loginName, password, domain, queryData);
            if (searchResultCollection != null) {
                for (GroupSearchResult searchResult : searchResultCollection) {
                    ProceedGroupsSearchResult(searchResult, groupsDict, generator);
                }
            }
            // If generator has results, it means there are parent groups
            if (generator.getHasValues()) {
                List<LdapQueryData> partialQueries = generator.getLdapQueriesData();
                for (LdapQueryData partialQuery : partialQueries) {
                    populateGroup(partialQuery, domain, groupsDict, loginName, password);
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

    private void proceedGroupsSearchResult(ExternalId groupId, List<String> groupDNList,
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
