package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
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
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters, List<String> messages) {
        return getAcceptableHosts(true, hosts, vm, messages);
    }

    public static List<VDS> getAcceptableHosts(boolean enforcing,
            List<VDS> hosts,
            VM vm,
            List<String> messages) {
        List<AffinityGroup> affinityGroups = getAffinityGroupDao().getAllAffinityGroupsByVmId(vm.getId());
        // no affinity groups found for VM return all hosts
        if (affinityGroups.isEmpty()) {
            return hosts;
        }

        Set<Guid> allVmIdsPositive = new HashSet<Guid>();
        Set<Guid> allVmIdsNegative = new HashSet<Guid>();

        List<String> positiveAffinityGroupNames = new ArrayList<>();
        // Group by all vms in affinity groups per positive or negative
        for (AffinityGroup affinityGroup : affinityGroups) {
            if (affinityGroup.isEnforcing() == enforcing) {
                for (Guid entityId : affinityGroup.getEntityIds()) {
                    // Skip current VM
                    if (entityId.equals(vm.getId())) {
                        continue;
                    }
                    if (affinityGroup.isPositive()) {
                        positiveAffinityGroupNames.add(affinityGroup.getName());
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
        Map<Guid, VM> runningVMsMap = new HashMap<Guid, VM>();
        for (VM iter : getVmDao().getAllRunningByCluster(vm.getVdsGroupId())) {
            runningVMsMap.put(iter.getId(), iter);
        }

        Set<Guid> acceptableHosts = new HashSet<Guid>();
        // Group all hosts for VMs with positive affinity
        for (Guid id : allVmIdsPositive) {
            VM runVm = runningVMsMap.get(id);
            if (runVm != null && runVm.getRunOnVds() != null) {
                acceptableHosts.add(runVm.getRunOnVds());
            }
        }
        Map<Guid, VDS> hostMap = new HashMap<>();
        for (VDS host : hosts) {
            hostMap.put(host.getId(), host);
        }
        boolean hasPositiveConstraint = false;
        // No hosts associated with positive affinity, all hosts is applicable.
        if (acceptableHosts.isEmpty()) {
            acceptableHosts.addAll(hostMap.keySet());
        } else if (acceptableHosts.size() == 1 && hostMap.containsKey(acceptableHosts.iterator().next())) {
            hasPositiveConstraint = true;
            // Only one host is allowed for positive affinity, i.e. if the VM contained in a positive
            // affinity group he must run on the host that all the other members are running, if the
            // VMs spread across hosts, the affinity rule isn't applied.
        } else {
            messages.add(String.format("$affinityGroupName %1$s", StringUtils.join(positiveAffinityGroupNames, ", ")));
            List<String> hostsNames = new ArrayList<>();
            for (Guid hostId : acceptableHosts) {
                if (hostMap.containsKey(hostId)) {
                    hostsNames.add(hostMap.get(hostId).getName());
                } else {
                    hostsNames.add(getVdsStaticDao().get(hostId).getName());
                }
            }
            messages.add(String.format("$hostName %1$s", StringUtils.join(hostsNames, ", ")));
            messages.add(VdcBllMessages.ACTION_TYPE_FAILED_POSITIVE_AFFINITY_GROUP.toString());
            return null;
        }
        // Handle negative affinity
        StringBuilder negativeLogMessage = new StringBuilder("Negative Affinity remove host(s):");
        for (Guid id : allVmIdsNegative) {
            VM runVm = runningVMsMap.get(id);
            if (runVm != null && runVm.getRunOnVds() != null) {
                acceptableHosts.remove(runVm.getRunOnVds());
                negativeLogMessage.append(MessageFormat.format(" {0} (vm {1}),", runVm.getRunOnVds(), vm.getName()));
            }
        }
        if (acceptableHosts.isEmpty()) {
            if (hasPositiveConstraint) {
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_MIX_POSITIVE_NEGATIVE_AFFINITY_GROUP.toString());
            } else {
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_NEGATIVE_AFFINITY_GROUP.toString());
            }
            log.info(negativeLogMessage);
            return null;
        }

        List<VDS> retList = new ArrayList<VDS>();
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
