package org.ovirt.engine.core.bll;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.network.FirewallType;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class IPTablesDeprecationNotifier implements BackendService {

    private static final Logger log = LoggerFactory.getLogger(IPTablesDeprecationNotifier.class);

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    @PostConstruct
    private void init() {
        log.info("Start initializing {}", getClass().getSimpleName());
        executor.scheduleWithFixedDelay(this::checkClustersWithIPTables,
                0,
                30,
                TimeUnit.DAYS);
        log.info("Finished initializing {}", getClass().getSimpleName());
    }

    private void checkClustersWithIPTables() {
        clusterDao.getAll().stream()
                .filter(cluster -> cluster.getFirewallType() == FirewallType.IPTABLES)
                .forEach(cluster -> logWarning(cluster));
    }

    private void logWarning(Cluster cluster) {
        AuditLogable auditLog = new AuditLogableImpl();
        auditLog.setClusterId(cluster.getId());
        auditLog.setClusterName(cluster.getName());
        auditLog.addCustomValue("DeprecatingVersion", Version.v4_2.toString());
        // we cannot add 4.3 to Version enum until 4.2 release is branched
        auditLog.addCustomValue("RemovingVersion", "4.3");
        auditLogDirector.log(auditLog, AuditLogType.DEPRECATED_IPTABLES_FIREWALL);
    }
}
