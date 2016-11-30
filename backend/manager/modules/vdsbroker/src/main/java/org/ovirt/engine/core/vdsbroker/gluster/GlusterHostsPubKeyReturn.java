package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class GlusterHostsPubKeyReturn extends StatusReturn {

    private List<String> geoRepPublicKeys = null;
    private static final String INFO = "info";

    private static final String GEO_REP_PUB_KEYS = "geoRepPubKeys";

    @SuppressWarnings("unchecked")
    public GlusterHostsPubKeyReturn(Map<String, Object> innerMap) {
        super(innerMap);
        if(innerMap.containsKey("info")) {
            innerMap = (Map<String, Object>) innerMap.get(INFO);
        }
        if (innerMap.containsKey(GEO_REP_PUB_KEYS)) {
            Object[] keys = (Object[]) innerMap.get(GEO_REP_PUB_KEYS);
            for (Object key : keys) {
                if (geoRepPublicKeys == null) {
                    geoRepPublicKeys = new ArrayList<>();
                }
                geoRepPublicKeys.add((String) key);
            }
        }
    }

    public List<String> getGeoRepPublicKeys() {
        return geoRepPublicKeys;
    }
}
