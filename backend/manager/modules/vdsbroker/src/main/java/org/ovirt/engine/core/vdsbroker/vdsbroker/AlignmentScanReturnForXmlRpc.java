package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public final class AlignmentScanReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String ALIGNMENT = "alignment";

    private Map<String, Object> alignment;

    public AlignmentScanReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        alignment = (Map<String, Object>) innerMap.get(ALIGNMENT);
    }

    public Map<String, Object> getAlignment() {
        return alignment;
    }
}
