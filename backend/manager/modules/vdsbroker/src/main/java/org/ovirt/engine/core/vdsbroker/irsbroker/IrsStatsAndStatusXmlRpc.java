package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public final class IrsStatsAndStatusXmlRpc extends StatusReturnForXmlRpc {
    private static final String STATS = "stats";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("stats")]
    public Map<String, Object> stats;

    @SuppressWarnings("unchecked")
    public IrsStatsAndStatusXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        stats = (Map<String, Object>) innerMap.get(STATS);
    }

}
