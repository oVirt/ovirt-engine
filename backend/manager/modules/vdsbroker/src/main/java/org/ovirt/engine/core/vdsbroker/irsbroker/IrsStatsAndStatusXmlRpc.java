package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class IrsStatsAndStatusXmlRpc extends StatusReturnForXmlRpc {
    private static final String STATS = "stats";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("stats")]
    public XmlRpcStruct stats;

    @SuppressWarnings("unchecked")
    public IrsStatsAndStatusXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(STATS);
        if (temp != null) {
            stats = new XmlRpcStruct((Map<String, Object>) temp);
        }
    }

}
