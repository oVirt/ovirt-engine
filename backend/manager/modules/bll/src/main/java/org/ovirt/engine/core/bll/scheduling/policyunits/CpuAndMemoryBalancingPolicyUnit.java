package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.external.BalanceResult;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.utils.FindVmAndDestinations;
import org.ovirt.engine.core.bll.scheduling.utils.VdsCpuUsageComparator;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CpuAndMemoryBalancingPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(CpuAndMemoryBalancingPolicyUnit.class);

    @Inject
    private VmDao vmDao;

    @Inject
    private VmStatisticsDao vmStatisticsDao;

    @Override
    protected Set<PolicyUnitParameter> getParameters() {
        Set<PolicyUnitParameter> params = super.getParameters();
        params.add(PolicyUnitParameter.LOW_MEMORY_LIMIT_FOR_OVER_UTILIZED);
        params.add(PolicyUnitParameter.HIGH_MEMORY_LIMIT_FOR_UNDER_UTILIZED);
        return params;
    }

    public CpuAndMemoryBalancingPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public Optional<BalanceResult> balance(final Cluster cluster,
            List<VDS> hosts,
            Map<String, String> parameters,
            ArrayList<String> messages) {

        Objects.requireNonNull(hosts);
        Objects.requireNonNull(cluster);

        if (hosts.size() < 2) {
            log.debug("No balancing for cluster '{}', contains only {} host(s)", cluster.getName(), hosts.size());
            return Optional.empty();
        }

        final List<VDS> overUtilizedPrimaryHosts = getPrimarySources(cluster, hosts, parameters);
        final List<VDS> overUtilizedSecondaryHosts = getSecondarySources(cluster, hosts, parameters);

        // if there aren't any overutilized hosts, then there is nothing to balance...
        if ((overUtilizedPrimaryHosts == null || overUtilizedPrimaryHosts.size() == 0)
                && (overUtilizedSecondaryHosts == null || overUtilizedSecondaryHosts.size() == 0)) {
            log.debug("There is no over-utilized host in cluster '{}'", cluster.getName());
            return Optional.empty();
        }

        FindVmAndDestinations findVmAndDestinations = getFindVmAndDestinations(cluster, parameters);
        Optional<BalanceResult> result = Optional.empty();

        // try balancing based on CPU first
        if (overUtilizedPrimaryHosts != null && overUtilizedPrimaryHosts.size() > 0) {
            // returns hosts with utilization lower than the specified threshold
            List<VDS> underUtilizedHosts = getPrimaryDestinations(cluster, hosts, parameters);

            /* if no host has a spare power, then there is nothing we can do to balance it here, try
               the secondary aporoach */
            if (underUtilizedHosts == null || underUtilizedHosts.size() == 0) {
                log.warn("All candidate hosts have been filtered, can't balance the cluster '{}'"
                                + " based on the CPU usage, will try memory based approach",
                        cluster.getName());
            } else {
                result = getBalance(findVmAndDestinations, overUtilizedPrimaryHosts, underUtilizedHosts);
            }
        }

        // if it is not possible (or necessary) to balance based on CPU, try with memory
        if (!result.isPresent() && (overUtilizedSecondaryHosts != null && overUtilizedSecondaryHosts.size() > 0)) {
            // returns hosts with more free memory than the specified threshold
            List<VDS> underUtilizedHosts = getSecondaryDestinations(cluster, hosts, parameters);

            // if no host has memory to spare, then there is nothing we can do to balance it..
            if (underUtilizedHosts == null || underUtilizedHosts.size() == 0) {
                log.warn("All candidate hosts have been filtered, can't balance the cluster '{}'"
                                + " using memory based approach",
                        cluster.getName());
                return Optional.empty();
            }

            result = getBalance(findVmAndDestinations, overUtilizedSecondaryHosts, underUtilizedHosts);
        }

        // add the current host, it is possible it is the best host after all,
        // because the balancer does not know about affinity for example
        Optional<BalanceResult> finalResult = result;
        result.map(BalanceResult::getCurrentHost)
                .filter(Objects::nonNull)
                .ifPresent(h ->
                        finalResult.ifPresent(res -> res.getCandidateHosts().add(h)));

        return result;
    }

    private Optional<BalanceResult> getBalance(FindVmAndDestinations findVmAndDestinations,
            final List<VDS> overUtilizedHosts,
            final List<VDS> underUtilizedHosts) {

        return findVmAndDestinations.invoke(overUtilizedHosts, underUtilizedHosts, getVmDao(), getVmStatisticsDao())
                .map(res -> new BalanceResult(res.getVmToMigrate().getId(),
                        res.getDestinationHosts().stream()
                                .map(VDS::getId)
                                .collect(Collectors.toList()),
                        res.getVmToMigrate().getRunOnVds())
                    );
    }


    /**
     * Get all hosts that have more free memory than minFreeMemory, but less free memory than maxFreeMemory.
     *
     * @param hosts - candidate hosts
     * @param minFreeMemory - minimum amount of free memory required (MiBú
     * @param maxFreeMemory - maximum amount of free memory allowed (MiB)
     * @return - normally utilized hosts
     */
    protected List<VDS> getNormallyUtilizedMemoryHosts(Collection<VDS> hosts, long minFreeMemory, long maxFreeMemory) {
        List<VDS> result = new ArrayList<>();

        for (VDS h: hosts) {
            if (h.getMaxSchedulingMemory() >= minFreeMemory
                    && h.getMaxSchedulingMemory() <= maxFreeMemory) {
                result.add(h);
            }
        }

        return result;
    }

    /**
     * Return a list of hosts that have more free memory than lowFreeMemoryLimit and more
     * VMs than minVmCount.
     *
     * minVmCount is useful for using this method to find a source of VMs for migration
     * (we do not care about hosts that have no VMs in that case). If you are looking
     * for a destination candidates, pass 0 there.
     *
     * @param hosts - candidate hosts
     * @param lowFreeMemoryLimit - minimal amount of free memory to be considered under utilized (MiB)
     * @param minVmCount - minimal number of VMs that need to be present on a host
     * @return - under-utilized hosts
     */
    protected List<VDS> getUnderUtilizedMemoryHosts(Collection<VDS> hosts, long lowFreeMemoryLimit, int minVmCount) {
        List<VDS> result = new ArrayList<>();

        for (VDS h: hosts) {
            if (h.getMaxSchedulingMemory() > lowFreeMemoryLimit
                    && h.getVmCount() >= minVmCount) {
                result.add(h);
            }
        }

        return result;
    }

    /**
     * Compute a set of hosts where less than a configured amount of memory is available
     * and there is more VMs than one.
     *
     * @param hosts A list of all hosts to consider
     * @param highFreeMemoryLimit The amount of free memory that has to be available for
     *                       the host to NOT be overutilized (in MB)
     * @return A list of hosts with a memory pressure situation
     */
    protected List<VDS> getOverUtilizedMemoryHosts(Collection<VDS> hosts, long highFreeMemoryLimit) {
        List<VDS> result = new ArrayList<>();

        for (VDS h: hosts) {
            if (h.getMaxSchedulingMemory() < highFreeMemoryLimit
                    && h.getVmCount() > 1) {
                result.add(h);
            }
        }

        return result;
    }

    /**
     * Get all hosts that are neither over- or under-utilized in terms of CPU power.
     * See getOverUtilizedCpuHosts and getUnderUtilizedCpuHosts for details.
     */
    protected List<VDS> getNormallyUtilizedCPUHosts(Cluster cluster,
                                                List<VDS> relevantHosts,
                                                final int highUtilization,
                                                final int cpuOverCommitDurationMinutes,
                                                final int highVdsCount) {
        Set<VDS> remainingHosts = new HashSet<>(relevantHosts);
        remainingHosts.removeAll(getOverUtilizedCPUHosts(remainingHosts, highUtilization, cpuOverCommitDurationMinutes));
        remainingHosts.removeAll(getUnderUtilizedCPUHosts(remainingHosts,
                highVdsCount,
                0,
                cpuOverCommitDurationMinutes));

        return new ArrayList<>(remainingHosts);
    }

    /**
     * Get hosts where the CPU has been utilized to more than highUtilization percentage for
     * more than cpuOverCommitDurationMinutes minutes.
     *
     * @param relevantHosts - candidate hosts
     * @param highUtilization - threshold cpu usage in percents
     * @param cpuOverCommitDurationMinutes - time limit in minutes
     * @return - over utilized hosts
     */
    protected List<VDS> getOverUtilizedCPUHosts(Collection<VDS> relevantHosts,
                                                final int highUtilization,
                                                final int cpuOverCommitDurationMinutes) {

        List<VDS> overUtilizedHosts = relevantHosts.stream()
                .filter(p -> (p.getUsageCpuPercent() + calcSpmCpuConsumption(p)) >= highUtilization
                    && p.getCpuOverCommitTimestamp() != null
                    && (getTime().getTime() - p.getCpuOverCommitTimestamp().getTime())
                    >= TimeUnit.MINUTES.toMillis(cpuOverCommitDurationMinutes)
                    && p.getVmCount() > 0)
                .collect(Collectors.toList());

        if (overUtilizedHosts.size() > 1) {
            // Assume all hosts belong to the same cluster
            Cluster cluster = getClusterDao().get(overUtilizedHosts.get(0).getClusterId());
            Collections.sort(overUtilizedHosts, new VdsCpuUsageComparator(
                    cluster != null && cluster.getCountThreadsAsCores()).reversed());
        }

        return overUtilizedHosts;
    }

    /**
     * Get hosts where the CPU is currently loaded to less than lowUtilization percents,
     * and which were over-utilized (in average) for more than cpuOverCommitDurationMinutes.
     *
     * Also filter out hosts with less than minVmCount VMs.
     *
     * minVmCount is useful for using this method to find a source of VMs for migration
     * (we do not care about hosts that have no VMs in that case). If you are looking
     * for a destination candidates, pass 0 there.
     *
     * @param relevantHosts - candidate hosts
     * @param lowUtilization - load threshold in percent
     * @param minVmCount - minimal number of VMs on a host
     * @param cpuOverCommitDurationMinutes - time limit in minutes
     */
    protected List<VDS> getUnderUtilizedCPUHosts(Collection<VDS> relevantHosts,
                                                 final int lowUtilization,
                                                 final int minVmCount,
                                                 final int cpuOverCommitDurationMinutes) {

        List<VDS> underUtilizedHosts = relevantHosts.stream()
                .filter(p -> (p.getUsageCpuPercent() + calcSpmCpuConsumption(p)) < lowUtilization
                    && p.getVmCount() >= minVmCount
                    && (p.getCpuOverCommitTimestamp() == null
                        || (getTime().getTime() - p.getCpuOverCommitTimestamp().getTime()) >=
                            TimeUnit.MINUTES.toMillis(cpuOverCommitDurationMinutes)))
                .collect(Collectors.toList());

        if (underUtilizedHosts.size() > 1) {
            // Assume all hosts belong to the same cluster
            Cluster cluster = getClusterDao().get(underUtilizedHosts.get(0).getClusterId());
            Collections.sort(underUtilizedHosts, new VdsCpuUsageComparator(
                    cluster != null && cluster.getCountThreadsAsCores()));
        }

        return underUtilizedHosts;
    }

    public static int calcSpmCpuConsumption(VDS vds) {
        return (vds.getSpmStatus() == VdsSpmStatus.None) ? 0 : Config
                .<Integer> getValue(ConfigValues.SpmVCpuConsumption)
                * Config.<Integer> getValue(ConfigValues.VcpuConsumptionPercentage) / vds.getCpuCores();
    }

    protected VdsDao getVdsDao() {
        return DbFacade.getInstance().getVdsDao();
    }

    protected VmDao getVmDao() {
        return vmDao;
    }

    protected VmStatisticsDao getVmStatisticsDao() {
        return vmStatisticsDao;
    }

    protected ClusterDao getClusterDao() {
        return DbFacade.getInstance().getClusterDao();
    }

    protected Date getTime() {
        return new Date();
    }

    protected int tryParseWithDefault(String candidate, int defaultValue) {
        if (candidate != null) {
            try {
                return Integer.parseInt(candidate);
            } catch (Exception e) {
                // do nothing
            }
        }
        return defaultValue;
    }

    /**
     * This method should return a configured FindVmAndDestinations objects for
     * cluster using the provided parameters.
     *
     * @param cluster - cluster instance for scheduling
     * @param parameters - scheduling parameters
     * @return - a configured FindVmAndDestinations instance
     */
    protected abstract FindVmAndDestinations getFindVmAndDestinations(Cluster cluster,
                                                                      Map<String, String> parameters);

    /**
     * Return a list of hosts that should be used as VM "donors" during the first
     * attempt of migration planning.
     *
     * @param cluster - cluster instance for scheduling
     * @param candidateHosts - all available hosts for this scheduling round
     * @param parameters - scheduling parameters
     * @return - subset of hosts from candidateHosts to be used as migration sources
     */
    protected abstract List<VDS> getPrimarySources(Cluster cluster,
                                                   List<VDS> candidateHosts,
                                                   Map<String, String> parameters);

    /**
     * Return a list of hosts that should be used as VM "receivers" during the first
     * attempt of migration planning.
     *
     * @param cluster - cluster instance for scheduling
     * @param candidateHosts - all available hosts for this scheduling round
     * @param parameters - scheduling parameters
     * @return - subset of hosts from candidateHosts to be used as migration destination
     */
    protected abstract List<VDS> getPrimaryDestinations(Cluster cluster,
                                                        List<VDS> candidateHosts,
                                                        Map<String, String> parameters);

    /**
     * Return a list of hosts that should be used as VM "donors" during the second
     * attempt of migration planning. The second attempt is used when there is no
     * possible (or needed) migration during the first attempt.
     *
     * @param cluster - cluster instance for scheduling
     * @param candidateHosts - all available hosts for this scheduling round
     * @param parameters - scheduling parameters
     * @return - subset of hosts from candidateHosts to be used as migration sources
     */
    protected abstract List<VDS> getSecondarySources(Cluster cluster,
                                                     List<VDS> candidateHosts,
                                                     Map<String, String> parameters);

    /**
     * Return a list of hosts that should be used as VM "receivers" during the second
     * attempt of migration planning. The second attempt is used when there is no
     * possible (or needed) migration during the first attempt.
     *
     * @param cluster - cluster instance for scheduling
     * @param candidateHosts - all available hosts for this scheduling round
     * @param parameters - scheduling parameters
     * @return - subset of hosts from candidateHosts to be used as migration destination
     */
    protected abstract List<VDS> getSecondaryDestinations(Cluster cluster,
                                                          List<VDS> candidateHosts,
                                                          Map<String, String> parameters);
}
