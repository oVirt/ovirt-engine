package org.ovirt.engine.core.bll;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
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

    /**
     * Future engine version, where below mentioned data center versions will not be supported. Administrators need to
     * upgrade those data centers to newer versions, otherwise they won't be able to upgrade engine to this future
     * version.
     */
    private static final String FUTURE_RELEASE = "4.3";

    /**
     * Set of versions which will be unsupported in above mentioned future engine release.
     */
    private static final HashSet<Version> REMOVED_IN_FUTURE_RELEASE =
            new HashSet<>(Arrays.asList(Version.v3_6, Version.v4_0));

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
            Optional<Version> retVal = Config.<HashSet<Version>> getValue(ConfigValues.SupportedClusterLevels).stream()
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
        AuditLogType msgType;
        AuditLogable auditLog = new AuditLogableImpl();
        if (REMOVED_IN_FUTURE_RELEASE.contains(storagePool.getCompatibilityVersion())) {
            msgType = AuditLogType.STORAGE_POOL_REMOVED_IN_NEXT_RELEASE;
            auditLog.addCustomValue("futureEngineVersion", FUTURE_RELEASE);
        } else {
            msgType = AuditLogType.STORAGE_POOL_LOWER_THAN_ENGINE_HIGHEST_CLUSTER_LEVEL;
            auditLog.addCustomValue("engineVersion", version.toString());
        }
        auditLog.setStoragePoolId(storagePool.getId());
        auditLog.setStoragePoolName(storagePool.getName());
        auditLog.addCustomValue("dcVersion", storagePool.getCompatibilityVersion().toString());
        auditLogDirector.log(auditLog, msgType);
    }

}
