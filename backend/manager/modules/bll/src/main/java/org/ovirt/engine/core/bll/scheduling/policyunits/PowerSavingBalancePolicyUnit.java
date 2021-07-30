package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.math.NumberUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.external.BalanceResult;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.pending.PendingVM;
import org.ovirt.engine.core.bll.scheduling.utils.FindVmAndDestinations;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VdsPowerDownParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "736999d0-1023-46a4-9a75-1316ed50e151",
        name = "OptimalForPowerSaving",
        type = PolicyUnitType.LOAD_BALANCING,
        description = "Load balancing VMs in cluster according to hosts CPU load, striving cluster's hosts CPU load to"
                + " be over 'LowUtilization' and under 'HighUtilization'",
        parameters = {
                PolicyUnitParameter.HIGH_UTILIZATION,
                PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED,
                PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED,
                PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES,
                PolicyUnitParameter.LOW_UTILIZATION,
                PolicyUnitParameter.ENABLE_AUTOMATIC_HOST_POWER_MANAGEMENT,
                PolicyUnitParameter.HOSTS_IN_RESERVE
        }
)
public class PowerSavingBalancePolicyUnit extends CpuAndMemoryBalancingPolicyUnit {
    private static final Logger log = LoggerFactory.getLogger(PowerSavingBalancePolicyUnit.class);

    @Inject
    private VdsDao vdsDao;

    @Inject
    private BackendInternal backend;

    public PowerSavingBalancePolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<BalanceResult> balance(Cluster cluster,
            List<VDS> hosts,
            Map<String, String> parameters) {
        final List<BalanceResult> migrationRule =  super.balance(cluster, hosts, parameters);

        List<VDS> allHosts = vdsDao.getAllForCluster(cluster.getId());

        List<VDS> emptyHosts = new ArrayList<>();
        List<VDS> maintenanceHosts = new ArrayList<>();
        List<VDS> downHosts = new ArrayList<>();
        List<VDS> maintenanceHostsWithOkExternalStatus = new ArrayList<>();

        getHostLists(allHosts, emptyHosts, maintenanceHosts, downHosts, maintenanceHostsWithOkExternalStatus);

        Pair<VDS, VDSStatus> action = evaluatePowerManagementSituation(
                cluster,
                downHosts,
                maintenanceHosts,
                maintenanceHostsWithOkExternalStatus,
                emptyHosts,
                parameters);

        if (action != null) {
            processPmAction(action);
        }

        return migrationRule;
    }

    private void logAction(VDS vds, AuditLogType type) {
        AuditLogable loggable = new AuditLogableImpl();
        loggable.addCustomValue("Host", vds.getName());
        loggable.setVdsName(vds.getName());
        loggable.setVdsId(vds.getId());
        loggable.setClusterId(vds.getClusterId());
        loggable.setClusterName(vds.getClusterName());
        Injector.get(AuditLogDirector.class).log(loggable, type);
    }

    private void processPmAction(Pair<VDS, VDSStatus> action) {
        VDS vds = action.getFirst();
        VDSStatus currentStatus = vds.getStatus();
        VDSStatus targetStatus = action.getSecond();

        if (targetStatus == VDSStatus.Maintenance && currentStatus == VDSStatus.Up) {
            logAction(vds, AuditLogType.PM_POLICY_UP_TO_MAINTENANCE);

            /* Up -> Maint */
            Guid[] vdsList = new Guid[] {vds.getId()};
            MaintenanceNumberOfVdssParameters parameters =
                    new MaintenanceNumberOfVdssParameters(Arrays.asList(vdsList), true, true);
            backend.runInternalAction(ActionType.MaintenanceNumberOfVdss,
                    parameters,
                    ExecutionHandler.createInternalJobContext());
        } else if (targetStatus == VDSStatus.Down && currentStatus == VDSStatus.Maintenance) {
            logAction(vds, AuditLogType.PM_POLICY_MAINTENANCE_TO_DOWN);

            /* Maint -> Down */
            VdsPowerDownParameters parameters = new VdsPowerDownParameters(vds.getId());
            parameters.setKeepPolicyPMEnabled(true);
            backend.runInternalAction(ActionType.VdsPowerDown,
                    parameters,
                    ExecutionHandler.createInternalJobContext());
        } else if (targetStatus == VDSStatus.Up && currentStatus == VDSStatus.Maintenance) {
            logAction(vds, AuditLogType.PM_POLICY_TO_UP);

            /* Maint -> Up */
            VdsActionParameters parameters = new VdsActionParameters(vds.getId());
            backend.runInternalAction(ActionType.ActivateVds,
                    parameters,
                    ExecutionHandler.createInternalJobContext());
        } else if (targetStatus == VDSStatus.Up && currentStatus == VDSStatus.Down) {
            logAction(vds, AuditLogType.PM_POLICY_TO_UP);

            /* Down -> Up */
            FenceVdsActionParameters parameters = new FenceVdsActionParameters(vds.getId());
            backend.runInternalAction(ActionType.StartVds,
                    parameters,
                    ExecutionHandler.createInternalJobContext());
        } else {
            /* Should not ever happen... */
            log.error("Unknown host power management transition '{}' -> '{}'",
                    currentStatus,
                    targetStatus);
        }
    }

    /**
     * This method will prepare the lists that are necessary for the power management part of this
     * policy.
     *
     * @param allHosts All hosts in the cluster regardless of their status or PM configuration
     * @param emptyHosts Pre-initialized list that will be filled by empty hosts
     * @param maintenanceHosts Pre-initialized list that will be filled by hosts in maintenance
     *                         that have automatic power management still enabled
     * @param maintenanceHostsWithOkExternalStatus Pre-initialized list that will be filled by
     *                                             hosts in maintenance status that have PM
     *                                             enabled and also have 'Ok' external-status
     * @param downHosts Pre-initialized list that will be filled by hosts that are down
     *                  that have automatic power management still enabled, and whose external
     *                  status is Ok. This external status is important because we do not want
     *                  hosts with non-Ok external status to be potentially selected for power-up
     */
    protected void getHostLists(List<VDS> allHosts, List<VDS> emptyHosts, List<VDS> maintenanceHosts,
            List<VDS> downHosts, List<VDS> maintenanceHostsWithOkExternalStatus) {
        for (VDS vds: allHosts) {
            if (vds.getStatus() == VDSStatus.Up
                    && vds.getVmCount() == 0
                    && vds.getVmMigrating() == 0
                    && PendingVM.collectForHost(getPendingResourceManager(), vds.getId()).size() == 0) {
                emptyHosts.add(vds);
            } else if (vds.isPowerManagementControlledByPolicy() && !vds.isDisablePowerManagementPolicy()) {
                if (vds.getStatus() == VDSStatus.Maintenance) {
                    maintenanceHosts.add(vds);
                    if (ExternalStatus.Ok.equals(vds.getExternalStatus())) {
                        maintenanceHostsWithOkExternalStatus.add(vds);
                    }
                } else if (vds.getStatus() == VDSStatus.Down && ExternalStatus.Ok.equals(vds.getExternalStatus())) {
                    downHosts.add(vds);
                }
            }
        }
    }

    /**
     * This method will investigate the current state of all hosts in cluster and return the advised
     * action that should take place to make the cluster closer to the expected balancing state.
     *
     * @param pmDownHosts hosts that were previously powered down by the power management policy
     * @param pmMaintenanceHosts hosts that were previously powered down by the power management policy
     * @param pmMaintenanceHostsWithOkExternalStatus hosts that were previously powered down by
     *                                               the power management policy and have Ok external-status
     * @param emptyHosts hosts that are still up, but contain no Vms
     * @return a pair of VDS to update and the desired VDSStatus to get it to
     */
    protected Pair<VDS, VDSStatus> evaluatePowerManagementSituation(Cluster cluster, List<VDS> pmDownHosts,
                                                                    List<VDS> pmMaintenanceHosts,
                                                                    List<VDS> pmMaintenanceHostsWithOkExternalStatus,
                                                                    List<VDS> emptyHosts,
                                                                    Map<String, String> parameters) {
        final int requiredReserve = NumberUtils.toInt(
                parameters.get(PolicyUnitParameter.HOSTS_IN_RESERVE.getDbName()),
                Config.<Integer> getValue(ConfigValues.HostsInReserve)
        );

        String enableAutoPMParameter = parameters.get(
                PolicyUnitParameter.ENABLE_AUTOMATIC_HOST_POWER_MANAGEMENT.getDbName());
        Boolean enableAutoPM = enableAutoPMParameter == null ? null : Boolean.valueOf(enableAutoPMParameter);
        if (enableAutoPM == null) {
            enableAutoPM = Config.<Boolean> getValue(ConfigValues.EnableAutomaticHostPowerManagement);
        }

        /* Automatic power management is disabled */
        if (!enableAutoPM) {
            log.info("Automatic power management is disabled for cluster '{}'.", cluster.getName());
            return null;
        }

        /* We need more hosts but there are no available for us */
        if (requiredReserve > emptyHosts.size()
                && pmDownHosts.isEmpty()
                && pmMaintenanceHosts.isEmpty()) {
            log.info("Cluster '{}' does not have enough spare hosts, but no additional host is available.",
                    cluster.getName());
            return null;
        } else if (requiredReserve < emptyHosts.size() && pmMaintenanceHostsWithOkExternalStatus.size() > 1) {
            /* We have enough free hosts so shut some hosts in maintenance down
               keep at least one spare in maintenance during the process.
             */
            log.info("Cluster '{}' does have enough spare hosts, shutting one host down.", cluster.getName());
            return new Pair<>(pmMaintenanceHostsWithOkExternalStatus.get(0), VDSStatus.Down);
        } else if (requiredReserve < emptyHosts.size()) {
            /* We do have enough empty hosts to put something to maintenance */

            /* Find hosts with automatic PM enabled that are not the current SPM */
            /* and their external-status is OK*/

            Optional<VDS> hostsWithAutoPM = emptyHosts.stream()
                    .filter(vds -> !vds.isDisablePowerManagementPolicy()
                                && vds.getSpmStatus() != VdsSpmStatus.SPM
                                && vds.isPmEnabled()
                                && ExternalStatus.Ok.equals(vds.getExternalStatus())
                    ).findFirst();

            if (!hostsWithAutoPM.isPresent()) {
                log.info("Cluster '{}' does have too many spare hosts, but none can be put to maintenance.",
                        cluster.getName());
                return null;
            } else {
                return new Pair<>(hostsWithAutoPM.get(), VDSStatus.Maintenance);
            }
        } else if (requiredReserve == emptyHosts.size() && !pmMaintenanceHostsWithOkExternalStatus.isEmpty()) {
            /* We have the right amount of empty hosts to start shutting the
               hosts that are resting in maintenance down.
             */
            log.info("Cluster '{}' does have enough spare hosts, shutting one host down.", cluster.getName());
            return new Pair<>(pmMaintenanceHostsWithOkExternalStatus.get(0), VDSStatus.Down);
        } else if (requiredReserve > emptyHosts.size() && !pmMaintenanceHosts.isEmpty()) {
            /* We do not have enough free hosts, but we still have some hosts
               in maintenance. We can easily activate those.
             */
            log.info("Cluster '{}' does not have enough spare hosts, reactivating one.", cluster.getName());
            return new Pair<>(pmMaintenanceHosts.get(0), VDSStatus.Up);
        } else if (requiredReserve > emptyHosts.size() && pmMaintenanceHosts.isEmpty()) {
            /* We do not have enough free hosts and no hosts in pm maintenance, so we need to start some hosts up. */
            log.info("Cluster '{}' does not have enough spare hosts, trying to start one up.", cluster.getName());
            return new Pair<>(pmDownHosts.get(0), VDSStatus.Up);
        }

        /* All power management constraints were satisfied, no need to do anything */
        return null;
    }

    @Override
    protected FindVmAndDestinations getFindVmAndDestinations(Cluster cluster, Map<String, String> parameters) {
        final int highUtilization = NumberUtils.toInt(parameters.get(PolicyUnitParameter.HIGH_UTILIZATION.getDbName()),
                Config.<Integer>getValue(ConfigValues.HighUtilizationForPowerSave));

        final long overUtilizedMemory = NumberUtils.toLong(parameters.get(
                PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName()),
                0L
        );

        return new FindVmAndDestinations(cluster, highUtilization, overUtilizedMemory);
    }

    /**
     * Returns hosts that are over-utilized or under-utilized with respect to CPU load.
     */
    @Override
    protected List<VDS> getPrimarySources(Cluster cluster, List<VDS> candidateHosts, Map<String, String> parameters) {
        int highUtilization = NumberUtils.toInt(parameters.get(PolicyUnitParameter.HIGH_UTILIZATION.getDbName()),
                Config.<Integer>getValue(ConfigValues.HighUtilizationForPowerSave));
        final int lowUtilization = NumberUtils.toInt(parameters.get(PolicyUnitParameter.LOW_UTILIZATION.getDbName()),
                Config.<Integer>getValue(ConfigValues.LowUtilizationForPowerSave));

        final int highVdsCount = Math.min(
                Config.<Integer>getValue(ConfigValues.UtilizationThresholdInPercent) * highUtilization / 100,
                highUtilization - Config.<Integer>getValue(ConfigValues.VcpuConsumptionPercentage)
        );

        // Over-utilized hosts are in the front of the list, because it is more important
        // to migrate a VM from an over-utilized host than from an under-utilized
        List<VDS> result = new ArrayList<>();
        result.addAll(getOverUtilizedCPUHosts(candidateHosts, new CpuAndMemoryBalancingParameters(parameters, highVdsCount)));
        result.addAll(getUnderUtilizedCPUHosts(candidateHosts, new CpuAndMemoryBalancingParameters(parameters, lowUtilization, 1)));

        return result;
    }

    /**
     * Returns list of hosts, that are not over-utilized with respect to CPU load.
     * The PowerSavingCPUWeightPolicyUnit prefers hosts that are the most utilized,
     * so the under-utilized hosts are used only if the VM cannot fit to other hosts.
     */
    @Override
    protected List<VDS> getPrimaryDestinations(Cluster cluster,
            List<VDS> candidateHosts,
            Map<String, String> parameters) {
        int highUtilization = NumberUtils.toInt(parameters.get(PolicyUnitParameter.HIGH_UTILIZATION.getDbName()),
                Config.<Integer>getValue(ConfigValues.HighUtilizationForPowerSave));
        return candidateHosts.stream()
                .filter(host -> !isHostCpuOverUtilized(host, new CpuAndMemoryBalancingParameters(parameters, highUtilization)))
                .collect(Collectors.toList());
    }

    /**
     * Returns hosts that are over-utilized or under-utilized with respect to memory.
     */
    @Override
    protected List<VDS> getSecondarySources(Cluster cluster,
            List<VDS> candidateHosts,
            Map<String, String> parameters) {
        long lowMemoryLimit = parameters.containsKey(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName()) ?
                Long.parseLong(parameters.get(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName())) : 0L;
        long highMemoryLimit = parameters.containsKey(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName()) ?
                Long.parseLong(parameters.get(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName()))
                : Long.MAX_VALUE;

        // Over-utilized hosts are in the front of the list, because it is more important
        // to migrate a VM from an over-utilized host than from an under-utilized
        List<VDS> result = new ArrayList<>();
        result.addAll(getHostsWithLessFreeMemory(candidateHosts, lowMemoryLimit));
        result.addAll(getHostsWithMoreFreeMemory(candidateHosts, highMemoryLimit, 1));
        return result;
    }

    /**
     * Returns list of hosts, that are not over-utilized with respect to memory.
     * The PowerSavingMemoryWeightPolicyUnit prefers hosts that are the most utilized,
     * so the under-utilized hosts are used only if the VM cannot fit to other hosts.
     */
    @Override
    protected List<VDS> getSecondaryDestinations(Cluster cluster, List<VDS> candidateHosts, Map<String, String> parameters) {
        long lowMemoryLimit = parameters.containsKey(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName()) ?
                Long.parseLong(parameters.get(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName())) : 0L;

        return getHostsWithMoreFreeMemory(candidateHosts, lowMemoryLimit, 0);
    }

    protected VdsDao getVdsDao() {
        return vdsDao;
    }
}
