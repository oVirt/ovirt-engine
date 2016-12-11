package org.ovirt.engine.core.bll.scheduling.arem;

import static java.util.Collections.min;
import static java.util.Comparator.comparingInt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    /**
     * Choose a valid VM for migration by applying affinity rules in the following order:
     * <p>
     * 1.VM to Hosts Affinity
     * 2.VM to VM affinity
     *
     * @param cluster current cluster
     * @return Valid VM for migration, null otherwise
     */
    public VM chooseNextVmToMigrate(Cluster cluster) {
        List<AffinityGroup> allAffinityGroups = affinityGroupDao.getAllAffinityGroupsByClusterId(cluster.getId());

        Optional<VM> vm = chooseNextVmToMigrateFromVMsToHostsAffinity(cluster, allAffinityGroups);
        if (vm.isPresent()) {
            return vm.get();
        }
        return chooseNextVmToMigrateFromVMsAffinity(cluster, allAffinityGroups);
    }

    /**
     * Choose a VM to migrate by applying VM to host affinity rules.
     * Candidate VMs will selected in the following order:
     * <p>
     * 1.Candidate VMs violating positive enforcing affinity to hosts.
     * 2.Candidate VMs violating negative enforcing affinity to hosts.
     * 3.Candidate VMs violating positive non enforcing affinity to hosts.
     * 4.Candidate VMs violating negative non enforcing affinity to hosts.
     *
     * @param cluster           Current cluster
     * @param allAffinityGroups All affinity groups for the current cluster.
     * @return Valid VM for migration by VM to host affinity, empty result otherwise
     */
    private Optional<VM> chooseNextVmToMigrateFromVMsToHostsAffinity(Cluster cluster, List<AffinityGroup>
            allAffinityGroups) {

        List<AffinityGroup> allVmToHostsAffinityGroups = getAllAffinityGroupsForVMsToHostsAffinity(allAffinityGroups);

        if (allVmToHostsAffinityGroups.isEmpty()) {
            return Optional.empty();
        }
        Map<Guid, VM> vmsMap = getVMsMap(allVmToHostsAffinityGroups);

        List<Guid> candidateVMsPositiveEnforcingAffinity =
                getVmToHostsAffinityGroupCandidates(allVmToHostsAffinityGroups, vmsMap, true, true);
        List<Guid> candidateVMsNegativeEnforcingAffinity =
                getVmToHostsAffinityGroupCandidates(allVmToHostsAffinityGroups, vmsMap, false, true);
        List<Guid> candidateVMsPositiveNonEnforcingAffinity =
                getVmToHostsAffinityGroupCandidates(allVmToHostsAffinityGroups, vmsMap, true, false);
        List<Guid> candidateVMsNegativeNonEnforcingAffinity =
                getVmToHostsAffinityGroupCandidates(allVmToHostsAffinityGroups, vmsMap, false, false);

        //TODO check intersections between candidate groups and warn user if needed.

        List<Guid> candidateVMs = new ArrayList<>();
        candidateVMs.addAll(candidateVMsPositiveEnforcingAffinity);
        candidateVMs.addAll(candidateVMsNegativeEnforcingAffinity);
        candidateVMs.addAll(candidateVMsPositiveNonEnforcingAffinity);
        candidateVMs.addAll(candidateVMsNegativeNonEnforcingAffinity);

        if (!candidateVMs.isEmpty()) {
            log.info("vm to hosts affinity group violation detected");
        }

        for (Guid id : candidateVMs) {
            VM candidateVM = vmsMap.get(id);
            if (isVmMigrationValid(cluster, candidateVM)) {
                return Optional.of(candidateVM);
            }
        }
        return Optional.empty();
    }

    /**
     * Create a VM id to VM object map from the affinity groups list input.
     * Each VM will appear only once in the map.
     * <p>
     * Example: Given affinity group 1 containing VM ids {1,2,3} and affinity group 2 containing VM ids {3,4}
     * the resultant map would be {(1,Vm1),(2,Vm2),(3,Vm3),(4,Vm4)}.
     *
     * @param allVMtoHostsAffinityGroups All VM to hosts affinity groups for the current cluster
     * @return VMs map with key: id, value: associated vm object
     */
    private Map<Guid, VM> getVMsMap(List<AffinityGroup> allVMtoHostsAffinityGroups) {
        Map<Guid, VM> vmsMap = vmDao.getVmsByIds(allVMtoHostsAffinityGroups.stream()
                .map(AffinityGroup::getVmIds)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(VM::getId, vm -> vm));

        return vmsMap;
    }

    /**
     * Get a list of candidate VMs (by VM ids) from the VM to host affinity groups.
     * This list will contain all VMs that violate the host positive
     * and host enforcing policies of the given affinity groups.
     *
     * @param allVMtoHostsAffinityGroups VM to Host affinity groups.
     * @param vmsMap                     VMs map with key: vm id , value: associated vm object.
     * @param isVdsAffinityPositive      true - positive affinity, false - negative affinity.
     * @param isVdsAffinityEnforcing     true - Hard affinity constraint, false - Soft affinity constraint.
     * @return list of candidate VMs for migration by VM to Host affinities.
     */
    private List<Guid> getVmToHostsAffinityGroupCandidates(List<AffinityGroup> allVMtoHostsAffinityGroups,
            Map<Guid, VM> vmsMap, boolean isVdsAffinityPositive, boolean isVdsAffinityEnforcing) {
        Map<Guid, Set<Guid>> vmToHostsAffinityMap = new HashMap<>();
        allVMtoHostsAffinityGroups.stream()
                .filter(ag -> ag.getVdsPolarityBooleanObject().equals(isVdsAffinityPositive))
                .filter(ag -> ag.isVdsEnforcing() == isVdsAffinityEnforcing)
                .forEach(g -> {
                    g.getVmIds()
                            .forEach(vm_id -> {
                                mapVmsToHosts(vm_id, vmToHostsAffinityMap, vmsMap, g);
                            });
                });

        return getSortedVmsViolatingVmToHostsAffinity(vmToHostsAffinityMap, vmsMap, isVdsAffinityPositive);
    }

    /**
     * Determines the set of hosts on which the indicated VM
     * can or cannot run (according to positive/negative affinity)
     * from those within the provided affinity group and adds them to the VM to Hosts Affinity map
     *
     * @param id                   VM id
     * @param vmToHostsAffinityMap current VM to Hosts Affinity map to be updated.
     * @param vmsMap               VMs map with key: vm id , value :associated vm object.
     * @param affinityGroup        Current affinity group.
     */
    private void mapVmsToHosts(Guid id, Map<Guid, Set<Guid>> vmToHostsAffinityMap, Map<Guid, VM> vmsMap,
            AffinityGroup affinityGroup) {

        //non running vms are not handled
        if (vmsMap.get(id).getRunOnVds() == null) {
            return;
        }
        Set<Guid> hosts = new HashSet<>(affinityGroup.getVdsIds());
        Set<Guid> currentHosts = vmToHostsAffinityMap.get(id);
        if (currentHosts == null) {
            vmToHostsAffinityMap.put(id, hosts);
        } else {
            vmToHostsAffinityMap.get(id).addAll(hosts);
            if (affinityGroup.isVdsPositive()) {
                Set<Guid> intersections = new HashSet<>(currentHosts);
                intersections.retainAll(hosts);
                if (intersections.isEmpty()) {
                    log.warn("VM {} {} belongs to a completely different positive affinity hosts group: {}.", id, vmsMap
                            .get(id).getName(), affinityGroup.getName());
                }
            }
        }
    }

    /**
     * Check affinity group violations according to VM to Host affinity rules
     * and return a sorted list of violating VMs.
     * <p>
     * Possible violations:
     * <p>
     * 1.Positive affinity -
     * VMs that are not running on any of the hosts from vmToHostsAffinityMap are violating positive affinity.
     * The candidates are sorted by the largest hosts group first (as taken from vmToHostsAffinityMap).
     * 2.Negative affinity -
     * VMs that are running on at least one of the hosts from vmToHostsAffinityMap are violating negative affinity.
     * The candidates are sorted by the smallest hosts group first (as taken from vmToHostsAffinityMap).
     *
     * @param vmToHostsAffinityMap  current VM to Hosts Affinity map.
     * @param vmsMap                VMs map with key: vm id, value: associated vm object.
     * @param isVdsAffinityPositive true - positive affinity, false - negative affinity.
     * @return Sorted list of candidate VMs that violate VM to Host affinity.
     */
    private List<Guid> getSortedVmsViolatingVmToHostsAffinity(Map<Guid, Set<Guid>> vmToHostsAffinityMap,
            Map<Guid, VM> vmsMap, boolean isVdsAffinityPositive) {

        List<Guid> nonViolatingVms = vmToHostsAffinityMap.keySet().stream()
                .filter(vm_id -> {
                    Set<Guid> hosts = vmToHostsAffinityMap.get(vm_id);
                    boolean vmRunsOnAffinityHost = hosts.contains(vmsMap.get(vm_id).getRunOnVds());

                    if ((isVdsAffinityPositive && vmRunsOnAffinityHost)
                            || (!isVdsAffinityPositive && !vmRunsOnAffinityHost)) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());

        vmToHostsAffinityMap.keySet().removeAll(nonViolatingVms);

        List<Guid> candidateVMids = new ArrayList<>();

        //for negative affinity groups sort the candidates from the smallest hosts group first
        vmToHostsAffinityMap.entrySet()
                .stream()
                .sorted(comparingInt(e -> e.getValue().size()))
                .forEachOrdered(x -> candidateVMids.add(x.getKey()));

        //for positive affinity groups sort the candidates from the largest hosts group first
        if (isVdsAffinityPositive) {
            Collections.reverse(candidateVMids);
        }
        return candidateVMids;
    }

    private VM chooseNextVmToMigrateFromVMsAffinity(Cluster cluster, List<AffinityGroup> allAffinityGroups) {

        List<AffinityGroup> allHardAffinityGroups = getAllHardAffinityGroupsForVMsAffinity(allAffinityGroups);
        Set<Set<Guid>> unifiedPositiveAffinityGroups = AffinityRulesUtils.getUnifiedPositiveAffinityGroups(
                allHardAffinityGroups);
        List<AffinityGroup> unifiedAffinityGroups = AffinityRulesUtils.setsToAffinityGroups(
                unifiedPositiveAffinityGroups);

        // Add negative affinity groups
        for (AffinityGroup ag : allHardAffinityGroups) {
            if (ag.isVmNegative()) {
                unifiedAffinityGroups.add(ag);
            }
        }

        // Create a set of all VMs in affinity groups
        Set<Guid> allVms = new HashSet<>();
        for (AffinityGroup group : unifiedAffinityGroups) {
            allVms.addAll(group.getVmIds());
        }

        Map<Guid, Guid> vmToHost = createMapOfVmToHost(allVms);

        // There is no need to migrate when no collision was detected
        Set<AffinityGroup> violatedAffinityGroups =
                checkForVMAffinityGroupViolations(unifiedAffinityGroups, vmToHost, FailMode.GET_ALL);
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

            if (affinityGroup.isVmPositive()) {
                candidateVms = vmDao.getVmsByIds(findVmViolatingPositiveAg(affinityGroup, vmToHost));
                log.info("Positive affinity group violation detected");
            } else if (affinityGroup.isVmNegative()) {
                candidateVms = vmDao.getVmsByIds(findVmViolatingNegativeAg(affinityGroup, vmToHost));
                log.info("Negative affinity group violation detected");
            } else {
                continue;
            }

            while (!candidateVms.isEmpty()) {
                final int index = random.nextInt(candidateVms.size());
                final VM candidateVm = candidateVms.get(index);
                if (isVmMigrationValid(cluster, candidateVm)) {
                    return candidateVm;
                }
                candidateVms.remove(index);
            }

        }

        // No possible migration..
        return null;
    }

    /**
     * Test whether any migration is possible using current
     * AffinityGroup settings to prevent any further breakage.
     *
     * @param cluster     Current cluster.
     * @param candidateVm VM candidate for migration.
     * @return true - if the candidate VM is a viable candidate for solving the affinity group violation situation.
     * false - otherwise.
     */
    private boolean isVmMigrationValid(Cluster cluster, VM candidateVm) {

        List<Guid> vdsBlackList =
                candidateVm.getRunOnVds() == null ?
                        Collections.emptyList() : Arrays.asList(candidateVm.getRunOnVds());

        boolean canMove = schedulingManager.canSchedule(cluster, candidateVm,
                vdsBlackList, Collections.emptyList(), new ArrayList<>());

        if (canMove) {
            log.debug("VM {} is a viable candidate for solving the affinity group violation situation.",
                    candidateVm.getId());
            return true;
        }
        log.debug("VM {} is NOT a viable candidate for solving the affinity group violation situation.",
                candidateVm.getId());
        return false;
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
        for (Guid vm : affinityGroup.getVmIds()) {
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
        for (Guid vm : affinityGroup.getVmIds()) {
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
    protected static Set<AffinityGroup> checkForVMAffinityGroupViolations(Iterable<AffinityGroup> affinityGroups,
            Map<Guid, Guid> vmToHost, FailMode mode) {

        Set<AffinityGroup> broken = new HashSet<>();

        for (AffinityGroup affinity : affinityGroups) {
            // Negative groups
            if (affinity.isVmNegative()) {
                // Record all hosts that are already occupied by VMs from this group
                Map<Guid, Guid> usedHosts = new HashMap<>();

                for (Guid vm : affinity.getVmIds()) {
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
            } else if (affinity.isVmPositive()) {
                // All VMs from this group have to be running on a single host
                Guid targetHost = null;

                for (Guid vm : affinity.getVmIds()) {
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

    private List<AffinityGroup> getAllHardAffinityGroupsForVMsAffinity(List<AffinityGroup> allAffinityGroups) {
        return allAffinityGroups.stream()
                .filter(AffinityGroup::isVmAffinityEnabled)
                .filter(AffinityGroup::isVmEnforcing)
                .collect(Collectors.toList());
    }

    private List<AffinityGroup> getAllAffinityGroupsForVMsToHostsAffinity(List<AffinityGroup> allAffinityGroups) {
        return allAffinityGroups.stream()
                .filter(g -> !g.getVdsIds().isEmpty() && !g.getVmIds().isEmpty())
                .collect(Collectors.toList());
    }

    private static class AffinityGroupComparator implements Comparator<AffinityGroup>, Serializable {
        @Override
        public int compare(AffinityGroup thisAffinityGroup, AffinityGroup thatAffinityGroup) {
            final List<Guid> thisEntityIds = thisAffinityGroup.getVmIds();
            final List<Guid> otherEntityIds = thatAffinityGroup.getVmIds();

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
