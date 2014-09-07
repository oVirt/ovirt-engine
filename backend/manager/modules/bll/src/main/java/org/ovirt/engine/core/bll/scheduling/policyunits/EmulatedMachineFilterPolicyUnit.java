package org.ovirt.engine.core.bll.scheduling.policyunits;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EmulatedMachineFilterPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(EmulatedMachineFilterPolicyUnit.class);

    public EmulatedMachineFilterPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters,
                            PerHostMessages messages) {
        String requiredEmulatedMachine;
        List<VDS> hostsToRunOn = new ArrayList<VDS>();

        /* get required emulated machine */
        if (StringUtils.isNotEmpty(vm.getEmulatedMachine())) { // dynamic check - used for 1.migrating vms 2.run-once 3.after dynamic field is updated with current static-field\cluster
            requiredEmulatedMachine = vm.getEmulatedMachine();
        } else if (StringUtils.isNotEmpty(vm.getCustomEmulatedMachine())) { // static check - used only for cases where the dynamic value hasn't been updated yet(canDo validation)
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
                    messages.addMessage(host.getId(), VdcBllMessages.VAR__DETAIL__UNSUPPORTED_EMULATED_MACHINE.toString());
                }
            }
        }
        return hostsToRunOn;
    }
}
