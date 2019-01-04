package org.ovirt.engine.core.bll.scheduling.policyunits;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.ovirt.engine.core.bll.scheduling.PolicyUnitImpl;
import org.ovirt.engine.core.bll.scheduling.SchedulingUnit;
import org.ovirt.engine.core.bll.scheduling.pending.PendingResourceManager;
import org.ovirt.engine.core.bll.scheduling.selector.SelectorInstance;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@SchedulingUnit(
        guid = "0c89f215-9c4f-4bbb-8419-31a2d43ae7ea",
        name = "BasicWeighting",
        type = PolicyUnitType.SELECTOR,
        description = "The host with lowest accumulated weight wins. No ranking or scaling is performed."
)
public class BasicWeightSelectorPolicyUnit extends PolicyUnitImpl {
    public BasicWeightSelectorPolicyUnit(PolicyUnit policyUnit, PendingResourceManager pendingResourceManager) {
        super(policyUnit, pendingResourceManager);
    }

    @Override
    public SelectorInstance selector(Map<String, String> parameters) {
        return new Selector();
    }

    public static class Selector implements SelectorInstance {
        final Map<Guid, Integer> weightTable = new HashMap<>();
        final Map<Guid, Integer> factorTable = new HashMap<>();

        @Override
        public void init(List<Pair<Guid, Integer>> policyUnits, List<Guid> hosts) {
            for (Pair<Guid, Integer> pair: policyUnits) {
                factorTable.put(pair.getFirst(), pair.getSecond());
            }
        }

        @Override
        public void record(Guid policyUnit, Guid host, Integer weight) {
            weightTable.putIfAbsent(host, 0);
            Integer acc = weightTable.get(host);
            Integer factor = factorTable.getOrDefault(policyUnit, 1);
            acc += factor * weight;
            weightTable.put(host, acc);
        }

        @Override
        public Optional<Guid> best() {
            return weightTable.entrySet().stream()
                    .min(Comparator.comparingInt(Entry::getValue))
                    .map(Map.Entry::getKey);
        }
    }
}
