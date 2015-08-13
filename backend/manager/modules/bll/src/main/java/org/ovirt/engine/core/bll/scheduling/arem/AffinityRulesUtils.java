package org.ovirt.engine.core.bll.scheduling.arem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;

public class AffinityRulesUtils {
    /**
     * Take the unified positive groups and check whether a conflict exists between positive
     * and negative VM affinity.
     *
     * @param affinityGroups - All affinity groups
     * @param unifiedPositiveGroups - Computed unified groups of positive affinities
     * @return true when a conflict was detected, false otherwise
     */
    public static AffinityGroupConflict checkForAffinityGroupConflict(Iterable<AffinityGroup> affinityGroups,
            Set<Set<Guid>> unifiedPositiveGroups) {
        for(AffinityGroup ag : affinityGroups) {
            if(ag.isPositive()) {
                continue;
            }

            for (Set<Guid> positiveGroup : unifiedPositiveGroups) {
                Set<Guid> intersection = new HashSet<>(ag.getEntityIds());
                intersection.retainAll(positiveGroup);

                if(intersection.size() > 1) {
                    return new AffinityGroupConflict(positiveGroup, ag.getEntityIds());
                }
            }
        }

        return null;
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
        for(Iterator<AffinityGroup> it = affinityGroups.iterator(); it.hasNext();) {
            AffinityGroup ag = it.next();

            for(Guid id : ag.getEntityIds()) {
                Set<Guid> temp = new HashSet<>();
                temp.add(id);
                uag.add(temp);
                vmIndex.put(id, temp);
            }
        }

        // Go through each positive affinity group and merge all existing groups
        // that contain the referenced VMs into one.
        for(AffinityGroup ag : affinityGroups) {
            if(ag.isPositive()) {
                Set<Guid> mergedGroup = new HashSet<>();

                for(Guid id : ag.getEntityIds()) {
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
        List<AffinityGroup> output = new ArrayList<>();

        for(Set<Guid> s : uag) {
            AffinityGroup temp = new AffinityGroup();
            temp.setPositive(true);
            List<Guid> entities = new ArrayList<>();

            entities.addAll(s);
            temp.setEntityIds(entities);
            output.add(temp);
        }

        return output;
    }

    public static class AffinityGroupConflict {
        final Collection<Guid> positiveVms;
        final Collection<Guid> negativeVms;

        public AffinityGroupConflict(Collection<Guid> positiveVms, Collection<Guid> negativeVms) {
            this.positiveVms = positiveVms;
            this.negativeVms = negativeVms;
        }

        public Collection<Guid> getPositiveVms() {
            return positiveVms;
        }

        public Collection<Guid> getNegativeVms() {
            return negativeVms;
        }
    }
}
