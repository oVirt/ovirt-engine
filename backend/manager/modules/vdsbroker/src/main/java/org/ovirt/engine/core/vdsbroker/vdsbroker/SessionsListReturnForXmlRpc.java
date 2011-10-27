package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class SessionsListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String SESSIONS = "sessions";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("sessions")]
    public XmlRpcStruct[] sessionsList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(sessionsList, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public SessionsListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] temp = (Object[]) innerMap.get(SESSIONS);
        if (temp != null) {
            sessionsList = new XmlRpcStruct[temp.length];
            for (int i = 0; i < temp.length; i++) {
                sessionsList[i] = new XmlRpcStruct((Map<String, Object>) temp[i]);
            }
        }
    }

}
