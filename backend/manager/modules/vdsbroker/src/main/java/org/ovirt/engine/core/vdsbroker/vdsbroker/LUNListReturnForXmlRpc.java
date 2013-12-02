package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;

public final class LUNListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String DEV_LIST = "devList";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("devList")]
    public Map<String, Object>[] lunList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.toStringBuilder(lunList, builder);
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
            lunList = new Map[tempArray.length];
            for (int i = 0; i < tempArray.length; i++) {
                lunList[i] = (Map<String, Object>) tempArray[i];
            }
        }
    }

}
