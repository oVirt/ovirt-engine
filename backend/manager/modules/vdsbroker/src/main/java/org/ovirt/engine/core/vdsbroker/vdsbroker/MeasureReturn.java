package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class MeasureReturn extends StatusReturn {
    private static final String INFO = "info";
    private static final String REQUIRED_SIZE = "required";

    private Map<String, Object> volumeSize;

    public MeasureReturn(Map<String, Object> innerMap) {
        super(innerMap);
        volumeSize = (Map<String, Object>) innerMap.get(INFO);
    }

    public long getVolumeRequiredSize() {
        return Long.valueOf(volumeSize.get(REQUIRED_SIZE).toString());
    }
}
