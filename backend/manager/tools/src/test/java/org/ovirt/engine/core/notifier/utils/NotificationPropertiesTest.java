package org.ovirt.engine.core.notifier.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ovirt.engine.core.notifier.NotificationServiceException;

public class NotificationPropertiesTest {

    private static NotificationProperties prop = null;

    @BeforeClass
    static public void beforeClass() {
        NotificationProperties.setDefaults(
            "src/test/resources/conf/notifier-prop-test.conf",
            "src/test/resources/conf/missing.conf"
        );
        prop = NotificationProperties.getInstance();
        assertNotNull(prop);
    }

    @Test
    public void testProperties() {
        assertEquals(60, prop.getLong(NotificationProperties.INTERVAL_IN_SECONDS));
    }
}

