package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public class GlusterVolumeConfigListXmlRpc extends StatusReturnForXmlRpc {

    protected static final String GEO_REP_CONFIG = "geoRepConfig";
    protected static final String OPTION_NAME = "optionName";
    protected static final String OPTION_VALUE = "optionValue";

    private List<GlusterGeoRepSessionConfiguration> sessionConfig = new ArrayList<>();

    public GlusterVolumeConfigListXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        if(innerMap.containsKey(GEO_REP_CONFIG)) {
            prepareConfigMap((Object[])((((Map<String, Object>)(innerMap.get(GEO_REP_CONFIG))).get(GEO_REP_CONFIG))));
        }
    }

    private void prepareConfigMap(Object[] geoRepConfigs) {
        for(Object currentGeoRepConfig : geoRepConfigs) {
            sessionConfig.add(getConfig((Map<String, Object>) currentGeoRepConfig));
        }
    }

    private GlusterGeoRepSessionConfiguration getConfig(Map<String, Object> innerMap) {
        GlusterGeoRepSessionConfiguration config = new GlusterGeoRepSessionConfiguration();
        config.setKey(innerMap.containsKey(OPTION_NAME) ? (String)innerMap.get(OPTION_NAME) : null);
        config.setValue(innerMap.containsKey(OPTION_VALUE) ? (String) innerMap.get(OPTION_VALUE) : null);
        return config;
    }

    public List<GlusterGeoRepSessionConfiguration> getSessionConfig() {
        return sessionConfig;
    }

    public void setSessionConfig(List<GlusterGeoRepSessionConfiguration> sessionConfig) {
        this.sessionConfig = sessionConfig;
    }
}
