package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CPUPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(CPUPolicyUnit.class);

    public CPUPolicyUnit(PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(VDSGroup cluster, List<VDS> hosts, VM vm, Map<String, String> parameters, PerHostMessages messages) {
        List<VDS> list = new ArrayList<>();
        for (VDS vds : hosts) {
            Integer cores = SlaValidator.getEffectiveCpuCores(vds,
                    cluster != null && cluster.getCountThreadsAsCores());
            if (cores != null && vm.getNumOfCpus(false) > cores) {
                messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__NOT_ENOUGH_CORES.toString());
                log.debug("Host '{}' has less cores ({}) than vm cores ({})",
                        vds.getName(),
                        cores,
                        vm.getNumOfCpus());
                continue;
            }
            list.add(vds);
        }
        return list;
    }
}
