package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public class GlusterVolumeGeoRepConfigListXmlRpc extends StatusReturnForXmlRpc {

    private static final String GEO_REP_CONFIG = "geoRepConfig";
    private static final String SESSION_CONFIG = "sessionConfig";
    private static final String INFO = "info";

    private List<GlusterGeoRepSessionConfiguration> geoRepConfigList = new ArrayList<>();

    public GlusterVolumeGeoRepConfigListXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        if (innerMap.containsKey(INFO)) {
            innerMap = (Map<String, Object>) innerMap.get(INFO);
        }
        if (innerMap.containsKey(SESSION_CONFIG)) {
            prepareConfigMap((Map<String, Object>) ((Map<String, Object>) innerMap.get(SESSION_CONFIG)).get(GEO_REP_CONFIG));
        }
    }

    private void prepareConfigMap(Map<String, Object> geoRepConfigs) {
        for (Entry<String, Object> currentGeoRepConfig : geoRepConfigs.entrySet()) {
            GlusterGeoRepSessionConfiguration config = new GlusterGeoRepSessionConfiguration();
            config.setKey(currentGeoRepConfig.getKey());
            config.setValue((String) currentGeoRepConfig.getValue());
            geoRepConfigList.add(config);
        }
    }

    public List<GlusterGeoRepSessionConfiguration> getSessionConfig() {
        return geoRepConfigList;
    }

    public void setSessionConfig(List<GlusterGeoRepSessionConfiguration> sessionConfig) {
        this.geoRepConfigList = sessionConfig;
    }
}
