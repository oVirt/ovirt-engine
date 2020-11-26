package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

@SuppressWarnings("unchecked")
public class VmExternalDataReturn {

    private static final String STATUS = "status";
    private static final String INFO = "info";
    private static final String DATA = "_X_data";
    private static final String HASH = "hash";

    public final Status status;
    public final String data;
    public final String hash;

    public VmExternalDataReturn(Map<String, Object> innerMap) {
        status = new Status((Map<String, Object>) innerMap.get(STATUS));
        final Map<String, String> result = (Map<String, String>) innerMap.get(INFO);
        if (status.code == 0) {
            data = result.getOrDefault(DATA, null);
            hash = result.get(HASH);
        } else {
            data = hash = "";
        }
    }
}
