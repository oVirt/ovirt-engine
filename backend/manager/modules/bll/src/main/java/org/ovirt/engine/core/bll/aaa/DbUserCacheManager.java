package org.ovirt.engine.core.bll.aaa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbUserCacheManager {
    private static final Logger log = LoggerFactory.getLogger(DbUserCacheManager.class);
    private static final DbUserCacheManager _instance = new DbUserCacheManager();
    private boolean initialized = false;
    private final Map<String, DbGroup> groupsMap = new HashMap<>();

    public static DbUserCacheManager getInstance() {
        return _instance;
    }

    private DbUserCacheManager() {
    }

    public void init() {
        if (!initialized) {
            log.info("Start initializing {}", getClass().getSimpleName());

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
            log.info("Finished initializing {}", getClass().getSimpleName());

        }
    }


    /**
     * Load all the users from the database and refresh them.
     */
    @OnTimerMethodAnnotation("refreshAllUsers")
    public void refreshAllUsers() {
        List<DbUser> activeUsers = new ArrayList<>();
        for (DbUser dbUser : DbFacade.getInstance().getDbUserDao().getAll()) {
            if (dbUser.isActive()) {
                activeUsers.add(dbUser);
            }
        }
        for (DbUser user : SyncUsers.sync(activeUsers)) {
            DbFacade.getInstance().getDbUserDao().update(user);
        }
    }

}
