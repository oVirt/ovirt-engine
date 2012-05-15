package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class ServerConnectionStatusReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String STATUS_LIST = "statuslist";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("statuslist")]
    public XmlRpcStruct[] mStatusList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(mStatusList, builder);
        return builder.toString();
    }

    public ServerConnectionStatusReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(STATUS_LIST);
        if (temp == null) {
            mStatusList = null;
        } else {
            Object[] tempArray = (Object[]) temp;
            mStatusList = new XmlRpcStruct[tempArray.length];
            for (int i = 0; i < tempArray.length; i++) {
                mStatusList[i] = new XmlRpcStruct((Map<String, Object>) tempArray[i]);
            }
        }
    }

    public Map<String, String> convertToStatusList() {
        HashMap<String, String> result = new HashMap<String, String>();
        for (XmlRpcStruct st : this.mStatusList) {
            String status = st.getItem("status").toString();
            String id = st.getItem("id").toString();
            result.put(id, status);
        }
        return result;
    }
}
