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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.collections.IteratorUtils;
import org.ovirt.engine.core.bll.scheduling.SchedulingManager;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesUtils.AffinityGroupConflicts;
import org.ovirt.engine.core.common.FeatureSupported;
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
        List<AffinityGroup> allAffinityGroups = affinityGroupDao.getAllAffinityGroupsWithFlatLabelsByClusterId(cluster.getId());

        if (FeatureSupported.isImplicitAffinityGroupSupported(cluster.getCompatibilityVersion())) {
            List<Label> allAffinityLabels = labelDao.getAllByClusterId(cluster.getId());
            allAffinityGroups.addAll(AffinityRulesUtils.affinityGroupsFromLabels(allAffinityLabels, cluster.getId()));
        }

        Cache cache = new Cache(cluster, allAffinityGroups);

        Pair<Iterable<Guid>, Iterable<Guid>> vmToHostConflicts = getCandidateVmsFromVmsToHostAffinity(cache);

        Iterator<Guid> hardConflicts = Pipeline
                // Check hard VM to host affinity
                .create(() -> vmToHostConflicts.getFirst().iterator())
                // Check hard VM to VM affinity
                .append(() -> getCandidateVmsFromVmToVmAffinity(true, cache))
                .iterator();

        Set<Guid> softConflictIds = new HashSet<>();
        Iterator<Guid> softConflicts = Pipeline
                // Check soft VM to host affinity
                .create(() -> vmToHostConflicts.getSecond().iterator())
                // Check soft VM to VM affinity
                .append(() -> getCandidateVmsFromVmToVmAffinity(false, cache))
                .execute(softConflictIds::add)
                .iterator();

        return Pipeline
                .create(hardConflicts)
                .append(softConflicts)

                .distinct()
                .map(cache::getVm)
                .filter(vm -> isVmMigrationValid(cluster, vm))
                .filter(vm -> !softConflictIds.contains(vm.getId()) ||
                        migrationImprovesSoftAffinity(vm, cache))
                .iterator();
    }

    /**
     * Choose a VM to migrate by applying VM to host affinity rules.
     * Candidate VMs will selected in the following order:
     * <p>
     * 1.Candidate VMs violating enforcing affinity to hosts.
     * 2.Candidate VMs violating non enforcing affinity to hosts.
     *
     * @return Pair of streams. The first contains VMs breaking hard vm to host affinity,
     *   the second contains VMs breaking soft vm to host affinity.
     */
    private Pair<Iterable<Guid>, Iterable<Guid>> getCandidateVmsFromVmsToHostAffinity(Cache cache) {
        List<AffinityGroup> allVmToHostsAffinityGroups = cache.getAllGroups().stream()
                .filter(AffinityGroup::isVdsAffinityEnabled)
                .filter(g -> !g.getVdsIds().isEmpty() && !g.getVmIds().isEmpty())
                .collect(Collectors.toList());

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

    private Iterator<Guid> getCandidateVmsFromVmToVmAffinity(boolean enforcing, Cache cache) {
        cache.computeUnifiedPositiveGroups();
        List<AffinityGroup> unifiedAffinityGroups = cache.getAllGroups().stream()
                .filter(AffinityGroup::isVmAffinityEnabled)
                .filter(ag -> !ag.getVmIds().isEmpty())
                .filter(ag -> ag.isVmEnforcing() == enforcing)
                .collect(Collectors.toList());

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
            log.debug(enforcing ?
                    "No enforcing VM affinity group collision detected." :
                    "No VM affinity group collision detected.");
            return IteratorUtils.emptyIterator();
        }

        // Find a VM that is breaking the affinityGroup and can be theoretically migrated
        // Sort by:
        //  - enforcing groups first
        //  - groups with higher priority first (only for soft groups)
        //  - bigger affinity groups first
        Comparator<AffinityGroup> comparator = ((Comparator<AffinityGroup>) (a, b) ->
                Boolean.compare(b.isVmEnforcing(), a.isVmEnforcing())
        ).thenComparing((a, b) ->
                a.isVmEnforcing() ? 0 : Long.compare(b.getPriority(), a.getPriority())
        ).thenComparing(new AffinityGroupComparator().reversed());

        violatedAffinityGroups.sort(comparator);

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

        boolean canMove = !schedulingManager.prepareCall(cluster)
                .hostBlackList(vdsBlackList)
                .canSchedule(candidateVm).isEmpty();

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

    private boolean migrationImprovesSoftAffinity(VM vm, Cache cache) {
        log.debug("Testing if migration would improve soft affinity. VM: {}", vm.getName());
        Guid sourceHost = vm.getRunOnVds();
        Guid targetHost = schedulingManager.prepareCall(cache.getCluster()).scheduleStateless(vm).orElse(null);

        if (targetHost == null) {
            return false;
        }

        if (targetHost.equals(sourceHost)) {
            return false;
        }

        log.debug("Source host is '{}', target host is '{}'", sourceHost, targetHost);

        List<AffinityGroup> sortedAffinityGroupsForVm = cache.getAllGroupsForVmSorted(vm.getId());

        // The number of conflicting host affinity groups must be lower than before
        Supplier<Boolean> improvesHostAffinity = () -> {
            int oldConflictCount = 0;
            int newConflictCount = 0;
            long priority = Long.MAX_VALUE;

            for (AffinityGroup ag : sortedAffinityGroupsForVm) {
                if (!ag.isVdsAffinityEnabled() || ag.isVdsEnforcing()) {
                    continue;
                }

                if (ag.getPriority() < priority) {
                    // If the conflict count changed, it is not needed to check groups with lower priority
                    if (oldConflictCount != newConflictCount) {
                        log.debug("Host conflicts on source: {}, on destination: {}. Affinity priority: {}",
                                oldConflictCount, newConflictCount, priority);
                        return newConflictCount < oldConflictCount;
                    }
                    priority = ag.getPriority();
                }

                oldConflictCount += (ag.getVdsIds().contains(sourceHost) != ag.isVdsPositive()) ? 1 : 0;
                newConflictCount += (ag.getVdsIds().contains(targetHost) != ag.isVdsPositive()) ? 1 : 0;
            }

            log.debug("Host conflicts on source: {}, on destination: {}. Affinity priority: {}",
                    oldConflictCount, newConflictCount, priority);
            return newConflictCount < oldConflictCount;
        };

        Supplier<Boolean> improvesVmAffinity = () -> {
            int posAffScore = 0;
            int negAffScore = 0;
            long priority = Long.MAX_VALUE;

            for (AffinityGroup group : sortedAffinityGroupsForVm) {
                if (!group.isVmAffinityEnabled() || group.isVmEnforcing()) {
                    continue;
                }

                if (group.getPriority() < priority) {
                    // If some score is non-zero it is not needed to check groups with lower priority
                    if (posAffScore != 0 || negAffScore != 0) {
                        log.debug("VM conflicts: positive affinity score: {}, negative affinity score: {}, affinity priority: {}",
                                posAffScore, negAffScore, priority);
                        return posAffScore >= 0 && negAffScore >= 0;
                    }
                    priority = group.getPriority();
                }

                Map<Guid, Integer> vmCountOnHosts = cache.getHostsForGroup(group.getId());
                if (group.isVmPositive()) {
                    // Subtract 1 from source count, because the current VM should not be counted
                    int sourceCount = vmCountOnHosts.getOrDefault(sourceHost, 1) - 1;
                    int targetCount = vmCountOnHosts.getOrDefault(targetHost, 0);
                    posAffScore += Integer.compare(targetCount, sourceCount);
                } else {
                    negAffScore += vmCountOnHosts.getOrDefault(sourceHost, 1) > 1 ? 1 : 0;
                    negAffScore += vmCountOnHosts.containsKey(targetHost) ? -1 : 0;
                }
            }

            log.debug("VM conflicts: positive affinity score: {}, negative affinity score: {}, affinity priority: {}",
                    posAffScore, negAffScore, priority);

            // If any of the scores is worse, the vm affinity did not improve
            if (posAffScore < 0 || negAffScore < 0) {
                return false;
            }

            return posAffScore > 0 || negAffScore > 0;
        };

        return cache.isHostAffinityMoreImportant() ?
                improvesHostAffinity.get() || improvesVmAffinity.get() :
                improvesVmAffinity.get() || improvesHostAffinity.get();
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
        private final Cluster cluster;
        private Map<Guid, VM> vms = new HashMap<>();

        private List<AffinityGroup> allGroups;
        private Map<Guid, List<AffinityGroup>> groupsForVm;
        private boolean unifiedGroupsComputed = false;

        // Map: Affinity group id -> host id -> number of VMs running on the host
        private Map<Guid, Map<Guid, Integer>> hostsForGroups;

        Boolean hostAffinityMoreImportant;

        public Cache(Cluster cluster, List<AffinityGroup> allGroups) {
            this.cluster = cluster;
            this.allGroups = allGroups;
        }

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

            if (missingIds.isEmpty()) {
                return;
            }

            vmDao.getVmsByIds(missingIds)
                    .forEach(vm -> vms.put(vm.getId(), vm));
        }

        public List<AffinityGroup> getAllGroups() {
            return allGroups;
        }

        public void computeUnifiedPositiveGroups() {
            if (unifiedGroupsComputed) {
                return;
            }

            List<AffinityGroup> vmPositiveEnforcingGroups = allGroups.stream()
                    .filter(AffinityGroup::isVmPositive)
                    .filter(AffinityGroup::isVmEnforcing)
                    .filter(ag -> !ag.getVmIds().isEmpty())
                    .collect(Collectors.toList());

            List<AffinityGroup> unifiedPositiveEnforcingGroups = AffinityRulesUtils.setsToAffinityGroups(
                    AffinityRulesUtils.getUnifiedPositiveAffinityGroups(vmPositiveEnforcingGroups));

            unifiedPositiveEnforcingGroups.forEach(ag -> ag.setVmEnforcing(true));

            // Disable vm affinity in all other vm positive enforcing affinity groups.
            // They are not removed, because the host affinity can still be enabled
            for (AffinityGroup ag : allGroups) {
                if (ag.isVmPositive() && ag.isVmEnforcing()) {
                    ag.setVmAffinityRule(EntityAffinityRule.DISABLED);
                }
            }

            allGroups.addAll(unifiedPositiveEnforcingGroups);

            unifiedGroupsComputed = true;
            groupsForVm = null;
        }

        public List<AffinityGroup> getAllGroupsForVmSorted(Guid vmId) {
            if (groupsForVm == null) {
                groupsForVm = new HashMap<>();
                for (AffinityGroup ag : allGroups) {
                    for (Guid id : ag.getVmIds()) {
                        groupsForVm.computeIfAbsent(id, k -> new ArrayList<>()).add(ag);
                    }
                }

                // Sorting lists by priority here, so it is not needed later
                groupsForVm.values().forEach(groups -> groups.sort(Comparator.comparingLong(AffinityGroup::getPriority).reversed()));
            }

            return groupsForVm.getOrDefault(vmId, Collections.emptyList());
        }

        public Map<Guid, Map<Guid, Integer>> getHostsForGroups() {
            if (hostsForGroups == null) {
                Set<Guid> allVms = allGroups.stream()
                        .flatMap(ag -> ag.getVmIds().stream())
                        .collect(Collectors.toSet());

                fetchVms(allVms);

                hostsForGroups = new HashMap<>(allGroups.size());
                for (AffinityGroup group : allGroups) {
                    if (!group.isVmAffinityEnabled()) {
                        continue;
                    }

                    Map<Guid, Integer> vmCountOnHosts = group.getVmIds().stream()
                            .map(this::getVm)
                            .filter(vm -> vm != null && vm.getRunOnVds() != null)
                            .collect(Collectors.toMap(VM::getRunOnVds, v -> 1, Integer::sum));

                    hostsForGroups.put(group.getId(), vmCountOnHosts);
                }
            }

            return hostsForGroups;
        }

        public Map<Guid, Integer> getHostsForGroup(Guid groupId) {
            return getHostsForGroups().get(groupId);
        }

        public Cluster getCluster() {
            return cluster;
        }

        public boolean isHostAffinityMoreImportant() {
            if (hostAffinityMoreImportant == null) {
                hostAffinityMoreImportant =
                        schedulingManager.isHostAffinityMoreImportantThanVmAffinity(cluster);
            }
            return hostAffinityMoreImportant;
        }
    }
}
