package org.ovirt.engine.core.bll.scheduling.arem;

import static java.util.Collections.min;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
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
     * 1.Candidate VMs violating enforcing affinity to hosts.
     * 2.Candidate VMs violating non enforcing affinity to hosts.
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
        Map<Guid, VM> vmsMap = getRunningVMsMap(allVmToHostsAffinityGroups);

        List<Guid> candidateVMs =
                getVmToHostsAffinityGroupCandidates(allVmToHostsAffinityGroups, vmsMap, true);

        if (candidateVMs.isEmpty()) {
            log.debug("No vm to hosts hard-affinity group violation detected");
        } else {
            List<AffinityRulesUtils.AffinityGroupConflicts> conflicts = AffinityRulesUtils
                    .checkForAffinityGroupHostsConflict(allVmToHostsAffinityGroups);
            for (AffinityRulesUtils.AffinityGroupConflicts conflict : conflicts) {
                if (conflict.isVmToVmAffinity()) {
                    log.warn(conflict.getType().getMessage(),
                            conflict.getVms().stream()
                                    .map(id -> id.toString())
                                    .collect(Collectors.joining(",")),
                            AffinityRulesUtils.getAffinityGroupsNames(conflict.getAffinityGroups()),
                            conflict.getNegativeVms().stream()
                                    .map(id -> id.toString())
                                    .collect(Collectors.joining(","))
                    );
                } else {
                    log.warn(conflict.getType().getMessage(),
                            AffinityRulesUtils.getAffinityGroupsNames(conflict.getAffinityGroups()),
                            conflict.getHosts().stream()
                                    .map(id -> id.toString())
                                    .collect(Collectors.joining(",")),
                            conflict.getVms().stream()
                                    .map(id -> id.toString())
                                    .collect(Collectors.joining(",")));
                }
            }
        }

        for (Guid id : candidateVMs) {
            VM candidateVM = vmsMap.get(id);
            if (isVmMigrationValid(cluster, candidateVM)) {
                return Optional.of(candidateVM);
            }
        }

        candidateVMs =
                getVmToHostsAffinityGroupCandidates(allVmToHostsAffinityGroups, vmsMap, false);

        if (candidateVMs.isEmpty()) {
            log.debug("No vm to hosts soft-affinity group violation detected");
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
     * The map will contain only running VMs and each VM will appear only once.
     * <p>
     * Example: Given affinity group 1 containing VM ids {1,2,3} and affinity group 2 containing VM ids {3,4}
     * the resultant map would be {(1,Vm1),(2,Vm2),(3,Vm3),(4,Vm4)}.
     *
     * @param allVMtoHostsAffinityGroups All VM to hosts affinity groups for the current cluster
     * @return VMs map with key: id, value: associated vm object
     */
    private Map<Guid, VM> getRunningVMsMap(List<AffinityGroup> allVMtoHostsAffinityGroups) {
        List<Guid> vmIds = allVMtoHostsAffinityGroups.stream()
                .map(AffinityGroup::getVmIds)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        return vmDao.getVmsByIds(vmIds).stream()
                .filter(VM::isRunning)
                .collect(Collectors.toMap(VM::getId, vm -> vm));
    }

    /**
     * Get a list of candidate VMs (by VM ids) from the VM to host affinity groups.
     * This list will contain all VMs that violate the host affinity policies
     * sorted according to the number of violations (descending).
     *
     * @param allVMtoHostsAffinityGroups VM to Host affinity groups.
     * @param vmsMap                     VMs map with key: vm id , value: associated vm object.
     * @param isVdsAffinityEnforcing     true - Hard affinity constraint, false - Soft affinity constraint.
     * @return list of candidate VMs for migration by VM to Host affinities.
     */
    private List<Guid> getVmToHostsAffinityGroupCandidates(List<AffinityGroup> allVMtoHostsAffinityGroups,
            Map<Guid, VM> vmsMap, boolean isVdsAffinityEnforcing) {
        Map<Guid, Integer> vmToHostsAffinityMap = new HashMap<>();

        // Iterate over all affinity groups and check the currently running
        // VMs for compliance, record violations per VM
        allVMtoHostsAffinityGroups.stream()
                .filter(AffinityGroup::isVdsAffinityEnabled)
                .filter(ag -> ag.isVdsEnforcing() == isVdsAffinityEnforcing)
                .forEach(g -> {
                    Set<Guid> affHosts = new HashSet<>(g.getVdsIds());
                    g.getVmIds()
                            .forEach(vm_id -> {
                                VM vm = vmsMap.get(vm_id);

                                if (vm == null) {
                                    return;
                                }

                                if (affHosts.contains(vm.getRunOnVds()) && !g.isVdsPositive()) {
                                    // Negative affinity violated
                                    vmToHostsAffinityMap.put(vm_id,
                                            1 + vmToHostsAffinityMap.getOrDefault(vm_id, 0));
                                } else if (!affHosts.contains(vm.getRunOnVds()) && g.isVdsPositive()) {
                                    // Positive affinity violated
                                    vmToHostsAffinityMap.put(vm_id,
                                            1 + vmToHostsAffinityMap.getOrDefault(vm_id, 0));
                                }
                            });
                });

        // Sort according the to the number of violations
        return vmToHostsAffinityMap.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
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

        Map<Guid, VM> vmsMap = vmDao.getVmsByIds(new ArrayList<>(allVms)).stream()
                .collect(Collectors.toMap(VM::getId, vm -> vm));

        Map<Guid, Guid> vmToHost = vmsMap.values().stream()
                .filter(vm -> vm.getRunOnVds() != null)
                .collect(Collectors.toMap(VM::getId, VM::getRunOnVds));

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
            final List<List<Guid>> candidateVmGroups;

            if (affinityGroup.isVmPositive()) {
                candidateVmGroups = groupVmsViolatingPositiveAg(affinityGroup, vmsMap);
                log.info("Positive affinity group violation detected");
            } else if (affinityGroup.isVmNegative()) {
                candidateVmGroups = groupVmsViolatingNegativeAg(affinityGroup, vmsMap);
                log.info("Negative affinity group violation detected");
            } else {
                continue;
            }

            // Look for a valid VM to migrate on all hosts
            for(List<Guid> group : candidateVmGroups) {
                Collections.shuffle(group);

                for(Guid vmId : group) {
                    VM vm = vmsMap.get(vmId);
                    if (isVmMigrationValid(cluster, vm)) {
                        return vm;
                    }
                }
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

        if (candidateVm.isHostedEngine()) {
            log.debug("VM {} is NOT a viable candidate for solving the affinity group violation situation"
                            + " since its a hosted engine VM.",
                    candidateVm.getId());
            return false;
        }

        if (candidateVm.getMigrationSupport() != MigrationSupport.MIGRATABLE) {
            log.debug("VM {} is NOT a viable candidate for solving the affinity group violation situation"
                            + " since it is not migratable.",
                    candidateVm.getId());
            return false;
        }

        List<Guid> vdsBlackList =
                candidateVm.getRunOnVds() == null ?
                        Collections.emptyList() : Arrays.asList(candidateVm.getRunOnVds());

        boolean canMove = !schedulingManager.canSchedule(cluster, candidateVm,
                vdsBlackList, Collections.emptyList(), new ArrayList<>()).isEmpty();

        if (canMove) {
            log.debug("VM {} is a viable candidate for solving the affinity group violation situation.",
                    candidateVm.getId());
            return true;
        }
        log.debug("VM {} is NOT a viable candidate for solving the affinity group violation situation.",
                candidateVm.getId());
        return false;
    }

    /**
     * Group VMs violating a negative affinity group by host
     * and sort groups by size from largest to smallest
     *
     * @param affinityGroup broken affinity rule
     * @param vmsMap        VM id to VM assignments
     * @return a list of groups of vms which are candidates for migration
     */
    private List<List<Guid>> groupVmsViolatingNegativeAg(AffinityGroup affinityGroup, Map<Guid, VM> vmsMap) {
        return groupVmsByHostFiltered(vmsMap, affinityGroup.getVmIds()).values().stream()
                .filter(s -> s.size() > 1)
                .sorted(Comparator.<List>comparingInt(List::size).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Group VMs violating a positive affinity group by host
     * and sort groups by size
     *
     * Ex.: Host1: [A, B, C, D]  Host2: [E, F]  -> returns [[E,F],[A,B,C,D]]
     *
     * @param affinityGroup broken affinity group
     * @param vmsMap        VM id to VM assignments
     * @return a list of groups of vms which are candidates for migration
     */
    private List<List<Guid>> groupVmsViolatingPositiveAg(AffinityGroup affinityGroup, Map<Guid, VM> vmsMap) {
        return groupVmsByHostFiltered(vmsMap, affinityGroup.getVmIds()).values().stream()
                .sorted(Comparator.comparingInt(List::size))
                .collect(Collectors.toList());
    }

    private Map<Guid, List<Guid>> groupVmsByHostFiltered(Map<Guid, VM> vmsMap, List<Guid> vms) {
        // Hosts containing nonmigratable VMs will be removed from result
        Set<Guid> removedHosts = new HashSet<>();

        Map<Guid, List<Guid>> res = new HashMap<>();
        for(Guid vmId : vms) {
            VM vm = vmsMap.get(vmId);

            Guid host = vm.getRunOnVds();
            if (host == null) {
                continue;
            }

            if (vm.getMigrationSupport() != MigrationSupport.MIGRATABLE || vm.isHostedEngine()) {
                removedHosts.add(host);
            } else {
                res.putIfAbsent(host, new ArrayList<>());
                res.get(host).add(vmId);
            }
        }

        res.keySet().removeAll(removedHosts);
        return res;
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
