package org.ovirt.engine.core.bll.scheduling.external;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public interface ExternalSchedulerBroker {
    ExternalSchedulerDiscoveryResult runDiscover();

    List<Guid> runFilters(List<String> filterNames, List<Guid> hostIDs, Guid vmID, Map<String, String> propertiesMap);

    List<Pair<Guid, Integer>> runScores(List<Pair<String, Integer>> scoreNameAndWeight,
            List<Guid> hostIDs,
            Guid vmID,
            Map<String, String> propertiesMap);

    Pair<List<Guid>, Guid> runBalance(String balanceName, List<Guid> hostIDs, Map<String, String> propertiesMap);

}
