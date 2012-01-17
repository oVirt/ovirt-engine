package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class OneStorageDomainStatsReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String STATS = "stats";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("stats")]
    public XmlRpcStruct mStorageStats;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(mStorageStats, builder);
        return builder.toString();
    }

    public OneStorageDomainStatsReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(STATS);
        if (temp != null) {
            mStorageStats = new XmlRpcStruct((Map<String, Object>) temp);
        }
    }
}
