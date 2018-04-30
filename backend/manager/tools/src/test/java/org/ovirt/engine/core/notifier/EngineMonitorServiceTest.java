package org.ovirt.engine.core.notifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;

/**
 * The test engage the engine monitor service, which sample the server status and report upon its status.
 */
public class EngineMonitorServiceTest {
    @Test
    public void testNotificationService() {
        EngineMonitorService engineMonitorService = null;
        try {
            File config = new File("src/test/resources/conf/notifier.conf");
            NotificationProperties.setDefaults(config.getAbsolutePath(), null);

            engineMonitorService = new EngineMonitorService(NotificationProperties.getInstance());
        } catch (NotificationServiceException e) {
            e.printStackTrace();
        }
        assertNotNull(engineMonitorService);
        engineMonitorService.run();
    }
}
