package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.commons.lang.math.NumberUtils;
import org.ovirt.engine.core.bll.scheduling.HaReservationHandling;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.external.BalanceResult;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This balancing policy, is for use in cases the user selected HA Reservation for its Cluster. The basic methodology
 * is: 1. get the optimal HA VMs for each VM assuming evenly spreaded across the cluster 2. calc the overUtilize as
 * (1)*user configured threshold in percent. 3. randomly choose a VM from a busy host to move to another more available
 * host.
 */
public class HaReservationBalancePolicyUnit extends PolicyUnitImpl {

    private static final Logger log = LoggerFactory.getLogger(HaReservationBalancePolicyUnit.class);

    private static final long serialVersionUID = 4926515666890804243L;

    public HaReservationBalancePolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<BalanceResult> balance(Cluster cluster,
            List<VDS> hosts,
            Map<String, String> parameters) {

        Objects.requireNonNull(hosts);
        Objects.requireNonNull(cluster);

        log.debug("Started HA reservation balancing method for cluster '{}'", cluster.getName());
        if (!cluster.supportsHaReservation()) {
            return Collections.emptyList();
        }
        if (hosts.size() < 2) {
            log.debug("No balancing for cluster '{}', contains only {} host(s)", cluster.getName(), hosts.size());
            return Collections.emptyList();
        }

        int haVmsInCluster = 0;

        Map<Guid, List<VM>> hostId2HaVmMapping = HaReservationHandling.mapHaVmToHostByCluster(cluster.getId());
        haVmsInCluster = countHaVmsInCluster(hostId2HaVmMapping);


        int optimalHaDistribution = (int) Math.ceil((double) haVmsInCluster / hosts.size());

        int overUtilizationParam = parameters.get("OverUtilization") != null ?
                NumberUtils.toInt(parameters.get("OverUtilization")) :
                Config.<Integer> getValue(ConfigValues.OverUtilizationForHaReservation);

        log.debug("optimalHaDistribution value: {}", optimalHaDistribution);

        int overUtilizationThreshold = (int) Math.ceil(optimalHaDistribution * (overUtilizationParam / 100.0));
        log.debug("overUtilizationThreshold value: {}", overUtilizationThreshold);

        List<VDS> overUtilizedHosts =
                getHostUtilizedByCondition(hosts, hostId2HaVmMapping, overUtilizationThreshold, Condition.MORE_THAN);
        if (overUtilizedHosts.isEmpty()) {
            log.debug("No over utilized hosts for cluster '{}'", cluster.getName());
            return Collections.emptyList();
        }

        List<VDS> underUtilizedHosts =
                getHostUtilizedByCondition(hosts, hostId2HaVmMapping, overUtilizationParam, Condition.LESS_THAN);
        if (underUtilizedHosts.size() == 0) {
            log.debug("No under utilized hosts for cluster '{}'", cluster.getName());
            return Collections.emptyList();
        }

        // Get random host from the over utilized hosts
        VDS randomHost = overUtilizedHosts.get(new Random().nextInt(overUtilizedHosts.size()));

        List<VM> migrableVmsOnRandomHost = getMigrableVmsRunningOnVds(randomHost.getId(), hostId2HaVmMapping);
        if (migrableVmsOnRandomHost.isEmpty()) {
            log.debug("No migratable hosts were found for cluster '{}'", cluster.getName());
            return Collections.emptyList();
        }

        // Get random vm to migrate
        VM vm = migrableVmsOnRandomHost.get(new Random().nextInt(migrableVmsOnRandomHost.size()));
        log.info("VM to be migrated '{}'", vm.getName());

        List<Guid> underUtilizedHostsKeys = new ArrayList<>();
        for (VDS vds : underUtilizedHosts) {
            underUtilizedHostsKeys.add(vds.getId());
        }

        return Collections.singletonList(new BalanceResult(vm.getId(), underUtilizedHostsKeys));

    }

    private int countHaVmsInCluster(Map<Guid, List<VM>> hostId2HaVmMapping) {
        int result = 0;
        for (Entry<Guid, List<VM>> entry : hostId2HaVmMapping.entrySet()) {
            result += entry.getValue().size();
        }
        return result;
    }

    private List<VDS> getHostUtilizedByCondition(List<VDS> hosts,
            Map<Guid, List<VM>> hostId2HaVmMapping,
            int UtilizationThreshold, Condition cond) {

        List<VDS> utilizedHosts = new ArrayList<>();

        for (VDS host : hosts) {
            int count = 0;
            List<VM> vms = hostId2HaVmMapping.get(host.getId());
            if (vms != null) {
                count = vms.size();
            }

            if (cond.equals(Condition.LESS_THAN)) {
                if (count < UtilizationThreshold) {
                    utilizedHosts.add(host);
                }
            } else if (cond.equals(Condition.MORE_THAN)) {
                if (count >= UtilizationThreshold) {
                    utilizedHosts.add(host);
                }

            }
        }
        return utilizedHosts;

    }

    private enum Condition {
        LESS_THAN,
        MORE_THAN
    }

    private List<VM> getMigrableVmsRunningOnVds(final Guid hostId, Map<Guid, List<VM>> hostId2HaVmMapping) {
        return hostId2HaVmMapping.get(hostId).stream()
                .filter(v -> v.getMigrationSupport() == MigrationSupport.MIGRATABLE).collect(Collectors.toList());
    }
}
