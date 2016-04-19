package org.ovirt.engine.core.bll.scheduling.arem;

import static java.util.Collections.min;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to detect affinity group violations and select VMs for
 * migration, to resolve the violations.
 */
public class AffinityRulesEnforcer {

    private static final Logger log = LoggerFactory.getLogger(AffinityRulesEnforcer.class);

    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private SchedulingManager schedulingManager;
    private final Random random = new Random();

    protected enum FailMode {
        IMMEDIATELY, // Fail when first violation is detected
        GET_ALL // Collect all violations
    }

    public VM chooseNextVmToMigrate(Cluster cluster) {
        List<AffinityGroup> allHardAffinityGroups = getAllHardAffinityGroups(cluster);

        Set<Set<Guid>> unifiedPositiveAffinityGroups = AffinityRulesUtils.getUnifiedPositiveAffinityGroups(
                allHardAffinityGroups);
        List<AffinityGroup> unifiedAffinityGroups = AffinityRulesUtils.setsToAffinityGroups(
                unifiedPositiveAffinityGroups);

        // Add negative affinity groups
        for (AffinityGroup ag : allHardAffinityGroups) {
            if (!ag.isPositive()) {
                unifiedAffinityGroups.add(ag);
            }
        }

        // Create a set of all VMs in affinity groups
        Set<Guid> allVms = new HashSet<>();
        for (AffinityGroup group : unifiedAffinityGroups) {
            allVms.addAll(group.getEntityIds());
        }

        Map<Guid, Guid> vmToHost = createMapOfVmToHost(allVms);

        // There is no need to migrate when no collision was detected
        Set<AffinityGroup> violatedAffinityGroups =
                checkForAffinityGroupViolations(unifiedAffinityGroups, vmToHost, FailMode.GET_ALL);
        if (violatedAffinityGroups.isEmpty()) {
            log.debug("No affinity group collision detected for cluster {}. Standing by.", cluster.getId());
            return null;
        }

        // Find a VM that is breaking the affinityGroup and can be theoretically migrated
        // - start with bigger Affinity Groups
        List<AffinityGroup> affGroupsBySize = new ArrayList<>(violatedAffinityGroups);
        Collections.sort(affGroupsBySize, Collections.reverseOrder(new AffinityGroupComparator()));

        for (AffinityGroup affinityGroup : affGroupsBySize) {
            final List<VM> candidateVms;

            if (affinityGroup.isPositive()) {
                candidateVms = vmDao.getVmsByIds(findVmViolatingPositiveAg(affinityGroup, vmToHost));
                log.info("Positive affinity group violation detected");
            } else {
                candidateVms = vmDao.getVmsByIds(findVmViolatingNegativeAg(affinityGroup, vmToHost));
                log.info("Negative affinity group violation detected");
            }

            while (!candidateVms.isEmpty()) {
                final int index = random.nextInt(candidateVms.size());
                final VM candidateVm = candidateVms.get(index);

                // Test whether any migration is possible, this uses current AffinityGroup settings
                // and so won't allow more breakage
                boolean canMove = schedulingManager.canSchedule(cluster, candidateVm,
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), new ArrayList<>());

                if (canMove) {
                    log.debug("VM {} is a viable candidate for solving affinity group violation situation.",
                            candidateVm.getId());
                    return candidateVm;
                }
                log.debug("VM {} is NOT a viable candidate for solving affinity group violation situation.",
                        candidateVm.getId());
                candidateVms.remove(index);
            }

        }

        // No possible migration..
        return null;
    }

    private Map<Guid, Guid> createMapOfVmToHost(Set<Guid> allVms) {
        Map<Guid, Guid> outputMap = new HashMap<>();

        for (VM vm : vmDao.getVmsByIds(new ArrayList<>(allVms))) {
            Guid hostId = vm.getRunOnVds();

            if (hostId != null) {
                outputMap.put(vm.getId(), hostId);
            }
        }

        return outputMap;
    }

    /**
     * Select VMs from the broken affinity group that are running on the same host.
     *
     * @param affinityGroup broken affinity rule
     * @param vmToHost      vm to host assignments
     * @return a list of vms which are candidates for migration
     */
    private List<Guid> findVmViolatingNegativeAg(AffinityGroup affinityGroup, Map<Guid, Guid> vmToHost) {
        Map<Guid, Guid> firstAssignment = new HashMap<>();
        Set<Guid> violatingVms = new HashSet<>();

        // When a VM runs on an already occupied host, report both
        // the vm and the previous occupant as candidates for migration
        for (Guid vm : affinityGroup.getEntityIds()) {
            Guid host = vmToHost.get(vm);

            // Ignore stopped VMs
            if (host == null) {
                continue;
            }

            if (firstAssignment.containsKey(host)) {
                violatingVms.add(vm);
                violatingVms.add(firstAssignment.get(host));
            } else {
                firstAssignment.put(host, vm);
            }
        }

        List<Guid> violatingVmsArray = new ArrayList<>(violatingVms);
        return violatingVmsArray;
    }

    /**
     * Select VMs from the broken affinity group that are running on the host with the minimal amount
     * of VMs from the broken affinity group.
     * <p>
     * Ex.: Host1: A, B, C, D  Host2: E, F  -> select E or F
     *
     * @param affinityGroup broken affinity group
     * @param vmToHost      vm to host assignments
     * @return a list of vms which are candidates for migration
     */
    private List<Guid> findVmViolatingPositiveAg(AffinityGroup affinityGroup, Map<Guid, Guid> vmToHost) {
        Map<Guid, List<Guid>> hostCount = new HashMap<>();

        // Prepare affinity group related host counts
        for (Guid vm : affinityGroup.getEntityIds()) {
            Guid host = vmToHost.get(vm);

            // Ignore stopped VMs
            if (host == null) {
                continue;
            }

            if (hostCount.containsKey(host)) {
                hostCount.get(host).add(vm);
            } else {
                hostCount.put(host, new ArrayList<>());
                hostCount.get(host).add(vm);
            }
        }

        // Select the host with the least amount of VMs
        Guid host = chooseCandidateHostForMigration(hostCount);
        if (host == null) {
            return Collections.emptyList();
        }

        return hostCount.get(host);
    }

    /**
     * Select a host to source a VM belonging to the Affinity Group. The assumption here is that
     * the host with the lowest amount of VMs from the affinity group is the best source,
     * because the number of needed migrations will be minimal when compared to other solutions.
     */
    protected Guid chooseCandidateHostForMigration(Map<Guid, ? extends Collection<Guid>> mapOfHostsToVms) {
        int maxNumberOfVms = Integer.MAX_VALUE;
        Guid bestHost = null;

        for (Map.Entry<Guid, ? extends Collection<Guid>> entry : mapOfHostsToVms.entrySet()) {
            if (entry.getValue().size() < maxNumberOfVms) {
                maxNumberOfVms = entry.getValue().size();
                bestHost = entry.getKey();
            }
        }

        return bestHost;
    }

    /**
     * Detect whether the current VM to VDS assignment violates current Affinity Groups.
     *
     * @param affinityGroups Unified affinity groups
     * @param vmToHost       Mapping of VM to currently assigned VDS
     * @return broken AffinityGroups
     */
    protected static Set<AffinityGroup> checkForAffinityGroupViolations(Iterable<AffinityGroup> affinityGroups,
            Map<Guid, Guid> vmToHost, FailMode mode) {

        Set<AffinityGroup> broken = new HashSet<>();

        for (AffinityGroup affinity : affinityGroups) {
            // Negative groups
            if (!affinity.isPositive()) {
                // Record all hosts that are already occupied by VMs from this group
                Map<Guid, Guid> usedHosts = new HashMap<>();

                for (Guid vm : affinity.getEntityIds()) {
                    Guid host = vmToHost.get(vm);
                    if (host == null) {
                        continue;
                    }

                    // Report a violation when any host has more than one VM from this group
                    if (usedHosts.containsKey(host)) {
                        log.debug("Negative affinity rule violated between VMs {} and {} on host {}",
                                vm, usedHosts.get(host), host);
                        broken.add(affinity);

                        if (mode.equals(FailMode.IMMEDIATELY)) {
                            return broken;
                        }
                    } else {
                        usedHosts.put(host, vm);
                    }
                }

                // Positive groups
            } else {
                // All VMs from this group have to be running on a single host
                Guid targetHost = null;

                for (Guid vm : affinity.getEntityIds()) {
                    Guid host = vmToHost.get(vm);
                    if (host == null) {
                        continue;
                    }

                    // Report a violation when two VMs do not share a common host
                    if (targetHost != null && !targetHost.equals(host)) {
                        log.debug("Positive affinity rule violated by VM {} running at {} when other VM(s) are at {}",
                                vm, host, targetHost);
                        broken.add(affinity);

                        if (mode.equals(FailMode.IMMEDIATELY)) {
                            return broken;
                        }
                    } else if (targetHost == null) {
                        targetHost = host;
                    }
                }
            }
        }

        return broken;
    }

    public List<AffinityGroup> getAllHardAffinityGroups(Cluster cluster) {
        return affinityGroupDao.getAllAffinityGroupsByClusterId(cluster.getId())
                .stream().filter(AffinityGroup::isEnforcing).collect(Collectors.toList());
    }

    private static class AffinityGroupComparator implements Comparator<AffinityGroup>, Serializable {
        @Override
        public int compare(AffinityGroup thisAffinityGroup, AffinityGroup thatAffinityGroup) {
            final List<Guid> thisEntityIds = thisAffinityGroup.getEntityIds();
            final List<Guid> otherEntityIds = thatAffinityGroup.getEntityIds();

            // Avoid NoSuchElementExceptions from Collections.min()
            if (thisEntityIds.isEmpty() && otherEntityIds.isEmpty()) {
                return 0;
            }

            int diff = Integer.compare(thisEntityIds.size(), otherEntityIds.size());

            // Merged affinity groups do not have an ID, so use the VM with the tiniest ID instead
            return diff != 0 ? diff : min(thisEntityIds).compareTo(min(otherEntityIds));
        }
    }
}
