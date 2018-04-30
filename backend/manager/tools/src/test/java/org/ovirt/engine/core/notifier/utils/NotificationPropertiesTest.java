package org.ovirt.engine.core.notifier.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NotificationPropertiesTest {

    private static NotificationProperties prop = null;

    @BeforeAll
    public static void beforeClass() throws UnsupportedEncodingException {
        NotificationProperties.release();
        NotificationProperties.setDefaults(
                URLDecoder.decode(ClassLoader.getSystemResource("conf/notifier-prop-test.conf").getPath(), "UTF-8"),
                ""
        );
        prop = NotificationProperties.getInstance();
        assertNotNull(prop);
    }

    @Test
    public void testProperties() {
        assertEquals(60, prop.getLong(NotificationProperties.INTERVAL_IN_SECONDS));
    }
}

