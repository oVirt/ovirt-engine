package org.ovirt.engine.core.bll.storage.pool;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class StoragePoolStatusHandler implements BackendService {
    private static final Logger log = LoggerFactory.getLogger(StoragePoolStatusHandler.class);

    private final Map<Guid, ScheduledFuture<?>> nonOperationalPools = new ConcurrentHashMap<>();

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService schedulerService;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private VdsDao vdsDao;
    @Inject
    private BackendInternal backend;

    private ScheduledFuture<?> scheduleTimeout(Guid poolId) {
        return schedulerService.schedule(() -> handleTimeout(poolId),
                Config.<Long>getValue(ConfigValues.StoragePoolNonOperationalResetTimeoutInMin),
                TimeUnit.MINUTES);
    }

    private void deScheduleTimeout(Guid poolId) {
        // The key will be removed from the map since the function returns null
        nonOperationalPools.computeIfPresent(poolId, (key, scheduledTask) -> {
            scheduledTask.cancel(true);
            return null;
        });
    }

    private void handleTimeout(Guid poolId) {
        if (nonOperationalPools.containsKey(poolId)) {
            try {
                StoragePool pool = storagePoolDao.get(poolId);
                if (pool != null && pool.getStatus() == StoragePoolStatus.NotOperational) {
                    nonOperationalPoolTreatment(pool);
                }
            } catch (Exception ignore) {
            }
        }
    }

    public void poolStatusChanged(Guid poolId, StoragePoolStatus status) {
        if (nonOperationalPools.containsKey(poolId) && status != StoragePoolStatus.NotOperational) {
            deScheduleTimeout(poolId);
        } else if (status == StoragePoolStatus.NotOperational) {
            nonOperationalPools.put(poolId, scheduleTimeout(poolId));
        }
    }

    private void nonOperationalPoolTreatment(StoragePool pool) {
        if (!getAllRunningVdssInPool(pool).isEmpty()) {
            log.info("Moving data center '{}' with Id '{}' to status Problematic from status NotOperational on a one"
                    + " time basis to try to recover",
                    pool.getName(),
                    pool.getId());
            backend.runInternalAction(
                    ActionType.SetStoragePoolStatus,
                    new SetStoragePoolStatusParameters(pool.getId(), StoragePoolStatus.NonResponsive,
                            AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC_FROM_NON_OPERATIONAL));
            nonOperationalPools.remove(pool.getId());
        }
    }

    private List<VDS> getAllRunningVdssInPool(StoragePool pool) {
        return vdsDao.getAllForStoragePoolAndStatus(pool.getId(), VDSStatus.Up);
    }

    @PostConstruct
    public void init() {
        List<StoragePool> allPools = storagePoolDao.getAll();
        for (StoragePool pool : allPools) {
            if (pool.getStatus() == StoragePoolStatus.NotOperational) {
                poolStatusChanged(pool.getId(), StoragePoolStatus.NotOperational);
            }
        }
    }
}
