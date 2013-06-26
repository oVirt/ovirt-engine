package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;

public class CPUPolicyUnit extends PolicyUnitImpl {

    public CPUPolicyUnit(PolicyUnit policyUnit) {
        super(policyUnit);
    }

    @Override
    public List<VDS> filter(List<VDS> hosts, Map<String, Object> parameters, List<String> messages) {
        VM vm = (VM) parameters.get(PolicyUnitImpl.VM);
        List<VDS> list = new ArrayList<VDS>();
        for (VDS vds : hosts) {
            Integer cores = SlaValidator.getInstance().getEffectiveCpuCores(vds);
            if (cores != null && vm.getNumOfCpus() > cores) {
                messages.add(VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_CPUS.toString());
                log.debugFormat("host {0} hass less cores ({1}) than vm cores ({2})",
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
