package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;

@SuppressWarnings("unchecked")
public final class OneStorageDomainInfoReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String INFO = "info";

    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("info")]
    public Map<String, Object> storageInfo;

    public OneStorageDomainInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        storageInfo = (Map<String, Object>) innerMap.get(INFO);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.toStringBuilder(storageInfo, builder);
        return builder.toString();
    }
}
