package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class PrepareImageReturn extends StatusReturn {
    private static final String PATH = "path";
    private static final String INFO = "info";
    private String imagePath;

    @SuppressWarnings("unchecked")
    public PrepareImageReturn(Map<String, Object> innerMap) {
        super(innerMap);
        Map<String, Object> infoMap = (Map<String, Object>) innerMap.get(INFO);
        if (infoMap != null) {
            imagePath = (String) infoMap.get(PATH);
        }
        if (imagePath == null) {
            // Some backends (e.g. managed block) may return path at top level or omit it
            imagePath = (String) innerMap.get(PATH);
        }
    }

    public String getImagePath() {
        return imagePath;
    }
}
