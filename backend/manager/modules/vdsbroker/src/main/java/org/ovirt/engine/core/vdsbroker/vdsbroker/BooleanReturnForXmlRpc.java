package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public class BooleanReturnForXmlRpc extends StatusReturnForXmlRpc {

    private static final String INFO = "info";
    private boolean isEmpty;

    public BooleanReturnForXmlRpc(Map<String, Object> innerMap, String flagName) {
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
