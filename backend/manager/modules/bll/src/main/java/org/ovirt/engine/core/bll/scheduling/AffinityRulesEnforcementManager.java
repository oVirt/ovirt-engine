package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesEnforcementPerCluster;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.di.qualifier.Created;
import org.ovirt.engine.core.common.di.qualifier.Deleted;
import org.ovirt.engine.core.common.di.qualifier.Updated;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsGroupDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AffinityRulesEnforcementManager implements BackendService {
    //Fields
    protected Map<VDSGroup, AffinityRulesEnforcementPerCluster> perClusterMap;
    private Iterator<AffinityRulesEnforcementPerCluster> areClusterIterator = Collections.emptyIterator();
    private int currentInterval;

    protected Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    protected AuditLogDirector auditLogDirector;
    @Inject
    protected VdsDao vdsDao;
    @Inject
    protected VdsGroupDao vdsGroupDao;
    @Inject
    protected Instance<AffinityRulesEnforcementPerCluster> perClusterProvider;

    @PostConstruct
    protected void wakeup() {
        currentInterval = getRegularInterval();
        perClusterMap = new HashMap<>();

        //Check that AffinityRulesEnforcementPerCluster exist in perClusterMap for each cluster in the engine.
        List<VDSGroup> vdsGroups = getClusters();

        for (VDSGroup vdsGroup : vdsGroups) {
            if (!perClusterMap.containsKey(vdsGroup)) {
                AffinityRulesEnforcementPerCluster perCluster = perClusterProvider.get();
                perCluster.setClusterId(vdsGroup.getId());
                perClusterMap.put(vdsGroup, perCluster);
            }
        }

        // Initialize Migrations in perClusters.
        for (AffinityRulesEnforcementPerCluster perCluster : perClusterMap.values()) {
            perCluster.initMigrations();
        }

        AuditLogableBase logable = new AuditLogableBase();
        auditLogDirector.log(logable, AuditLogType.AFFINITY_RULES_ENFORCEMENT_MANAGER_START);

        scheduleJobs(currentInterval, getInitialInterval());
    }

    protected List<VDSGroup> getClusters() {
        return vdsGroupDao.getAll();
    }

    private Integer getInitialInterval() {
        return Config.<Integer>getValue(ConfigValues.AffinityRulesEnforcementManagerInitialDelay);
    }

    /**
     * Regular interval is used when the manager is migrating VMs and enforcing affinity rules.
     *
     * @return - The regular interval from the ConfigValues.
     */
    private Integer getRegularInterval() {
        return Config.<Integer>getValue(ConfigValues.AffinityRulesEnforcementManagerRegularInterval);
    }

    private Integer getMaximumMigrationTries() {
        return Config.<Integer>getValue(ConfigValues.AffinityRulesEnforcementManagerMaximumMigrationTries);
    }

    /**
     * Standby interval is used in two cases:
     * 1. All affinity rules are enforced.
     * 2. Too many migrations failed and we don't want to overload the system with migrations. We want to give the system time to adjust itself and maybe allow migrations again.
     *
     * @return - The long interval from the ConfigValues.
     */
    private Integer getStandbyInterval() {
        return Config.<Integer>getValue(ConfigValues.AffinityRulesEnforcementManagerStandbyInterval);
    }

    /**
     * refresh method is called each interval of AREM. It will try to find a broken affinity rule, choose a VM then, migrate it in order
     * to fix the breakage.
     */
    @OnTimerMethodAnnotation("refresh")
    public void refresh() {

        log.debug("Affinity Rules Enforcement Manager interval reached.");

        if (areClusterIterator == null || !areClusterIterator.hasNext()) {
            areClusterIterator = perClusterMap.values().iterator();
        }

        AffinityRulesEnforcementPerCluster perCluster;

        while (true) {
            if (!areClusterIterator.hasNext()) {
                return;
            }

            perCluster = areClusterIterator.next();
            log.debug("Checking affinity rules compliance for cluster {}", perCluster.getClusterId());

            if (perCluster.checkIfCurrentlyMigrating()) {
                currentInterval = getRegularInterval();
                log.debug("Migration in progress, checking different cluster.");
                continue;
            }

            break;
        }

        //Current migration complete.
        if (perCluster.lastMigrationFailed()) {
            perCluster.updateMigrationFailure();
        }

        if (perCluster.getMigrationTries() >= getMaximumMigrationTries()) {
            currentInterval = getStandbyInterval();
            perCluster.setMigrationTries(0);
            return;
        } else if (currentInterval > getRegularInterval()) {
            currentInterval = getRegularInterval();
            perCluster.setMigrationTries(0);
        }

        VM vm;
        vm = perCluster.chooseNextVmToMigrate();

        // No action for the current cluster, check if other clusters are OK
        while (vm == null && areClusterIterator.hasNext()) {
            perCluster = areClusterIterator.next();

            if (perCluster.checkIfCurrentlyMigrating()) {
                log.debug("Migration in progress, checking different cluster.");
                continue;
            }

            vm = perCluster.chooseNextVmToMigrate();
        }

        // All affinity groups are enforced lowering the wake-up frequency
        if (vm == null) {
            currentInterval = getStandbyInterval();
            return;
        }

        // Affinity Group fix needed, initiate the migration
        Guid vmToMigrate = vm.getId();
        MigrateVmParameters parameters = new MigrateVmParameters(false, vmToMigrate);
        parameters.setInitialHosts(new ArrayList<>(getInitialHosts()));

        VdcReturnValueBase migrationStatus = executeMigration(parameters);
        perCluster.updateMigrationStatus(migrationStatus);

        currentInterval = getRegularInterval();
    }

    protected VdcReturnValueBase executeMigration(MigrateVmParameters parameters) {
        return Backend.getInstance().runInternalAction(VdcActionType.MigrateVm,
                parameters,
                ExecutionHandler.createInternalJobContext());
    }

    private List<Guid> getInitialHosts() {
        List<Guid> initialHosts = new ArrayList<>();

        for (VDS host : vdsDao.getAll()) {
            initialHosts.add(host.getId());
        }

        return initialHosts;
    }

    //TODO: Make perClusterMap add the new change instead of initializing the entire perClusterMap again.
    public void onChange(@Observes @Updated @Created @Deleted VDSGroup cluster) {
        // handle cluster changes
        perClusterMap = new HashMap<>();

        /* initialize structures */

        //Check that AffinityRulesEnforcementPerCluster exist in perClusterMap for each cluster in the engine.
        List<VDSGroup> vdsGroups = getClusters();

        for (VDSGroup vdsGroup : vdsGroups) {
            if (!perClusterMap.containsKey(vdsGroup)) {
                AffinityRulesEnforcementPerCluster perCluster = perClusterProvider.get();
                perCluster.setClusterId(vdsGroup.getId());
                perClusterMap.put(vdsGroup, perCluster);
            }
        }

        // Initialize Migrations in perClusters.
        for (AffinityRulesEnforcementPerCluster perCluster : perClusterMap.values()) {
            perCluster.initMigrations();
        }
    }

    private void scheduleJobs(long regularInterval, long longInterval) {
        /* start the interval refreshing */
        Injector.get(SchedulerUtilQuartzImpl.class).scheduleAFixedDelayJob(
                this,
                "refresh",
                new Class[] {},
                new Object[] {},
                regularInterval,
                longInterval,
                TimeUnit.MINUTES);
    }
}
