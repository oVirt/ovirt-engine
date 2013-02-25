package org.ovirt.engine.core.notifier.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.ovirt.engine.core.notifier.NotificationServiceException;

public class NotificationConfiguratorTest {

    private static NotificationConfigurator config = null;

    @BeforeClass
    static public void initConfigurator() {
        try {
            config = new NotificationConfigurator("src/test/resources/conf/error-notifier.conf");
        } catch (NotificationServiceException e) {
            e.printStackTrace();
        }
        assertNotNull(config);
    }

    @Test
    public void testConfiguration() {
        NotificationConfigurator configurator = null;
        try {
            configurator = new NotificationConfigurator("src/test/resources/conf/notifier.conf");
        } catch (NotificationServiceException e) {
            e.printStackTrace();
        }

        assertNotNull(configurator);

        long timerInterval = -1;
        try {
            timerInterval = configurator.getTimerInterval(NotificationProperties.INTERVAL_IN_SECONDS, 10);
        } catch (NotificationServiceException e) {
        }
        assertEquals(timerInterval, 60);
    }

    @Test
    public void testIntervalTimerWrongValue() {
        boolean failure = false;
        try {
            config.getTimerInterval(NotificationProperties.INTERVAL_IN_SECONDS, 0);
        } catch (NotificationServiceException e) {
            failure = true;
        }
        assertTrue(failure);
    }
}

