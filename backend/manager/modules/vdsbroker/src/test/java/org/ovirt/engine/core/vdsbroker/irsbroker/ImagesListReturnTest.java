package org.ovirt.engine.core.vdsbroker.irsbroker;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class ImagesListReturnTest {

    private static final String IMAGES_LIST = "imageslist";

    @Test
    public void validateBadUUIDSFiltered() {
        Map<String, Object> innerMap =
                Collections.singletonMap(IMAGES_LIST,
                    new Object[]{"28e983b9-c2f6-4a50-a5d5-7a38089797c7",
                                 "c3d75234-b360-4aed-8d78-413d7488bd9b",
                                 "_remove_me_2f17489c-285e-496d-bd86-0d63d1be2efe"});
        ImagesListReturn imagesListReturn = new ImagesListReturn(innerMap);
        String[] expectedUUIDList = new String[]{"28e983b9-c2f6-4a50-a5d5-7a38089797c7",
                                                 "c3d75234-b360-4aed-8d78-413d7488bd9b"};

        assertArrayEquals(expectedUUIDList, imagesListReturn.getImageList());
    }
}
