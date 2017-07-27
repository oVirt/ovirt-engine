package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public final class LldpReturn extends StatusReturn {
    private static final String LLDP_INFO = "info";
    private final Map<String, Object> lldp;

    public LldpReturn(Map<String, Object> innerMap) {
        super(innerMap);
        lldp = (Map<String, Object>) innerMap.get(LLDP_INFO);
    }

    public Map<String, Object> getLldp() {
        return Collections.unmodifiableMap(lldp);
    }
}
