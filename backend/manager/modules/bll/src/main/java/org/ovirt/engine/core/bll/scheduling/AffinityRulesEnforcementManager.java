package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesEnforcer;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.MessageBundler;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AffinityRulesEnforcementManager implements BackendService {

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private AffinityRulesEnforcer rulesEnforcer;
    @Inject
    private BackendInternal backend;
    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    @PostConstruct
    protected void wakeup() {

        auditLogDirector.log(new AuditLogableImpl(), AuditLogType.AFFINITY_RULES_ENFORCEMENT_MANAGER_START);

        scheduleJobs(getRegularInterval(), getInitialInterval());
    }

    private long getInitialInterval() {
        return Config.<Long>getValue(ConfigValues.AffinityRulesEnforcementManagerInitialDelay);
    }

    /**
     * Regular interval is used when the manager is migrating VMs and enforcing affinity rules.
     *
     * @return - The regular interval from the ConfigValues.
     */
    private long getRegularInterval() {
        return Config.<Long>getValue(ConfigValues.AffinityRulesEnforcementManagerRegularInterval);
    }

    /**
     * refresh method is called each interval of AffinityRulesEnforcementManager. It will try to find a broken affinity rule, choose a VM then, migrate it in order
     * to fix the breakage.
     */
    public void refresh() {
        try {
            log.debug("Affinity Rules Enforcement Manager interval reached.");

            final List<Iterator<VM>> vmCandidatesPerCluster = new ArrayList<>();
            for (Cluster cluster : clusterDao.getWithoutMigratingVms()) {
                if (!cluster.isInUpgradeMode()) {
                    vmCandidatesPerCluster.add(rulesEnforcer.chooseVmsToMigrate(cluster));
                }
            }

            // Migrate 1 VM from each cluster
            for (Iterator<VM> candidates : vmCandidatesPerCluster) {
                while (candidates.hasNext()) {
                    VM vm = candidates.next();
                    if (migrateVM(vm)) {
                        break;
                    }
                }
            }

        } catch (Throwable t) {
            log.error("Exception in refreshing affinity rules: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    protected boolean migrateVM(final VM vmToMigrate) {
        MigrateVmParameters parameters = new MigrateVmParameters(false, vmToMigrate.getId());

        parameters.setReason(MessageBundler.getMessage(AuditLogType.MIGRATION_REASON_AFFINITY_ENFORCEMENT));

        ActionReturnValue res = backend.runInternalAction(ActionType.BalanceVm,
                parameters,
                ExecutionHandler.createInternalJobContext());

        return res.getSucceeded();
    }

    private void scheduleJobs(long regularInterval, long initialInterval) {
        executor.scheduleWithFixedDelay(this::refresh,
                initialInterval,
                regularInterval,
                TimeUnit.MINUTES);
    }
}
