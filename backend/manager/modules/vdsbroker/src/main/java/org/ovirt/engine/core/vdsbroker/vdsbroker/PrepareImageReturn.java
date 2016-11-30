package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class PrepareImageReturn extends StatusReturn {
    private static final String PATH= "path";
    private static final String INFO= "info";
    private String imagePath;

    @SuppressWarnings("unchecked")
    public PrepareImageReturn(Map<String, Object> innerMap) {
        super(innerMap);
        imagePath = (String) ((Map<String, Object>)innerMap.get(INFO)).get(PATH);
    }

    public String getImagePath() {
        return imagePath;
    }
}
