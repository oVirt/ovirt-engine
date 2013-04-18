package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;

@SuppressWarnings("unchecked")
public final class OneLUNReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String INFO = "info";

    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("info")]
    public Map<String, Object> lunInfo;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(lunInfo, builder);
        return builder.toString();
    }

    public OneLUNReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        lunInfo = (Map<String, Object>) innerMap.get(INFO);
    }

}
