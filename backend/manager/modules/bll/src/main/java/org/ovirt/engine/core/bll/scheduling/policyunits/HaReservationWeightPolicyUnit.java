package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.HaReservationHandling;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HaReservationWeightPolicyUnit extends PolicyUnitImpl {

    private static final Logger log = LoggerFactory.getLogger(HaReservationWeightPolicyUnit.class);

    private static final int RATIO_FACTOR = 100;
    private static final int DEFAULT_SCORE = 0;

    public HaReservationWeightPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<Pair<Guid, Integer>> score(List<VDS> hosts, VM vm, Map<String, String> parameters) {

        log.info("Started HA reservation scoring method");
        List<Pair<Guid, Integer>> scores = new ArrayList<Pair<Guid, Integer>>();

        Map<Guid, Integer> hostsHaVmCount = new HashMap<Guid, Integer>();

        // If the vm is not HA or the cluster is not marked as HA Reservation set default score.
        VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(hosts.get(0).getVdsGroupId());

        if (!vm.isAutoStartup() || !vdsGroup.supportsHaReservation()) {
            fillDefaultScores(hosts, scores);
        } else {
            // Use a single call to the DB to retrieve all VM in the Cluster and map them by Host id
            Map<Guid, List<VM>> hostId2HaVmMapping = HaReservationHandling.mapHaVmToHostByCluster(vdsGroup.getId());

            int maxCount = 0;
            for (VDS host : hosts) {

                int count = 0;
                if (hostId2HaVmMapping.containsKey(host.getId())) {
                    count = hostId2HaVmMapping.get(host.getId()).size();
                }
                maxCount = (maxCount < count) ? count : maxCount;
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
            Integer scaleDownParameter = 1;
            if (parameters.get("ScaleDown") != null) {
                scaleDownParameter = Integer.parseInt(parameters.get("ScaleDown"));
            } else {
                scaleDownParameter = Config.<Integer> getValue(ConfigValues.ScaleDownForHaReservation);
            }

            // Set the score pairs
            for (VDS host : hosts) {
                // Scale down if needed
                int haCount = hostsHaVmCount.get(host.getId());
                haCount = (int) Math.ceil(haCount / scaleDownParameter.floatValue());

                scores.add(new Pair<Guid, Integer>(host.getId(), haCount));

                log.info("Score for host '{}' is {}", host.getName(), haCount);
            }

        }

        return scores;
    }

    // Fill all host with a neutral score
    private void fillDefaultScores(List<VDS> hosts, List<Pair<Guid, Integer>> scores) {
        for (VDS host : hosts) {
            scores.add(new Pair<Guid, Integer>(host.getId(), DEFAULT_SCORE));
        }

    }

}
