package org.ovirt.engine.core.bll.scheduling.arem;

import static java.util.Collections.min;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.collections.IteratorUtils;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.scheduling.SchedulingParameters;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesUtils.AffinityGroupConflicts;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.Pipeline;
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
    private LabelDao labelDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private SchedulingManager schedulingManager;

    /**
     * Choose a valid VM for migration by applying affinity rules in the following order:
     * <p>
     * 1. Hard VM to Hosts Affinity
     * 2. Hard VM to VM affinity
     * 3. Soft VM to Hosts Affinity
     * 4. Soft VM to VM affinity
     *
     * @param cluster current cluster
     * @return Iterator returning valid VMs for migration
     */
    public Iterator<VM> chooseVmsToMigrate(Cluster cluster) {
        List<AffinityGroup> allAffinityGroups = affinityGroupDao.getAllAffinityGroupsByClusterId(cluster.getId());
        List<Label> allAffinityLabels = labelDao.getAllByClusterId(cluster.getId());
        allAffinityGroups.addAll(affinityGroupsFromLabels(allAffinityLabels, cluster.getId()));

        Cache cache = new Cache();

        Pair<Iterable<Guid>, Iterable<Guid>> vmToHostConflicts =
                getCandidateVmsFromVmsToHostAffinity(allAffinityGroups, cache);

        return Pipeline
                // Check hard VM to host affinity
                .create(() -> vmToHostConflicts.getFirst().iterator())
                // Check hard VM to VM affinity
                .append(() -> getCandidateVmsFromVmToVmAffinity(allAffinityGroups, true, cache))
                // Check soft VM to host affinity
                .append(() -> vmToHostConflicts.getSecond().iterator())
                // Check soft VM to VM affinity
                .append(() -> getCandidateVmsFromVmToVmAffinity(allAffinityGroups, false, cache))

                .distinct()
                .map(cache::getVm)
                .filter(vm -> isVmMigrationValid(cluster, vm))
                .iterator();
    }

    private List<AffinityGroup> affinityGroupsFromLabels(List<Label> labels, Guid clusterId) {
        return labels.stream()
                .map(label -> {
                    AffinityGroup group = new AffinityGroup();
                    group.setId(label.getId());
                    group.setClusterId(clusterId);
                    group.setName("Label: " + label.getName());

                    group.setVdsAffinityRule(EntityAffinityRule.POSITIVE);
                    group.setVdsEnforcing(true);
                    group.setVmAffinityRule(EntityAffinityRule.DISABLED);

                    group.setVmIds(new ArrayList<>(label.getVms()));
                    group.setVdsIds(new ArrayList<>(label.getHosts()));

                    return group;
                })
                .collect(Collectors.toList());
    }

    /**
     * Choose a VM to migrate by applying VM to host affinity rules.
     * Candidate VMs will selected in the following order:
     * <p>
     * 1.Candidate VMs violating enforcing affinity to hosts.
     * 2.Candidate VMs violating non enforcing affinity to hosts.
     *
     * @param allAffinityGroups All affinity groups for the current cluster.
     * @return Pair of streams. The first contains VMs breaking hard vm to host affinity,
     *   the second contains VMs breaking soft vm to host affinity.
     */
    private Pair<Iterable<Guid>, Iterable<Guid>> getCandidateVmsFromVmsToHostAffinity(List<AffinityGroup> allAffinityGroups, Cache cache) {
        List<AffinityGroup> allVmToHostsAffinityGroups = getAllAffinityGroupsForVMsToHostsAffinity(allAffinityGroups);
        if (allVmToHostsAffinityGroups.isEmpty()) {
            return new Pair<>(IteratorUtils::emptyIterator,  IteratorUtils::emptyIterator);
        }

        List<Guid> vmIds = allVmToHostsAffinityGroups.stream()
                .map(AffinityGroup::getVmIds)
                .flatMap(List::stream)
                .distinct()
                .collect(Collectors.toList());

        cache.fetchVms(vmIds);

        Iterable<Guid> vmsBreakingHardAffinity = () -> {
            List<Guid> candidateVMs = getVmToHostsAffinityGroupCandidates(allVmToHostsAffinityGroups, cache, true);
            if (candidateVMs.isEmpty()) {
                log.debug("No vm to hosts hard-affinity group violation detected");
            } else {
                logVmToHostConflicts(allVmToHostsAffinityGroups);
            }
            return candidateVMs.iterator();
        };

        Iterable<Guid> vmsBreakingSoftAffinity = () -> {
            List<Guid> candidateVMs = getVmToHostsAffinityGroupCandidates(allVmToHostsAffinityGroups, cache, false);
            if (candidateVMs.isEmpty()) {
                log.debug("No vm to hosts soft-affinity group violation detected");
            }
            return candidateVMs.iterator();
        };

        return new Pair<>(vmsBreakingHardAffinity, vmsBreakingSoftAffinity);
    }

    /**
     * Get a list of candidate VMs (by VM ids) from the VM to host affinity groups.
     * This list will contain all VMs that violate the host affinity policies
     * sorted according to the number of violations (descending).
     *
     * @param allVMtoHostsAffinityGroups VM to Host affinity groups.
     * @param cache                   cache of VMs
     * @param isVdsAffinityEnforcing     true - Hard affinity constraint, false - Soft affinity constraint.
     * @return list of candidate VMs for migration by VM to Host affinities.
     */
    private List<Guid> getVmToHostsAffinityGroupCandidates(List<AffinityGroup> allVMtoHostsAffinityGroups,
            Cache cache,
            boolean isVdsAffinityEnforcing) {
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
                                VM vm = cache.getVm(vm_id);
                                if (vm == null || vm.getRunOnVds() == null) {
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

    private void logVmToHostConflicts(List<AffinityGroup> groups) {
        List<AffinityGroupConflicts> conflicts = AffinityRulesUtils.checkForAffinityGroupHostsConflict(groups);
        for (AffinityGroupConflicts conflict : conflicts) {
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

    private Iterator<Guid> getCandidateVmsFromVmToVmAffinity(List<AffinityGroup> allAffinityGroups, boolean onlyEnforcing, Cache cache) {
        List<AffinityGroup> vmToVmAffinityGroups = allAffinityGroups.stream()
                .filter(AffinityGroup::isVmAffinityEnabled)
                .filter(ag -> !onlyEnforcing || ag.isVmEnforcing())
                .collect(Collectors.toList());
        Set<Set<Guid>> unifiedPositiveAffinityGroups = AffinityRulesUtils.getUnifiedPositiveAffinityGroups(
                vmToVmAffinityGroups);
        List<AffinityGroup> unifiedAffinityGroups = AffinityRulesUtils.setsToAffinityGroups(
                unifiedPositiveAffinityGroups);

        // Add negative affinity groups
        for (AffinityGroup ag : vmToVmAffinityGroups) {
            if (ag.isVmNegative()) {
                unifiedAffinityGroups.add(ag);
            }
        }

        // Create a set of all VMs in affinity groups
        Set<Guid> allVms = new HashSet<>();
        for (AffinityGroup group : unifiedAffinityGroups) {
            allVms.addAll(group.getVmIds());
        }

        cache.fetchVms(allVms);

        List<VM> vms = allVms.stream()
                .map(cache::getVm)
                .collect(Collectors.toList());

        List<AffinityGroup> violatedAffinityGroups = checkForVMAffinityGroupViolations(unifiedAffinityGroups, vms);
        if (violatedAffinityGroups.isEmpty()) {
            log.debug(onlyEnforcing ?
                    "No enforcing VM affinity group collision detected." :
                    "No VM affinity group collision detected.");
            return IteratorUtils.emptyIterator();
        }

        // Find a VM that is breaking the affinityGroup and can be theoretically migrated
        // - start with bigger Affinity Groups
        violatedAffinityGroups.sort(Collections.reverseOrder(new AffinityGroupComparator()));

        return violatedAffinityGroups.stream()
                .flatMap(affinityGroup -> {
                    if (affinityGroup.isVmPositive()) {
                        log.info("Positive affinity group violation detected");
                        return groupVmsViolatingPositiveAg(affinityGroup, cache);
                    } else {
                        log.info("Negative affinity group violation detected");
                        return groupVmsViolatingNegativeAg(affinityGroup, cache);
                    }
                })
                .flatMap(group -> {
                    Collections.shuffle(group);
                    return group.stream();
                })
                .iterator();
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

        List<Guid> vdsBlackList = candidateVm.getRunOnVds() == null ?
                Collections.emptyList() : Collections.singletonList(candidateVm.getRunOnVds());

        boolean canMove = !schedulingManager.canSchedule(cluster,
                candidateVm,
                vdsBlackList,
                Collections.emptyList(),
                new SchedulingParameters(),
                new ArrayList<>()).isEmpty();

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
     * @param cache      cache of VMs
     * @return a stream of groups of vms which are candidates for migration
     */
    private Stream<List<Guid>> groupVmsViolatingNegativeAg(AffinityGroup affinityGroup, Cache cache) {
        return groupVmsByHostFiltered(cache, affinityGroup.getVmIds()).values().stream()
                .filter(s -> s.size() > 1)
                .sorted(Comparator.<List>comparingInt(List::size).reversed());
    }

    /**
     * Group VMs violating a positive affinity group by host
     * and sort groups by size
     *
     * Ex.: Host1: [A, B, C, D]  Host2: [E, F]  -> returns [[E,F],[A,B,C,D]]
     *
     * @param affinityGroup broken affinity group
     * @param cache      cache of VMs
     * @return a stream of groups of vms which are candidates for migration
     */
    private Stream<List<Guid>> groupVmsViolatingPositiveAg(AffinityGroup affinityGroup, Cache cache) {
        return groupVmsByHostFiltered(cache, affinityGroup.getVmIds()).values().stream()
                .sorted(Comparator.comparingInt(List::size));
    }

    private Map<Guid, List<Guid>> groupVmsByHostFiltered(Cache cache, List<Guid> vms) {
        // Hosts containing nonmigratable VMs will be removed from result
        Set<Guid> removedHosts = new HashSet<>();

        Map<Guid, List<Guid>> res = new HashMap<>();
        for(Guid vmId : vms) {
            VM vm = cache.getVm(vmId);

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
     * @param vms            Collection of VMs
     * @return broken AffinityGroups
     */
    private static List<AffinityGroup> checkForVMAffinityGroupViolations(Collection<AffinityGroup> affinityGroups,
            Collection<VM> vms) {

        Map<Guid, Guid> vmToHost = vms.stream()
                .filter(vm -> vm.getRunOnVds() != null)
                .collect(Collectors.toMap(VM::getId, VM::getRunOnVds));

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
                    } else if (targetHost == null) {
                        targetHost = host;
                    }
                }
            }
        }

        return new ArrayList<>(broken);
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

    private class Cache {
        private Map<Guid, VM> vms = new HashMap<>();

        public VM getVm(Guid id) {
            if (!vms.containsKey(id)) {
                vms.put(id, vmDao.get(id));
            }
            return vms.get(id);
        }

        public void fetchVms(Collection<Guid> ids) {
            List<Guid> missingIds = ids.stream()
                    .filter(id -> !vms.containsKey(id))
                    .collect(Collectors.toList());

            vmDao.getVmsByIds(missingIds)
                    .forEach(vm -> vms.put(vm.getId(), vm));
        }
    }
}
