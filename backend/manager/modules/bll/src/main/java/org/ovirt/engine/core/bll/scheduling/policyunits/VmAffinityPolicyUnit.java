package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.pending.PendingVM;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class VmAffinityPolicyUnit extends PolicyUnitImpl{
    private static final Logger log = LoggerFactory.getLogger(VmAffinityPolicyUnit.class);

    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private VmDao vmDao;

    protected VmAffinityPolicyUnit(PolicyUnit policyUnit, PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    /**
     * Get hosts which satisfy affinity groups for a VM.
     * Returns also the number of nonmigratable VMs running on each host.
     *
     * @param onlyEnforcing - check only enforcing affinity groups
     * @return Map containing host ids and the number of nonmigratable VMs running on the host
     */
    protected List<VDS> getAcceptableHosts(boolean onlyEnforcing,
            List<VDS> hosts,
            VM vm,
            PerHostMessages messages) {

        List<AffinityGroup> affinityGroups = affinityGroupDao.getAllAffinityGroupsByVmId(vm.getId());
        // no affinity groups found for VM return all hosts
        if (affinityGroups.isEmpty()) {
            return hosts;
        }

        Set<Guid> allVmIdsPositive = new HashSet<>();
        Set<Guid> allVmIdsNegative = new HashSet<>();

        // Group by all vms in affinity groups per positive or negative
        for (AffinityGroup affinityGroup : affinityGroups) {
            if (affinityGroup.isVmAffinityEnabled() && (!onlyEnforcing || affinityGroup.isVmEnforcing())) {
                for (Guid entityId : affinityGroup.getVmIds()) {
                    // Skip current VM
                    if (entityId.equals(vm.getId())) {
                        continue;
                    }
                    if (affinityGroup.isVmPositive()) {
                        allVmIdsPositive.add(entityId);
                    } else if (affinityGroup.isVmNegative()) {
                        allVmIdsNegative.add(entityId);
                    }
                }
            }
        }

        // No entities, all hosts are valid
        if (allVmIdsPositive.isEmpty() && allVmIdsNegative.isEmpty()) {
            return hosts;
        }

        // Get all running VMs in cluster
        Map<Guid, VM> runningVMsMap = new HashMap<>();
        for (VM iter : vmDao.getAllRunningByCluster(vm.getClusterId())) {
            runningVMsMap.put(iter.getId(), iter);
        }

        // Update the VM list with pending VMs
        for (PendingVM resource: pendingResourceManager.pendingResources(PendingVM.class)) {
            VM pendingVm = new VM();
            pendingVm.setId(resource.getVm());
            pendingVm.setRunOnVds(resource.getHost());
            runningVMsMap.put(pendingVm.getId(), pendingVm);
        }

        Set<Guid> acceptableHosts = new HashSet<>();
        // Group all hosts for VMs with positive affinity
        for (Guid id : allVmIdsPositive) {
            VM runVm = runningVMsMap.get(id);
            if (runVm != null && runVm.getRunOnVds() != null) {
                acceptableHosts.add(runVm.getRunOnVds());
            }
        }

        Set<Guid> unacceptableHosts = new HashSet<>();
        // Group all hosts for VMs with negative affinity
        for (Guid id : allVmIdsNegative) {
            VM runVm = runningVMsMap.get(id);
            if (runVm != null && runVm.getRunOnVds() != null) {
                unacceptableHosts.add(runVm.getRunOnVds());
            }
        }

        Map<Guid, VDS> hostMap = new HashMap<>();
        for (VDS host : hosts) {
            hostMap.put(host.getId(), host);
        }

        // Compute the intersection of hosts with positive and negative affinity and report that
        // contradicting rules to the log
        unacceptableHosts.retainAll(acceptableHosts);
        for (Guid id: unacceptableHosts) {
            log.warn("Host '{}' ({}) belongs to both positive and negative affinity list" +
                            " while scheduling VM '{}' ({})",
                    hostMap.get(id).getName(), id,
                    vm.getName(), vm.getId());
        }

        // No hosts associated with positive affinity, all hosts are applicable.
        if (acceptableHosts.isEmpty()) {
            acceptableHosts.addAll(hostMap.keySet());
        }
        else if (acceptableHosts.size() > 1) {
            log.warn("Invalid affinity situation was detected while scheduling VM '{}' ({})." +
                            " VMs belonging to the same affinity groups are running on more than one host.",
                    vm.getName(), vm.getId());
        }

        // Report hosts that were removed because of violating the positive affinity rules
        for (VDS host : hosts) {
            if (!acceptableHosts.contains(host.getId())) {
                messages.addMessage(host.getId(),
                        String.format("$affinityRules %1$s", "")); // TODO compute the affinity rule names
                messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__AFFINITY_FAILED_POSITIVE.toString());
            }
        }

        // Remove hosts that contain VMs with negaive affinity to the currently scheduled Vm
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

        List<VDS> retList = new ArrayList<>();
        for (VDS host : hosts) {
            if (acceptableHosts.contains(host.getId())) {
                retList.add(host);
            }
        }

        return retList;
    }
}
