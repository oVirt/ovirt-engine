package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LabelDao;

@SchedulingUnit(
        guid = "27846536-f653-11e5-9ce9-5e5517507c66",
        name = "Label",
        type = PolicyUnitType.FILTER,
        description = "Filters out hosts that do not have the required labels"
)
public class LabelFilterPolicyUnit extends PolicyUnitImpl {
    public LabelFilterPolicyUnit(PolicyUnit policyUnit,
                                 PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Inject
    LabelDao labelDao;

    @Override
    public List<VDS> filter(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {
        Map<Guid, Set<Guid>> objectToTags = new HashMap<>();

        final List<Guid> objects = hosts.stream().map(VDS::getId).collect(Collectors.toList());
        objects.add(vm.getId());

        // Prepare the entity -> label reverse mapping
        for (Label label: labelDao.getAllByEntityIds(objects)) {
            for (Guid entity: label.getVms()) {
                objectToTags.putIfAbsent(entity, new HashSet<>());
                objectToTags.get(entity).add(label.getId());
            }

            for (Guid entity: label.getHosts()) {
                objectToTags.putIfAbsent(entity, new HashSet<>());
                objectToTags.get(entity).add(label.getId());
            }
        }

        return hosts.stream()
                .filter(new TagMatcher(objectToTags, vm)).collect(Collectors.<VDS>toList());
    }

    static class TagMatcher implements Predicate<VDS> {
        final Map<Guid, Set<Guid>> objectToTags;
        final VM vm;

        public TagMatcher(@NotNull Map<Guid, Set<Guid>> objectToTags, @NotNull VM vm) {
            this.objectToTags = objectToTags;
            this.vm = vm;
        }

        @Override
        public boolean test(final VDS vds) {
            // Compute whether all VM Labels are present for the host under test
            final Set<Guid> vmLabels = objectToTags.getOrDefault(vm.getId(), Collections.emptySet());
            final Set<Guid> hostLabels = objectToTags.getOrDefault(vds.getId(), Collections.emptySet());

            return hostLabels.containsAll(vmLabels);
        }
    }
}
