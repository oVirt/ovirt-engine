package org.ovirt.engine.core.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class TrustStoreTestUtils {
    private TrustStoreTestUtils() {
    }

    public static String getTrustStorePath() {
        try {
            return URLDecoder.decode(ClassLoader.getSystemResource("key.p12").getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
