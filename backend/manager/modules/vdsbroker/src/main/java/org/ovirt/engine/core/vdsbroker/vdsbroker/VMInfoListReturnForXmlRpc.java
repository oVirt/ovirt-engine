package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class VMInfoListReturnForXmlRpc {
    private static final String STATUS = "status";
    private static final String STATS_LIST = "statsList";

    // [XmlRpcMember("status")]
    public StatusForXmlRpc mStatus;
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("statsList")]
    public XmlRpcStruct[] mInfoList;

    @SuppressWarnings("unchecked")
    public VMInfoListReturnForXmlRpc(Map<String, Object> innerMap) {
        mStatus = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));
        Object[] temp = (Object[]) innerMap.get(STATS_LIST);
        if (temp != null) {
            mInfoList = new XmlRpcStruct[temp.length];
            for (int i = 0; i < temp.length; i++) {
                mInfoList[i] = new XmlRpcStruct((Map<String, Object>) temp[i]);
            }
        }
    }

}
