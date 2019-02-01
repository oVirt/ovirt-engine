package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SchedulingUnit(
        guid = "591cdb81-ba67-45b4-9642-e28f61a97d57",
        name = "PreferredHosts",
        description = "Prioritize preferred hosts during VM startup.",
        type = PolicyUnitType.WEIGHT
)
public class PreferredHostsWeightPolicyUnit extends PolicyUnitImpl {
    private static final Logger log = LoggerFactory.getLogger(PreferredHostsWeightPolicyUnit.class);

    public PreferredHostsWeightPolicyUnit(PolicyUnit policyUnit, PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public List<Pair<Guid, Integer>> score(SchedulingContext context, List<VDS> hosts, VM vm) {
        // Preferred hosts are only used during VM startup
        if (!vm.isDown()) {
            return Collections.emptyList();
        }

        List<Pair<Guid, Integer>> weights = new ArrayList<>();
        Set<Guid> preferredHosts = new HashSet<>(vm.getDedicatedVmForVdsList());
        boolean isAnyPreferredHost = preferredHosts.size() > 0;

        // Add penalization to hosts that are not in the preferred list
        for (VDS host: hosts) {
            if (isAnyPreferredHost && !preferredHosts.contains(host.getId())) {
                log.debug("Penalizing host '{}' because it is not preferred.", host.getName());
                weights.add(new Pair<>(host.getId(), 10000)); // TODO externalize weight
            } else {
                weights.add(new Pair<>(host.getId(), 0));
            }
        }

        return weights;
    }
}
