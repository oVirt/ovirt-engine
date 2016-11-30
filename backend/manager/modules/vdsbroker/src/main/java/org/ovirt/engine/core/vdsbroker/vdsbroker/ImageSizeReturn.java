package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class ImageSizeReturn extends StatusReturn {
    private static final String SIZE = "size";

    private Long imageSize;

    public ImageSizeReturn(Map<String, Object> innerMap) {
        super(innerMap);
        imageSize = (innerMap.get(SIZE) != null) ?
                Long.valueOf((String) innerMap.get(SIZE)) : null;
    }

    public Long getImageSize() {
        return imageSize;
    }
}
