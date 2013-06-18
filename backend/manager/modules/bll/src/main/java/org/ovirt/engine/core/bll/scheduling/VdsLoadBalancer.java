package org.ovirt.engine.core.bll.scheduling;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

/**
 * This class responsible to perform periodically load balancing of servers Its
 * activated only if load balancing enabled by configuration.
 */
public final class VdsLoadBalancer {
    private static Log log = LogFactory.getLog(VdsLoadBalancer.class);
    private static VdsLoadBalancer instance = null;
    private MigrationHandler migrationHandler = null;

    public static VdsLoadBalancer getInstance() {
        if (instance == null) {
            synchronized (VdsLoadBalancer.class) {
                if (instance == null) {
                    instance = new VdsLoadBalancer();
                    if (Config.<Boolean> GetValue(ConfigValues.EnableVdsLoadBalancing)) {
                        EnableLoadBalancer();
                    }
                }
            }
        }
        return instance;
    }

    private VdsLoadBalancer() {
    }

    @OnTimerMethodAnnotation("PerformLoadBalancing")
    public void PerformLoadBalancing() {
        log.debugFormat("VdsLoadBalancer: Load Balancer timer entered.");
        // get all clusters
        List<VDSGroup> groups = DbFacade.getInstance().getVdsGroupDao().getAll();
        for (VDSGroup group : groups) {
            if (group.getselection_algorithm() != VdsSelectionAlgorithm.None) {
                VdsLoadBalancingAlgorithm loadBalancingAlgorithm = VdsLoadBalancingAlgorithm
                        .CreateVdsLoadBalancingAlgorithm(group);
                log.infoFormat("VdsLoadBalancer: Starting load balance for cluster: {0}, algorithm: {1}.",
                        group.getName(), group.getselection_algorithm().toString());
                log.infoFormat("VdsLoadBalancer: high util: {0}, low util: {1}, duration: {2}, threashold: {3}",
                        group.gethigh_utilization(), group.getlow_utilization(),
                        group.getcpu_over_commit_duration_minutes(),
                        Config.<Integer> GetValue(ConfigValues.UtilizationThresholdInPercent));
                migrationHandler.migrateVMs(loadBalancingAlgorithm.LoadBalance());
            } else {
                log.debugFormat("VdsLoadBalancer: Cluster {0} skipped because no selection algorithm selected.",
                        group.getName());
            }
        }
    }

    public static void EnableLoadBalancer() {
        log.info("Start scheduling to enable vds load balancer");
        SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(instance, "PerformLoadBalancing", new Class[] {},
                new Object[] {}, Config.<Integer> GetValue(ConfigValues.VdsLoadBalancingeIntervalInMinutes),
                Config.<Integer> GetValue(ConfigValues.VdsLoadBalancingeIntervalInMinutes), TimeUnit.MINUTES);
        log.info("Finished scheduling to enable vds load balancer");
    }

    public void setMigrationHandler(MigrationHandler migrationHandler) {
        if (this.migrationHandler != null) {
            throw new RuntimeException("Load balance migration handler should be set only once");
        }
        this.migrationHandler = migrationHandler;
    }
}
