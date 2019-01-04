package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "58894b5b-d55d-4f85-8f82-5bf217e640b0",
        name = "Emulated-Machine",
        description = "Runs VMs only on hosts with a proper emulated machine support",
        type = PolicyUnitType.FILTER
)
public class EmulatedMachineFilterPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(EmulatedMachineFilterPolicyUnit.class);

    public EmulatedMachineFilterPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {
        String requiredEmulatedMachine;
        List<VDS> hostsToRunOn = new ArrayList<>();

        /* get required emulated machine */
        if (StringUtils.isNotEmpty(vm.getEmulatedMachine())) { // dynamic check - used for 1.migrating vms 2.run-once 3.after dynamic field is updated with current static-field\cluster
            requiredEmulatedMachine = vm.getEmulatedMachine();
        } else if (StringUtils.isNotEmpty(vm.getCustomEmulatedMachine())) { // static check - used only for cases where the dynamic value hasn't been updated yet(validate)
            requiredEmulatedMachine = vm.getCustomEmulatedMachine();
        } else { // use cluster default - all hosts are valid
            return hosts;
        }

        /* find compatible hosts */
        for (VDS host : hosts) {
            String supportedEmulatedMachines = host.getSupportedEmulatedMachines();
            if(StringUtils.isNotEmpty(supportedEmulatedMachines)) {
                if (Arrays.asList(supportedEmulatedMachines.split(",")).contains(requiredEmulatedMachine)) {
                    hostsToRunOn.add(host);
                    log.debug("Host {} wasn't filtered out as it supports the VM required emulated machine ({})",
                            host.getName(),
                            requiredEmulatedMachine);
                } else {
                    log.debug("Host {} was filtered out as it doesn't support the VM required emulated machine ({}). Host supported emulated machines are: {}.",
                            host.getName(),
                            requiredEmulatedMachine,
                            supportedEmulatedMachines);
                    messages.addMessage(host.getId(), String.format("$vmEmulatedMachine %1$s", requiredEmulatedMachine));
                    messages.addMessage(host.getId(), String.format("$hostEmulatedMachines %1$s", supportedEmulatedMachines));
                    messages.addMessage(host.getId(), EngineMessage.VAR__DETAIL__UNSUPPORTED_EMULATED_MACHINE.toString());
                }
            }
        }
        return hostsToRunOn;
    }
}
