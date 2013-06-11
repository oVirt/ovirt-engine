package org.ovirt.engine.core.bll.adbroker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.session.SessionDataContainer;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public abstract class LdapBrokerCommandBase extends BrokerCommandBase {
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
        IVdcUser curUser;
        if (StringUtils.isEmpty(getParameters().getSessionId())) {
            curUser = SessionDataContainer.getInstance().getUser(false);
        } else {
            curUser = SessionDataContainer.getInstance().getUser(getParameters().getSessionId(), false);
        }
        // verify that in auto login mode , user is not taken from session.
        if (curUser != null && !StringUtils.isEmpty(curUser.getPassword())) {
            setLoginName(curUser.getUserName());
            setPassword(curUser.getPassword());
            setAuthenticationDomain(curUser.getDomainControler());
        } else {
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
    }

    @Override
    public LdapReturnValueBase execute() {
        boolean exceptionOccured = false;
        try {
            log.debugFormat("Running LDAP command: {0}", getClass().getName());
            String loginNameForKerberos =
                    LdapBrokerUtils.modifyLoginNameForKerberos(getLoginName(), getAuthenticationDomain());
            LdapCredentials ldapCredentials = new LdapCredentials(loginNameForKerberos, getPassword());
            DirectorySearcher directorySearcher = new DirectorySearcher(ldapCredentials);
            executeQuery(directorySearcher);
            exceptionOccured = directorySearcher.getException() != null;
        } catch (RuntimeException e) {
            log.error(String.format("Failed to run command %s. Domain is %s. User is %s.",
                    getClass().getSimpleName(), getDomain(), getLoginName()));
        }
 finally {
            if (exceptionOccured) {
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
        if (user == null) {
            return null;
        }
        user.setDomainControler(domain);

        // Getting the groups
        java.util.HashMap<String, LdapGroup> groupsDict = new java.util.HashMap<String, LdapGroup>();

        GroupsDNQueryGenerator generator = new GroupsDNQueryGenerator();
        proceedGroupsSearchResult(user.getMemberof(), groupsDict, generator);

        user.setGroups(groupsDict);
        if (user.getUserName() != null && !user.getUserName().contains("@")) {
            user.setUserName(user.getUserName() + "@" + user.getDomainControler());
        }
        return user;
    }

    protected void PopulateGroup(LdapQueryData queryData,
            String domain,
            java.util.Map<String, LdapGroup> groupsDict,
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
                    PopulateGroup(partialQuery, domain, groupsDict, loginName, password);
                }
            }
        } catch (RuntimeException e) {
            log.infoFormat("PopulateGroup failed. Exception: {0}", e);
        }
    }

    private void ProceedGroupsSearchResult(GroupSearchResult groupsResult,
            java.util.Map<String, LdapGroup> groupsDict, GroupsDNQueryGenerator generator) {
        List<String> groupsList = groupsResult.getMemberOf();
        proceedGroupsSearchResult(groupsList, groupsDict, generator);
    }

    private void proceedGroupsSearchResult(List<String> groupDNList,
            Map<String, LdapGroup> groupsDict, GroupsDNQueryGenerator generator) {
        if (groupDNList == null) {
            return;
        }
        for (String groupDN : groupDNList) {
            String groupName = LdapBrokerUtils.generateGroupDisplayValue(groupDN);
            if (!groupsDict.containsKey(groupName)) {
                LdapGroup group = DbFacade.getInstance().getAdGroupDao().getByName(groupName);
                if (group == null) {
                    group = new LdapGroup();
                    group.setname(groupName);
                }
                group.setDistinguishedName(groupDN);
                groupsDict.put(groupName, group);
                generator.add(groupDN);
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

    private static Log log = LogFactory.getLog(LdapBrokerCommandBase.class);
}
