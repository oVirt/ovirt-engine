package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.pending.PendingVM;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.MemoizingSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "84e6ddee-ab0d-42dd-82f0-c297779db566",
        name = "VmAffinityGroups",
        description = "Enables Affinity Groups hard enforcement for VMs; VMs in group are required to run either on"
                + " the same hypervisor host (positive) or on independent hypervisor hosts (negative)",
        type = PolicyUnitType.FILTER
)
public class VmAffinityFilterPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(VmAffinityFilterPolicyUnit.class);

    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private VmDao vmDao;

    public VmAffinityFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, List<VM> vmGroup, PerHostMessages messages) {
        if (context.isIgnoreHardVmToVmAffinity()) {
            return hosts;
        }

        Set<AffinityGroup> affinityGroups = new HashSet<>();
        // TODO - get all affinity gorups in 1 DB call
        vmGroup.forEach(vm -> affinityGroups.addAll(affinityGroupDao.getAllAffinityGroupsWithFlatLabelsByVmId(vm.getId())));

        // no affinity groups found for VM group return all hosts
        if (affinityGroups.isEmpty()) {
            return hosts;
        }

        Set<Guid> acceptableHosts = getAcceptableHosts(hosts, vmGroup, affinityGroups, messages);

        return hosts.stream()
                .filter(h -> acceptableHosts.contains(h.getId()))
                .collect(Collectors.toList());
    }

    private Set<Guid> getAcceptableHosts(List<VDS> hosts,
            List<VM> vmGroup,
            Set<AffinityGroup> affinityGroups,
            PerHostMessages messages) {

        Set<Guid> vmIdSet = vmGroup.stream()
                .map(VM::getId)
                .collect(Collectors.toSet());

        Set<Guid> allVmIdsPositive = new HashSet<>();
        Set<Guid> allVmIdsNegative = new HashSet<>();

        // Group by all vms in affinity groups per positive or negative
        for (AffinityGroup affinityGroup : affinityGroups) {
            if (affinityGroup.isVmAffinityEnabled() && (affinityGroup.isVmEnforcing())) {
                List<Guid> vmsNotInVmGroup = affinityGroup.getVmIds().stream()
                        .filter(id -> !vmIdSet.contains(id))
                        .collect(Collectors.toList());

                if (affinityGroup.isVmPositive()) {
                    allVmIdsPositive.addAll(vmsNotInVmGroup);
                } else if (affinityGroup.isVmNegative()) {
                    // Check if the negative AG contains more VMs from the group
                    List<Guid> vmsInPositiveAndNegativeGroup = affinityGroup.getVmIds().stream()
                            .filter(vmIdSet::contains)
                            .collect(Collectors.toList());

                    if (vmsInPositiveAndNegativeGroup.size() > 1) {
                        log.warn(
                                "Affinity conflict detected! Negative affinity group '{}' contains VMs that are in positive affinity: {}",
                                affinityGroup.getName(),
                                vmGroup.stream()
                                        .filter(vm -> vmsInPositiveAndNegativeGroup.contains(vm.getId()))
                                        .map(VM::getName)
                                        .collect(Collectors.toList()));

                        return Collections.emptySet();
                    }

                    allVmIdsNegative.addAll(vmsNotInVmGroup);
                }
            }
        }

        // No entities, all hosts are valid
        if (allVmIdsPositive.isEmpty() && allVmIdsNegative.isEmpty()) {
            return hosts.stream().map(VDS::getId).collect(Collectors.toSet());
        }

        Guid clusterId = vmGroup.get(0).getClusterId();

        // Get all running VMs in cluster
        Map<Guid, VM> runningVMsMap = vmDao.getAllRunningByCluster(clusterId).stream()
                .collect(Collectors.toMap(VM::getId, vm -> vm));

        // Update the VM list with pending VMs
        for (PendingVM resource: pendingResourceManager.pendingResources(PendingVM.class)) {
            VM pendingVm = new VM();
            pendingVm.setId(resource.getVm());
            pendingVm.setRunOnVds(resource.getHost());
            runningVMsMap.put(pendingVm.getId(), pendingVm);
        }

        // Group all hosts for VMs with positive affinity
        Set<Guid> acceptableHosts = allVmIdsPositive.stream()
                .map(runningVMsMap::get)
                .filter(v -> v != null && v.getRunOnVds() != null)
                .map(VM::getRunOnVds)
                .collect(Collectors.toSet());

        // Group all hosts for VMs with negative affinity
        Set<Guid> unacceptableHosts = allVmIdsNegative.stream()
                .map(runningVMsMap::get)
                .filter(v -> v != null && v.getRunOnVds() != null)
                .map(VM::getRunOnVds)
                .collect(Collectors.toSet());

        Supplier<String> vmNames = new MemoizingSupplier<>(() -> getVmNames(vmGroup));// Compute the intersection of hosts with positive and negative affinity and report that
        // contradicting rules to the log
        unacceptableHosts.retainAll(acceptableHosts);

        if (!unacceptableHosts.isEmpty()) {
            Map<Guid, VDS> hostMap = hosts.stream().collect(Collectors.toMap(VDS::getId, host -> host));
            for (Guid id : unacceptableHosts) {
                log.warn("Host '{}' ({}) belongs to both positive and negative affinity list" +
                                " while scheduling VMs: {}",
                        hostMap.get(id).getName(), id,
                        vmNames.get());
            }
        }

        if (acceptableHosts.size() > 1) {
            log.warn("Invalid affinity situation was detected while scheduling VMs: {}." +
                            " VMs belonging to the same positive enforcing affinity groups are" +
                            " running on more than one host.",
                    vmNames.get());
        }

        // No hosts associated with positive affinity, all hosts are applicable.
        if (acceptableHosts.isEmpty()) {
            acceptableHosts = hosts.stream().map(VDS::getId).collect(Collectors.toSet());
        }

        // Report hosts that were removed because of violating the positive affinity rules
        for (VDS host : hosts) {
            if (!acceptableHosts.contains(host.getId())) {
                messages.addMessage(host.getId(),
                        String.format("$affinityRules %1$s", "")); // TODO compute the affinity rule names
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__AFFINITY_FAILED_POSITIVE.toString());
            }
        }

        // Remove hosts that contain VMs with negative affinity to the currently scheduled Vm
        for (Guid id : allVmIdsNegative) {
            VM runVm = runningVMsMap.get(id);
            if (runVm != null && runVm.getRunOnVds() != null
                    && acceptableHosts.contains(runVm.getRunOnVds())) {
                acceptableHosts.remove(runVm.getRunOnVds());
                messages.addMessage(runVm.getRunOnVds(),
                        String.format("$affinityRules %1$s", "")); // TODO compute the affinity rule names
                messages.addMessage(runVm.getRunOnVds(),
                        EngineMessage.VAR__DETAIL__AFFINITY_FAILED_NEGATIVE.toString());
            }
        }

        return acceptableHosts;
    }

    private String getVmNames(List<VM> vmGroup) {
        return vmGroup.stream()
                .map(vm -> String.format("'%s' (%s)", vm.getName(), vm.getId()))
                .collect(Collectors.joining(", "));
    }
}
