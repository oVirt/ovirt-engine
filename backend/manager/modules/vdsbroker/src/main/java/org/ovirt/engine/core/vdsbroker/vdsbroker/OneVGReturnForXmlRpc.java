package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class OneVGReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String INFO = "info";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("info")]
    public XmlRpcStruct vgInfo;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(vgInfo, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public OneVGReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(INFO);
        if (temp == null) {
            vgInfo = null;
        } else {
            vgInfo = new XmlRpcStruct((Map<String, Object>) temp);
        }
    }

}
