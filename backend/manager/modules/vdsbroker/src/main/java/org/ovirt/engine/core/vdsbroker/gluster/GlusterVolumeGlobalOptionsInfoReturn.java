package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGlobalVolumeOptionEntity;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class GlusterVolumeGlobalOptionsInfoReturn extends StatusReturn {
    private static final String OPTION_MAP = "globalOptionMap";
    private List<GlusterGlobalVolumeOptionEntity> globalOptionList = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public GlusterVolumeGlobalOptionsInfoReturn(Map<String, Object> innerMap) {
        super(innerMap);
        if (getStatus().code != GlusterConstants.CODE_SUCCESS) {
            return;
        }
        Map<String, Object> globalOptions1 = (Map<String, Object>) innerMap.get("info");
        Map<String, Object> globalOptions = (Map<String, Object>) globalOptions1.get(OPTION_MAP);
        if (globalOptions != null && !globalOptions.isEmpty()) {
            for (String s : globalOptions.keySet()) {
                if (!StringUtils.isEmpty(s)) {
                    GlusterGlobalVolumeOptionEntity option = new GlusterGlobalVolumeOptionEntity();
                    option.setKey(s);
                    option.setValue((String) globalOptions.get(s));
                    globalOptionList.add(option);
                }
            }
        }
    }

    public List<GlusterGlobalVolumeOptionEntity> globalOptionList() {
        return globalOptionList;
    }

}
