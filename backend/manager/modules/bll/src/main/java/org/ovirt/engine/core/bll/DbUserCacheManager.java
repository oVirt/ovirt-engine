package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapBrokerUtils;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByIdParameters;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByUserIdListParameters;
import org.ovirt.engine.core.bll.adbroker.UsersDomainsCacheManagerService;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

public class DbUserCacheManager {
    private static final Log log = LogFactory.getLog(DbUserCacheManager.class);
    private static final DbUserCacheManager _instance = new DbUserCacheManager();
    private String jobId;
    private boolean initialized = false;

    private static class UsersPerDomainPredicate implements Predicate<DbUser> {

        private final List<String> domains;

        public UsersPerDomainPredicate(List<String> domains) {
            this.domains = domains;
        }

        @Override
        public boolean eval(DbUser t) {

            // The predicate is used to filter out users which are not in one of
            // the domains that is defined by the "DomainName" configuration
            // value
            return domains.contains(t.getDomain());
        }

    }

    public static DbUserCacheManager getInstance() {
        return _instance;
    }

    private DbUserCacheManager() {
    }

    public void init() {
        if (!initialized) {
            log.info("Start initializing " + getClass().getSimpleName());

            int mRefreshRate = Config.<Integer> getValue(ConfigValues.UserRefreshRate);
            jobId = SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this, "onTimer", new Class[] {},
                    new Object[] {}, 0, mRefreshRate, TimeUnit.SECONDS);
            initialized = true;
            log.info("Finished initializing " + getClass().getSimpleName());

        }
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();
    }

    /**
     * detect differences between current DB users and the directory server users/groups and persist them
     *
     * @param dbUser
     *            DB user
     * @param ldapUser
     *            LDAP user
     * @param updatedUsers
     *            list of changed users.
     */
    private static void updateDBUserFromADUser(DbUser dbUser, LdapUser ldapUser, HashSet<Guid> updatedUsers) {
        boolean succeeded = false;

        if ((ldapUser == null) || (ldapUser.getUserId().equals(Guid.Empty))
                || (!ldapUser.getUserId().equals(dbUser.getId()))) {
            if (dbUser.isActive()) {
                log.warnFormat("User {0} not found in directory server, its status switched to InActive",
                        dbUser.getFirstName());
                dbUser.setActive(false);
                succeeded = true;
            }
        } else {
            if (!dbUser.isActive()) {
                log.warnFormat("Inactive User {0} found in directory server, its status switched to Active",
                        dbUser.getFirstName());
                dbUser.setActive(true);
                succeeded = true;
            }
            if (!StringUtils.equals(dbUser.getFirstName(), ldapUser.getName())) {
                dbUser.setFirstName(ldapUser.getName());
                succeeded = true;
            }
            if (!StringUtils.equals(dbUser.getLastName(), ldapUser.getSurName())) {
                dbUser.setLastName(ldapUser.getSurName());
                succeeded = true;
            }
            if (!StringUtils.equals(dbUser.getDomain(), ldapUser.getDomainControler())) {
                dbUser.setDomain(ldapUser.getDomainControler());
                succeeded = true;
            }
            if (!StringUtils.equals(dbUser.getLoginName(), ldapUser.getUserName())) {
                dbUser.setLoginName(ldapUser.getUserName());
                succeeded = true;
            }
            if (!StringUtils.equals(dbUser.getGroupNames(), ldapUser.getGroup())) {
                dbUser.setGroupNames(ldapUser.getGroup());
                succeeded = true;
                updatedUsers.add(dbUser.getId());
            }
            if (!StringUtils.equals(dbUser.getDepartment(), ldapUser.getDepartment())) {
                dbUser.setDepartment(ldapUser.getDepartment());
                succeeded = true;
            }
            if (!StringUtils.equals(dbUser.getRole(), ldapUser.getTitle())) {
                dbUser.setRole(ldapUser.getTitle());
                succeeded = true;
            }
            if (!StringUtils.equals(dbUser.getEmail(), ldapUser.getEmail())) {
                dbUser.setEmail(ldapUser.getEmail());
                succeeded = true;
            }
            if (!StringUtils.equals(dbUser.getGroupIds(), ldapUser.getGroupIds())) {
                dbUser.setGroupIds(ldapUser.getGroupIds());
                succeeded = true;
            }
        }
        if (succeeded) {
            DbFacade.getInstance().getDbUserDao().update(dbUser);
        }
    }

    public void refreshAllUserData(List<DbGroup> updatedGroups) {
        try {
            log.info("Start refreshing all users data");
            List<DbUser> allUsers = DbFacade.getInstance().getDbUserDao().getAll();

            List<String> domainsList = LdapBrokerUtils.getDomainsList(true);
            List<DbUser> filteredUsers = LinqUtils.filter(allUsers, new UsersPerDomainPredicate(domainsList));
            Map<String, Map<Guid, DbUser>> userByDomains = new HashMap<String, Map<Guid, DbUser>>();

            // Map all users by domains
            for (DbUser user : filteredUsers) {
                Map<Guid, DbUser> domainUser;
                if (!userByDomains.containsKey(user.getDomain())) {
                    domainUser = new HashMap<Guid, DbUser>();
                    userByDomains.put(user.getDomain(), domainUser);
                } else {
                    domainUser = userByDomains.get(user.getDomain());
                }
                domainUser.put(user.getId(), user);
            }

            if (userByDomains.size() != 0) {
                // Refresh users in each domain separately
                for (Map.Entry<String, Map<Guid, DbUser>> entry : userByDomains.entrySet()) {
                    String domain = entry.getKey();
                    List<LdapUser> adUsers =
                            (List<LdapUser>) LdapFactory.getInstance(domain)
                            .runAdAction(
                                    AdActionType.GetAdUserByUserIdList,
                                    new LdapSearchByUserIdListParameters(domain,
                                            new ArrayList<Guid>(entry.getValue().keySet()),
                                            false))
                                    .getReturnValue();
                    HashSet<Guid> updatedUsers = new HashSet<Guid>();
                    if (adUsers == null) {
                        log.warn("No users returned from directory server during refresh users");
                    } else {
                        List<LdapGroup> ldapUpdatedGroups = new ArrayList<>(updatedGroups.size());
                        for (DbGroup dbGroup : updatedGroups) {
                            LdapGroup ldapGroup = new LdapGroup(dbGroup);
                            ldapUpdatedGroups.add(ldapGroup);
                        }
                        LdapBrokerUtils.performGroupPopulationForUsers(adUsers, domain, ldapUpdatedGroups);
                        for (LdapUser adUser : adUsers) {
                            updateDBUserFromADUser(userByDomains.get(domain).get(adUser.getUserId()), adUser, updatedUsers);
                            userByDomains.get(domain).remove(adUser.getUserId());
                        }
                    }
                    Collection<DbUser> usersForDomain = entry.getValue().values();
                    if (usersForDomain == null) {
                        log.warnFormat("No users for domain {0}", domain);
                    } else {
                        for (DbUser dbUser : usersForDomain) {
                            if (dbUser.isActive()) {
                                log.warnFormat("User {0} not found in directory server, its status switched to InActive",
                                        dbUser.getFirstName());
                                dbUser.setActive(false);
                                DbFacade.getInstance().getDbUserDao().update(dbUser);
                            }
                        }
                    }
                    // update lastAdminCheckStatus property for users that their
                    // group or role was changed
                    if (updatedUsers.size() > 0) {
                        DbFacade.getInstance().updateLastAdminCheckStatus(updatedUsers.toArray(new Guid[updatedUsers
                                .size()]));
                    }
                }
            }
        } catch (RuntimeException e) {
            log.error("Failed to refresh users data.", e);
        }
    }

    @OnTimerMethodAnnotation("onTimer")
    public void onTimer() {
        List<DbGroup> groups = updateGroups();
        refreshAllUserData(groups);
    }

    private static List<DbGroup> updateGroups() {
        List<DbGroup> groups = DbFacade.getInstance().getDbGroupDao().getAll();
        for (DbGroup group : groups) {
            /*
             * Vitaly workaround. Temporary treatment on missing group domains
             */

            // Waiting for the GUI team to fix the ad_group class. When the
            // class is fixed,
            // domain name will be passed correctly to the backend, and the
            // following code should not occur
            if (group.getDomain() == null && group.getName().contains("@")) {
                StringBuilder logMsg = new StringBuilder();
                logMsg.append("domain name for ad group ")
                        .append(group.getName())
                        .append(" is null. This should not occur, please check that domain name is passed correctly from client");
                log.warn(logMsg.toString());
                String partAfterAtSign = group.getName().split("[@]", -1)[1];
                String newDomainName = partAfterAtSign;
                if (partAfterAtSign.contains("/")) {
                    String partPreviousToSlashSign = partAfterAtSign.split("[/]", -1)[0];
                    newDomainName = partPreviousToSlashSign;

                }

                group.setDomain(newDomainName);
            }
            // We check if the domain is null or empty for internal groups.
            // An internal group does not have a domain, and there is no need to query
            // the ldap server for it. Note that if we will add support in the future for
            // domain-less groups in the ldap server then this code will have to change in order
            // to fetch for them
            if (group.getDomain() != null && !group.getDomain().isEmpty()) {
                if (UsersDomainsCacheManagerService.getInstance().getDomain(group.getDomain()) == null) {
                    log.errorFormat("Cannot query for group {0} from domain {1} because the domain is not configured. Please use the manage domains utility if you wish to add this domain.",
                            group.getName(),
                            group.getDomain());
                } else {
                    LdapGroup groupFromAD =
                            (LdapGroup) LdapFactory
                                    .getInstance(group.getDomain())
                                    .runAdAction(AdActionType.GetAdGroupByGroupId,
                                            new LdapSearchByIdParameters(group.getDomain(), group.getId()))
                                    .getReturnValue();

                    if (group.isActive() && (groupFromAD == null || !groupFromAD.isActive())) {
                        group.setActive(false);
                    } else if (groupFromAD != null
                            && (!StringUtils.equals(group.getName(), groupFromAD.getname())
                                    || group.isActive() != groupFromAD.isActive()
                                    || !StringUtils.equals(group.getDistinguishedName(),
                                    groupFromAD.getDistinguishedName()))) {
                        group = new DbGroup(groupFromAD);
                    }
                    DbFacade.getInstance().getDbGroupDao().update(group);

                    // memberOf is not persistent and should be set in the returned groups list from the LDAP queries
                    if (groupFromAD != null) {
                        group.setMemberOf(new HashSet<>(groupFromAD.getMemberOf()));
                    }
                }
            }
        }
        return groups;

    }

    public void dispose() {
        if (jobId != null) {
            SchedulerUtilQuartzImpl.getInstance().deleteJob(jobId);
        }
    }

}
