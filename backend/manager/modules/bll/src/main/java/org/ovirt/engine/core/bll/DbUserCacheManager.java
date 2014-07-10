package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

public class DbUserCacheManager {
    private static final Log log = LogFactory.getLog(DbUserCacheManager.class);
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
      SyncUsers.sync(DbFacade.getInstance().getDbUserDao().getAll());
    }

}
