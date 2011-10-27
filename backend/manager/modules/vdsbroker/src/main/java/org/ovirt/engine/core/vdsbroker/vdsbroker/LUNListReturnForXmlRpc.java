package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class LUNListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String DEV_LIST = "devList";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("devList")]
    public XmlRpcStruct[] lunList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(lunList, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public LUNListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(DEV_LIST);
        if (temp == null) {
            lunList = null;
        } else {
            Object[] tempArray = (Object[]) temp;
            lunList = new XmlRpcStruct[tempArray.length];
            for (int i = 0; i < tempArray.length; i++) {
                lunList[i] = new XmlRpcStruct((Map<String, Object>) tempArray[i]);
            }
        }
    }

}
