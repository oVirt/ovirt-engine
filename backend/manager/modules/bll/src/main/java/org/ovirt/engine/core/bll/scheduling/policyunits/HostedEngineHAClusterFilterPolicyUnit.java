package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "e659c871-0bf1-4ccc-b748-f28f5d08dffd",
        name = "HA",
        description = "Runs the hosted engine VM only on hosts with a positive score",
        type = PolicyUnitType.FILTER
)
public class HostedEngineHAClusterFilterPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(HostedEngineHAClusterFilterPolicyUnit.class);

    public HostedEngineHAClusterFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(Cluster cluster, List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {

        // The filter is relevant only for Hosted Engine VM
        if (vm.isHostedEngine()) {

            List<VDS> hostsToRunOn = new ArrayList<>();
            for (VDS host : hosts) {
                int haScore = host.getHighlyAvailableScore();
                if (haScore > 0) {
                    hostsToRunOn.add(host);
                    log.debug("Host '{}' wasn't filtered out as it has a score of {}",
                            host.getName(),
                            haScore);
                } else {
                    log.debug("Host '{}' was filtered out as it doesn't have a positive score (the score is {})",
                            host.getName(), haScore);
                    messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__NOT_HE_HOST.name());
                }
            }

            return hostsToRunOn;
        } else {
            return hosts;
        }
    }
}
