package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public final class AlignmentScanReturn extends StatusReturn {
    private static final String ALIGNMENT = "alignment";

    private Map<String, Object> alignment;

    @SuppressWarnings("unchecked")
    public AlignmentScanReturn(Map<String, Object> innerMap) {
        super(innerMap);
        alignment = (Map<String, Object>) innerMap.get(ALIGNMENT);
    }

    public Map<String, Object> getAlignment() {
        return alignment;
    }
}
