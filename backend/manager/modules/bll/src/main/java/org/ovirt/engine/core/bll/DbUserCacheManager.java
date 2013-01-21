package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.adbroker.AdActionType;
import org.ovirt.engine.core.bll.adbroker.LdapBrokerUtils;
import org.ovirt.engine.core.bll.adbroker.LdapFactory;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByIdParameters;
import org.ovirt.engine.core.bll.adbroker.LdapSearchByUserIdListParameters;
import org.ovirt.engine.core.bll.adbroker.UsersDomainsCacheManagerService;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.businessentities.LdapRefStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

public class DbUserCacheManager {
    private static Log log = LogFactory.getLog(DbUserCacheManager.class);
    private static DbUserCacheManager _instance = new DbUserCacheManager();
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
            return domains.contains(t.getdomain());
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
            // clean all user sessions in DB
            DbFacade.getInstance().getDbUserDao().removeAllSessions();

            int mRefreshRate = Config.<Integer> GetValue(ConfigValues.UserRefreshRate);
            jobId = SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this, "OnTimer", new Class[] {},
                    new Object[] {}, 0, mRefreshRate, TimeUnit.SECONDS);
            initialized = true;
            log.info("Finished initializing " + getClass().getSimpleName());

        }
    }

    @Override
    protected void finalize() throws Throwable {
        Dispose();
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
    private void updateDBUserFromADUser(DbUser dbUser, LdapUser ldapUser, HashSet<Guid> updatedUsers) {
        boolean succeded = false;

        if ((ldapUser == null) || (ldapUser.getUserId().equals(Guid.Empty))
                || (!ldapUser.getUserId().equals(dbUser.getuser_id()))) {
            if (dbUser.getstatus() != 0) {
                log.warnFormat("User {0} not found in directory server, its status switched to InActive",
                        dbUser.getname());
                dbUser.setstatus(0);
                succeded = true;
            }
        } else {
            if (dbUser.getstatus() == 0) {
                log.warnFormat("Inactive User {0} found in directory server, its status switched to Active",
                        dbUser.getname());
                dbUser.setstatus(1);
                succeded = true;
            }
            if (!StringUtils.equals(dbUser.getname(), ldapUser.getName())) {
                dbUser.setname(ldapUser.getName());
                succeded = true;
            }
            if (!StringUtils.equals(dbUser.getsurname(), ldapUser.getSurName())) {
                dbUser.setsurname(ldapUser.getSurName());
                succeded = true;
            }
            if (!StringUtils.equals(dbUser.getdomain(), ldapUser.getDomainControler())) {
                dbUser.setdomain(ldapUser.getDomainControler());
                succeded = true;
            }
            if (!StringUtils.equals(dbUser.getusername(), ldapUser.getUserName())) {
                dbUser.setusername(ldapUser.getUserName());
                succeded = true;
            }
            if (!StringUtils.equals(dbUser.getgroups(), ldapUser.getGroup())) {
                dbUser.setgroups(ldapUser.getGroup());
                succeded = true;
                updatedUsers.add(dbUser.getuser_id());
            }
            if (!StringUtils.equals(dbUser.getdepartment(), ldapUser.getDepartment())) {
                dbUser.setdepartment(ldapUser.getDepartment());
                succeded = true;
            }
            if (!StringUtils.equals(dbUser.getrole(), ldapUser.getTitle())) {
                dbUser.setrole(ldapUser.getTitle());
                succeded = true;
            }
            if (!StringUtils.equals(dbUser.getemail(), ldapUser.getEmail())) {
                dbUser.setemail(ldapUser.getEmail());
                succeded = true;
            }
            if (!StringUtils.equals(dbUser.getGroupIds(), ldapUser.getGroupIds())) {
                dbUser.setGroupIds(ldapUser.getGroupIds());
                succeded = true;
            }
            if (succeded) {
                dbUser.setstatus(dbUser.getstatus() + 1);
            }
        }
        if (succeded) {
            DbFacade.getInstance().getDbUserDao().update(dbUser);
        } else {
        }
    }

    public void refreshAllUserData(List<LdapGroup> updatedGroups) {
        try {
            log.info("Start refreshing all users data");
            List<DbUser> allUsers = DbFacade.getInstance().getDbUserDao().getAll();

            List<String> domainsList = LdapBrokerUtils.getDomainsList(true);
            List<DbUser> filteredUsers = LinqUtils.filter(allUsers, new UsersPerDomainPredicate(domainsList));
            java.util.HashMap<String, java.util.HashMap<Guid, DbUser>> userByDomains =
                    new java.util.HashMap<String, java.util.HashMap<Guid, DbUser>>();

            /**
             * Filter all users by domains
             */
            for (DbUser user : filteredUsers) {
                java.util.HashMap<Guid, DbUser> domainUser;
                if (!userByDomains.containsKey(user.getdomain())) {
                    domainUser = new java.util.HashMap<Guid, DbUser>();
                    userByDomains.put(user.getdomain(), domainUser);
                } else {
                    domainUser = userByDomains.get(user.getdomain());
                }
                domainUser.put(user.getuser_id(), user);
            }

            if (userByDomains.size() != 0) {
                /**
                 * refresh users in each domain separately
                 */
                for (String domain : userByDomains.keySet()) {
                    java.util.ArrayList<LdapUser> adUsers =
                            (java.util.ArrayList<LdapUser>) LdapFactory.getInstance(domain)
                            .RunAdAction(
                                    AdActionType.GetAdUserByUserIdList,
                                    new LdapSearchByUserIdListParameters(domain, new java.util.ArrayList<Guid>(userByDomains
                                            .get(domain).keySet()),false)).getReturnValue();
                    HashSet<Guid> updatedUsers = new HashSet<Guid>();
                    if (adUsers == null) {
                        log.warn("No users returned from directory server during refresh users");
                    } else {
                        LdapBrokerUtils.performGroupPopulationForUsers(adUsers,domain,updatedGroups);
                        for (LdapUser adUser : adUsers) {
                            updateDBUserFromADUser(userByDomains.get(domain).get(adUser.getUserId()), adUser, updatedUsers);
                            userByDomains.get(domain).remove(adUser.getUserId());
                        }
                    }
                    Collection<DbUser> usersForDomain = userByDomains.get(domain).values();
                    if (usersForDomain == null) {
                        log.warnFormat("No users for domain {0}",domain);
                    } else {
                        for (DbUser dbUser : usersForDomain) {
                            if (dbUser.getstatus() != 0) {
                                log.warnFormat("User {0} not found in directory sevrer, its status switched to InActive",
                                        dbUser.getname());
                                dbUser.setstatus(AsyncTaskStatusEnum.unknown.getValue());
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

    @OnTimerMethodAnnotation("OnTimer")
    public void OnTimer() {
        List<LdapGroup> groups = updateGroups();
        refreshAllUserData(groups);
    }

    private List<LdapGroup> updateGroups() {
        List<LdapGroup> groups = DbFacade.getInstance().getAdGroupDao().getAll();
        for (LdapGroup group : groups) {
            /**
             * Vitaly workaround. Temporary treatment on missing group domains
             */

            // Waiting for the GUI team to fix the ad_group class. When the
            // class is fixed,
            // domain name will be passed correctly to the backend, and the
            // following code should not occur
            if (group.getdomain() == null && group.getname().contains("@")) {
                StringBuilder logMsg = new StringBuilder();
                logMsg.append("domain name for ad group ")
                        .append(group.getname())
                        .append(" is null. This should not occur, please check that domain name is passed corectly from client");
                log.warn(logMsg.toString());
                String partAfterAtSign = group.getname().split("[@]", -1)[1];
                String newDomainName = partAfterAtSign;
                if (partAfterAtSign.contains("/")) {
                    String partPreviousToSlashSign = partAfterAtSign.split("[/]", -1)[0];
                    newDomainName = partPreviousToSlashSign;

                }

                group.setdomain(newDomainName);
            }
            // We check if the domain is null or empty for internal groups.
            // An internal group does not have a domain, and there is no need to query
            // the ldap server for it. Note that if we will add support in the future for
            // domain-less groups in the ldap server then this code will have to change in order
            // to fetch for them
            if (group.getdomain() != null && !group.getdomain().isEmpty()) {
                if (UsersDomainsCacheManagerService.getInstance().getDomain(group.getdomain()) == null) {
                    log.errorFormat("Cannot query for group {0} from domain {1} because the domain is not configured. Please use the manage domains utility if you wish to add this domain.",
                            group.getname(),
                            group.getdomain());
                } else {
                    LdapGroup groupFromAD =
                            (LdapGroup) LdapFactory
                                    .getInstance(group.getdomain())
                                    .RunAdAction(AdActionType.GetAdGroupByGroupId,
                                            new LdapSearchByIdParameters(group.getdomain(), group.getid()))
                                    .getReturnValue();

                    if (group.getstatus() == LdapRefStatus.Active
                                && (groupFromAD == null || groupFromAD.getstatus() == LdapRefStatus.Inactive)) {
                        group.setstatus(LdapRefStatus.Inactive);
                        DbFacade.getInstance().getAdGroupDao().update(group);
                    } else if (groupFromAD != null
                                && (!StringUtils.equals(group.getname(), groupFromAD.getname())
                                        || group.getstatus() != groupFromAD
                                                .getstatus() || !StringUtils.equals(group.getDistinguishedName(),
                                        groupFromAD.getDistinguishedName()))) {
                        DbFacade.getInstance().getAdGroupDao().update(groupFromAD);
                    }
                    // memberOf is not persistent and should be set in the returned groups list from the LDAP queries
                    if (groupFromAD != null) {
                        group.setMemberOf(groupFromAD.getMemberOf());
                    }
                }
            }
        }
        return groups;

    }

    public void Dispose() {
        if (jobId != null) {
            SchedulerUtilQuartzImpl.getInstance().deleteJob(jobId);
        }
    }

}
