package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.pending.PendingVM;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
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

    public VmAffinityFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {
        return getAcceptableHosts(true, hosts, vm, messages, getPendingResourceManager());
    }

    public static List<VDS> getAcceptableHosts(boolean enforcing,
            List<VDS> hosts,
            VM vm,
            PerHostMessages messages,
            PendingResourceManager pendingResourceManager) {
        List<AffinityGroup> affinityGroups = getAffinityGroupDao().getAllAffinityGroupsByVmId(vm.getId());
        // no affinity groups found for VM return all hosts
        if (affinityGroups.isEmpty()) {
            return hosts;
        }

        Set<Guid> allVmIdsPositive = new HashSet<>();
        Set<Guid> allVmIdsNegative = new HashSet<>();

        // Group by all vms in affinity groups per positive or negative
        for (AffinityGroup affinityGroup : affinityGroups) {
            if (affinityGroup.isEnforcing() == enforcing) {
                for (Guid entityId : affinityGroup.getEntityIds()) {
                    // Skip current VM
                    if (entityId.equals(vm.getId())) {
                        continue;
                    }
                    if (affinityGroup.isPositive()) {
                        allVmIdsPositive.add(entityId);
                    } else {
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
        for (VM iter : getVmDao().getAllRunningByCluster(vm.getClusterId())) {
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

    protected static VdsStaticDao getVdsStaticDao() {
        return DbFacade.getInstance().getVdsStaticDao();
    }

    protected static AffinityGroupDao getAffinityGroupDao() {
        return DbFacade.getInstance().getAffinityGroupDao();
    }

    protected static VmDao getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }
}
