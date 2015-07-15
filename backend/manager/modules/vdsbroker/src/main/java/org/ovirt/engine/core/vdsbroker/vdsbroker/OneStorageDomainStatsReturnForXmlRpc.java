package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;

@SuppressWarnings("unchecked")
public final class OneStorageDomainStatsReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String STATS = "stats";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("stats")]
    public Map<String, Object> storageStats;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.toStringBuilder(storageStats, builder);
        return builder.toString();
    }

    public OneStorageDomainStatsReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        storageStats = (Map<String, Object>) innerMap.get(STATS);
    }
}
