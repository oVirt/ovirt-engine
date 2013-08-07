package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;

public class MigrationPolicyUnit extends PolicyUnitImpl {

    public MigrationPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters, List<String> messages) {
        if (vm.getRunOnVds() != null) {
            for (VDS host : hosts) {
                if (host.getId().equals(vm.getRunOnVds())) {
                    log.debugFormat("Vm {0} run on host {1}, filtering host", vm.getName(), host.getName());
                    hosts.remove(host);
                    break;
                }
            }
        }
        return hosts;
    }
}
