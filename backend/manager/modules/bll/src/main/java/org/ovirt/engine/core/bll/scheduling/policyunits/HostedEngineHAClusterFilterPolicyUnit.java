package org.ovirt.engine.core.bll.scheduling.policyunits;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HostedEngineHAClusterFilterPolicyUnit extends PolicyUnitImpl {
    public HostedEngineHAClusterFilterPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {

        // The filter is relevant only for Hosted Engine VM
        if (vm.isHostedEngine()) {

            List<VDS> hostsToRunOn = new ArrayList<VDS>();
            for (VDS host : hosts) {
                int haScore = host.getHighlyAvailableScore();
                if (haScore > 0) {
                    hostsToRunOn.add(host);
                    log.debugFormat("Host {0} wasn't filtered out as it has a score of {1}",
                            host.getName(),
                            haScore);
                } else {
                    log.debugFormat("Host {0} was filtered out as it doesn't have a positive score (the score is {1})", host.getName(), haScore);
                    messages.addMessage(host.getId(), VdcBllMessages.VAR__DETAIL__NOT_HE_HOST.name());
                }
            }

            return hostsToRunOn;
        } else {
            return hosts;
        }
    }
}
