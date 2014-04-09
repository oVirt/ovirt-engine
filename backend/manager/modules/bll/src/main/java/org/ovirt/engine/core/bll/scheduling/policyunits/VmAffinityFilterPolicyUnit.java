package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class VmAffinityFilterPolicyUnit extends PolicyUnitImpl {
    public VmAffinityFilterPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {
        return getAcceptableHosts(true, hosts, vm, messages);
    }

    public static List<VDS> getAcceptableHosts(boolean enforcing,
            List<VDS> hosts,
            VM vm,
            PerHostMessages messages) {
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
        for (VM iter : getVmDao().getAllRunningByCluster(vm.getVdsGroupId())) {
            runningVMsMap.put(iter.getId(), iter);
        }
        Map<Guid, VDS> hostMap = new HashMap<>();
        for (VDS host : hosts) {
            hostMap.put(host.getId(), host);
        }

        Set<Guid> acceptableHosts = new HashSet<>();
        // Group all hosts for VMs with positive affinity
        for (Guid id : allVmIdsPositive) {
            VM runVm = runningVMsMap.get(id);
            if (runVm != null && runVm.getRunOnVds() != null && hostMap.get(runVm.getRunOnVds()) != null
                    // when a host preparing for maintenance, we should ignore the positive affinity (without that we
                    // can't migrate).
                    && hostMap.get(runVm.getRunOnVds()).getStatus() != VDSStatus.PreparingForMaintenance) {
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

        // Compute the intersection of hosts with positive and negative affinity and report that
        // contradicting rules to the log
        unacceptableHosts.retainAll(acceptableHosts);
        for (Guid id: unacceptableHosts) {
            log.warnFormat("Host {1} ({2}) belongs to both positive and negative affinity list" +
                    " while scheduling VM {3} ({4})",
                    hostMap.get(id).getName(), id.toString(),
                    vm.getName(), vm.getId());
        }

        // No hosts associated with positive affinity, all hosts are applicable.
        if (acceptableHosts.isEmpty()) {
            acceptableHosts.addAll(hostMap.keySet());
        }
        else if (acceptableHosts.size() > 1) {
            log.warnFormat("Invalid affinity situation was detected while scheduling VM {1} ({2})." +
                    " VMs belonging to the same affinity groups are running on more than one host.",
                    vm.getName(), vm.getId());
        }

        // Report hosts that were removed because of violating the positive affinity rules
        for (VDS host : hosts) {
            if (!acceptableHosts.contains(host.getId())) {
                messages.addMessage(host.getId(),
                        String.format("$affinityRules %1$s", "")); // TODO compute the affinity rule names
                messages.addMessage(host.getId(), VdcBllMessages.VAR__DETAIL__AFFINITY_FAILED_POSITIVE.toString());
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
                        VdcBllMessages.VAR__DETAIL__AFFINITY_FAILED_NEGATIVE.toString());
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

    protected static VdsStaticDAO getVdsStaticDao() {
        return DbFacade.getInstance().getVdsStaticDao();
    }

    protected static AffinityGroupDao getAffinityGroupDao() {
        return DbFacade.getInstance().getAffinityGroupDao();
    }

    protected static VmDAO getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }
}
