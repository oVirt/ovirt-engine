package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.pending.PendingVM;
import org.ovirt.engine.core.bll.scheduling.utils.FindVmAndDestinations;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.FenceVdsActionParameters;
import org.ovirt.engine.core.common.action.MaintenanceNumberOfVdssParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.action.VdsPowerDownParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
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
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
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
                PolicyUnitParameter.LOW_UTILIZATION,
                PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED,
                PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED,
                PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES
        }
)
public class PowerSavingBalancePolicyUnit extends CpuAndMemoryBalancingPolicyUnit {
    private static final Logger log = LoggerFactory.getLogger(PowerSavingBalancePolicyUnit.class);

    public PowerSavingBalancePolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public Pair<List<Guid>, Guid> balance(Cluster cluster,
            List<VDS> hosts,
            Map<String, String> parameters,
            ArrayList<String> messages) {
        final Pair<List<Guid>, Guid> migrationRule =  super.balance(cluster, hosts, parameters, messages);

        List<VDS> allHosts = getVdsDao().getAllForCluster(cluster.getId());

        List<VDS> emptyHosts = new ArrayList<>();
        List<VDS> maintenanceHosts = new ArrayList<>();
        List<VDS> downHosts = new ArrayList<>();

        getHostLists(allHosts, parameters, emptyHosts, maintenanceHosts, downHosts);

        Pair<VDS, VDSStatus> action = evaluatePowerManagementSituation(
                cluster,
                downHosts,
                maintenanceHosts,
                emptyHosts,
                parameters);

        if (action != null) {
            processPmAction(action);
        }

        return migrationRule;
    }

    private void logAction(VDS vds, AuditLogType type) {
        AuditLogableBase loggable = new AuditLogableBase();
        loggable.addCustomValue("Host", vds.getName());
        new AuditLogDirector().log(loggable, type);
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
            Backend.getInstance().runInternalAction(VdcActionType.MaintenanceNumberOfVdss,
                    parameters,
                    ExecutionHandler.createInternalJobContext());
        }
        else if (targetStatus == VDSStatus.Down && currentStatus == VDSStatus.Maintenance) {
            logAction(vds, AuditLogType.PM_POLICY_MAINTENANCE_TO_DOWN);

            /* Maint -> Down */
            VdsPowerDownParameters parameters = new VdsPowerDownParameters(vds.getId());
            parameters.setKeepPolicyPMEnabled(true);
            Backend.getInstance().runInternalAction(VdcActionType.VdsPowerDown,
                    parameters,
                    ExecutionHandler.createInternalJobContext());
        }
        else if (targetStatus == VDSStatus.Up && currentStatus == VDSStatus.Maintenance) {
            logAction(vds, AuditLogType.PM_POLICY_TO_UP);

            /* Maint -> Up */
            VdsActionParameters parameters = new VdsActionParameters(vds.getId());
            Backend.getInstance().runInternalAction(VdcActionType.ActivateVds,
                    parameters,
                    ExecutionHandler.createInternalJobContext());
        }
        else if (targetStatus == VDSStatus.Up && currentStatus == VDSStatus.Down) {
            logAction(vds, AuditLogType.PM_POLICY_TO_UP);

            /* Down -> Up */
            FenceVdsActionParameters parameters = new FenceVdsActionParameters(vds.getId());
            Backend.getInstance().runInternalAction(VdcActionType.StartVds,
                    parameters,
                    ExecutionHandler.createInternalJobContext());
        }
        else {
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
     * @param downHosts Pre-initialized list that will be filled by hosts that are down
     *                  that have automatic power management still enabled
     */
    protected void getHostLists(List<VDS> allHosts, Map<String, String> parameters,
                                     List<VDS> emptyHosts, List<VDS> maintenanceHosts, List<VDS> downHosts) {
        for (VDS vds: allHosts) {
            if (vds.getStatus() == VDSStatus.Up
                    && vds.getVmCount() == 0
                    && vds.getVmMigrating() == 0
                    && PendingVM.collectForHost(getPendingResourceManager(), vds.getId()).size() == 0) {
                emptyHosts.add(vds);
            }
            else if (vds.isPowerManagementControlledByPolicy() && !vds.isDisablePowerManagementPolicy()) {
                if (vds.getStatus() == VDSStatus.Maintenance) {
                    maintenanceHosts.add(vds);
                }
                else if (vds.getStatus() == VDSStatus.Down) {
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
     * @param emptyHosts hosts that are still up, but contain no Vms
     * @return a pair of VDS to update and the desired VDSStatus to get it to
     */
    protected Pair<VDS, VDSStatus> evaluatePowerManagementSituation(Cluster cluster, List<VDS> pmDownHosts,
                                                                    List<VDS> pmMaintenanceHosts,
                                                                    List<VDS> emptyHosts,
                                                                    Map<String, String> parameters) {
        final int requiredReserve = tryParseWithDefault(parameters.get("HostsInReserve"), Config
                .<Integer> getValue(ConfigValues.HostsInReserve));
        String enableAutoPMParameter = parameters.get("EnableAutomaticHostPowerManagement");
        Boolean enableAutoPM = enableAutoPMParameter == null ? null : Boolean.valueOf(enableAutoPMParameter);
        if (enableAutoPM == null) {
            enableAutoPM = Config.<Boolean> getValue(ConfigValues.EnableAutomaticHostPowerManagement);
        }

        /* Automatic power management is disabled */
        if (!enableAutoPM.booleanValue()) {
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
        }

        /* We have enough free hosts so shut some hosts in maintenance down
           keep at least one spare in maintenance during the process.
         */
        else if (requiredReserve < emptyHosts.size()
                && pmMaintenanceHosts.size() > 1) {
            log.info("Cluster '{}' does have enough spare hosts, shutting one host down.", cluster.getName());
            return new Pair<>(pmMaintenanceHosts.get(0), VDSStatus.Down);
        }

        /* We do have enough empty hosts to put something to maintenance */
        else if (requiredReserve < emptyHosts.size()) {
            /* Find hosts with automatic PM enabled that are not the current SPM */

            Optional<VDS> hostsWithAutoPM = emptyHosts.stream()
                    .filter(vds -> !vds.isDisablePowerManagementPolicy()
                                && vds.getSpmStatus() != VdsSpmStatus.SPM
                                && vds.isPmEnabled()
                    ).findFirst();

            if (!hostsWithAutoPM.isPresent()) {
                log.info("Cluster '{}' does have too many spare hosts, but none can be put to maintenance.",
                        cluster.getName());
                return null;
            } else {
                return new Pair<>(hostsWithAutoPM.get(), VDSStatus.Maintenance);
            }
        }

        /* We have the right amount of empty hosts to start shutting the
           hosts that are resting in maintenance down.
         */
        else if (requiredReserve == emptyHosts.size()
                && pmMaintenanceHosts.isEmpty() == false) {
            log.info("Cluster '{}' does have enough spare hosts, shutting one host down.", cluster.getName());
            return new Pair<>(pmMaintenanceHosts.get(0), VDSStatus.Down);
        }

        /* We do not have enough free hosts, but we still have some hosts
           in maintenance. We can easily activate those.
         */
        else if (requiredReserve > emptyHosts.size()
                && pmMaintenanceHosts.isEmpty() == false) {
            log.info("Cluster '{}' does not have enough spare hosts, reactivating one.", cluster.getName());
            return new Pair<>(pmMaintenanceHosts.get(0), VDSStatus.Up);
        }

        /* We do not have enough free hosts and no hosts in pm maintenance,
           so we need to start some hosts up.
         */
        else if (requiredReserve > emptyHosts.size()
                && pmMaintenanceHosts.isEmpty()) {
            log.info("Cluster '{}' does not have enough spare hosts, trying to start one up.", cluster.getName());
            return new Pair<>(pmDownHosts.get(0), VDSStatus.Up);
        }

        /* All power management constraints were satisfied, no need to do anything */
        return null;
    }

    @Override
    protected FindVmAndDestinations getFindVmAndDestinations(Cluster cluster, Map<String, String> parameters) {
        final int highUtilization = tryParseWithDefault(parameters.get("HighUtilization"), Config
                .<Integer>getValue(ConfigValues.HighUtilizationForPowerSave));
        final long overUtilizedMemory =
                parameters.containsKey(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName()) ?
                        Long.parseLong(parameters.get(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName())) :
                        0L;

        return new FindVmAndDestinations(cluster, highUtilization, overUtilizedMemory);
    }

    @Override
    protected List<VDS> getPrimarySources(Cluster cluster, List<VDS> candidateHosts, Map<String, String> parameters) {
        int highUtilization = tryParseWithDefault(parameters.get(PolicyUnitParameter.HIGH_UTILIZATION.getDbName()),
                Config.<Integer>getValue(ConfigValues.HighUtilizationForPowerSave));
        final int lowUtilization = tryParseWithDefault(parameters.get(PolicyUnitParameter.LOW_UTILIZATION.getDbName()),
                Config.<Integer>getValue(ConfigValues.LowUtilizationForPowerSave));
        final int cpuOverCommitDurationMinutes =
                tryParseWithDefault(parameters.get(PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES.getDbName()),
                        Config.<Integer>getValue(ConfigValues.CpuOverCommitDurationMinutes));
        final int highVdsCount = Math
                .min(Config.<Integer>getValue(ConfigValues.UtilizationThresholdInPercent)
                                * highUtilization / 100,
                        highUtilization
                                - Config.<Integer>getValue(ConfigValues.VcpuConsumptionPercentage));

        List<VDS> result = new ArrayList<>();
        result.addAll(getUnderUtilizedCPUHosts(candidateHosts, lowUtilization, 1, cpuOverCommitDurationMinutes));
        result.addAll(getOverUtilizedCPUHosts(candidateHosts, highVdsCount, cpuOverCommitDurationMinutes));

        return result;
    }

    @Override
    protected List<VDS> getPrimaryDestinations(Cluster cluster,
            List<VDS> candidateHosts,
            Map<String, String> parameters) {
        int highUtilization = tryParseWithDefault(parameters.get(PolicyUnitParameter.HIGH_UTILIZATION.getDbName()),
                Config.<Integer>getValue(ConfigValues.HighUtilizationForPowerSave));
        final int lowUtilization = tryParseWithDefault(parameters.get(PolicyUnitParameter.LOW_UTILIZATION.getDbName()),
                Config.<Integer>getValue(ConfigValues.LowUtilizationForPowerSave));
        final int cpuOverCommitDurationMinutes =
                tryParseWithDefault(parameters.get(PolicyUnitParameter.CPU_OVERCOMMIT_DURATION_MINUTES.getDbName()),
                        Config.<Integer>getValue(ConfigValues.CpuOverCommitDurationMinutes));

        final List<VDS> result = getNormallyUtilizedCPUHosts(cluster,
                candidateHosts,
                highUtilization,
                cpuOverCommitDurationMinutes,
                lowUtilization);
        return result;
    }

    @Override
    protected List<VDS> getSecondarySources(Cluster cluster,
            List<VDS> candidateHosts,
            Map<String, String> parameters) {
        long lowMemoryLimit = parameters.containsKey(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName()) ?
                Long.parseLong(parameters.get(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName())) : 0L;
        long highMemoryLimit = parameters.containsKey(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName()) ?
                Long.parseLong(parameters.get(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName())) : 0L;

        List<VDS> result = new ArrayList<>();
        result.addAll(getUnderUtilizedMemoryHosts(candidateHosts, highMemoryLimit, 1));
        result.addAll(getOverUtilizedMemoryHosts(candidateHosts, lowMemoryLimit));
        return result;
    }

    @Override
    protected List<VDS> getSecondaryDestinations(Cluster cluster, List<VDS> candidateHosts, Map<String, String> parameters) {
        long notEnoughMemory = parameters.containsKey(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName()) ?
                Long.parseLong(parameters.get(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED.getDbName())) : 0L;
        long tooMuchMemory = parameters.containsKey(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName()) ?
                Long.parseLong(parameters.get(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED.getDbName())) : 0L;

        return getNormallyUtilizedMemoryHosts(candidateHosts, notEnoughMemory, tooMuchMemory);
    }
}
