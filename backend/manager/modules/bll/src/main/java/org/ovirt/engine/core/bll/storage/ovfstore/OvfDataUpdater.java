package org.ovirt.engine.core.bll.storage.ovfstore;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ProcessOvfUpdateParameters;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class OvfDataUpdater implements BackendService {
    private static final Logger log = LoggerFactory.getLogger(OvfDataUpdater.class);

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private BackendInternal backend;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService schedulerService;

    private final ReentrantLock lock = new ReentrantLock();

    private volatile ScheduledFuture updateTimerJob;

    @PostConstruct
    public void initOvfDataUpdater() {
        updateTimerJob = schedulerService.scheduleWithFixedDelay(this::ovfUpdate,
                Config.<Long> getValue(ConfigValues.OvfUpdateIntervalInMinutes),
                Config.<Long> getValue(ConfigValues.OvfUpdateIntervalInMinutes),
                TimeUnit.MINUTES);
        log.info("Initialization of OvfDataUpdater completed successfully.");
    }

    protected void performOvfUpdateForDomain(Guid storagePoolId, Guid domainId) {
        backend.runInternalAction(ActionType.ProcessOvfUpdateForStorageDomain,
                new ProcessOvfUpdateParameters(storagePoolId, domainId));
    }

    protected ActionReturnValue performOvfUpdateForStoragePool(Guid storagePoolId) {
        ProcessOvfUpdateParameters parameters =
                new ProcessOvfUpdateParameters(storagePoolId, null);
        return backend.runInternalAction(ActionType.ProcessOvfUpdateForStoragePool, parameters);
    }

    public void ovfUpdate() {
        lock.lock();
        try {
            List<StoragePool> storagePools = storagePoolDao.getAllByStatus(StoragePoolStatus.Up);
            updateOvfData(storagePools);
        } catch (Throwable t) {
            log.error("Exception updating ovf data: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        } finally {
            lock.unlock();
        }
    }


    public void updateOvfData(List<StoragePool> storagePools) {
        log.info("Attempting to update VMs/Templates Ovf.");
        for (StoragePool pool : storagePools) {
            ActionReturnValue returnValueBase = performOvfUpdateForStoragePool(pool.getId());
            if (!returnValueBase.getSucceeded()) {
                log.error("Exception while trying to update or remove VMs/Templates ovf in Data Center '{}'.", pool.getName());
            }

            log.debug("Attempting to update ovfs in domain in Data Center '{}'",
                    pool.getName());

            Set<Guid> domainsToUpdate = returnValueBase.getActionReturnValue();
            if (domainsToUpdate != null) {
                for (Guid id : domainsToUpdate) {
                    performOvfUpdateForDomain(pool.getId(), id);
                }
            } else {
                log.error("Data Center '{}' domains list for OVF update returned as NULL", pool.getName());
            }
        }
    }

    public void triggerNow() {
        if (updateTimerJob != null) {
            try {
                updateTimerJob.cancel(false);
            } catch (Throwable t) {
                log.debug("Exception cancelling existing job: {}", ExceptionUtils.getRootCauseMessage(t));
            }
        }
        updateTimerJob = schedulerService.scheduleWithFixedDelay(this::ovfUpdate,
                0,
                Config.<Long> getValue(ConfigValues.OvfUpdateIntervalInMinutes),
                TimeUnit.MINUTES);
    }
}
