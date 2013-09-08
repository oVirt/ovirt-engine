package org.ovirt.engine.core.bll.storage;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.SetStoragePoolStatusParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

public final class StoragePoolStatusHandler {
    private static java.util.HashMap<Guid, StoragePoolStatusHandler> _nonOperationalPools =
            new java.util.HashMap<Guid, StoragePoolStatusHandler>();

    private Guid poolId;
    private String timerId;

    private StoragePoolStatusHandler(Guid poolId) {
        this.poolId = poolId;
        timerId = null;
    }

    private SchedulerUtil getScheduler() {
        return SchedulerUtilQuartzImpl.getInstance();
    }

    private StoragePoolStatusHandler scheduleTimeout() {
        Class[] argTypes = new Class[0];
        Object[] args = new Object[0];
        Integer timeout = Config.<Integer> GetValue(ConfigValues.StoragePoolNonOperationalResetTimeoutInMin);

        timerId = getScheduler().scheduleAOneTimeJob(this, "onTimeout", argTypes, args, timeout, TimeUnit.MINUTES);

        return this;
    }

    private void deScheduleTimeout() {
        if (timerId != null) {
            getScheduler().deleteJob(timerId);
            timerId = null;
        }
    }

    @OnTimerMethodAnnotation("onTimeout")
    public void onTimeout() {
        if (_nonOperationalPools.containsKey(poolId)) {
            try {
                StoragePool pool = DbFacade.getInstance().getStoragePoolDao().get(poolId);
                if (pool != null && pool.getstatus() == StoragePoolStatus.NotOperational) {
                    nonOperationalPoolTreatment(pool);
                }
            } catch (java.lang.Exception e) {
            }
        }
    }

    public static void poolStatusChanged(Guid poolId, StoragePoolStatus status) {
        if (_nonOperationalPools.containsKey(poolId) && status != StoragePoolStatus.NotOperational) {
            StoragePoolStatusHandler handler = _nonOperationalPools.get(poolId);

            if (handler != null) {
                synchronized (handler) {
                    handler.deScheduleTimeout();
                }
            }
            synchronized (_nonOperationalPools) {
                _nonOperationalPools.remove(poolId);
            }
        } else if (status == StoragePoolStatus.NotOperational) {
            synchronized (_nonOperationalPools) {
                _nonOperationalPools.put(poolId, new StoragePoolStatusHandler(poolId).scheduleTimeout());
            }
        }
    }

    private static void nonOperationalPoolTreatment(StoragePool pool) {
        boolean changeStatus = false;
        if (StorageHandlingCommandBase.GetAllRunningVdssInPool(pool).size() > 0) {
            changeStatus = true;
        }
        if (changeStatus) {
            log.info("Moving data center " + pool.getName() + " with Id " + pool.getId()
                    + " to status Problematic from status NotOperational on a one time basis to try to recover");
            Backend.getInstance().runInternalAction(
                    VdcActionType.SetStoragePoolStatus,
                    new SetStoragePoolStatusParameters(pool.getId(), StoragePoolStatus.NonResponsive,
                            AuditLogType.SYSTEM_CHANGE_STORAGE_POOL_STATUS_PROBLEMATIC_FROM_NON_OPERATIONAL));
            synchronized (_nonOperationalPools) {
                _nonOperationalPools.remove(pool.getId());
            }
        }
    }

    public static void Init() {
        List<StoragePool> allPools = DbFacade.getInstance().getStoragePoolDao().getAll();
        for (StoragePool pool : allPools) {
            if (pool.getstatus() == StoragePoolStatus.NotOperational) {
                poolStatusChanged(pool.getId(), StoragePoolStatus.NotOperational);
            }
        }
    }

    private static Log log = LogFactory.getLog(StoragePoolStatusHandler.class);
}
