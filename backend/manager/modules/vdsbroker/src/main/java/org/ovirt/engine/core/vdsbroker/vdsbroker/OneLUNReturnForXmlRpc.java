package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class OneLUNReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String INFO = "info";

    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("info")]
    public XmlRpcStruct lunInfo;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(lunInfo, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public OneLUNReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        lunInfo = (innerMap.get(INFO) == null) ? null : new XmlRpcStruct((Map<String, Object>) innerMap.get(INFO));
    }

}
