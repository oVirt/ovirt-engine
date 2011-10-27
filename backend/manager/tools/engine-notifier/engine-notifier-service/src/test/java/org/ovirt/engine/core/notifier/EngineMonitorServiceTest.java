package org.ovirt.engine.core.notifier;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.ovirt.engine.core.notifier.utils.NotificationConfigurator;

/**
 * The test engage the engine monitor service, which sample the server status and report upon its status.
 */
public class EngineMonitorServiceTest {
    @Test
    public void testNotificationService() {
        EngineMonitorService engineMonitorService = null;
        try {
            engineMonitorService =
                    new EngineMonitorService(new NotificationConfigurator("src/test/resources/conf/notifier.conf"));
        } catch (NotificationServiceException e) {
            e.printStackTrace();
        }
        assertNotNull(engineMonitorService);
        engineMonitorService.run();
    }
}
