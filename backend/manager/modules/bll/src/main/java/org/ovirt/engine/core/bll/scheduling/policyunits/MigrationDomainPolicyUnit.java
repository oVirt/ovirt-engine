package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;

public class MigrationDomainPolicyUnit extends PolicyUnitImpl {

    public MigrationDomainPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, Map<String, Object> parameters, List<String> messages) {
        VM vm = (VM) parameters.get(PolicyUnitImpl.VM);
        List<VDS> toRemoveHostList = new ArrayList<VDS>();
        for (VDS host : hosts) {
            if ((!host.getVdsGroupId().equals(vm.getVdsGroupId())) || (host.getStatus() != VDSStatus.Up)) {
                log.debugFormat("host: {0} is not in up status or belongs to the VM's cluster", host.getName());
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_CLUSTER.toString());
                toRemoveHostList.add(host);
            }
        }
        hosts.removeAll(toRemoveHostList);
        return hosts;
    }
}
