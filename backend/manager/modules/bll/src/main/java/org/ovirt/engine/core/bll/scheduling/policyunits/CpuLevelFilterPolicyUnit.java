package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CpuFlagsManagerHandler;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ovirt.engine.core.compat.Version;

public class CpuLevelFilterPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(CpuLevelFilterPolicyUnit.class);

    public CpuLevelFilterPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters,
                            PerHostMessages messages) {
        List<VDS> hostsToRunOn = new ArrayList<VDS>();
        String customCpu; // full name of the vm cpu
        Version latestVer = CpuFlagsManagerHandler.getLatestDictionaryVersion();

        /* get required cpu name */
        if (StringUtils.isNotEmpty(vm.getCpuName())) { // dynamic check - used for 1.migrating vms 2.run-once 3.after dynamic field is updated with current static-field\cluster
            customCpu = vm.getCpuName();
        } else if (StringUtils.isNotEmpty(vm.getCustomCpuName())) { // static check - used only for cases where the dynamic value hasn't been updated yet(canDo validation)
            customCpu = vm.getCustomCpuName();
        } else { // use cluster default - all hosts are valid
            return hosts;
        }

        customCpu = CpuFlagsManagerHandler.getCpuNameByCpuId(customCpu, latestVer); // translate vdsVerb to full cpu name
        if(StringUtils.isNotEmpty(customCpu)) { // checks if there's a cpu with the given vdsVerb

            /* find compatible hosts */
            for (VDS host : hosts) {
                ServerCpu cpu = CpuFlagsManagerHandler.findMaxServerCpuByFlags(host.getCpuFlags(), latestVer);
                String hostCpuName = cpu == null ? null : cpu.getCpuName();
                if (StringUtils.isNotEmpty(hostCpuName)) {
                    if (CpuFlagsManagerHandler.checkIfCpusSameManufacture(customCpu, hostCpuName, latestVer)) { // verify comparison uses only one cpu-level scale
                        int compareResult = CpuFlagsManagerHandler.compareCpuLevels(customCpu, hostCpuName, latestVer);
                        if (compareResult <= 0) {
                            hostsToRunOn.add(host);
                            log.debug("Host '{}' wasn't filtered out as it has a CPU level ({}) which is higher or equal than the CPU level the VM was run with ({})",
                                    host.getName(),
                                    hostCpuName,
                                    customCpu);
                        } else {
                            log.debug("Host '{}' was filtered out as it has a CPU level ({}) which is lower than the CPU level the VM was run with ({})",
                                    host.getName(),
                                    hostCpuName,
                                    customCpu);
                            messages.addMessage(host.getId(), String.format("$hostCPULevel %1$s", hostCpuName));
                            messages.addMessage(host.getId(), String.format("$vmCPULevel %1$s", customCpu));
                            messages.addMessage(host.getId(), VdcBllMessages.VAR__DETAIL__LOW_CPU_LEVEL.toString());
                        }
                    }
                }
            }
        }
        return hostsToRunOn;
    }
}
