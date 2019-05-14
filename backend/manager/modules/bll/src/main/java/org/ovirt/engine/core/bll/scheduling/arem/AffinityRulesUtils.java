package org.ovirt.engine.core.bll.scheduling.arem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.compat.Guid;

public class AffinityRulesUtils {
    /**
     * Take the unified positive groups and check whether a conflict exists between positive
     * and negative VM affinity.
     *
     * @param affinityGroups - All affinity groups
     */
    private static void checkForAffinityGroupConflict(List<AffinityGroup> affinityGroups,
            List<AffinityGroupConflicts> conflicts) {

        Set<Set<Guid>> unifiedPositiveGroups = getUnifiedPositiveAffinityGroups(affinityGroups.stream()
                .filter(AffinityGroup::isVmAffinityEnabled)
                .collect(Collectors.toList()));
        affinityGroups.stream()
                .filter(AffinityGroup::isVmAffinityEnabled)
                .filter(AffinityGroup::isVmNegative)
                .forEach(ag -> {
                    for (Set<Guid> positiveGroup : unifiedPositiveGroups) {
                        Set<Guid> intersection = new HashSet<>(ag.getVmIds());
                        intersection.retainAll(positiveGroup);
                        if (intersection.size() > 1) {
                            conflicts.add(new AffinityGroupConflicts(new HashSet<>(Collections.singletonList(ag)),
                                    AffinityRulesConflicts.VM_TO_VM_AFFINITY_CONFLICTS,
                                    AuditLogType.VM_TO_VM_AFFINITY_CONFLICTS, positiveGroup, intersection)
                            );
                        }
                    }
                });
    }

    /**
     * Take all positive affinity groups and merge all groups that reference the same VM
     * together.
     *
     * Ex. groups A+B, B+C, D+E, A+D, F+G are in fact just two bigger groups:
     *     A+B+C+D+E and F+G
     *
     * The algorithm starts by creating single element groups from all VMs
     * It then goes through all affinity groups and merges all VM groups that contain
     * VMs from the currently processed AffinityGroup.
     */
    public static Set<Set<Guid>> getUnifiedPositiveAffinityGroups(List<AffinityGroup> affinityGroups) {
        Set<Set<Guid>> uag = new HashSet<>();
        Map<Guid, Set<Guid>> vmIndex = new HashMap<>();

        /**
         * Initialize the single element groups by taking all VMs that are referenced
         * from any affinity group
         */
        for (AffinityGroup ag : affinityGroups) {
            for (Guid id : ag.getVmIds()) {
                Set<Guid> temp = new HashSet<>();
                temp.add(id);
                uag.add(temp);
                vmIndex.put(id, temp);
            }
        }

        // Go through each positive affinity group and merge all existing groups
        // that contain the referenced VMs into one.
        for(AffinityGroup ag : affinityGroups) {
            if(ag.isVmPositive()) {
                Set<Guid> mergedGroup = new HashSet<>();

                for(Guid id : ag.getVmIds()) {
                    // Get the current groups VM(id) belongs to
                    Set<Guid> existingGroup = vmIndex.get(id);

                    // Merge it with the currently computed mergeGroup
                    mergedGroup.addAll(existingGroup);

                    // And remove it from the valid groups
                    // (it will be re-added as part of a bigger group)
                    uag.remove(existingGroup);

                    // Update the per-VM index
                    for (Guid vm: existingGroup) {
                        vmIndex.put(vm, mergedGroup);
                    }
                }

                uag.add(mergedGroup);
            }
        }

        return uag;
    }

    /**
     * Convert positive affinity group sets to a proper AffinityGroup objects.
     * Use lists so the order is defined as we want an deterministic algorithm further on.
     *
     * @param uag A set of sets containing the unified POSITIVE affinity groups.
     * @return List of AffinityGroup objects representing the provided unified affinity groups
     */
    static List<AffinityGroup> setsToAffinityGroups(Set<Set<Guid>> uag) {
        List<AffinityGroup> output = new ArrayList<>(uag.size());

        for(Set<Guid> s : uag) {
            AffinityGroup temp = new AffinityGroup();
            temp.setVmAffinityRule(EntityAffinityRule.POSITIVE);

            List<Guid> entities = new ArrayList<>(s);
            temp.setVmIds(entities);
            output.add(temp);
        }

        return output;
    }

    /**
     * Check for conflicts in VMs to hosts affinity groups.
     * These conflicts will include :
     * Hosts that have enforcing positive and negative affinity conflicts.
     * Hosts that have positive and negative affinity conflicts.
     * Non intersecting hosts conflicts.
     * Vm to host affinity with positive vm to vm conflicts
     * Vm to host affinity with negative vm to vm conflicts
     * Vms have positive and negative affinity conflicts.
     *
     * @param affinityGroups affinity groups to examine.
     * @return An object that contains a list of host ids for each of the described conflicts.
     */
    public static List<AffinityGroupConflicts> checkForAffinityGroupHostsConflict(List<AffinityGroup>
            affinityGroups) {

        List<AffinityGroupConflicts> conflicts = new ArrayList<>();

        checkHostsInPositiveAndNegativeAffinity(affinityGroups.stream()
                        .filter(AffinityGroup::isVdsEnforcing)
                        .collect(Collectors.toList()),
                true,
                conflicts);

        checkHostsInPositiveAndNegativeAffinity(affinityGroups, false, conflicts);

        checkVmToHostWithPositiveVmToVmConflict(affinityGroups, conflicts);

        checkVmToHostWithNegativeVmToVmConflicts(affinityGroups, conflicts);

        checkNonIntersectingPositiveHosts(affinityGroups, conflicts);

        checkForAffinityGroupConflict(affinityGroups, conflicts);

        return conflicts;
    }

    /**
     * Checks for affinity groups that have positive and negative affinity conflicts:
     * A host that has negative enforcing/non enforcing affinity with a VM in one or more affinity groups
     * and the same host that has positive enforcing affinity with that VM in one or more affinity groups.
     *
     * example of conflicts :
     * {vm1 + host1} , {vm1 - host1}
     * {vm1 [+] host1} , {vm1 - host1}
     * {vm1 [+] host1} , {vm1 [-] host1}
     *
     * (  + is enforcing positive affinity)
     * ( [+] is non enforcing positive affinity)
     * (  - is enforcing negative affinity)
     * ( [-] is non enforcing negative affinity)
     * ( {} is an affinity group)
     *
     * @param affinityGroups affinity groups to examine.
     * @param isVdsEnforcing is check done against enforcing affinity or not
     * @param conflicts      affinity conflicts list
     */
    private static void checkHostsInPositiveAndNegativeAffinity(List<AffinityGroup> affinityGroups,
            boolean isVdsEnforcing, List<AffinityGroupConflicts> conflicts) {

        Set<AffinityGroup> conflictingAffinityGroups = new HashSet<>();
        Set<Guid> conflictingVMs = new HashSet<>();
        Set<Guid> conflictingHosts = new HashSet<>();

        List<AffinityGroup> affinityGroupsWithHosts = affinityGroups.stream()
                .filter(AffinityGroup::isVdsAffinityEnabled)
                .filter(ag -> !ag.getVdsIds().isEmpty())
                .collect(Collectors.toList());

        affinityGroupsWithHosts.stream()
                .filter(ag -> !ag.isVdsPositive())
                .forEach(negativeGroup -> {
                    affinityGroupsWithHosts.stream()
                            .filter(AffinityGroup::isVdsPositive)
                            .forEach(positiveGroup -> {
                                List<Guid> intersectingVMs = new ArrayList<>(negativeGroup.getVmIds());
                                intersectingVMs.retainAll(positiveGroup.getVmIds());
                                if (!intersectingVMs.isEmpty()) {
                                    List<Guid> intersectingHosts = new ArrayList<>(negativeGroup.getVdsIds());
                                    intersectingHosts.retainAll(positiveGroup.getVdsIds());
                                    if (!intersectingHosts.isEmpty()) {
                                        conflictingAffinityGroups.add(positiveGroup);
                                        conflictingAffinityGroups.add(negativeGroup);
                                        conflictingVMs.addAll(intersectingVMs);
                                        conflictingHosts.addAll(intersectingHosts);
                                    }
                                }
                            });
                });
        if (!conflictingAffinityGroups.isEmpty()) {
            if (isVdsEnforcing) {
                conflicts.add(new AffinityGroupConflicts(conflictingAffinityGroups,
                        conflictingVMs,
                        conflictingHosts,
                        AffinityRulesConflicts.VM_TO_HOST_CONFLICT_IN_ENFORCING_POSITIVE_AND_NEGATIVE_AFFINITY,
                        AuditLogType.VM_TO_HOST_CONFLICT_IN_ENFORCING_POSITIVE_AND_NEGATIVE_AFFINITY));
            } else {
                conflicts.add(new AffinityGroupConflicts(conflictingAffinityGroups,
                        conflictingVMs,
                        conflictingHosts,
                        AffinityRulesConflicts.VM_TO_HOST_CONFLICT_IN_POSITIVE_AND_NEGATIVE_AFFINITY,
                        AuditLogType.VM_TO_HOST_CONFLICT_IN_POSITIVE_AND_NEGATIVE_AFFINITY));
            }
        }
    }

    /**
     * Checks vm to host affinity with positive vm to vm conflicts.
     * Hosts that have positive and negative affinity to vms and those vms have positive affinity
     * can cause a conflict.
     *
     * example of conflicts :
     * {vm1 + host1},{vm1+vm2},{vm2 - host1}
     *
     * (  + is enforcing positive affinity)
     * ( [+] is non enforcing positive affinity)
     * (  - is enforcing negative affinity)
     * ( [-] is non enforcing negative affinity)
     * ( {} is an affinity group)
     *
     * @param affinityGroups affinity groups to examine.
     * @param conflicts      affinity conflicts list
     */
    private static void checkVmToHostWithPositiveVmToVmConflict(List<AffinityGroup> affinityGroups,
            List<AffinityGroupConflicts> conflicts) {

        Set<AffinityGroup> conflictingAffinityGroups = new HashSet<>();
        Set<Guid> conflictingVMs = new HashSet<>();
        Set<Guid> conflictingHosts = new HashSet<>();
        List<AffinityGroup> affinityGroupsWithHosts = affinityGroups.stream()
                .filter(AffinityGroup::isVdsAffinityEnabled)
                .filter(affinityGroup -> !affinityGroup.getVdsIds().isEmpty())
                .collect(Collectors.toList());

        Set<Set<Guid>> unifiedPositive = AffinityRulesUtils.getUnifiedPositiveAffinityGroups(affinityGroups.stream()
                .filter(AffinityGroup::isVmAffinityEnabled)
                .collect(Collectors.toList()));
        unifiedPositive.forEach(vms_list -> {
            vms_list.forEach(vm_first -> {
                vms_list.stream()
                        .filter(vm_second -> !vm_first.equals(vm_second))
                        .forEach(vm_second -> {
                            findVMtoHostWithPositiveVmToVmConflicts(conflictingAffinityGroups,
                                    conflictingVMs,
                                    conflictingHosts,
                                    affinityGroupsWithHosts,
                                    vm_first,
                                    vm_second);
                        });
            });
        });

        if (!conflictingAffinityGroups.isEmpty()) {
            conflicts.add(new AffinityGroupConflicts(conflictingAffinityGroups,
                    conflictingVMs,
                    conflictingHosts,
                    AffinityRulesConflicts.VM_TO_HOST_CONFLICTS_POSITIVE_VM_TO_VM_AFFINITY,
                    AuditLogType.VM_TO_HOST_CONFLICTS_POSITIVE_VM_TO_VM_AFFINITY));
        }
    }

    /**
     * For each pair of vms : vm_first and vm_second , find all the pairs of
     * affinity groups that each have one of these vms (each group with a different vm),
     * with different affinity polarity to hosts and at least one intersecting host.
     */
    private static void findVMtoHostWithPositiveVmToVmConflicts(Set<AffinityGroup> conflictingAffinityGroups,
            Set<Guid> conflictingVMs,
            Set<Guid> conflictingHosts,
            List<AffinityGroup> affinityGroupsWithHosts,
            Guid vm_first, Guid vm_second) {

        affinityGroupsWithHosts.stream()
                //get all affinity groups that contain vm_first
                .filter(affinityGroup -> affinityGroup.getVmIds().contains(vm_first))
                .forEach(affinityGroupFirst -> {
                    affinityGroupsWithHosts.stream()
                            //get all affinity groups that contain vm_second
                            .filter(ag -> ag.getVmIds().contains(vm_second))
                            .forEach(affinityGroupSecond -> {
                                //check if the affinity groups have different polarity to hosts
                                if (affinityGroupFirst.isVdsPositive() != affinityGroupSecond.isVdsPositive()) {
                                    Set<Guid> vds_ids_first =
                                            new HashSet<>(affinityGroupFirst.getVdsIds());
                                    Set<Guid> vds_ids_second =
                                            new HashSet<>(affinityGroupSecond.getVdsIds());
                                    vds_ids_first.retainAll(vds_ids_second);
                                    //check if there are intersecting hosts
                                    if (!vds_ids_first.isEmpty()) {
                                        conflictingAffinityGroups.addAll(Arrays.asList
                                                (affinityGroupFirst, affinityGroupSecond));
                                        conflictingVMs.addAll(Arrays.asList(vm_first, vm_second));
                                        conflictingHosts.addAll(vds_ids_first);
                                    }
                                }

                            });
                });
    }

    /**
     * Checks vm to host affinity with negative vm to vm conflicts.
     * Hosts that have positive affinity to vms and those vms have negative affinity
     * can cause a conflict.
     *
     * example of conflicts :
     *
     * {vm1 + host1},{vm1 - vm2},{vm2 + host1}
     *
     * (  + is enforcing positive affinity)
     * ( [+] is non enforcing positive affinity)
     * (  - is enforcing negative affinity)
     * ( [-] is non enforcing negative affinity)
     * ( {} is an affinity group)
     *
     * @param affinityGroups affinity groups to examine.
     * @param conflicts      affinity conflicts list
     */
    private static void checkVmToHostWithNegativeVmToVmConflicts(List<AffinityGroup> affinityGroups,
            List<AffinityGroupConflicts> conflicts) {

        Set<AffinityGroup> conflictingAffinityGroups = new HashSet<>();
        Set<Guid> conflictingVMs = new HashSet<>();
        Set<Guid> conflictingHosts = new HashSet<>();

        affinityGroups.stream()
                //get all affinity groups with negative vm to vm affinity
                .filter(AffinityGroup::isVmAffinityEnabled)
                .filter(affinityGroup -> !affinityGroup.isVmPositive())
                .forEach(affinityGroup -> {
                    affinityGroup.getVmIds()
                            .forEach(vm_first -> {
                                affinityGroup.getVmIds().stream()
                                        //get all affinity groups that do not contain vm_first
                                        .filter(vm_second -> !vm_second.equals(vm_first))
                                        .forEach(vm_second -> {
                                            Set<AffinityGroup> conflictingAffinityGroupsCandidates = new HashSet<>();

                                            //add the first affinity group as a candidate for conflict
                                            conflictingAffinityGroupsCandidates.add(affinityGroup);

                                            Set<Guid> intersectingHosts =
                                                    findIntersectingPositiveHostsForVms(affinityGroups,
                                                            vm_first,
                                                            vm_second,
                                                            conflictingAffinityGroupsCandidates);
                                            //check if there are intersecting hosts
                                            if (!intersectingHosts.isEmpty()) {
                                                conflictingAffinityGroups.addAll(conflictingAffinityGroupsCandidates);
                                                conflictingVMs.addAll(Arrays.asList(vm_first,
                                                        vm_second));
                                                conflictingHosts.addAll(intersectingHosts);
                                            }

                                        });
                            });
                });

        if (!conflictingAffinityGroups.isEmpty()) {
            conflicts.add(new AffinityGroupConflicts(conflictingAffinityGroups,
                    conflictingVMs,
                    conflictingHosts,
                    AffinityRulesConflicts.VM_TO_HOST_CONFLICTS_NEGATIVE_VM_TO_VM_AFFINITY,
                    AuditLogType.VM_TO_HOST_CONFLICTS_NEGATIVE_VM_TO_VM_AFFINITY));
        }
    }

    /**
     * Get intersecting hosts from positive affinity groups that either contain vm_first
     * or vm_second
     */
    private static Set<Guid> findIntersectingPositiveHostsForVms(List<AffinityGroup> affinityGroups,
            Guid vm_first,
            Guid vm_second, Set<AffinityGroup> conflictingAffinityGroupsCandidates) {
        Set<Guid> vds_ids_first = new HashSet<>();
        Set<Guid> vds_ids_second = new HashSet<>();
        affinityGroups.stream()
                .filter(AffinityGroup::isVdsPositive)
                .forEach(ag -> {
                    if (ag.getVmIds().contains(vm_first)) {
                        vds_ids_first.addAll(ag.getVdsIds());
                    } else if (ag.getVmIds().contains(vm_second)) {
                        vds_ids_second.addAll(ag.getVdsIds());
                    }
                    //add the second affinity group as a candidate for conflict
                    conflictingAffinityGroupsCandidates.add(ag);
                });

        vds_ids_first.retainAll(vds_ids_second);
        return vds_ids_first;
    }

    /**
     * Checks for non intersecting hosts conflicts: One or more hosts that have positive enforcing affinity to a VM in
     * an
     * affinity
     * group and different host/hosts that have positive enforcing affinity to that VM in a different affinity group.
     *
     * example of conflict :
     * {vm1 + host1,host2} , {vm1 + host1,host3}
     *
     * host2 and host3 will be included in the conflicting hosts since they are
     * not in the intersection of vm1 affinity groups.
     *
     * (  + is enforcing positive affinity)
     * ( [+] is non enforcing positive affinity)
     * (  - is enforcing negative affinity)
     * ( [-] is non enforcing negative affinity)
     * ( {} is an affinity group)
     *
     * @param affinityGroups affinity groups to examine.
     * @param conflicts      affinity conflicts list
     */
    private static void checkNonIntersectingPositiveHosts(List<AffinityGroup> affinityGroups,
            List<AffinityGroupConflicts> conflicts) {

        Set<Guid> conflictingVMs = new HashSet<>();
        Set<Guid> conflictingHosts = new HashSet<>();

        List<AffinityGroup> vdsPositiveAffinityGroups = affinityGroups.stream()
                .filter(AffinityGroup::isVdsPositive)
                .filter(ag -> !ag.getVdsIds().isEmpty())
                .collect(Collectors.toList());
        Set<AffinityGroup> conflictingAffinityGroups = new HashSet<>();
        vdsPositiveAffinityGroups
                .forEach(ag -> {
                    ag.getVmIds().forEach(
                            vm_id -> {
                                addConflictsForIntersectingHostsByVm(conflictingVMs,
                                        conflictingHosts,
                                        vdsPositiveAffinityGroups,
                                        conflictingAffinityGroups,
                                        ag,
                                        vm_id);
                            }

                    );
                });

        if (!conflictingAffinityGroups.isEmpty()) {
            conflicts.add(new AffinityGroupConflicts(conflictingAffinityGroups,
                    conflictingVMs,
                    conflictingHosts,
                    AffinityRulesConflicts.NON_INTERSECTING_POSITIVE_HOSTS_AFFINITY_CONFLICTS,
                    AuditLogType.NON_INTERSECTING_POSITIVE_HOSTS_AFFINITY_CONFLICTS));
        }
    }

    /**
     * Find intersecting hosts from affinity groups that contain vm_id
     */
    private static void addConflictsForIntersectingHostsByVm(Set<Guid> conflictingVMs,
            Set<Guid> conflictingHosts,
            List<AffinityGroup> vdsPositiveAffinityGroups,
            Set<AffinityGroup> conflictingAffinityGroups,
            AffinityGroup ag, Guid vm_id) {

        vdsPositiveAffinityGroups.stream()
                .filter(affinityGroup -> affinityGroup.getVmIds().contains(vm_id))
                .forEach(affinityGroup -> {
                    Set<Guid> vds_ids = new HashSet<>(ag.getVdsIds());
                    vds_ids.removeAll(affinityGroup.getVdsIds());
                    //check if there are intersecting hosts
                    if (!vds_ids.isEmpty()) {
                        conflictingAffinityGroups.add(ag);
                        conflictingAffinityGroups.add(affinityGroup);
                        conflictingHosts.addAll(vds_ids);
                        conflictingVMs.add(vm_id);
                    }

                });
    }

    public static List<AffinityGroup> affinityGroupsFromLabels(List<Label> labels, Guid clusterId) {
        return labels.stream()
                .filter(Label::isImplicitAffinityGroup)
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

    public static class AffinityGroupConflicts {

        final Set<AffinityGroup> affinityGroups;
        final Set<Guid> vms;
        final Set<Guid> negativeVms;
        final Set<Guid> hosts;
        final AffinityRulesConflicts type;
        final AuditLogType auditLogType;
        final boolean isVmToVmAffinity;

        public AffinityGroupConflicts(Set<AffinityGroup> affinityGroups,
                Set<Guid> vms,
                Set<Guid> hosts,
                AffinityRulesConflicts type, AuditLogType auditLogType) {
            this.affinityGroups = affinityGroups;
            this.vms = vms;
            this.hosts = hosts;
            this.type = type;
            this.auditLogType = auditLogType;
            this.isVmToVmAffinity = false;
            this.negativeVms = new HashSet<>();
        }

        public AffinityGroupConflicts(Set<AffinityGroup> affinityGroups, AffinityRulesConflicts type,
                AuditLogType auditLogType, Set<Guid> positiveVms, Set<Guid> negativeVms) {
            this.affinityGroups = affinityGroups;
            this.vms = positiveVms;
            this.negativeVms = negativeVms;
            this.type = type;
            this.auditLogType = auditLogType;
            this.isVmToVmAffinity = true;
            this.hosts = new HashSet<>();
        }

        public Set<AffinityGroup> getAffinityGroups() {
            return affinityGroups;
        }

        public Set<Guid> getVms() {
            return vms;
        }

        public Set<Guid> getHosts() {
            return hosts;
        }

        public AffinityRulesConflicts getType() {
            return type;
        }

        public AuditLogType getAuditLogType() {
            return auditLogType;
        }

        public Set<Guid> getNegativeVms() {
            return negativeVms;
        }

        public boolean isVmToVmAffinity() {
            return isVmToVmAffinity;
        }
    }

    public static String getAffinityGroupsNames(Set<AffinityGroup> affinityGroups) {
        return affinityGroups.stream()
                .map(ag -> ag.getName())
                .collect(Collectors.joining(","));
    }
}
