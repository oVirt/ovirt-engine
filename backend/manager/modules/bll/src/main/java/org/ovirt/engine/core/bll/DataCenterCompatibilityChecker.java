package org.ovirt.engine.core.bll;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DataCenterCompatibilityChecker implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(DataCenterCompatibilityChecker.class);

    @Inject
    private SchedulerUtilQuartzImpl schedulerUtil;

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private AuditLogDirector auditLogDirector;

    @PostConstruct
    private void init() {
        log.info("Start initializing {}", getClass().getSimpleName());
        schedulerUtil.scheduleAFixedDelayJob(this,
                "onTimer",
                new Class[]{},
                new Object[]{},
                0,
                7,
                TimeUnit.DAYS);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    @OnTimerMethodAnnotation("onTimer")
    public void onTimer() {
        Optional<Version> retVal = Config.<HashSet<Version>> getValue(ConfigValues.SupportedClusterLevels).stream()
                .max(Comparator.naturalOrder());
        if (retVal.isPresent()) {
            Version version = retVal.get();
            storagePoolDao.getAll().stream()
                    .filter(storagePool -> version.compareTo(storagePool.getCompatibilityVersion()) > 0)
                    .forEach(storagePool -> logAlert(version, storagePool));

        }
    }

    private void logAlert(Version version, StoragePool storagePool) {
        AuditLogableBase auditLog = Injector.injectMembers(new AuditLogableBase());
        auditLog.setStoragePool(storagePool);
        auditLog.addCustomValue("engineVersion", version.toString());
        auditLog.addCustomValue("dcVersion", storagePool.getCompatibilityVersion().toString());
        auditLogDirector.log(auditLog, AuditLogType.STORAGE_POOL_LOWER_THAN_ENGINE_HIGHEST_CLUSTER_LEVEL);
    }

}
