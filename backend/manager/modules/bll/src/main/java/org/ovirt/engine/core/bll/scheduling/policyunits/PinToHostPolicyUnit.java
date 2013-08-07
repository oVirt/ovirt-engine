package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;

public class PinToHostPolicyUnit extends PolicyUnitImpl {

    public PinToHostPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters, List<String> messages) {
        if (vm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST) {
            for (VDS host : hosts) {
                if (host.getId().equals(vm.getDedicatedVmForVds())) {
                    return Arrays.asList(host);
                }
            }
        }
        return hosts;
    }
}
