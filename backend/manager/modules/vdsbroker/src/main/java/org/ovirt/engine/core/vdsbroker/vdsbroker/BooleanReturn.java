package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class BooleanReturn extends StatusReturn {

    private static final String INFO = "info";
    private boolean isEmpty;

    @SuppressWarnings("unchecked")
    public BooleanReturn(Map<String, Object> innerMap, String flagName) {
        super(innerMap);
        if (innerMap.containsKey(INFO)) {
            innerMap = (Map<String, Object>) innerMap.get(INFO);
        }
        if (innerMap.containsKey(flagName)) {
            isEmpty = (boolean) innerMap.get(flagName);
        }
    }

    public boolean isVolumeEmpty() {
        return isEmpty;
    }
}
