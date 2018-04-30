package org.ovirt.engine.core.notifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.notifier.utils.NotificationProperties;

/**
 * The tests runs a single loop
 */
public class NotificationServiceTest {

    /**
     * The test executes a notifications for events which haven't been processed until the start of the test.<br>
     * Once those events are notified to the subscribers, they will be marked as processed and won't be send again<br>
     * The test covers the entire functional of the event notification service:<br>
     * <li>Retrieval of events to be processed
     * <li>Creating a secured mail client
     * <li>Notify each event to its subscriber by mail
     * <li>Add a record representing the notification to the history table
     * <li>Mark each sent event as processed
     * A completion to this test is the following steps:<br>
     * <li>Collect number of events to be processed before the test
     * <li>Save the numbers of elements in the history table before notification is run
     * <li>Verify the same amount of events which suppose to be notified were added to the history table
     * <li>Verify the same amount of events were marked as processed
     * <li>Verify there are no events documented in the history table with failure status
     * <li>Using a read mail client, connect to the mailbox and count the received mails
     * @throws NotificationServiceException an exception during initialization of the notification service
     */
    @Test
    public void testNotificationService() {
        NotificationService notificationService = null;
        try {
            File config = new File("src/test/resources/conf/notifier.conf");
            NotificationProperties.setDefaults(config.getAbsolutePath(), null);

            notificationService =
                    new NotificationService(NotificationProperties.getInstance());
        } catch (NotificationServiceException e) {
            e.printStackTrace();
        }
        assertNotNull(notificationService);
        notificationService.run();
    }

    /**
     * Verifies the configuration properties validation.<br>
     */
    @Test
    public void testNegativeNotificationServiceConfiguration() {
        // TODO: once engine config is added, add tests and init the service with
        // varied configuration files
    }

}
