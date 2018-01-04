package org.ovirt.engine.core.bll.storage.pool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StoragePoolStatusHandler {
    private static final Logger log = LoggerFactory.getLogger(StoragePoolStatusHandler.class);

    private static final Map<Guid, StoragePoolStatusHandler> nonOperationalPools = new HashMap<>();

    private final Guid poolId;
    private ScheduledFuture scheduledTask;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService schedulerService;

    private StoragePoolStatusHandler(Guid poolId) {
        this.poolId = poolId;
        this.scheduledTask = null;
    }

    private StoragePoolStatusHandler scheduleTimeout() {
        scheduledTask = schedulerService.schedule(this::handleTimeout,
                Config.<Long>getValue(ConfigValues.StoragePoolNonOperationalResetTimeoutInMin),
                TimeUnit.MINUTES);

        return this;
    }

    private void deScheduleTimeout() {
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            scheduledTask = null;
        }
    }

    private void handleTimeout() {
        if (nonOperationalPools.containsKey(poolId)) {
            try {
                StoragePool pool = DbFacade.getInstance().getStoragePoolDao().get(poolId);
                if (pool != null && pool.getStatus() == StoragePoolStatus.NotOperational) {
                    nonOperationalPoolTreatment(pool);
                }
            } catch (Exception ignore) {
            }
        }
    }

    public static void poolStatusChanged(Guid poolId, StoragePoolStatus status) {
        if (nonOperationalPools.containsKey(poolId) && status != StoragePoolStatus.NotOperational) {
            StoragePoolStatusHandler handler = nonOperationalPools.get(poolId);

            if (handler != null) {
                synchronized (handler) {
                    handler.deScheduleTimeout();
                }
            }
            synchronized (nonOperationalPools) {
                nonOperationalPools.remove(poolId);
            }
        } else if (status == StoragePoolStatus.NotOperational) {
            synchronized (nonOperationalPools) {
                final StoragePoolStatusHandler storagePoolStatusHandler =
                        Injector.injectMembers(new StoragePoolStatusHandler(poolId));
                nonOperationalPools.put(poolId, storagePoolStatusHandler.scheduleTimeout());
            }
        }
    }

    private static void nonOperationalPoolTreatment(StoragePool pool) {
        boolean changeStatus = false;
        if (!getAllRunningVdssInPool(pool).isEmpty()) {
            changeStatus = true;
        }
        if (changeStatus) {
            log.info("Moving data center '{}' with Id '{}' to status Problematic from status NotOperational on a one"
                    + " time basis to try to recover",
                    pool.getName(),
                    pool.getId());
            Backend.getInstance().runInternalAction(
                    ActionType.SetStoragePoolStatus,
                    new SetStoragePoolStatusParameters(pool.getId(), StoragePoolStatus.NonResponsive,
                            AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC_FROM_NON_OPERATIONAL));
            synchronized (nonOperationalPools) {
                nonOperationalPools.remove(pool.getId());
            }
        }
    }

    private static List<VDS> getAllRunningVdssInPool(StoragePool pool) {
        return DbFacade.getInstance().getVdsDao().getAllForStoragePoolAndStatus(pool.getId(), VDSStatus.Up);
    }


    public static void init() {
        List<StoragePool> allPools = DbFacade.getInstance().getStoragePoolDao().getAll();
        for (StoragePool pool : allPools) {
            if (pool.getStatus() == StoragePoolStatus.NotOperational) {
                poolStatusChanged(pool.getId(), StoragePoolStatus.NotOperational);
            }
        }
    }
}
