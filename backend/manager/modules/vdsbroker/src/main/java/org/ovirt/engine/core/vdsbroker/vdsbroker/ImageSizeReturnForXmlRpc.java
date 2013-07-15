package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

import java.util.Map;

public class ImageSizeReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String SIZE = "size";

    private Long imageSize;

    public ImageSizeReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        imageSize = (innerMap.get(SIZE) != null) ?
                Long.valueOf((String) innerMap.get(SIZE)) : null;
    }

    public Long getImageSize() {
        return imageSize;
    }
}
