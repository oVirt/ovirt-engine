package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.compat.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "3e4a7d54-9e7f-11e5-8994-feff819cdc9f",
        name = "Compatibility-Version",
        type = PolicyUnitType.FILTER,
        description = "Runs VMs only on hosts with a proper compatibility-version support"
)
public class CompatibilityVersionFilterPolicyUnit extends PolicyUnitImpl {

    private static final Logger log = LoggerFactory.getLogger(CompatibilityVersionFilterPolicyUnit.class);

    public CompatibilityVersionFilterPolicyUnit(PolicyUnit policyUnit, PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context,
            List<VDS> hosts,
            VM vm,
            PerHostMessages messages) {
        // get required compatibility version
        Version vmCustomCompatibilityVersion = vm.getCustomCompatibilityVersion();
        if (vmCustomCompatibilityVersion == null) { // use cluster default - all hosts are valid
            return hosts;
        }

        // find compatible hosts
        List<VDS> hostsToRunOn = new ArrayList<>();
        for (VDS host : hosts) {
            if (host.getSupportedClusterVersionsSet().contains(vmCustomCompatibilityVersion)) {
                hostsToRunOn.add(host);
                log.debug("Host {} wasn't filtered out as it supports the VM required compatibility version({})",
                        host.getName(),
                        vmCustomCompatibilityVersion);
            } else {
                log.debug(
                        "Host {} was filtered out as it doesn't support the VM required compatibility-version ({}). Host supported compatibility-versions are: {}.",
                        host.getName(),
                        vmCustomCompatibilityVersion,
                        host.getSupportedClusterLevels());
                messages.addMessage(host.getId(),
                        String.format("$vmCompatibilityVersions %1$s", vmCustomCompatibilityVersion));
                messages.addMessage(host.getId(),
                        String.format("$hostCompatibilityVersions %1$s", host.getSupportedClusterLevels()));
                messages.addMessage(host.getId(),
                        EngineMessage.VAR__DETAIL__UNSUPPORTED_COMPATIBILITY_VERSION.toString());
            }
        }
        return hostsToRunOn;
    }

}
