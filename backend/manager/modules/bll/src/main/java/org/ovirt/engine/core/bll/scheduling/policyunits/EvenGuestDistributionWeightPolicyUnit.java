package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;
import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class EvenGuestDistributionWeightPolicyUnit extends PolicyUnitImpl {
    final int spmVmGrace;

    public EvenGuestDistributionWeightPolicyUnit (PolicyUnit policyUnit,
            PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
        spmVmGrace = Config.<Integer> getValue(ConfigValues.SpmVmGraceForEvenGuestDistribute);
    }

    private int getOccupiedVmSlots(VDS vds, Map<String, String> parameters) {
        int occupiedSlots = vds.getVmActive();
        final int SPMVMCountGrace = NumberUtils.toInt(parameters.get("SpmVmGrace"),
                spmVmGrace);
        if (vds.isSpm())
            occupiedSlots += SPMVMCountGrace;

        return occupiedSlots;
    }

    private int calcEvenGuestDistributionScore(VDS vds, Map<String, String> parameters) {
        return Math.max(0, getOccupiedVmSlots(vds, parameters));
    }

    @Override
    public List<Pair<Guid, Integer>> score(VDSGroup cluster, List<VDS> hosts, VM vm, Map<String, String> parameters) {
        List<Pair<Guid, Integer>> scores = new ArrayList<>();
        for (VDS vds : hosts) {
            scores.add(new Pair<>(vds.getId(), calcEvenGuestDistributionScore(vds, parameters)));
        }
        return scores;
    }
}
