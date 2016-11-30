package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class OneMapReturn extends StatusReturn {
    private static final String MAP_VDS_RETVAL_KEY = "statsMap";
    private Map<String, Object> resultMap;

    @SuppressWarnings("unchecked")
    public OneMapReturn(Map<String, Object> innerMap) {
        super(innerMap);
        resultMap = (Map<String, Object>)innerMap.get(MAP_VDS_RETVAL_KEY);
    }

    public Map<String, Object> getResultMap() {
        return resultMap;
    }
}
