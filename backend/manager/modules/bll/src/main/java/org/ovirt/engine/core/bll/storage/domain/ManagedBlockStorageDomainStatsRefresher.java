package org.ovirt.engine.core.bll.storage.domain;

import java.util.Map;

import javax.ejb.Singleton;
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

    public void refresh(Guid storageDomainId) {
        if (storageDomainId == null) {
            return;
        }
        try {
            ManagedBlockStorage mbs = managedBlockStorageDao.get(storageDomainId);
            if (mbs == null) {
                log.warn("MBS domain '{}' has no driver options row; skipping stats refresh",
                        storageDomainId);
                return;
            }
            AddManagedBlockStorageDomainParameters params =
                    new AddManagedBlockStorageDomainParameters();
            params.setStorageDomainId(storageDomainId);
            params.setDriverOptions(mbs.getDriverOptions());
            params.setSriverSensitiveOptions(mbs.getDriverSensitiveOptions());

            ActionReturnValue rv = backend.runInternalAction(
                    ActionType.GetManagedBlockStorageStats, params);
            if (rv == null || !rv.getSucceeded() || rv.getActionReturnValue() == null) {
                log.warn("Could not retrieve stats for MBS domain '{}'", storageDomainId);
                return;
            }
            Map<String, Object> stats = rv.getActionReturnValue();
            Integer totalGb = toIntOrNull(stats.get("total_capacity_gb"));
            Integer freeGb = toIntOrNull(stats.get("free_capacity_gb"));
            if (totalGb == null || freeGb == null) {
                log.warn("Stats for MBS domain '{}' missing capacity fields: {}",
                        storageDomainId, stats);
                return;
            }
            StorageDomainDynamic dynamic = storageDomainDynamicDao.get(storageDomainId);
            if (dynamic == null) {
                dynamic = new StorageDomainDynamic();
                dynamic.setId(storageDomainId);
            }
            dynamic.setAvailableDiskSize(freeGb);
            dynamic.setUsedDiskSize(totalGb - freeGb);
            storageDomainDynamicDao.update(dynamic);
            log.info("Refreshed capacity for MBS domain '{}': total={}GB free={}GB",
                    storageDomainId, totalGb, freeGb);
        } catch (Exception e) {
            log.warn("Could not refresh stats for MBS domain '{}': {}",
                    storageDomainId, e.getMessage());
        }
    }

    private static Integer toIntOrNull(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
