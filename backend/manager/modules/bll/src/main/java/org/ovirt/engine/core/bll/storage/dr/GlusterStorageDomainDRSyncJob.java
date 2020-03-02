package org.ovirt.engine.core.bll.storage.dr;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.OnTimerMethodAnnotation;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.GlusterStorageSyncCommandParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlusterStorageDomainDRSyncJob {

    private static final Logger log = LoggerFactory.getLogger(GlusterStorageDomainDRSyncJob.class);

    private final StorageDomainDao storageDomainDao;

    private final GlusterGeoRepDao geoRepDao;

    private final BackendInternal backend;

    public GlusterStorageDomainDRSyncJob() {
        super();
        // The @Inject annotation does not work when the GlusterStorageDomainDRSyncJob
        // is instantiated as part of Quartz trigger - even when the class passed to
        // quartz is instantiated using Injector.injectMembers.
        // TBD - change when quartz classes use CDI too
        backend = Injector.get(BackendInternal.class);
        storageDomainDao = Injector.get(StorageDomainDao.class);
        geoRepDao = Injector.get(GlusterGeoRepDao.class);
    }

    @OnTimerMethodAnnotation("syncData")
    public void syncData(String storageDomainId, String geoRepSessionId) {
        try {
            // Get storage domain and georep session
            StorageDomain storageDomain = storageDomainDao.get(new Guid(storageDomainId));
            if (storageDomain == null) {
                log.error("No storage domain found for id '{}'", storageDomainId);
                return;
            }
            GlusterGeoRepSession session = geoRepDao.getById(new Guid(geoRepSessionId));
            if (session == null) {
                log.error("No geo-replication session found for id '{}'", geoRepSessionId);
                return;
            }
            backend.runInternalAction(ActionType.GlusterStorageSync,
                    new GlusterStorageSyncCommandParameters(storageDomain.getId(), session.getId()),
                    ExecutionHandler.createInternalJobContext());
        } catch (Exception e) {
            log.error("Error running dr sync", e);
        }
    }
}
