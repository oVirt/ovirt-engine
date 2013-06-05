package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public final class IrsStatsAndStatusXmlRpc extends StatusReturnForXmlRpc {
    private static final String STATS = "stats";

    @SuppressWarnings("unchecked")
    public IrsStatsAndStatusXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
    }

}
