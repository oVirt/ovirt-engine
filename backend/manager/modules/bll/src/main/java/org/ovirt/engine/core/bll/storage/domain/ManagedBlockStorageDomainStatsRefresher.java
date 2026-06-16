package org.ovirt.engine.core.bll.storage.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Singleton;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddManagedBlockStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ManagedBlockStorageDao;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Re-invoke GetManagedBlockStorageStats for an MBS domain and persist the
 * returned capacity into storage_domain_dynamic. Used by:
 *   - AddManagedBlockStorageDomainCommand (post-create)
 *   - Backend.initialize() (post-startup, for existing Active domains)
 *   - Disk-mutating MBS commands (post-execute, lazy refresh)
 *
 * Failure is non-fatal: callers continue regardless. Capacity is best-effort
 * eventually-consistent, never load-bearing.
 */
@Singleton
public class ManagedBlockStorageDomainStatsRefresher {

    private static final Logger log = LoggerFactory.getLogger(ManagedBlockStorageDomainStatsRefresher.class);

    @Inject
    private BackendInternal backend;

    @Inject
    private ManagedBlockStorageDao managedBlockStorageDao;

    @Inject
    private StorageDomainDynamicDao storageDomainDynamicDao;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    private enum RefreshState {
        RUNNING,
        RERUN_REQUESTED
    }

    private final ConcurrentHashMap<Guid, RefreshState> refreshStates = new ConcurrentHashMap<>();

    public void refresh(Guid storageDomainId) {
        if (storageDomainId == null) {
            return;
        }
        // Claim the per-domain slot - if a refresh is already in flight it may
        // have sampled stats before the caller's operation took effect, so
        // request a trailing rerun instead of scheduling a concurrent one
        if (refreshStates.compute(storageDomainId,
                (id, state) -> state == null ? RefreshState.RUNNING : RefreshState.RERUN_REQUESTED)
                == RefreshState.RUNNING) {
            executor.execute(() -> runRefreshLoop(storageDomainId));
        }
    }

    private void runRefreshLoop(Guid storageDomainId) {
        try {
            while (true) {
                doRefresh(storageDomainId);
                if (refreshStates.remove(storageDomainId, RefreshState.RUNNING)) {
                    return;
                }
                refreshStates.put(storageDomainId, RefreshState.RUNNING);
            }
        } catch (Throwable t) {
            refreshStates.remove(storageDomainId);
            throw t;
        }
    }

    private void doRefresh(Guid storageDomainId) {
        try {
            ManagedBlockStorage mbs = managedBlockStorageDao.get(storageDomainId);
            if (mbs == null) {
                log.warn("MBS domain '{}' has no driver options row; skipping stats refresh",
                        storageDomainId);
                return;
            }
            if (mbs.getDriverOptions() == null && mbs.getDriverSensitiveOptions() == null) {
                log.warn("MBS domain '{}' has no driver options; skipping stats refresh",
                        storageDomainId);
                return;
            }
            AddManagedBlockStorageDomainParameters params =
                    new AddManagedBlockStorageDomainParameters();
            params.setStorageDomainId(storageDomainId);
            params.setDriverOptions(mbs.getDriverOptions());
            params.setDriverSensitiveOptions(mbs.getDriverSensitiveOptions());

            ActionReturnValue rv = backend.runInternalAction(
                    ActionType.GetManagedBlockStorageStats, params);
            if (rv == null || !rv.getSucceeded() || rv.getActionReturnValue() == null) {
                log.warn("Could not retrieve stats for MBS domain '{}'", storageDomainId);
                return;
            }
            Map<String, Object> stats = rv.getActionReturnValue();
            Object totalRaw = stats.get("total_capacity_gb");
            Object freeRaw = stats.get("free_capacity_gb");
            Integer totalGb = parseCapacityGb(totalRaw);
            Integer freeGb = parseCapacityGb(freeRaw);
            if (totalGb == null || freeGb == null) {
                if (isCapacitySentinel(totalRaw) || isCapacitySentinel(freeRaw)) {
                    // expected per the Cinder volume-stats spec - keep the last persisted values
                    log.debug("MBS domain '{}' reports non-numeric capacity (total={}, free={})",
                            storageDomainId, totalRaw, freeRaw);
                } else {
                    log.warn("Stats for MBS domain '{}' missing usable capacity fields (total={}, free={})",
                            storageDomainId, totalRaw, freeRaw);
                }
                return;
            }
            StorageDomainDynamic dynamic = storageDomainDynamicDao.get(storageDomainId);
            boolean isNewRow = dynamic == null;
            if (isNewRow) {
                dynamic = new StorageDomainDynamic();
                dynamic.setId(storageDomainId);
            }
            dynamic.setAvailableDiskSize(freeGb);
            dynamic.setUsedDiskSize(Math.max(0, totalGb - freeGb));
            if (isNewRow) {
                storageDomainDynamicDao.save(dynamic);
            } else {
                storageDomainDynamicDao.update(dynamic);
            }
            log.info("Refreshed capacity for MBS domain '{}': total={}GB free={}GB",
                    storageDomainId, totalGb, freeGb);
        } catch (Exception e) {
            log.warn("Could not refresh stats for MBS domain '{}': {}",
                    storageDomainId, e.getMessage());
        }
    }

    /**
     * Cinder drivers report capacity as a number, a numeric string (possibly
     * fractional), or the sentinels 'unknown' / 'infinite'.
     */
    private static Integer parseCapacityGb(Object v) {
        if (v == null || isCapacitySentinel(v)) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return (int) Double.parseDouble(v.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean isCapacitySentinel(Object v) {
        if (!(v instanceof String)) {
            return false;
        }
        String s = ((String) v).trim();
        return "unknown".equalsIgnoreCase(s) || "infinite".equalsIgnoreCase(s);
    }
}
