package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.HaReservationHandling;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitParameter;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "7f262d70-6cac-11e3-981f-0800200c9a66",
        name = "OptimalForHaReservation",
        description = "Weights hosts according to their HA score regardless of hosted engine",
        type = PolicyUnitType.WEIGHT,
        parameters = PolicyUnitParameter.SCALE_DOWN
)
public class HaReservationWeightPolicyUnit extends PolicyUnitImpl {

    private static final Logger log = LoggerFactory.getLogger(HaReservationWeightPolicyUnit.class);

    private static final int RATIO_FACTOR = 100;
    private static final int DEFAULT_SCORE = 0;

    public HaReservationWeightPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, VM vm) {

        log.debug("Started HA reservation scoring method");
        List<Pair<Guid, Integer>> scores = new ArrayList<>();

        Map<Guid, Integer> hostsHaVmCount = new HashMap<>();

        // If the vm is not HA or the cluster is not marked as HA Reservation set default score.
        if (!vm.isAutoStartup() || !context.getCluster().supportsHaReservation()) {
            fillDefaultScores(hosts, scores);
        } else {
            // Use a single call to the DB to retrieve all VM in the Cluster and map them by Host id
            Map<Guid, List<VM>> hostId2HaVmMapping = HaReservationHandling.mapHaVmToHostByCluster(context.getCluster().getId());

            int maxCount = 0;
            for (VDS host : hosts) {

                int count = 0;
                if (hostId2HaVmMapping.containsKey(host.getId())) {
                    count = hostId2HaVmMapping.get(host.getId()).size();
                }
                maxCount = Math.max(maxCount, count);
                hostsHaVmCount.put(host.getId(), count);
            }

            // Fit count to scale of 0 to RATIO_FACTOR
            if (maxCount > 0) {
                for (VDS host : hosts) {
                    int fittedCount =
                            Math.round(hostsHaVmCount.get(host.getId()).floatValue() / maxCount * RATIO_FACTOR);
                    hostsHaVmCount.put(host.getId(), fittedCount);
                }
            }

            // Get scale down param
            Integer scaleDownParameter = context.getPolicyParameters().containsKey(PolicyUnitParameter.SCALE_DOWN.getDbName()) ?
                    Integer.parseInt(context.getPolicyParameters().get(PolicyUnitParameter.SCALE_DOWN.getDbName())) :
                    Config.<Integer> getValue(ConfigValues.ScaleDownForHaReservation);

            // Set the score pairs
            for (VDS host : hosts) {
                // Scale down if needed
                int haCount = hostsHaVmCount.get(host.getId());
                haCount = (int) Math.ceil(haCount / scaleDownParameter.floatValue());

                scores.add(new Pair<>(host.getId(), haCount));

                log.info("Score for host '{}' is {}", host.getName(), haCount);
            }

        }

        log.debug("Ended HA reservation scoring method");
        return scores;
    }

    // Fill all host with a neutral score
    private void fillDefaultScores(List<VDS> hosts, List<Pair<Guid, Integer>> scores) {
        for (VDS host : hosts) {
            scores.add(new Pair<>(host.getId(), DEFAULT_SCORE));
        }

    }

}
