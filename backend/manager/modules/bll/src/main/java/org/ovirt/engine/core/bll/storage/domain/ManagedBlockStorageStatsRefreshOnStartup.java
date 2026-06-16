package org.ovirt.engine.core.bll.storage.domain;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
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
            List<Guid> domainIds = storageDomainDao.getAll().stream()
                    .filter(domain -> domain.getStorageType() == StorageType.MANAGED_BLOCK_STORAGE)
                    .filter(domain -> domain.getStatus() == StorageDomainStatus.Active)
                    .map(StorageDomain::getId)
                    .distinct()
                    .collect(Collectors.toList());
            domainIds.forEach(refresher::refresh);
            if (!domainIds.isEmpty()) {
                log.info("Triggered startup capacity refresh for {} Active MBS domain(s)",
                        domainIds.size());
            }
        } catch (Exception e) {
            log.warn("Could not refresh MBS domain stats at startup: {}", e.getMessage());
        }
    }
}
