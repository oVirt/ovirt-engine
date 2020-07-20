package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesUtils;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

@SchedulingUnit(
        guid = "e69808a9-8a41-40f1-94ba-dd5d385d82d8",
        name = "VmToHostsAffinityGroups",
        description = "Enables Affinity Groups hard enforcement for VMs to hosts;"
                + " VMs in group are required to run either on one of the hosts in group (positive) "
                + "or on independent hosts which are excluded from the hosts in group (negative).",
        type = PolicyUnitType.FILTER
)
public class VmToHostAffinityFilterPolicyUnit extends PolicyUnitImpl {

    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private LabelDao labelDao;

    public VmToHostAffinityFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context,
            List<VDS> hosts,
            VM vm,
            PerHostMessages messages) {

        List<AffinityGroup> affinityGroups = affinityGroupDao.getAllAffinityGroupsWithFlatLabelsByVmId(vm.getId()).stream()
                .filter(ag -> ag.isVdsEnforcing() && ag.isVdsAffinityEnabled())
                .collect(Collectors.toList());

        if (FeatureSupported.isImplicitAffinityGroupSupported(context.getCluster().getCompatibilityVersion()) ) {
            List<Label> labels = labelDao.getAllByEntityIds(Collections.singleton(vm.getId()));
            affinityGroups.addAll(AffinityRulesUtils.affinityGroupsFromLabels(labels, context.getCluster().getId()));
        }

        // no affinity groups found for VM return all hosts with no violations
        if (affinityGroups.isEmpty()) {
            return hosts;
        }

        Set<Guid> hostViolations = new HashSet<>();
        for (AffinityGroup affinityGroup : affinityGroups) {
            List<Guid> vdsIds = affinityGroup.getVdsIds();
            if (affinityGroup.isVdsPositive()) {
                // log and score hosts that violate the positive affinity rules
                hosts.stream()
                        .filter(host -> !vdsIds.contains(host.getId()))
                        .forEach(host -> {
                            // TODO compute the affinity rule names
                            messages.addMessage(host.getId(), String.format("$affinityRules %1$s", ""));
                            messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__AFFINITY_FAILED_POSITIVE.toString());
                            hostViolations.add(host.getId());
                        });
            } else {
                // log and score hosts that violate the negative affinity rules
                hosts.stream()
                        .filter(host -> vdsIds.contains(host.getId()))
                        .forEach(host -> {
                            // TODO compute the affinity rule names
                            messages.addMessage(host.getId(), String.format("$affinityRules %1$s", ""));
                            messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__AFFINITY_FAILED_NEGATIVE.toString());
                            hostViolations.add(host.getId());
                        });
            }
        }

        return hosts.stream()
                .filter(host -> !hostViolations.contains(host.getId()))
                .collect(Collectors.toList());
    }
}
