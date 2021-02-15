package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

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
import org.ovirt.engine.core.dao.VdsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "47ea3cd0-8a2f-438d-9f63-49534178882a",
        name = "Migration-Tsc-Frequency",
        description = "High-Performance VMs can only be migrated to hosts with the same TSC frequency",
        type = PolicyUnitType.FILTER
)
public class MigrationTscFrequencyPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(MigrationTscFrequencyPolicyUnit.class);

    /* Taken from libvirt: 250 parts per million (ppm) is a half of NTP threshold */
    private static final int TSC_TOLERANCE = 250;

    @Inject
    private VdsDao vdsDao;

    public MigrationTscFrequencyPolicyUnit(PolicyUnit policyUnit, PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<VDS> filter(SchedulingContext context, List<VDS> hosts, VM vm, PerHostMessages messages) {
        // High-performance VMs can be only migrated to hosts with the same TSC frequency
        // There's no need to check for invtsc flag, because CpuLevelFilterPolicyUnit will take care of that
        if (vm.getRunOnVds() != null && vm.getUseTscFrequency()) {
            VDS vmVds = vdsDao.get(vm.getRunOnVds());
            if (vmVds == null || vmVds.getTscFrequency() == null) {
                return hosts;
            }

            return hosts.stream()
                    .filter(vds -> vds.isTscScalingEnabled() || vdsMatchesTscFrequency(messages, vmVds, vds))
                    .collect(Collectors.toList());
        }

        return hosts;
    }

    private boolean tscFrequenciesWithinTolerance(String srcTscFrequency, String dstTscFrequency) {
        if (srcTscFrequency == null || dstTscFrequency == null) {
            return false;
        }

        try {
            final long srcFrequency = Long.parseLong(srcTscFrequency);
            final long dstFrequency = Long.parseLong(dstTscFrequency);
            // The same check as in libvirt
            final long tolerance = srcFrequency * TSC_TOLERANCE / 1000000;
            return Math.abs(dstFrequency - srcFrequency) <= tolerance;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean vdsMatchesTscFrequency(PerHostMessages messages, VDS vmVds, VDS vds) {
        boolean valid = tscFrequenciesWithinTolerance(vmVds.getTscFrequencyIntegral(), vds.getTscFrequencyIntegral());
        if (!valid) {
            log.debug("Host '{}' has a different TSC frequency than '{}', filtering out", vds.getName(), vmVds.getName());
            messages.addMessage(vds.getId(), EngineMessage.VAR__DETAIL__TSC_FREQ_MISMATCH.toString());
        }

        return valid;
    }
}
