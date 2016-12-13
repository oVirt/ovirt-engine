package org.ovirt.engine.core.bll.scheduling.external;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public interface ExternalSchedulerBroker {
    Optional<ExternalSchedulerDiscoveryResult> runDiscover();

    List<Guid> runFilters(List<String> filterNames,
            List<Guid> hostIDs, Guid vmID, Map<String, String> propertiesMap);

    List<WeightResultEntry> runScores(List<Pair<String, Integer>> scoreNameAndWeight,
            List<Guid> hostIDs,
            Guid vmID,
            Map<String, String> propertiesMap);

    Optional<BalanceResult> runBalance(String balanceName, List<Guid> hostIDs, Map<String, String> propertiesMap);

}
