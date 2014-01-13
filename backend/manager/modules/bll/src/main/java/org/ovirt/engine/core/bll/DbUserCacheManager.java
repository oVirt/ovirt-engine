package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.authentication.Directory;
import org.ovirt.engine.core.authentication.DirectoryGroup;
import org.ovirt.engine.core.authentication.DirectoryManager;
import org.ovirt.engine.core.authentication.DirectoryUser;
import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.ExternalId;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DbUserDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

public class DbUserCacheManager {
    private static final Log log = LogFactory.getLog(DbUserCacheManager.class);
    private static final DbUserCacheManager _instance = new DbUserCacheManager();
    private boolean initialized = false;
    private final Map<ExternalId, DbGroup> groupsMap = new HashMap<>();

    public static DbUserCacheManager getInstance() {
        return _instance;
    }

    private DbUserCacheManager() {
    }

    public void init() {
        if (!initialized) {
            log.info("Start initializing " + getClass().getSimpleName());

            int mRefreshRate = Config.<Integer> getValue(ConfigValues.UserRefreshRate);
            SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(
                this,
                "refreshAllUsers",
                new Class[] {},
                new Object[] {},
                0,
                mRefreshRate,
                TimeUnit.SECONDS
            );
            initialized = true;
            log.info("Finished initializing " + getClass().getSimpleName());

        }
    }


    /**
     * Load all the users from the database and refresh them.
     */
    @OnTimerMethodAnnotation("refreshAllUsers")
    public void refreshAllUsers() {
        // We will need the DAO:
        DbUserDAO dao = DbFacade.getInstance().getDbUserDao();

        // Retrieve all the users from the database:
        List<DbUser> dbUsers = dao.getAll();
        List<DbGroup> dbGroups = DbFacade.getInstance().getDbGroupDao().getAll();
        for (DbGroup group : dbGroups) {
            groupsMap.put(group.getExternalId(), group);
        }

        // Classify the users by directory. Note that the resulting map may have an entry with a null key, that
        // corresponds to the users whose directory has been removed from the configuration.
        Map<Directory, List<DbUser>> index = new HashMap<>();
        for (DbUser dbUser : dbUsers) {
            Directory key = DirectoryManager.getInstance().getDirectory(dbUser.getDomain());
            List<DbUser> value = index.get(key);
            if (value == null) {
                value = new ArrayList<DbUser>();
                index.put(key, value);
            }
            value.add(dbUser);
        }

        // Refresh the users for each directory:
        List<DbUser> updates = new ArrayList<>();
        for (Map.Entry<Directory, List<DbUser>> entry : index.entrySet()) {
            List<DbUser> refreshed = refreshUsers(entry.getValue(), entry.getKey());
            updates.addAll(refreshed);
        }

        // Actually update the users in the database (note that this should be done with a batch update, but we don't
        // have support for that yet):
        for (DbUser dbUser : updates) {
            dao.update(dbUser);
        }
    }

    /**
     * Refresh a list of users retrieving their data from a given directory.
     *
     * @param dbUsers the list of users to refresh
     * @param directory the directory where the data of the users will be extracted from, it may be {@code null} if the
     *     directory has already been removed from the configuration
     * @return the list of database users that have been actually modified and that need to be updated in the database
     */
    private List<DbUser> refreshUsers(List<DbUser> dbUsers, Directory directory) {
        // Find all the users in the directory using a batch operation to improve performance:
        List<ExternalId> ids = new ArrayList<>(dbUsers.size());
        for (DbUser dbUser : dbUsers) {
            ids.add(dbUser.getExternalId());
        }
        List<DirectoryUser> directoryUsers = null;
        if (directory != null) {
            directoryUsers = directory.findUsers(ids);
        }
        else {
            directoryUsers = Collections.emptyList();
        }

        // Build a map of users indexed by directory id to simplify the next step where we want to find the directory
        // user corresponding to each database user:
        Map<ExternalId, DirectoryUser> index = new HashMap<>();
        for (DirectoryUser directoryUser : directoryUsers) {
            index.put(directoryUser.getId(), directoryUser);
        }

        // For each database user refresh it using the corresponding directory user and collect those users that need to
        // be updated:
        List<DbUser> refreshed = new ArrayList<>();
        for (DbUser dbUser : dbUsers) {
            DirectoryUser directoryUser = index.get(dbUser.getExternalId());
            dbUser = refreshUser(dbUser, directoryUser);
            if (dbUser != null) {
                refreshed.add(dbUser);
            }
        }

        return refreshed;
    }

    /**
     * Detect differences between a user as stored in the database and the same user retrieved from the directory.
     *
     * @param dbUser the database user
     * @param directoryUser the directory user, it may be {@code null} if the user doesn't exist in the directory
     * @return the updated database user if it was actually updated or {@code null} if it doesn't need to be updated
     */
    private DbUser refreshUser(DbUser dbUser, DirectoryUser directoryUser) {
        // If there the user doesn't exist in the directory then we only need to mark the database user as not active
        // if it isn't marked already:
        if (directoryUser == null) {
            if (dbUser.isActive()) {
                log.warnFormat(
                    "User \"{0}\" will be marked as not active as it wasn't found in the directory \"{1}\".",
                    dbUser.getLoginName(), dbUser.getDomain()
                );
                dbUser.setActive(false);
            }
            return dbUser;
        }

        // This flag indicates if there are any differences and thus if the database update should actually be
        // performed:
        boolean update = false;

        // If the user was marked as not active in the database then mark it as active:
        if (!dbUser.isActive()) {
            log.infoFormat(
                "User \"{0}\" will be marked as active as it was found in directory \"{1}\".",
                dbUser.getLoginName(), dbUser.getDomain()
            );
            dbUser.setActive(true);
            update = true;
        }

        // Compare the attributes of the database user with those of the directory, copy those that changed and update
        // the flag that indicates that the database needs to be updated:
        if (!StringUtils.equals(dbUser.getFirstName(), directoryUser.getName())) {
            dbUser.setFirstName(directoryUser.getName());
            update = true;
        }
        if (!StringUtils.equals(dbUser.getLastName(), directoryUser.getLastName())) {
            dbUser.setLastName(directoryUser.getLastName());
            update = true;
        }
        if (!StringUtils.equals(dbUser.getDomain(), directoryUser.getDirectory().getName())) {
            dbUser.setDomain(directoryUser.getDirectory().getName());
            update = true;
        }
        if (!StringUtils.equals(dbUser.getLoginName(), directoryUser.getName())) {
            dbUser.setLoginName(directoryUser.getName());
            update = true;
        }
        if (!StringUtils.equals(dbUser.getDepartment(), directoryUser.getDepartment())) {
            dbUser.setDepartment(directoryUser.getDepartment());
            update = true;
        }
        if (!StringUtils.equals(dbUser.getRole(), directoryUser.getTitle())) {
            dbUser.setRole(directoryUser.getTitle());
            update = true;
        }
        if (!StringUtils.equals(dbUser.getEmail(), directoryUser.getEmail())) {
            dbUser.setEmail(directoryUser.getEmail());
            update = true;
        }

        // Compare the new list of group names and identifiers:
        StringBuilder groupNamesBuffer = new StringBuilder();
        StringBuilder groupIdsBuffer = new StringBuilder();
        boolean first = true;
        for (DirectoryGroup directoryGroup : directoryUser.getGroups()) {
            DbGroup dbGroup = groupsMap.get(directoryGroup.getId());
            if (!first) {
                groupNamesBuffer.append(',');
                groupIdsBuffer.append(dbGroup.getId());
            }
            groupNamesBuffer.append(directoryGroup.getName());
            groupIdsBuffer.append(directoryGroup.getId());
            first = false;
        }
        String groupNames = groupNamesBuffer.toString();
        String groupIds = groupIdsBuffer.toString();
        if (!StringUtils.equals(dbUser.getGroupNames(), groupNames)) {
            dbUser.setGroupNames(groupNames);
            update = true;
        }
        if (!StringUtils.equals(dbUser.getGroupIds(), groupIds)) {
            dbUser.setGroupIds(groupIds);
            update = true;
        }

        return update? dbUser: null;
    }
}
