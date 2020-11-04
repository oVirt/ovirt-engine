package org.ovirt.engine.core.bll;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataCenterCompatibilityChecker implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(DataCenterCompatibilityChecker.class);

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    @PostConstruct
    private void init() {
        log.info("Start initializing {}", getClass().getSimpleName());
        executor.scheduleWithFixedDelay(this::checkCompatibility,
                0,
                7,
                TimeUnit.DAYS);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    private void checkCompatibility() {
        try {
            Optional<Version> retVal = Config.<Set<Version>> getValue(ConfigValues.SupportedClusterLevels)
                    .stream()
                    .max(Comparator.naturalOrder());
            if (retVal.isPresent()) {
                Version version = retVal.get();
                storagePoolDao.getAll().stream()
                        .filter(storagePool -> version.compareTo(storagePool.getCompatibilityVersion()) > 0)
                        .forEach(storagePool -> logAlert(version, storagePool));

            }
        } catch (Throwable t) {
            log.error("Failed to check certification validity: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    private void logAlert(Version version, StoragePool storagePool) {
        AuditLogable auditLog = new AuditLogableImpl();
        auditLog.setStoragePoolId(storagePool.getId());
        auditLog.setStoragePoolName(storagePool.getName());
        auditLog.addCustomValue("latestVersion", version.toString());
        auditLog.addCustomValue("dcVersion", storagePool.getCompatibilityVersion().toString());
        auditLogDirector.log(auditLog, AuditLogType.STORAGE_POOL_VERSION_LOWER_THAN_LATEST_AVAILABLE_VERSION);
    }

}
