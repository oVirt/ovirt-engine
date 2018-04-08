package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

@SuppressWarnings("unchecked")
public final class OneStorageDomainStatsReturn extends StatusReturn {
    private static final String STATS = "stats";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [MissingMapping(MappingAction.Ignore), Member("stats")]
    public Map<String, Object> storageStats;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        ObjectDescriptor.toStringBuilder(storageStats, builder);
        return builder.toString();
    }

    public OneStorageDomainStatsReturn(Map<String, Object> innerMap) {
        super(innerMap);
        storageStats = (Map<String, Object>) innerMap.get(STATS);
    }
}
