package org.ovirt.engine.core.bll.storage.domain;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refresh capacity stats for every Active Managed Block Storage domain
 * once at engine startup. Without this, MBS domains show stale or [N/A]
 * capacity in the UI after an engine restart, because the SPM-style
 * storage monitor does not cover MBS domains.
 *
 * Runs after Backend.@PostConstruct via @DependsOn("Backend"), so the
 * helper's call into BackendInternal.runInternalAction is safe (no
 * recursive entry into Backend's @PostConstruct).
 *
 * Per-domain failures are non-fatal: each is best-effort.
 */
@Singleton
@Startup
@DependsOn("Backend")
public class ManagedBlockStorageStatsRefreshOnStartup {

    private static final Logger log = LoggerFactory.getLogger(ManagedBlockStorageStatsRefreshOnStartup.class);

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private ManagedBlockStorageDomainStatsRefresher refresher;

    @PostConstruct
    public void refreshAll() {
        try {
            List<StorageDomain> domains = storageDomainDao.getAll();
            if (domains == null) {
                return;
            }
            int refreshed = 0;
            for (StorageDomain domain : domains) {
                if (domain.getStorageType() != StorageType.MANAGED_BLOCK_STORAGE) {
                    continue;
                }
                if (domain.getStatus() != StorageDomainStatus.Active) {
                    continue;
                }
                refresher.refresh(domain.getId());
                refreshed++;
            }
            if (refreshed > 0) {
                log.info("Triggered startup capacity refresh for {} Active MBS domain(s)",
                        refreshed);
            }
        } catch (Exception e) {
            log.warn("Could not refresh MBS domain stats at startup: {}", e.getMessage());
        }
    }
}
