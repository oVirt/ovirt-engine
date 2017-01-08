package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImagesListReturn extends StatusReturn {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String IMAGES_LIST = "imageslist";

    private String[] imageList;

    public ImagesListReturn(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] tempObj = (Object[]) innerMap.get(IMAGES_LIST);

        if (tempObj != null) {
            imageList = Arrays.stream(tempObj)
                .map(String.class::cast)
                .filter(this::isValidUUID)
                .toArray(String[]::new);
        }
    }

    public String[] getImageList() {
        return imageList;
    }

    private boolean isValidUUID(String s) {
        try {
            UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            log.warn("String '{}' is not a valid UUID", s);
            return false;
        }

        return true;
    }
}
