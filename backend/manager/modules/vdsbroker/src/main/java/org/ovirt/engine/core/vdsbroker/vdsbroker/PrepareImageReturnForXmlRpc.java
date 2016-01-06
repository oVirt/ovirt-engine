package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public class PrepareImageReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String PATH= "path";
    private static final String INFO= "info";
    private String imagePath;

    public PrepareImageReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        imagePath = (String) ((Map<String, Object>)innerMap.get(INFO)).get(PATH);
    }

    public String getImagePath() {
        return imagePath;
    }
}
