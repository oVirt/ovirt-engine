package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(MigrationPolicyUnit.class);

    public MigrationPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {
        if (vm.getRunOnVds() != null) {
            for (VDS host : hosts) {
                if (host.getId().equals(vm.getRunOnVds())) {
                    log.debug("Vm '{}' run on host '{}', filtering host", vm.getName(), host.getName());
                    hosts.remove(host);
                    break;
                }
            }
        }
        return hosts;
    }
}
