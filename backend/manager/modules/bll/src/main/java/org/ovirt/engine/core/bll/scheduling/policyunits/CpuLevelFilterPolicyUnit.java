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

public class CpuLevelFilterPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(CpuLevelFilterPolicyUnit.class);

    public CpuLevelFilterPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, VM vm, Map<String, String> parameters,
                            PerHostMessages messages) {
        boolean filteredOutHosts = false;
        if (StringUtils.isNotEmpty(vm.getCpuName())) {
            List<VDS> hostsToRunOn = new ArrayList<VDS>();
            for (VDS host : hosts) {
                ServerCpu cpu = CpuFlagsManagerHandler.findMaxServerCpuByFlags(host.getCpuFlags(),
                        host.getVdsGroupCompatibilityVersion());
                String hostCpuName = cpu == null ? null : cpu.getCpuName();
                if (StringUtils.isNotEmpty(hostCpuName)) {
                    int compareResult = CpuFlagsManagerHandler.compareCpuLevels(vm.getCpuName(), hostCpuName, vm.getVdsGroupCompatibilityVersion());
                    if (compareResult <= 0) {
                        hostsToRunOn.add(host);
                        log.debug("Host '{}' wasn't filtered out as it has a CPU level ({}) which is higher or equal than the CPU level the VM was run with ({})",
                                host.getName(),
                                hostCpuName,
                                vm.getCpuName());
                    } else {
                        log.debug("Host '{}' was filtered out as it has a CPU level ({}) which is lower than the CPU level the VM was run with ({})",
                                host.getName(),
                                hostCpuName,
                                vm.getCpuName());
                        messages.addMessage(host.getId(), String.format("$hostCPULevel %1$s", hostCpuName));
                        messages.addMessage(host.getId(), String.format("$vmCPULevel %1$s", vm.getCpuName()));
                        messages.addMessage(host.getId(), VdcBllMessages.VAR__DETAIL__LOW_CPU_LEVEL.toString());
                    }
                }
            }

            return hostsToRunOn;
        } else {
            return hosts;
        }
    }
}
